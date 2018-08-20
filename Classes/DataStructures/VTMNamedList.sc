//A list where enumeration are numbers from 1 .. N.
//Optionally items can be named with symbols.
//... "store-now, name-later"
//Also stores time of creating items, and time when they are changed.
VTMNamedList {
	var <items;

	//init 'items' argument is an array of data. If an item
	//is a kindOf Association, the association key will be used as the
	//name for that item. Otherwise the item will be enumerated
	*new{arg items;
		^super.new.init(items);
	}

	*newFromKeyValuePairs{arg keyValPairs;
		var items;
		items = keyValPairs.clump(2).collect({arg item, i;
			var res, key, val;
			key = item[0];
			val = item[1];
			//if the key is an integer it is ignored at this point
			//it is assumed that the integer keys are in sorted order
			if(key.isInteger, {
				res = val;
				if(res.isString, {
					res = [res];
				});
			}, {
				res = key -> val;
			});
			res;
		}).flatten;
		^this.new(items);
	}

	init{arg items_;
		this.items_(items_);
	}

	items_{arg val;
		var temp;
		if(val.isNil, {
			temp = [];
		}, {
			if(val.isKindOf(SequenceableCollection), {
				temp = val.collect({arg item;
					if(item.isKindOf(Association), {
						//ignoring integer values as name
						if(item.key.isInteger, {
							(data: item, created: Date.getDate);
						}, {
							(data: item.value, created: Date.getDate, name: item.key);
						});
					}, {
						(data: item, created: Date.getDate);
					});

				});
			}, {
				VTMError("%:% - Items must be a kind of SequenceableCollection: %.".format(
					this.class.name, thisMethod.name, val
				)).throw;
			});
		});

		items = List.newFrom(temp);
	}

	names{
		var result;
		result = items.collect({arg item, i;
			if(item.includesKey(\name), {
				item[\name];
			}, {
				i;
			});
		});
		^result;
	}

	prAt{arg val;
		var result;
		if(val.isInteger, {
			if(val >= 0 and: {val <= items.size}, {
				^items[val];
			}, {
				^nil;
			});
		}, {
			if(val.isKindOf(Symbol), {
				result = items.detect({arg item;
					if(item.includesKey(\name), {
						item[\name] == val;
					}, {
						false;
					});
				});
			});
		});
		^result;
	}

	at{arg val;
		var result;
		result = this.prAt(val);
		if(result.notNil, {
			^result[\data];
		}, {
			^nil;
		});
	}

	associations{
		var result;
		result = items.collect({arg item, i;
			var itemName;
			itemName = item[\name] ? (i);
			Association.new(itemName, item[\data]);
		});
		^result;
	}

	asKeyValuePairs{
		var result;
		result = this.associations.collect({arg item;
			[item.key, item.value]
		}).flatten;
		^result;
	}

	setItemName{arg number, name;
		var itemToChange = this.prAt(number);
		if(itemToChange.notNil, {
			itemToChange.put(\name, name);
		}, {
			VTMError("%:% - Item number not found %.".format(
				this.class.name, thisMethod.name, number
			)).throw;
		});
	}

	getItems{
		^items.collect({arg it; it[\data]});
	}

	getItemName{arg number;
		var result;
		result = this.prAt(number);
		if(result.notNil, {
			result = result[\name];
		}, {
			result = nil;
		});
		^result;
	}

	getItemTimeCreated{arg name;
		var result = this.prAt(name);
		if(result.notNil, {
			^result[\created];
		}, { ^nil });
	}

	getItemTimeLastChanged{arg name;
		var result = this.prAt(name);
		if(result.notNil, {
			^result[\changed];
		}, { ^nil });
	}

	changeItem{arg name, data;
		var itemToChange = this.prAt(name);
		if(itemToChange.notNil, {
			itemToChange.put(\data, data);
			itemToChange.put(\changed, Date.getDate);
		});
	}

	addItem{arg data, name, slot;//name can also be an integer slot
		var newItem = (data: data, created: Date.getDate);
		if(name.notNil, {
			newItem.put(\name, name);
		});
		if(slot.isNil, {
			//add to tail by default
			items.add(newItem);
		}, {
			items.insert(slot, newItem);
		});
	}

	removeItem{arg name;
		var indexToRemove, removedItem;
		if(items.isEmpty, {^nil});
		if(name.isInteger, {
			indexToRemove = name;
		}, {
			if(name.isKindOf(Symbol), {
				indexToRemove = items.detectIndex({arg it;
					if(it.includesKey(\name), {
						it[\name] == name;
					}, {
						false;
					});
				});
			});
		});

		if(
			indexToRemove.notNil and:
			{indexToRemove >= 0} and:
			{indexToRemove < items.size}, {
			removedItem = items.removeAt(indexToRemove);
		});
		^removedItem;
	}

	moveItem{arg name, slot;
		var itemToMove;
		itemToMove = this.removeItem(name);
		if(itemToMove.notNil, {
			items.insert(slot, itemToMove);
		}, {
			VTMError("%:% - Slot number/name not found: %.".format(
				this.class.name, thisMethod.name, name
			)).throw;
		});
	}

	includes{arg val;
		^this.getItems.includes(val);
	}

	isEmpty{
		^items.isEmpty;
	}
}

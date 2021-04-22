/*
A ListValue will have items with arbitrary types.
*/
VTMListValue : VTMCollectionValue {
	var <itemType; //Which Value type to contain in this class
	var orderThunk;
	var itemAtThunk, <prItemDict;

	isValidType{| val |
		^(val.isArray and: val.isString.not);
	}

	*type{ ^\list; }

	*prDefaultValueForType{
		^[];
	}

	*new{| properties |
		^super.new(properties).initListValue;
	}

	initListValue{
		if(properties.notEmpty, {
			if(properties.includesKey(\itemType), {
				itemType = properties[\itemType];
			});
			if(properties.includesKey(\itemDescription), {
				itemDescription = properties[\itemDescription];
			});
		});
		//Using decimal as default item type so
		//that list Value can be made using empty properties.
		itemType = itemType ? \decimal;
		itemDescription = [ () ];

		//build the internal Values
		this.prBuildItemValues;
		orderThunk = Thunk{
			items.collect(_.name);
		};
		//build item dictionary for lookup
		prItemDict = Dictionary.new;
		items.do({| item |
			prItemDict.put(item.name, item);
		});
	}

	*propertyKeys{
		^super.propertyKeys.addAll([\itemDescription, \itemType]);
	}

	prBuildItemValues{
		//Check if the items are already built.
		//This forces you to always make a new list Value if one is
		//already made.
		if(items.isNil, {
			var itemClass, itemDescription, propertyKeys;
			var baseItemDesc;
			items = Dictionary.new;
			itemClass = VTMValue.typeToClass(itemType);

			//all sub Values have this base item properties
			baseItemDesc = (
				isSubValue: true
			);

			//Expand all the items in the item properties, e.g. arrayed keys etc.
			//All item properties should now be expanded into separate Associations
			itemDescription = this.class.prExpanditemDescription(itemDescription);
			propertyKeys = itemClass.propertyKeys.asSet.sect(properties.keys);
			itemDescription = itemDescription.collect({| itemAssoc, index |
				var itemName, itemDesc, newItemDesc;
				itemName = itemAssoc.key;
				itemDesc = itemAssoc.value;
				newItemDesc = itemDesc.deepCopy;

				//add the values from the outer properties that applies to all items of this type.
				//Getting only the keys that pertain to the itemClass, and which are defined in the
				//properties.
				itemClass.propertyKeys.asSet.sect(properties.keys).do({| attrKey |
					newItemDesc.put(attrKey, properties[attrKey]);
				});

				//add the base item desc, overriding some of the outer properties values
				newItemDesc.putAll(baseItemDesc.deepCopy);

				//override with the values in the itemDescription
				newItemDesc.putAll(itemDesc);

				Association.new(itemName, newItemDesc);
			});

			//Build the item Value objects
			items = itemDescription.collect({| itemDesc |
				VTMValue.makeFromType(properties[\itemType], itemDesc);
			});

		}, {
			VTMError("ListValue items already built, please free current and build a new Value.").throw;
		});
	}

	*prExpanditemDescription{| desc |
		var result;
		desc.do({| item, i |
			if(item.isKindOf(Association), {
				if(item.key.isArray and: {item.isString.not}, {
					item.key.do({| jtem, j |
						var jDesc = ();
						item.value.keysValuesDo({| ke, va |
							if(va.isArray and: {va.isString.not}, {
								//expand item properties value to arrayed key by wrapped indexing
								jDesc.put(
									ke,
									va.wrapAt(j)
								);

							}, {
								jDesc.put(ke, va);
							});
						});
						result = result.add(
							Association.new(
								jtem, jDesc
							);
						);
					});

				}, {
					result = result.add( item );
				});
			}, {
				if(item.isArray and: item.isString.not, {
					result = result.addAll(item);
				}, {
					result = result.add(item);
				});
			});
		});
		//Make all items into Associations with name pointing to a Dictionary
		result = result.collect({| item |
			var res = item;
			if(item.isKindOf(Association).not, {
				res = Association.new(item, ());
			});
			res;
		});
		^result;
	}

	itemOrder{
		^orderThunk.value;
	}

	at{| itemName |
		^prItemDict.at(itemName)
	}
}

/*
A ListParameter will have items with arbitrary types.
*/
VTMListParameter : VTMCollectionParameter {
	var <itemType; //Which parameter type to contain in this class
	var orderThunk;
	var itemAtThunk, <prItemDict;

	isValidType{arg val;
		^(val.isArray and: val.isString.not);
	}

	*type{ ^\list; }

	prDefaultValueForType{
		^[];
	}

	*new{arg name, attributes;
		^super.new(name, attributes).initListParameter;
	}

	initListParameter{
		if(attributes.notEmpty, {
			if(attributes.includesKey(\itemType), {
				itemType = attributes[\itemType];
			});
		});
		//Using decimal as default item type so 
		//that list parameter can be made using empty attributes.
		itemType = itemType ? \decimal;
		itemAttributes = [ () ];

		//build the internal parameters
		this.prBuildItemParameters;
		orderThunk = Thunk{
			items.collect(_.name);
		};
		//build item dictionary for lookup
		prItemDict = Dictionary.new;
		items.do({arg item;
			prItemDict.put(item.name, item);
		});
	}

	prBuildItemParameters{
		//Check if the items are already built.
		//This forces you to always make a new list parameter if one is
		//already made.
		if(items.isNil, {
			var itemClass, itemAttributes, attributeKeys;
			var baseItemDesc;
			items = Dictionary.new;
			itemClass = VTMParameter.typeToClass(itemType);

			//all sub parameters have this base item attributes
			baseItemDesc = (
				isSubParameter: true
			);

			//Expand all the items in the item attributes, e.g. arrayed keys etc.
			//All item attributes should now be expanded into separate Associations
			itemAttributes = this.class.prExpanditemAttributes(itemAttributes);
			attributeKeys = itemClass.attributeKeys.asSet.sect(attributes.keys);
			itemAttributes = itemAttributes.collect({arg itemAssoc, index;
				var itemName, itemDesc, newItemDesc;
				itemName = itemAssoc.key;
				itemDesc = itemAssoc.value;
				newItemDesc = itemDesc.deepCopy;

				//add the values from the outer attributes that applies to all items of this type.
				//Getting only the keys that pertain to the itemClass, and which are defined in the
				//attributes.
				itemClass.attributeKeys.asSet.sect(attributes.keys).do({arg attrKey;
					newItemDesc.put(attrKey, attributes[attrKey]);
				});

				//add the base item desc, overriding some of the outer attributes values
				newItemDesc.putAll(baseItemDesc.deepCopy);
				newItemDesc.put(\name, itemName);
				newItemDesc.put(\path, this.fullPath);//using the owner parameter fullPath
				newItemDesc.put(\type, attributes[\itemType]);

				//override with the values in the itemAttributes
				newItemDesc.putAll(itemDesc);

				Association.new(itemName, newItemDesc);
			});

			//Build the item parameter objects
			items = itemAttributes.collect({arg itemDesc;
				VTMParameter.makeFromAttributes(itemDesc.value);
			});

		}, {
			Error(
				"ListParameter items already built, please free current and build a new parameter. [%]".format(
					this.fullPath
				)
			).throw;
			^nil;
		});
	}

	*prExpanditemAttributes{arg desc;
		var result;
		desc.do({arg item, i;
			if(item.isKindOf(Association), {
				// "Parsing association: %".format(item).debug;
				if(item.key.isArray and: {item.isString.not}, {
					// "Expanding association key: %".format(item).debug;
					item.key.do({arg jtem, j;
						var jDesc = ();
						item.value.keysValuesDo({arg ke, va;
							if(va.isArray and: {va.isString.not}, {
								//expand item attributes value to arrayed key by wrapped indexing
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
		result = result.collect({arg item;
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

	at{arg itemName;
		^prItemDict.at(itemName)
	}
}

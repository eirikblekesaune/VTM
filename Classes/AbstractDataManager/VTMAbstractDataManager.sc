VTMAbstractDataManager {
	var items;
	var oscInterface;
	var itemDeclarations;
	var <parent;

	*dataClass{
		^this.subclassResponsibility(thisMethod);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	*new{arg itemDeclarations, parent;
		^super.new.initAbstractDataManager(itemDeclarations, parent);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	initAbstractDataManager{arg itemDeclarations_, parent_;
		itemDeclarations = itemDeclarations_;
		parent = parent_;
		items = VTMOrderedIdentityDictionary.new;
		if(itemDeclarations.notNil, {
			this.addItemsFromItemDeclarations(itemDeclarations);
		});
	}

	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			newItem = this.class.dataClass.new(itemName, itemDeclaration, this);
			this.addItem(newItem);
		});
	}

	addItem{arg newItem;
		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			items.put(newItem.name, newItem);
			this.changed(\items);
		});
	}

	removeItem{arg itemName;
		var removedItem;
		items.removeAt(itemName);
		this.changed(\items);
		^removedItem;
	}

	freeItem{arg itemName;
		if(this.hasItemNamed(itemName), {
			var removedItem;
			"% freeing item: %".format(this.fullPath, itemName).vtmdebug(2, thisMethod);
			items[itemName].disable;//dissable actions and messages
			removedItem = this.removeItem(itemName);
			removedItem.free;
		}, {
			"% not freeing item: % since it was not found".format(this.fullPath, itemName).vtmdebug(2, thisMethod);
		});
	}

	hasItemNamed{arg key;
		^items.includesKey(key);
	}

	items{
		^items.values;
	}

	numItems{
		^items.size;
	}

	at{arg key;
		^items.at(key);
	}

	isEmpty{ ^items.isEmpty; }

	free{
		this.disableOSC;
		this.names.do({arg itemName;
			this.freeItem(itemName);
		});
		this.changed(\freed);
	}

	names{
		^items.order;
	}

	name{ this.subclassResponsibility(thisMethod); }

	itemDeclarations{arg recursive;
		var result;
		if(recursive, {
			items.do({arg item;
				result = result.addAll([item.name, item.declaration]);
			});
		}, {
			items.do({arg item;
				result = result.addAll([item.name]);
			});
		});
		^result;
	}

	path{
		^'/';
	}

	fullPath{
		^(this.path ++ this.leadingSeparator ++	this.name).asSymbol;
	}

	leadingSeparator{ ^':'; }

	enableOSC {

		items.keysValuesDo { |key, value|
			value.enableOSC();
		};

		oscInterface !? { oscInterface.enable() };
		oscInterface ?? { oscInterface = VTMOSCInterface(this).enable() };
	}

	disableOSC {
		oscInterface !? { oscInterface.free() };
		oscInterface = nil;
	}

	oscEnabled {
		^oscInterface.notNil();
	}

	*makeDataManagerDeclaration{arg descriptions, valueDeclarations;
		var result = VTMOrderedIdentityDictionary[];
		descriptions.keysValuesDo({arg key, val;
			result.put(key, val);
			if(valueDeclarations.includesKey(key), {
				result[key].put(\value, valueDeclarations[key]);
			});
		});
		^result;
	}

	addForwarding{arg key, itemName, addr, path, vtmJson = false, mapFunc;
		var item = items[itemName];
		item.addForwarding(key, addr, path, vtmJson, mapFunc);
	}

	removeForwarding{arg key, itemName;
		var item = items[itemName];
		item.removeForwarding(key);
	}

	removeAllForwardings{
		items.do({arg item;
			item.removeAllForwarding;
		});
	}

	enableForwarding{
		items.do({arg item;
			item.enableForwarding;
		});
	}

	disableForwarding{
		items.do({arg item;
			item.disableForwarding;
		});
	}

	update{arg theChanged, whatChanged ...more;
		if(theChanged.isKindOf(this.class.dataClass) and: {
			items.includes(theChanged)
		}, {
			switch(whatChanged,
				\freed, {
					this.removeItem(theChanged.name);
				}
			);
		});
	}

	makeView{arg parent, bounds, viewDef, settings;
		^'VTMAbstractDataManagerView'.asClass.new(
			parent, bounds, viewDef, settings, this
		);
	}

	debugString{
		var result;
		result = "\n% [%]\n".format(this.name, this.class);
		result = result ++ "\tpath: %\n".format(this.fullPath);
		^result;
	}
}

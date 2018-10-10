VTMDataManager {
	var items;
	var oscInterface;
	var itemDeclarations;
	var <parent;

	*dataClass{
		^this.subclassResponsibility(thisMethod);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	*new{arg parent;
		^super.new.initDataManager(parent);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	initDataManager{arg  parent_;
		parent = parent_;
		items = VTMOrderedIdentityDictionary.new;
	}

	*buildItem{arg name, declaration, manager;
		var result;
		result = this.class.dataClass.new(
			name, declaration, manager
		);
		^result;
	}

	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			newItem = this.class.buildItem(
				itemName, itemDeclaration, this);
			this.addItem( newItem );
		});
	}

	addItem{arg newItem;
		"% added item: %".format(this.fullPath, newItem).vtmdebug(
			2, thisMethod);

		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			//If the manager has already registered a context of this name then
			//we free the old context.
			//TODO: See if this need to be scheduled/synced in some way.
			if(this.hasItemNamed(newItem.name), {
				this.freeItem(newItem.name);
			});

			items.put(newItem.name, newItem);
			this.addDependant(newItem);
			this.changed(\items, \added, newItem);
		});
	}

	removeItem{arg itemName;
		var removedItem;
		removedItem = items.removeAt(itemName);
		if(removedItem.notNil, {
			"% removed item: %".format(this.fullPath, removedItem).vtmdebug(2, thisMethod);
			this.changed(\items, \removed, removedItem);
			this.removeDependant(removedItem);
		})
		^removedItem;
	}

	freeItem{arg itemName;
		var removedItem;
		items[itemName].disable;//dissable actions and messages
		removedItem = this.removeItem(itemName);
		if(removedItem.notNil, {
			"% freeing item: %".format(this.fullPath, itemName).vtmdebug(2, thisMethod);
			removedItem.free;
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

	attributes {
		^items.select({arg it; it.isKindOf(VTMAttribute)});
	}

	commands{
		^items.select({arg it; it.isKindOf(VTMCommand)});
	}

	returns{
		^items.select({arg it; it.isKindOf(VTMReturn)});
	}

	signals{
		^items.select({arg it; it.isKindOf(VTMSignal)});
	}

	mappings {
		^items.select({arg it; it.isKindOf(VTMMapping)});
	}

	cues {
		^items.select({arg it; it.isKindOf(VTMCue)});
	}

	scores {
		^items.select({arg it; it.isKindOf(VTMScore)});
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
		^'VTMDataManagerView'.asClass.new(
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

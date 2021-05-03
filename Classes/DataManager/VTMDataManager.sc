VTMDataManager {
	var items;
	var oscInterface;
	var itemDeclarations;
	var <parent;

	*dataClass{
		^this.subclassResponsibility(thisMethod);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	*new{| parent |
		^super.new.initDataManager(parent);
	}

	//% itemDeclarations : VTMOrderedIdentityDictionary
	initDataManager{|  parent_ |
		parent = parent_;
		items = VTMOrderedIdentityDictionary.new;
	}

	*buildItem{| name, declaration, manager |
		var result;
		result = this.class.dataClass.new(
			name, declaration, manager
		);
		^result;
	}

	addItemsFromItemDeclarations{| itemDecls |
		itemDecls.keysValuesDo({| itemName, itemDeclaration |
			var newItem;
			newItem = this.class.buildItem(
				itemName, itemDeclaration, this);
			this.addItem( newItem );
		});
	}

	addItem{| newItem |
		"% added item: % name: % path: %".format(this.fullPath, newItem, newItem.name, newItem.path).vtmdebug(2, thisMethod);

		if(newItem.isKindOf(this.class.dataClass), {//check argument type
			//If the manager has already registered a context of this name then
			//we free the old context.
			//TODO: See if this need to be scheduled/synced in some way.
			if(this.hasItemNamed(newItem.name), {
				this.freeItem(newItem.name);
			});

			items.put(newItem.name, newItem);
			this.addDependant(newItem);
			this.changed(\items, \added, newItem);
			parent.registerChild(newItem);
		});
	}

	removeItem{| itemName |
		var removedItem;
		removedItem = items.removeAt(itemName);
		if(removedItem.notNil, {
			"% added item: % name: % path: %".format(this.fullPath, removedItem, removedItem.name, removedItem.path).vtmdebug(2, thisMethod);
			parent.unregisterChild(removedItem);
			this.changed(\items, \removed, removedItem);
			this.removeDependant(removedItem);
		})
		^removedItem;
	}

	freeItem{| itemName |
		var removedItem;
		removedItem = this.removeItem(itemName);
		if(removedItem.notNil, {
			removedItem.disable;//dissable actions and messages
			"% freeing item: %".format(this.fullPath, itemName).vtmdebug(2, thisMethod);
			removedItem.free;
		});
	}

	hasItemNamed{| key |
		^items.includesKey(key);
	}

	items{
		^items.values;
	}

	numItems{
		^items.size;
	}

	keys{ ^this.names; }

	at{| key |
		^items.at(key);
	}

	isEmpty{ ^items.isEmpty; }

	free{
		this.disableOSC;
		this.names.do({| itemName |
			this.freeItem(itemName);
		});
		this.changed(\freed);
	}

	names{
		^items.order;
	}

	name{ this.subclassResponsibility(thisMethod); }

	itemDeclarations{| recursive |
		var result;
		if(recursive, {
			items.do({| item |
				result = result.addAll([item.name, item.declaration]);
			});
		}, {
			items.do({| item |
				result = result.addAll([item.name]);
			});
		});
		^result;
	}

	attributes {
		^items.select({| it | it.isKindOf(VTMAttribute)});
	}

	commands{
		^items.select({| it | it.isKindOf(VTMCommand)});
	}

	returns{
		^items.select({| it | it.isKindOf(VTMReturn)});
	}

	signals{
		^items.select({| it | it.isKindOf(VTMSignal)});
	}

	mappings {
		^items.select({| it | it.isKindOf(VTMMapping)});
	}

	cues {
		^items.select({| it | it.isKindOf(VTMCue)});
	}

	scores {
		^items.select({| it | it.isKindOf(VTMScore)});
	}

	path{
		^parent.fullPath;
	}

	manager{
		^parent;
	}

	fullPath{
		var result;
		result = this.path;
		if(parent !== VTM.local, {
			result = result ++ "/";
		});
		result = result ++ this.leadingSeparator ++ this.name;
		^result.asSymbol;
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

	*makeDataManagerDeclaration{| descriptions, valueDeclarations |
		var result = VTMOrderedIdentityDictionary[];
		descriptions.keysValuesDo({| key, val |
			result.put(key, val);
			if(valueDeclarations.includesKey(key), {
				result[key].put(\value, valueDeclarations[key]);
			});
		});
		^result;
	}

	addForwarding{| key, itemName, addr, path, vtmJson = false, mapFunc |
		var item = items[itemName];
		item.addForwarding(key, addr, path, vtmJson, mapFunc);
	}

	removeForwarding{| key, itemName |
		var item = items[itemName];
		item.removeForwarding(key);
	}

	removeAllForwardings{
		items.do({| item |
			item.removeAllForwarding;
		});
	}

	enableForwarding{
		items.do({| item |
			item.enableForwarding;
		});
	}

	disableForwarding{
		items.do({| item |
			item.disableForwarding;
		});
	}

	update{| theChanged, whatChanged ...more |
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

	makeView{| parent, bounds, viewDef, settings |
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

	hasChildKey{arg key;
		^items.includesKey(key);
	}

	getChild{arg key;
		^items.at(key);
	}

	find{arg vtmPath;
		if(vtmPath.isKindOf(VTMPath), {
			if(vtmPath.isGlobal, {
				^VTM.local.find(vtmPath);
			}, {
				var i = 0, result;
				var child;
				child = this;
				while({i < vtmPath.length}, {
					var childKey = vtmPath.at(i).asSymbol;
					if(child.hasChildKey(childKey), {
						child = child.getChild(childKey);
						childKey = vtmPath.at(i).asSymbol;
						i = i + 1;
						if(i == vtmPath.length, {
							^child;
						});
					}, {
						i = vtmPath.length; //this stops the while loop
					});
				});
				^nil; //return nil here if not found
			});
		}, {
			"Not a VTMPath: %[%]".format(
				vtmPath, vtmPath.class).vtmwarn(0, thisMethod);
			^nil;
		});
	}

}

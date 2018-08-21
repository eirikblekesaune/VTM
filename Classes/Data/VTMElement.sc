/*
An Element is an object that has components.
*/
VTMElement : VTMData {
	var <components;
	var compNameRoutes;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initElement;
	}

	initElement{
		this.initComponents;
		this.changed(\components);
		//TODO: register with LocalNetworkNode singleton.
	}

	initComponents{
		var itemDeclarations;
		components = VTMOrderedIdentityDictionary[];

		itemDeclarations = VTMOrderedIdentityDictionary.new;
		this.class.attributeDescriptions.keysValuesDo({arg attrKey, attrDesc;
			itemDeclarations.put(attrKey, attrDesc.deepCopy);
			if(declaration.includesKey(attrKey), {
				itemDeclarations.at(attrKey).put(\value, declaration[attrKey]);
			});
		});
		components.put(\attributes,  VTMAttributeManager(itemDeclarations));

		itemDeclarations = this.class.signalDescriptions.deepCopy;
		components.put(\signals, VTMSignalManager(itemDeclarations));

		itemDeclarations = this.class.returnDescriptions.deepCopy;
		components.put(\returns, VTMReturnManager(itemDeclarations));

		itemDeclarations = this.class.commandDescriptions.deepCopy;
		components.put(\commands, VTMCommandManager(itemDeclarations));

		itemDeclarations = this.class.mappingDescriptions.deepCopy;
		components.put(\mappings, VTMMappingManager(itemDeclarations));

		itemDeclarations = this.class.cueDescriptions.deepCopy;
		components.put(\cues, VTMCueManager(itemDeclarations));

		itemDeclarations = this.class.scoreDescriptions.deepCopy;
		components.put(\scores, VTMScoreManager(itemDeclarations));

		//make a namespace routing dictionary
		compNameRoutes = VTMOrderedIdentityDictionary[];
		components.keysValuesDo({arg key, compMan;
			compMan.items.do({arg it;
				//Warn if some components have the same name
				if(compNameRoutes.includesKey(it.name), {
					"%\n\tComponent %:% hides component %:%".format(
						this.fullPath,
						key, it.name,
						compNameRoutes[it.name], it.name
					).warn;
				});
				compNameRoutes.put(it.name, key);
			})
		});
	}

	numComponents{
		^components.collect(_.size).sum;
	}

	free{
		this.components.do(_.free);
		super.free;
	}

	*attributeDescriptions{  ^VTMOrderedIdentityDictionary[]; }
	*commandDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*returnDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*signalDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*mappingDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*cueDescriptions{  ^VTMOrderedIdentityDictionary[]; }
	*scoreDescriptions{ ^VTMOrderedIdentityDictionary[]; }


	*description{
		var result = super.description;
		result.putAll(VTMOrderedIdentityDictionary[
			\attributes -> this.attributeDescriptions,
			\commands -> this.commandDescriptions,
			\signals -> this.signalDescriptions,
			\returns -> this.returnDescriptions,
			\mappings -> this.mappingDescriptions,
			\cues -> this.cueDescriptions,
			\scores -> this.scoreDescriptions
		]);
		^result;
	}

	//set attribute values.
	set{arg key...args;
		components[\attributes].set(key, *args);
	}

	//get attribute(init or run-time) or parameter(init-time) values.
	get{arg key;
		var result = components[\attributes].get(key);
		if(result.notNil, {
			^result;
		});
		//if no attribute found try getting a parameter
		^super.get(key);
	}

	//do command with possible value args. Only run-time.
	doCommand{arg key ...args;
		components[\commands].doCommand(key, *args);
	}

	//get return results. Only run-time
	query{arg key;
		^components[\returns].query(key);
	}

	//emits a signal
	//should not be used outside the class.
	//TODO: How to make this method esily avilable from within a
	//context definition, and still protected from the outside?
	emit{arg key...args;
		components[\signals].emit(key, *args);
	}

	return{arg key ...args;
		components[\returns].return(key, *args);
	}

	onSignal{arg key, func;
		if(components[\signals].hasItemNamed(key), {
			components[\signals][key].action_(func);
		});
	}

	attributes {
		^components[\attributes].names;
	}

	commands{
		^components[\commands].names;
	}

	returns{
		^components[\returns].names;
	}

	signals{
		^components[\signals].names;
	}

	mappings {
		^components[\mappings].names;
	}

	cues {
		^components[\cues].names;
	}

	scores {
		^components[\scores].names;
	}

	addForwarding{arg key, compName, itemName,  addr, path, vtmJson = false, mapFunc;
		var comp = switch(compName,
			\attributes, {components[\attributes]},
			\returns, {components[\returns]}
		);
		comp.addForwarding(key, itemName, addr, path, vtmJson, mapFunc);
	}

	removeForwarding{arg key, compName, itemName;
		var comp = switch(compName,
			\attributes, {components[\attributes]},
			\returns, {components[\returns]}
		);
		comp.removeForwarding(key, itemName);
	}

	removeAllForwardings{
		this.components.select(_.notNil).do({arg comp;
			comp.removeAllForwarding;
		});
	}

	enableForwarding{
		this.components.select(_.notNil).do({arg comp;
			comp.enableForwarding;
		});
	}

	disableForwarding{
		this.components.select(_.notNil).do({arg comp;
			comp.disableForwarding;
		});
	}

	*viewClass{
		^\VTMElementView.asClass;
	}

}


/*
An Element is an object that has controls.
*/
VTMElement : VTMData {
	var <controls;
	var compNameRoutes;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initElement;
	}

	initElement{
		this.initControlManagers;
		this.changed(\controls);
		//TODO: register with LocalNetworkNode singleton.
	}

	initControlManagers{
		var itemDeclarations;
		controls = VTMOrderedIdentityDictionary[];

		itemDeclarations = VTMOrderedIdentityDictionary.new;
		this.class.attributeDescriptions.keysValuesDo({arg attrKey, attrDesc;
			itemDeclarations.put(attrKey, attrDesc.deepCopy);
			if(declaration.includesKey(attrKey), {
				itemDeclarations.at(attrKey).put(\value, declaration[attrKey]);
			});
		});
		controls.put(\attributes,  VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.signalDescriptions.deepCopy;
		controls.put(\signals, VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.returnDescriptions.deepCopy;
		controls.put(\returns, VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.commandDescriptions.deepCopy;
		controls.put(\commands, VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.mappingDescriptions.deepCopy;
		controls.put(\mappings, VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.cueDescriptions.deepCopy;
		controls.put(\cues, VTMControlManager(itemDeclarations));

		itemDeclarations = this.class.scoreDescriptions.deepCopy;
		controls.put(\scores, VTMControlManager(itemDeclarations));

		//make a namespace routing dictionary
		compNameRoutes = VTMOrderedIdentityDictionary[];
		controls.keysValuesDo({arg key, compMan;
			compMan.items.do({arg it;
				//Warn if some controls have the same name
				if(compNameRoutes.includesKey(it.name), {
					"%\n\tControl %:% hides control %:%".format(
						this.fullPath,
						key, it.name,
						compNameRoutes[it.name], it.name
					).warn;
				});
				compNameRoutes.put(it.name, key);
			})
		});
	}

	numControlManagers{
		^controls.collect(_.size).sum;
	}

	free{
		this.controls.do(_.free);
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
		controls[\attributes].set(key, *args);
	}

	//get attribute(init or run-time) or parameter(init-time) values.
	get{arg key;
		var result = controls[\attributes].get(key);
		if(result.notNil, {
			^result;
		});
		//if no attribute found try getting a parameter
		^super.get(key);
	}

	//do command with possible value args. Only run-time.
	doCommand{arg key ...args;
		controls[\commands].doCommand(key, *args);
	}

	//get return results. Only run-time
	query{arg key;
		^controls[\returns].query(key);
	}

	//emits a signal
	//should not be used outside the class.
	//TODO: How to make this method esily avilable from within a
	//context definition, and still protected from the outside?
	emit{arg key...args;
		controls[\signals].emit(key, *args);
	}

	return{arg key ...args;
		controls[\returns].return(key, *args);
	}

	onSignal{arg key, func;
		if(controls[\signals].hasItemNamed(key), {
			controls[\signals][key].action_(func);
		});
	}

	attributes {
		^controls[\attributes].names;
	}

	commands{
		^controls[\commands].names;
	}

	returns{
		^controls[\returns].names;
	}

	signals{
		^controls[\signals].names;
	}

	mappings {
		^controls[\mappings].names;
	}

	cues {
		^controls[\cues].names;
	}

	scores {
		^controls[\scores].names;
	}

	addForwarding{arg key, compName, itemName,  addr, path, vtmJson = false, mapFunc;
		var comp = switch(compName,
			\attributes, {controls[\attributes]},
			\returns, {controls[\returns]}
		);
		comp.addForwarding(key, itemName, addr, path, vtmJson, mapFunc);
	}

	removeForwarding{arg key, compName, itemName;
		var comp = switch(compName,
			\attributes, {controls[\attributes]},
			\returns, {controls[\returns]}
		);
		comp.removeForwarding(key, itemName);
	}

	removeAllForwardings{
		this.controls.select(_.notNil).do({arg comp;
			comp.removeAllForwarding;
		});
	}

	enableForwarding{
		this.controls.select(_.notNil).do({arg comp;
			comp.enableForwarding;
		});
	}

	disableForwarding{
		this.controls.select(_.notNil).do({arg comp;
			comp.disableForwarding;
		});
	}

	*viewClass{
		^\VTMElementView.asClass;
	}

}


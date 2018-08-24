/*
An Element is an object that has controls.
*/
VTMElement : VTMData {
	var <controls;

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

		itemDeclarations = VTMOrderedIdentityDictionary.new;
		this.class.controlDescriptions.keysValuesDo({arg ctrlKey, ctrlDesc;
			itemDeclarations.put(ctrlKey, ctrlDesc.deepCopy);
			if(declaration.includesKey(ctrlKey), {
				itemDeclarations.at(ctrlKey).put(\value, declaration[ctrlKey]);
			});
		});
		controls = VTMControlManager();
	}

	numControls{
		^controls.size;
	}

	free{
		this.controls.do(_.free);
		super.free;
	}

	*controlDescriptions{
		var result = VTMOrderedIdentityDictionary.new;
	}

	*description{
		var result = super.description;
		result.putAll(VTMOrderedIdentityDictionary[
			\controls -> this.controlDescriptions;
		]);
		^result;
	}

	//set attribute values.
	set{arg key...args;
		controls[key].set(*args);
	}

	//get attribute(init or run-time) or parameter(init-time) values.
	get{arg key;
		var result = controls[key].get;
		if(result.notNil, {
			^result;
		});
		//if no attribute found try getting a parameter
		^super.get(key);
	}

	//do command with possible value args. Only run-time.
	doCommand{arg key ...args;
		controls[key].doCommand(*args);
	}

	//get return results. Only run-time
	query{arg key;
		^controls[key].query;
	}

	//emits a signal
	//should not be used outside the class.
	//TODO: How to make this method esily avilable from within a
	//context definition, and still protected from the outside?
	emit{arg key...args;
		controls[key].emit(*args);
	}

	return{arg key ...args;
		controls[key].return;
	}

	onSignal{arg key, func;
		if(controls.hasItemNamed(key), {
			controls[key].action_(func);
		});
	}

	attributes {
		^controls.select({arg it; it.isKindOf(VTMAttribute)});
	}

	commands{
		^controls.select({arg it; it.isKindOf(VTMCommand)});
	}

	returns{
		^controls.select({arg it; it.isKindOf(VTMReturn)});
	}

	signals{
		^controls.select({arg it; it.isKindOf(VTMSignal)});
	}

	mappings {
		^controls.select({arg it; it.isKindOf(VTMMapping)});
	}

	cues {
		^controls.select({arg it; it.isKindOf(VTMCue)});
	}

	scores {
		^controls.select({arg it; it.isKindOf(VTMScore)});
	}

	addForwarding{arg key, itemName,  addr, path, vtmJson = false, mapFunc;
		controls[key].addForwarding(addr, path, vtmJson, mapFunc);
	}

	removeForwarding{arg key, addr, path;
		controls[key].removeForwarding(addr, path);
	}

	removeAllForwardings{
		this.controls.do({arg ctrl;
			ctrl.removeAllForwarding;
		});
	}

	enableForwarding{
		this.controls.do({arg ctrl;
			ctrl.enableForwarding;
		});
	}

	disableForwarding{
		this.controls.do({arg ctrl;
			ctrl.disableForwarding;
		});
	}

	*viewClass{
		^\VTMElementView.asClass;
	}

}


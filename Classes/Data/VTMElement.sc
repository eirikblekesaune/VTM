/*
An Element is an object that has controls.
*/
VTMElement : VTMData {
	var <controls;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager).initElement;
	}

	initElement{
		this.initControls;
		//TODO: register with LocalNetworkNode singleton.
	}

	initControls{
		controls = VTMControlManager(this);
		this.class.controlDescriptions.keysValuesDo({|ctrlKey, ctrlDesc|
			var newCtrl;
			if(declaration.includesKey(ctrlKey), {
				ctrlDesc.put(\value, declaration[ctrlKey]);
			});
			newCtrl = VTMControl.makeFromDescription(ctrlKey, ctrlDesc, controls);
		});
		this.changed(\controls);
	}

	numControls{
		^controls.size;
	}

	free{
		controls.do(_.free);
		super.free;
	}

	*controlDescriptions{
		var result = VTMOrderedIdentityDictionary.new;
		^result;
	}

	*description{
		var result = super.description;
		result.putAll(VTMOrderedIdentityDictionary[
			\controls -> this.controlDescriptions;
		]);
		^result;
	}

	//set attribute values.
	set{| key...args |
		controls[key].valueAction_(*args);
	}

	//get attribute(init or run-time) or parameter(init-time) values.
	get{| key |
		var result = controls[key].value;
		if(result.notNil, {
			^result;
		});
		//if no attribute found try getting a parameter
		^super.get(key);
	}

	//do command with possible value args. Only run-time.
	doCommand{| key ...args |
		var ctrl = controls[key];
		if(ctrl.notNil, {
			ctrl.doCommand(*args);
		}, {
			"No command named: '%' for '%'".format(key, this.fullPath).vtmwarn(1, thisMethod);
		});
	}

	//get return results. Only run-time
	query{| key |
		^controls[key].query;
	}

	//emits a signal
	//should not be used outside the class.
	//TODO: How to make this method esily avilable from within a
	//context definition, and still protected from the outside?
	emit{| key...args |
		controls[key].emit(*args);
	}

	return{| key ...args |
		controls[key].return;
	}

	onSignal{| key, func |
		if(controls.hasItemNamed(key), {
			controls[key].action_(func);
		});
	}

	attributes { ^controls.attributes; }

	commands{ ^controls.command; }

	returns{ ^controls.return; }

	signals{ ^controls.signal; }

	mappings { ^controls.mappings; }

	cues { ^controls.cues; }

	scores { ^controls.scores; }

	addForwarding{| key, itemName,  addr, path, vtmJson = false, mapFunc |
		controls[key].addForwarding(addr, path, vtmJson, mapFunc);
	}

	removeForwarding{| key, addr, path |
		controls[key].removeForwarding(addr, path);
	}

	removeAllForwardings{
		this.controls.do({| ctrl |
			ctrl.removeAllForwarding;
		});
	}

	enableForwarding{
		this.controls.do({| ctrl |
			ctrl.enableForwarding;
		});
	}

	disableForwarding{
		this.controls.do({| ctrl |
			ctrl.disableForwarding;
		});
	}

	*viewClass{
		^\VTMElementView.asClass;
	}

	trace{arg bool = true;
		controls.trace(bool);
	}

}


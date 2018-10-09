/*
An Element is an object that has controls.
*/
VTMElement : VTMData {
	var <controls;

	*new{arg name, declaration;
		^super.new(name, declaration).initElement;
	}

	initElement{
		this.initControls;
		//TODO: register with LocalNetworkNode singleton.
	}

	initControls{
		controls = VTMControlManager(this);
		this.class.controlDescriptions.keysValuesDo({arg ctrlKey, ctrlDesc;
			var newCtrl;
			newCtrl = VTMControl.makeFromDescription(ctrlKey, ctrlDesc);
			if(declaration.includesKey(ctrlKey), {
				newCtrl.set(\value, declaration[ctrlKey]);
			});
			controls.addItem(ctrlKey, newCtrl);
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

	attributes { ^controls.attributes; }

	commands{ ^controls.command; }

	returns{ ^controls.return; }

	signals{ ^controls.signal; }

	mappings { ^controls.mappings; }

	cues { ^controls.cues; }

	scores { ^controls.scores; }

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


/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{| name, declaration, manager |
		manager = manager ?? { VTM.local.findManagerForContextClass(this) };
		"got amanager: %".format(manager.fullPath).postln;
		^super.new(name, declaration, manager).initControl;
	}

	*makeFromDescription{| name, description, manager |
		var result;
		var mode, descName;
		mode = description.removeAt(\mode);
		descName = description.removeAt(\name);
		name = name ? descName;
		result = this.perform(mode ? \attribute, name, description, manager);

		^result;
	}

	*attribute{| name, declaration |
		^VTMAttribute(name, declaration);
	}
	*command{| name, declaration |
		^VTMCommand(name, declaration);
	}
	*signal{| name, declaration |
		^VTMSignal(name, declaration);
	}
	*return{| name, declaration |
		^VTMReturn(name, declaration);
	}
	*score{| name, declaration |
		^VTMScore(name, declaration);
	}
	*cue{| name, declaration |
		^VTMCue(name, declaration);
	}
	*mapping{| name, declaration |
		^VTMMapping(name, declaration);
	}

	initControl{
	}

	free{
		super.free;
	}

	trace{arg bool = true;
	}

	makeView{| parent, bounds, viewDef, settings |
		^this.class.viewClass.new(parent, bounds, viewDef, settings, this);
	}

	action{
		^{}; //TEMP getter, cheat mode
	}

	action_{//TEMP setter, cheat mode
		//
	}
}

/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{| name, declaration, manager |
		manager = manager ?? {
			VTM.local.findManagerForContextClass(this)
		};
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

	*attribute{| name, declaration, manager |
		^VTMAttribute(name, declaration, manager);
	}
	*command{| name, declaration, manager |
		^VTMCommand(name, declaration, manager);
	}
	*signal{| name, declaration, manager |
		^VTMSignal(name, declaration, manager);
	}
	*return{| name, declaration, manager |
		^VTMReturn(name, declaration, manager);
	}
	*score{| name, declaration, manager |
		^VTMScore(name, declaration, manager);
	}
	*cue{| name, declaration, manager |
		^VTMCue(name, declaration, manager);
	}
	*mapping{| name, declaration, manager |
		^VTMMapping(name, declaration, manager);
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

	find{arg vtmPath;
		^manager.find(vtmPath);
	}
}

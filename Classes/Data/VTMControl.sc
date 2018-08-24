/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initControl;
	}

	*makeFromDescription{arg name, description, manager;
		var result;
		var mode;
		mode = description.removeAt(\mode);
		this.perform(mode ? \attribute, name, description, manager);

		^result;

	}

	*attribute{arg name, declaration, manager;
		^VTMAttribute(name, declaration, manager);
	}
	*command{arg name, declaration, manager;
		^VTMCommand(name, declaration, manager);
	}
	*signal{arg name, declaration, manager;
		^VTMSignal(name, declaration, manager);
	}
	*return{arg name, declaration, manager;
		^VTMReturn(name, declaration, manager);
	}
	*score{arg name, declaration, manager;
		^VTMScore(name, declaration, manager);
	}
	*cue{arg name, declaration, manager;
		^VTMCue(name, declaration, manager);
	}
	*mapping{arg name, declaration, manager;
		^VTMMapping(name, declaration, manager);
	}

	initControl{
	}

	free{
		super.free;
	}

	makeView{arg parent, bounds, viewDef, settings;
		^this.class.viewClass.new(parent, bounds, viewDef, settings, this);
	}
}

/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{| name, declaration |
		^super.new(name, declaration).initControl;
	}

	*makeFromDescription{| name, description |
		var result;
		var mode;
		mode = description.removeAt(\mode);
		this.perform(mode ? \attribute, name, description);

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
}

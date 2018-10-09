/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{arg name, declaration;
		^super.new(name, declaration).initControl;
	}

	*makeFromDescription{arg name, description;
		var result;
		var mode;
		mode = description.removeAt(\mode);
		this.perform(mode ? \attribute, name, description);

		^result;
	}

    *parameter{arg name, declaration;
		^VTMParameter(name, declaration);
    }
	*attribute{arg name, declaration;
		^VTMAttribute(name, declaration);
	}
	*command{arg name, declaration;
		^VTMCommand(name, declaration);
	}
	*signal{arg name, declaration;
		^VTMSignal(name, declaration);
	}
	*return{arg name, declaration;
		^VTMReturn(name, declaration);
	}
	*score{arg name, declaration;
		^VTMScore(name, declaration);
	}
	*cue{arg name, declaration;
		^VTMCue(name, declaration);
	}
	*mapping{arg name, declaration;
		^VTMMapping(name, declaration);
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

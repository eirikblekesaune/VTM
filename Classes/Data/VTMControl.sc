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

	initControl{
	}

	free{
		super.free;
	}

	makeView{arg parent, bounds, viewDef, settings;
		^this.class.viewClass.new(parent, bounds, viewDef, settings, this);
	}
}

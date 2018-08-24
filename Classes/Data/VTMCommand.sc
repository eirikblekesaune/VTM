VTMCommand : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initCommand;
	}

	initCommand{}

	doCommand{arg ...args;
		valueObj.valueAction_( *args );
	}
}

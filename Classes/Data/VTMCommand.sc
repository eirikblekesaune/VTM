VTMCommand : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{arg name, declaration;
		^super.new(name, declaration ).initCommand;
	}

	initCommand{}

	doCommand{arg ...args;
		valueObj.valueAction_( *args );
	}
}

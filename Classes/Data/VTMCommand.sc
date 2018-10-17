VTMCommand : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{| name, declaration |
		^super.new(name, declaration ).initCommand;
	}

	initCommand{}

	doCommand{| ...args |
		valueObj.valueAction_( *args );
	}
}

VTMCommand : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager ).initCommand;
	}

	initCommand{}

	doCommand{| ...args |
		valueObj.valueAction_( *args );
	}
}

VTMAttribute : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initAttribute;
	}

	initAttribute{}

}

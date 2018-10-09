VTMAttribute : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{arg name, declaration;
		^super.new(name, declaration ).initAttribute;
	}

	initAttribute{}

	value_{arg ...args;
		valueObj.value_(*args);
	}

	value{
		^valueObj.value;
	}

	valueAction_{arg ...args;
		valueObj.valueAction_(*args);
	}
}

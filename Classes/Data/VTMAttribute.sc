VTMAttribute : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager ).initAttribute;
	}

	initAttribute{}

	value_{| ...args |
		valueObj.value_(*args);
	}

	value{
		^valueObj.value;
	}

	valueAction_{| ...args |
		valueObj.valueAction_(*args);
	}
}

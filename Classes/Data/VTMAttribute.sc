VTMAttribute : VTMValueControl {
	classvar <isAbstractClass=false;

	*new{| name, declaration |
		^super.new(name, declaration ).initAttribute;
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

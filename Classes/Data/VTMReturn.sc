VTMReturn : VTMValueControl {
	classvar <isAbstractClass=false;

	query{
		var val;
		if(valueObj.action.notNil, {
			val = valueObj.action.value;
			valueObj.value = val;
		});
		^valueObj.value;
	}

	value{
		^this.query;
	}

	//This method is supposed to only be used inside
	//the context in which it has been defined.
	value_{arg ...args;
		valueObj.value_(*args);
	}
}

VTMTupleValue : VTMListValue {
	isValidType{| val |
		var result = false;
		if(super.isValidType(val), {
			result = this.validate(val);
		});
		^result;
	}

	*type{ ^\tuple; }
	prDefaultValueForType{
		^[];
	}
}

VTMIntegerValue : VTMNumberValue {
	*type{ ^\integer; }

	*prDefaultValueForType{ ^0; }
	//this class will accept numbers, either Integers or Floats
	//but it will convert Float numbers to Integers


	minVal_{| val |
		if(val.class == Float, {
			val = val.asInteger;
		});
		super.minVal_(val);
	}

	maxVal_{| val |
		if(val.class == Float, {
			val = val.asInteger;
		});
		super.maxVal_(val);
	}

	stepsize_{| val |
		if(val.class == Float, {
			val = val.asInteger;
		});
		super.stepsize_(val);
	}

	value_{| val |
		if(val.class == Float, {
			val = val.asInteger;
		});
		super.value_(val);
	}

	defaultValue_{| val |
		if(val.class == Float, {
			val = val.asInteger;
		});
		super.defaultValue_(val);
	}
}

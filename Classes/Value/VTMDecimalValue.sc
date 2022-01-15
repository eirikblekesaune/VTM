VTMDecimalValue : VTMNumberValue {
	*type{ ^\decimal; }

	*prDefaultValueForType{ ^0.0; }

	minVal_{| val |
		if(val.class == Integer, {
			val = val.asFloat;
		});
		super.minVal_(val);
	}

	maxVal_{| val |
		if(val.class == Integer, {
			val = val.asFloat;
		});
		super.maxVal_(val);
	}

	stepsize_{| val |
		if(val.class == Integer, {
			val = val.asFloat;
		});
		super.stepsize_(val);
	}

	prInitSpec{
		spec = ControlSpec(this.minVal, this.maxVal, default: this.defaultValue).asSpec;
	}

	value_{| val |
		if(val.class == Integer, {
			val = val.asFloat;
			super.value_(val);
		}, {
			super.value_(val);
		});
	}

	defaultValue_{| val |
		if(val.class == Integer, {
			val = val.asFloat;
		});
		super.defaultValue_(val);
	}

	parseStringValue{|str|
		str.asFloat;
	}

	*defaultViewType{ ^\slider; }

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\minVal -> ( type: \decimal, defaultValue: 0.0),
				\maxVal -> ( type: \decimal, defaultValue: 1.0),
				\stepsize -> ( type: \decimal, defaultValue: 0.01),
				\defaultValue -> ( type: \decimal, defaultValue: 0.0)
			]
		);
	}

	*minValDefault { ^0.0; }
	*maxValDefault { ^1.0; }
}

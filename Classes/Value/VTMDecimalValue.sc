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

	*defaultViewType{ ^\slider; }
}

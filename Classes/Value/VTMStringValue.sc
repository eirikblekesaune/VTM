VTMStringValue : VTMValue {

	*type{ ^\string; }

	*prDefaultValueForType{ ^""; }

	isValidType{| val |
		^(val.isKindOf(String) or: {val.isKindOf(Symbol)});
	}

	*new{| properties |
		^super.new(properties).initStringValue;
	}

	initStringValue{| stringAttr_ |
		if(properties.notEmpty, {
			if(properties.includesKey(\pattern), {
				this.pattern_(properties[\pattern]);
			});
			if(properties.includesKey(\matchPattern), {
				this.matchPattern_(properties[\matchPattern]);
			});
		});
	}

	clear{| doActionUponClear = false |
		var valToSet;
		//Set to default if pattern matching is enabled
		if(this.matchPattern and: {this.pattern.isEmpty.not}, {
			valToSet = this.defaultValue;
		}, {
			valToSet = "";
		});
		this.value_(valToSet);
		if(doActionUponClear, {
			this.doAction;
		});
	}

	*propertyKeys{
		^(super.propertyKeys ++ [\matchPattern, \pattern]);
	}

	//Properties getters and setters
	matchPattern_{| val |
		if(val.isKindOf(Boolean), {
			this.set(\matchPattern, val);
			//Check the current value for matching, set to default if not.
			if(this.matchPattern and: {this.pattern.notEmpty}, {
				if(this.pattern.matchRegexp(this.value).not, {
					this.value_(this.defaultValue);
				});
			});
		}, {
			"StringValue:matchPattern_- ignoring val because of non-matching pattern: '%[%]'".format(
				val, val.class
			).warn;
		});
	}
	matchPattern{ ^this.get(\matchPattern) ? false; }

	pattern_{| val |
		var result = val ? "";
		if(val.isString or: {val.isKindOf(Symbol)}, {
			this.set(\pattern, val.asString);
		}, {
			"StringValue:pattern_ - ignoring val because of invalid type: '%[%]'".format(
				val, val.class
			).warn;
		});
	}
	pattern{ ^this.get(\pattern) ? ""; }

	defaultValue_{| val |
		var inval = val.copy.asString;
		if(inval.class == Symbol, {//Symbols are accepted and converted into strings
			inval = inval.asString;
		});
		if(this.matchPattern and: {this.pattern.isEmpty.not}, {
			if(this.pattern.matchRegexp(inval), {
				super.defaultValue_(inval);
			}, {
				"StringValue:defaultValue_ - ignoring val because of unmatched pattern pattern: '%[%]'".format(
					inval, this.pattern
				).warn;
			});
		}, {
			super.defaultValue_(inval);
		});
	}

	enum_{|vals|
		var enumVals;
		vals.do({|val|
			var inval = val.copy.asString;
			if(inval.class == Symbol, {//Symbols are accepted and converted into strings
				inval = inval.asString;
			});
			enumVals = enumVals.add(inval);
		});
		super.enum_(enumVals);
	}

	parseStringValue{|str|
		^str;
	}

	value_{| val |
		var inval = val.copy.asString;
		if(inval.class == Symbol, {//Symbols are accepted and converted into strings
			inval = inval.asString;
		});
		if(this.matchPattern and: {this.pattern.isEmpty.not}, {
			if(this.pattern.matchRegexp(inval), {
				super.value_(inval, true);
			}, {
				"StringValue:value_ - ignoring val because of unmatched pattern pattern: '%[%]'".format(
					inval, this.pattern
				).warn;
			});
		}, {
			super.value_(inval, true);
		});
	}
}

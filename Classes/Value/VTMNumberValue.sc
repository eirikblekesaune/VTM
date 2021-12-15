VTMNumberValue : VTMValue {
	var <dataspace;//Optional instance of VTMDataspace
	var <scheduler;//Where instances of VTMNumberInterpolator will be
	var spec;

	*new{| properties |
		^super.new(properties).initNumberValue;
	}

	isValidType{| val |
		^val.isKindOf(SimpleNumber);
	}

	isValidValue{| val |
		^true; //TEMP implement checks in subclasses
	}

	initNumberValue{
		if(properties.notEmpty, {
			if(properties.includesKey(\clipmode), {
				this.clipmode = properties[\clipmode];
			});
			if(properties.includesKey(\minVal), {
				this.minVal = properties[\minVal];
			});
			if(properties.includesKey(\maxVal), {
				this.maxVal = properties[\maxVal];
			});
			if(properties.includesKey(\stepsize), {
				this.stepsize = properties[\stepsize];
			});
		});
		this.prInitSpec;
		scheduler = Routine.new({});
	}

	prInitSpec{
		spec = ControlSpec(this.minVal, this.maxVal, step: this.stepsize, default: this.defaultValue).asSpec;
	}

	stopRamp{
		scheduler.stop;
	}

	isRamping{
		^scheduler.notNil and: {scheduler.isPlaying()}
	}

	ramp{| targetValue, time, curve = \lin |
		if(this.isRamping, {
			this.stopRamp;
		});
		scheduler = fork{
			var stream, val;
			stream = Env([this.value, targetValue], [time]).asPseg.asStream;
			val = stream.next;
			loop{
				if(val.isNil, {
					this.valueAction_(targetValue);
					thisThread.stop;
				});
				this.valueAction_(val);
				0.05.wait;
				val = stream.next;
			};
		};
	}


	defaultValue_{| val |
		super.defaultValue = this.prCheckRangeAndClipValue(val);
	}

	increment{| doAction = true |
		if(doAction, {
			this.valueAction_(this.value + this.stepsize);
		}, {
			this.value_(this.value + this.stepsize);
		});
	}

	decrement{ | doAction = true |
		if(doAction, {
			this.valueAction_(this.value - this.stepsize);
		}, {
			this.value_(this.value - this.stepsize);
		});
	}

	*propertyKeys{
		^super.propertyKeys.addAll([\minVal, \maxVal, \stepsize, \clipmode/*, \dataspace*/]);
	}


	prCheckRangeAndClipValue{| val |
		var result;
		result = val;
		switch(this.clipmode,
			\none, {
				"NONE CLIPPING".vtmdebug(5, thisMethod);
			},
			\low, {
				"LOW CLIPPING".vtmdebug(5, thisMethod);
				if(this.minVal.notNil and: {val < this.minVal}, {
					result = val.max(this.minVal);
				});
			},
			\high, {
				"HIGH CLIPPING".vtmdebug(5, thisMethod);
				if(this.maxVal.notNil and: {val > this.maxVal}, {
					result = val.min(this.maxVal);
				});
			},
			\both, {
				"BOTH CLIPPING".vtmdebug(5, thisMethod);
				if(this.minVal.notNil and: {val < this.minVal}, {
					result = val.max(this.minVal);
				}, {
					if(this.maxVal.notNil and: {val > this.maxVal}, {
						result = val.min(this.maxVal);
					});
				});
			}
		);
		^result;
	}

	*defaultViewType{ ^\slider; }

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\clipmode -> (
					type: \string,
					enum: ["none", "low", "high", "both"],
					restrictValueToEnum: true,
					defaultValue: "both")
			]
		);
	}


	//Properties setters and getters
	minVal_{ | val |
		if(val.isNil, {
			this.set(\minVal, nil);
			this.prInitSpec;
			this.changed(\minVal);
		}, {
			if(this.isValidType(val), {
				this.set(\minVal, val);
				this.prInitSpec;
				this.changed(\minVal);
				this.value_(this.value);//update the value, might be clipped in the value set method
			}, {
				"NumberValue:minVal_ - ignoring val because of invalid type: '%[%]'".format(
					val, val.class
				).warn;
			});
		});
	}
	minVal{ ^this.get(\minVal) ? this.class.minValDefault; }

	maxVal_{ | val |
		if(val.isNil, {
			this.set(\maxVal, nil);
			this.prInitSpec;
			this.changed(\maxVal);
		}, {
			if(this.isValidType(val), {
				this.set(\maxVal, val);
				this.prInitSpec;
				this.changed(\maxVal);
				this.value_(this.value);//update the value, might be clipped in the value set method
			}, {
				"NumberValue:maxVal_ - ignoring val because of invalid type: '%[%]'".format(
					val, val.class
				).warn;
			});

		});
	}
	maxVal{ ^this.get(\maxVal) ? this.class.maxValDefault; }

	stepsize_{ | val |
		var newVal = val;
		if(this.isValidType(val), {
			if(newVal.isNegative, {
				newVal = newVal.abs;
				"NumberValue:stepsize_ - val converted to positive value".warn;
			});
			this.set(\stepsize, newVal);
			this.prInitSpec;
			this.changed(\stepsize);
		}, {
			"NumberValue:stepsize_ - ignoring val because of invalid type: '%[%]'".format(
				val, val.class
			).warn;
		});
	}
	stepsize{ ^this.get(\stepsize) ? 1; }

	clipmode_{ | val |
		if(#['none', 'low', 'high', 'both'].includes(val.asSymbol), {
			var newVal;
			this.set(\clipmode, val.asSymbol);
			this.value_(this.value);//update the value, might be clipped in the value set method
		}, {
			"NumberValue:clipmode_ - ignoring val because of invalid type: '%[%]'".format(
				val, val.class
			).warn;
		});
	}
	clipmode{ ^this.get(\clipmode) ? \none; }

	valueAction_{| val ...args|
		if(args.notNil and: {args.notEmpty}, {
			if(args.first == \ramp and: {args.size > 1} and: {args[1].isKindOf(SimpleNumber)}, {
				if(this.isRamping, {
					this.stopRamp();
				});
				this.ramp(val, args[1]);
			})
		}, {
			super.valueAction_(val);
		})
	}

	value_{| val |
		super.value_(
			this.prCheckRangeAndClipValue(val)
		);
	}

	spec{
		^spec.copy;
	}

	//Calculate a value mapped from 0.0..1.0
	//Does not set the value.
	calculateMappedValue{|val|
		^spec.map(val);
	}
	
	//Return the value unmapped to the range 0.0..1.0
	unmappedValue{
		^spec.unmap(this.value);
	}

	update{|theChanged, whatChanged ...args|
		if(theChanged === this, {
			switch(whatChanged,
				\minVal, {this.prInitSpec;},
				\maxVal, {this.prInitSpec;},
				\stepsize, {this.prInitSpec;}
			);
		});
	}

}

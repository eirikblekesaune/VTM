TestVTMNumberValue : TestVTMValue {

	*classesForTesting{
		^[
			VTMDecimalValue,
			VTMIntegerValue
		];
	}

	*makeRandomProperty{arg key, params;
		var result;
		result = super.makeRandomProperty(key, params);
		if(result.isNil, {
			switch(key,
				\minVal, { result = this.makeRandomValue(params); },
				\maxVal, { result = this.makeRandomValue(params); },
				\stepsize, { result = this.makeRandomValue(params ? (minVal: 0.0, maxVal: 1000.0)).abs; },
				\clipmode, { result = [\none, \low, \high, \both].choose; }
			);
		});
		^result;
	}

	test_DefaultProperties{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			testClass = this.class.testclassForType( class.type );

			this.assertEquals(
				valueObj.defaultValue, valueObj.class.prDefaultValueForType, "NumberValue defaultValue defaults to 0"
			);
			//value should default to 0 if value not defined
			this.assertEquals(
				valueObj.value, valueObj.defaultValue, "NumberValue value defaults to 0"
			);
			//stepsize should default to 0
			this.assertEquals(
				valueObj.stepsize, 0, "NumberValue stepsize defaults to 0"
			);
			//clipmode should default to 'none'
			this.assertEquals(
				valueObj.clipmode, \none, "NumberValue clipmode defaults to 'none'"
			);
			//minVal should default to nil (this might change later)
			this.assert(
				valueObj.minVal.isNil, "NumberValue minVal defaults to nil"
			);
			//maxVal should default to nil (this might change later)
			this.assert(
				valueObj.maxVal.isNil, "NumberValue maxVal defaults to nil"
			);
			valueObj.free;
		});
	}

	test_SetGetValue{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			testClass = this.class.testclassForType( class.type );
			testValue = testClass.makeRandomValue;
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, testValue, "NumberValue value was set"
			);
		});
	}

	// test_OnlyCheckRangesWhenMinValAndMaxValAreDefined{
	// 	this.class.classesForTesting.do({arg class;
	// 		var testClass, testValue;
	// 		var name = "my%".format(class.name);
	// 		var valueObj = class.new(name);
	// 		var testMinVal, testMaxVal;
	// 		testClass = this.class.testclassForType( class.type );
	//
	// 		//Should only check higher values when maxVal is not nil
	// 		testValue = testClass.makeRandomValue();
	// 		testMaxVal = testClass.makeRandomValue((minVal: testValue + 1));
	// 		valueObj.value = testValue;
	//
	//
	// 		//Should only check lower values when minVal is not nil
	//
	// 		this.assertEquals(
	// 			valueObj.value, testValue, "NumberValue value was set"
	// 		);
	// 	});
	// }

	test_SetGetProperties{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			testClass = this.class.testclassForType( class.type );
			//SetGet 'minVal'
			testValue = testClass.makeRandomValue;
			valueObj.minVal = testValue;
			this.assertEquals(
				valueObj.minVal, testValue, "NumberValue minVal was set"
			);
			//SetGet 'maxVal'
			testValue = testClass.makeRandomValue;
			valueObj.maxVal = testValue;
			this.assertEquals(
				valueObj.maxVal, testValue, "NumberValue maxVal was set"
			);
			//SetGet 'stepsize'
			testValue = testClass.makeRandomValue.abs;
			valueObj.stepsize = testValue;
			this.assertEquals(
				valueObj.stepsize, testValue, "NumberValue stepsize was set"
			);
			//SetGet 'clipmode'
			testValue = testClass.makeRandomProperty(\clipmode);
			valueObj.clipmode = testValue;
			this.assertEquals(
				valueObj.clipmode, testValue.asSymbol, "NumberValue clipmode was set"
			);
		});
	}

	test_IgnoreAndWarnOnWrongTypes{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			testClass = this.class.testclassForType( class.type );

			//Should ignore and warn on wrong 'minVal' type
			testValue = -100;
			valueObj.minVal = testValue;
			valueObj.minVal = \notAValidValue;
			this.assertEquals(
				valueObj.minVal, testValue, "% minVal wasn't changed by val of invalid type".format(testClass)
			);

			//Should ignore and warn on wrong 'maxVal' type
			testValue = 300;
			valueObj.maxVal = testValue;
			valueObj.maxVal = \notAValidValue;
			this.assertEquals(
				valueObj.maxVal, testValue, "% maxVal wasn't changed by val of invalid type".format(testClass)
			);

			//Should ignore and warn on wrong 'stepsize' type
			testValue = 11;
			valueObj.stepsize = testValue;
			valueObj.stepsize = \notAValidValue;
			this.assertEquals(
				valueObj.stepsize, testValue, "% stepsize wasn't changed by val of invalid type".format(testClass)
			);

			//Should ignore and warn on wrong 'clipmode' type
			testValue = \low;
			valueObj.clipmode = testValue;
			valueObj.clipmode = 4321;
			this.assertEquals(
				valueObj.clipmode, testValue, "% clipmode wasn't changed by val of invalid type".format(testClass)
			);
		});
	}

	test_CheckValueTypeValidity{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var invalidValue;
			testClass = this.class.testclassForType( class.type );

			//type validation should block setting value
			valueObj.clipmode = \none;
			testValue = 299;
			valueObj.value = testValue;
			invalidValue = \notANumber;
			if(valueObj.isValidType(invalidValue), {
				valueObj.value = invalidValue;
			});
			this.assertEquals(
				valueObj.value, testValue, "% value wasn't changed by val of invalid type".format(testClass)
			);
		});
	}

	test_Clipmode{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			testClass = this.class.testclassForType( class.type );
			valueObj.minVal = testClass.makeRandomValue((minVal: -100.0, maxVal: 100.0));
			valueObj.maxVal = testClass.makeRandomValue((minVal: 101.0, maxVal: 1000.0));

			//Should not clip values when clipmode == none
			valueObj.clipmode = \none; //should be set by default
			testValue = testClass.makeRandomValue((minVal: -200.0, maxVal: -101.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, testValue, "% lower value was not clipped in clipmode 'none'".format(testClass)
			);
			testValue = testClass.makeRandomValue((minVal: 1001.0, maxVal: 2000.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, testValue, "% higher value was not clipped in clipmode 'none'".format(testClass)
			);

			//Should clip larger values than maxVal when clipmode == high
			valueObj.clipmode = \high;
			testValue = testClass.makeRandomValue((minVal: 1001.0, maxVal: 2000.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.maxVal, "% higher value was clipped in clipmode 'high'".format(testClass)
			);
			testValue = testClass.makeRandomValue((minVal: -200.0, maxVal: -101.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, testValue, "% lower value was not clipped in clipmode 'high'".format(testClass)
			);

			//Should clip smaller values than minVal when clipmode == low
			valueObj.clipmode = \low;
			testValue = testClass.makeRandomValue((minVal: 1001.0, maxVal: 2000.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, testValue, "% higher value was not clipped in clipmode 'low'".format(testClass)
			);
			testValue = testClass.makeRandomValue((minVal: -200.0, maxVal: -101.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.minVal, "% lower value was clipped in clipmode 'low'".format(testClass)
			);

			//Should clip all values outside range when clipmode == both
			valueObj.clipmode = \both;
			testValue = testClass.makeRandomValue((minVal: 1001.0, maxVal: 2000.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.maxVal, "% higher value was clipped in clipmode 'both'".format(testClass)
			);
			testValue = testClass.makeRandomValue((minVal: -200.0, maxVal: -101.0));
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.minVal, "% lower value was clipped in clipmode 'both'".format(testClass)
			);
		});
	}

	test_StepsizeIncrementAndDecrement{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var testStepsize, wasRun, wasCorrectValue;
			testClass = this.class.testclassForType( class.type );
			valueObj.minVal = testClass.makeRandomValue((minVal: -100.0, maxVal: 0.0));
			valueObj.maxVal = testClass.makeRandomValue((minVal: 1.0, maxVal: 200.0));
			valueObj.clipmode = \both;

			//Stepsize should be positive numbers only
			//Convert into absolute value and print warning if negative value is used.
			testStepsize = testClass.makeRandomValue((minVal: -10.0, maxVal: -1.0));
			valueObj.stepsize = testStepsize;
			this.assertEquals(
				valueObj.stepsize, testStepsize.abs, "% converted negative stepsize to positive".format(testClass)
			);

			//Should increment according to stepsize
			valueObj.clipmode_(\none);
			testValue = testClass.makeRandomValue((minVal: 50.0, maxVal: 100.0));
			testStepsize = testClass.makeRandomValue((minVal: 1.0, maxVal: 4.0));
			valueObj.stepsize = testStepsize;
			valueObj.value = testValue;
			{ valueObj.increment; } ! 3; // increment 3 times
			this.assertEquals(
				valueObj.value, testValue + (testStepsize * 3), "% value was incremented correctly with stepsize %".format(testClass, testStepsize)
			);

			//Should run action send new value to it
			wasRun = false;
			wasCorrectValue = false;
			testValue = testClass.makeRandomValue((minVal: 1.0, maxVal: 15.0));
			testStepsize = testClass.makeRandomValue((minVal: 1.0, maxVal: 5.0));
			valueObj.clipmode = \none;
			valueObj.action = {arg p;
				wasRun = true;
				wasCorrectValue = (testValue + testStepsize) == p.value; };
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			valueObj.increment;
			this.assert(
				wasRun and: {wasCorrectValue}, "% ran action with correct value".format(testClass)
			);

			//Should decrement according to stepsize
			testValue = testClass.makeRandomValue((minVal: 20.0, maxVal: 30.0));
			testStepsize = testClass.makeRandomValue((minVal: 1.0, maxVal: 5.0));
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			{valueObj.decrement} ! 2;
			this.assertEquals(
				valueObj.value, testValue - (testStepsize * 2), "% value was decremented correctly".format(testClass)
			);

			//Should not increment outside range if clipped
			valueObj.minVal = -100.0;
			valueObj.maxVal = 100.0;
			testValue = 99.0;
			testStepsize = 3.0;
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			valueObj.clipmode = \both;
			{ valueObj.increment; } ! 5;
			this.assertEquals(
				valueObj.value, valueObj.maxVal, "% value was clipped when incremented more than maxVal".format(testClass)
			);

			//Should not decrement outside range if clipped
			testValue = -99.0;
			testStepsize = 10.0;
			valueObj.minVal = -100.0;
			valueObj.maxVal = 100.0;
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			valueObj.clipmode = \both;
			{ valueObj.decrement; } ! 5;
			this.assertEquals(
				valueObj.value, valueObj.minVal, "% value was clipped when decremented more than minVal".format(testClass)
			);

			//Should disable doAction optionally when incrementing
			wasRun = false;
			valueObj.action = {arg p; wasRun = true};
			testValue = 0.0;
			testStepsize = 3.0;
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			valueObj.increment(doAction: false);
			this.assert(
				valueObj.value == (testValue + testStepsize) and: {wasRun.not},
				"% value was incremented with optional doAction false causing action not to be run".format(testClass)
			);

			//Should disable doAction optionally when decrementing
			wasRun = false;
			valueObj.action = {arg p; wasRun = true};
			testValue = 0.0;
			testStepsize = 3.0;
			valueObj.value = testValue;
			valueObj.stepsize = testStepsize;
			valueObj.decrement(doAction: false);
			this.assert(
				valueObj.value == (testValue - testStepsize) and: {wasRun.not},
				"% value was decremented with optional doAction false causing action not to be run".format(testClass)
			)
		});
	}

	test_UpdateValueWhenMinValAndMaxValChange{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var wasRun = false;
			testClass = this.class.testclassForType( class.type );
			valueObj.minVal = testClass.makeRandomValue((minVal: -100.0, maxVal: 0.0));
			valueObj.maxVal = testClass.makeRandomValue((minVal: 100.0, maxVal: 200.0));
			valueObj.action = {|p| wasRun = true; };
			valueObj.clipmode = \both;
			//set the value higher than defined maxVal
			valueObj.value = testClass.makeRandomValue((minVal: 201.0, maxVal: 300.0));

			//Should update the value after maxVal set
			//Should not run the action when maxVal change
			valueObj.maxVal = testClass.makeRandomValue((minVal: 50.0, maxVal: 90.0));
			this.assertEquals(
				valueObj.value, valueObj.maxVal,
				"% value adjusted value when maxVal changed".format(testClass)
			);
			this.assert(
				wasRun.not,
				"% - adjusting out-of-range value for maxVal did not run action".format(testClass)
			);

			//Should update the value after minVal set
			//Should not run the action when minVal change
			wasRun = false;
			valueObj.minVal = testClass.makeRandomValue((minVal: -100.0, maxVal: 0.0));//0.0;
			valueObj.maxVal = testClass.makeRandomValue((minVal: 100.0, maxVal: 200.0));//5.0;
			valueObj.value = testClass.makeRandomValue((minVal: 1.0, maxVal: 20.0));//1.0;
			valueObj.minVal = testClass.makeRandomValue((minVal: 21.0, maxVal: 50.0));//1.5;
			this.assertEquals(
				valueObj.value, valueObj.minVal,
				"% value adjusted value when minVal changed".format(testClass)
			);
			this.assert(
				wasRun.not,
				"% - adjusting out-of-range value for minVal did not run action".format(testClass)
			);
		});
	}

	test_AllowSettingMinValAndMaxValToNil{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var wasRun = false;
			testClass = this.class.testclassForType( class.type );

			valueObj.minVal = testClass.makeRandomValue((minVal: -100.0, maxVal: 0.0));
			valueObj.maxVal = testClass.makeRandomValue((minVal: 1.0, maxVal: 100.0));

			//Should allow setting minVal to nil
			try{
				valueObj.minVal = nil;
				this.assert(
					valueObj.minVal.isNil,
					"% allowed setting minVal to nil".format(testClass)
				);
			} {
				this.failed(thisMethod,
					"% failed to allow setting minVal to nil".format(testClass)
				);
			};

			//Should allow setting maxVal to nil
			try{
				valueObj.maxVal = nil;
				this.assert(
					valueObj.maxVal.isNil,
					"% allowed setting maxVal to nil".format(testClass)
				);
			} {
				this.failed(thisMethod,
					"% failed to allow setting maxVal to nil".format(testClass)
				);
			};
		});
	}


	test_OnlyClipValuesWhenMinValAndMaxValAreDefined{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var wasRun = false;
			testClass = this.class.testclassForType( class.type );

			//Should only check for lower value in clipmode 'both' if 'maxVal' is not defined
			valueObj.clipmode = \both;
			testValue = 999999.0;
			valueObj.value = testValue;
			valueObj.maxVal = nil;
			valueObj.minVal = 0.0;
			this.assert(
				valueObj.maxVal.isNil and: {valueObj.value == testValue},
				message: "% didn't clip value when maxVal was nil.".format(testClass)
			);
			testValue = -999999.0;
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.minVal,
				"% clipped value to minVal in clipmode 'both' when maxVal was nil.".format(testClass)
			);

			//Should only check for higher value in clipmode 'both' if 'minVal' is not defined
			valueObj.clipmode = \both;
			valueObj.minVal = nil;
			valueObj.maxVal = 1000.0;
			testValue = -999999.0;
			valueObj.value = testValue;
			this.assert(
				valueObj.minVal.isNil and: {valueObj.value == testValue},
				message: "% didn't clip value when minVal was nil.".format(testClass)
			);
			testValue = 999999.0;
			valueObj.value = testValue;
			this.assertEquals(
				valueObj.value, valueObj.maxVal,
				"% clipped value to maxVal in clipmode 'both' when minVal was nil.".format(testClass)
			);
		});
	}

	test_UpdateValueWhenClipmodeChange{
		this.class.classesForTesting.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var valueObj = class.new(name);
			var wasRun = false;
			testClass = this.class.testclassForType( class.type );
			valueObj.minVal = -2.0;
			valueObj.maxVal = 22.0;
			valueObj.clipmode = \none;
			valueObj.action = {|p| wasRun = true; };
			valueObj.value = 25.0;

			//Should update the higher-than-maxVal-value when clipmode changes from 'none' to 'high'
			valueObj.clipmode = \high;
			this.assertEquals(
				valueObj.value, valueObj.maxVal,
				"% - adjusted value when clipmode changed to 'high'".format(testClass)
			);
			//Should not run the action when clipmode changed
			this.assert(
				wasRun.not, "% didn't run action when adjusting the clipmode".format(testClass)
			);

			//Should update the lower-than-minVal-value when clipmode changes 'none' to 'low'
			valueObj.clipmode = \none;
			valueObj.value = -10.0;
			valueObj.minVal = -5.5;
			valueObj.minVal = 0.0;
			valueObj.clipmode = \low;
			this.assertEquals(
				valueObj.value, valueObj.minVal,
				"% - adjusted value clipmode changed to 'low'".format(testClass)
			);

			//Should not adjust value when it is higher than maxVal, and clipmode is set
			//to 'low'
			valueObj.clipmode = \none;
			valueObj.minVal = 0.0;
			valueObj.maxVal = 1.2;
			testValue = 112.0;
			valueObj.value = testValue;
			valueObj.clipmode = \low;
			this.assertEquals(
				valueObj.value, testValue,
				"% - did not adjust higher value when clipmode changed to 'low'".format(testClass)
			);

			//Should not adjust value when it is lower than minVal, and clipmode is set
			//to 'high'
			valueObj.clipmode = \none;
			valueObj.minVal = -1.0;
			valueObj.maxVal = 300.0;
			testValue = -500.0;
			valueObj.value = testValue;
			valueObj.clipmode = \high;
			this.assertEquals(
				valueObj.value, testValue,
				"% - did not adjust higher value when clipmode changed to 'low'".format(testClass)
			);


			//Should adjust value if value is higher than maxVal and clipmode set to 'both'
			valueObj.clipmode = \none;
			valueObj.minVal = 0.0;
			valueObj.maxVal = 2.0;
			valueObj.value = 2.222;
			valueObj.clipmode = \both;
			this.assertEquals(
				valueObj.value, valueObj.maxVal,
				"% adjusted higher value when clipmode set to 'both'".format(testClass)
			);

			//Should adjust value if value is lower than minVal and clipmode is set to 'both'
			valueObj.clipmode = \none;
			valueObj.minVal = -111.0;
			valueObj.maxVal = 222.0;
			valueObj.value = -222.999;
			valueObj.clipmode = \both;
			this.assertEquals(
				valueObj.value, valueObj.minVal,
				"% adjusted lower value when clipmode set to 'both'".format(testClass)
			);

			//Should not adjust higher value in clipmode transition 'low' to 'none'
			valueObj.clipmode = \low;
			valueObj.minVal = 0.0;
			valueObj.maxVal = 11.0;
			testValue = 99.0;
			valueObj.value = testValue;
			valueObj.clipmode = \none;
			this.assertEquals(
				valueObj.value, testValue,
				"% kept higher value when clipmode went from 'low' 'none'".format(testClass)
			);

			//Should not adjust lower value in clipmode transition 'high' to 'none'
			valueObj.clipmode = \high;
			valueObj.minVal = -1.0;
			valueObj.maxVal = 146.0;
			testValue = -99.0;
			valueObj.value = testValue;
			valueObj.clipmode = \none;
			this.assertEquals(
				valueObj.value, testValue,
				"% kept higher value when clipmode went from 'low' 'none'".format(testClass)
			);

		});
	}
}

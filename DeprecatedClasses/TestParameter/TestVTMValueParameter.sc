TestVTMValueParameter : TestVTMParameter {
	*testClasses{
		^[
			VTMBooleanParameter,
			VTMStringParameter,
			// VTMListParameter,
			// VTMDictionaryParameter,
			// VTMArrayParameter,
			VTMTimecodeParameter,
			VTMDecimalParameter,
			VTMIntegerParameter
			// VTMSchemaParameter,
			// VTMTupleParameter
		];
	}

	*generateRandomAttributes{arg description;
		var result = super.generateRandomAttributes(description);
		^result;
	}

	*makeRandomValue{arg params;
		^this.subclassResponsibility(thisMethod);
	}

	*makeRandomEnum{arg params;
		var minRand = 5, maxRand = 10;
		^rrand(minRand, maxRand).collect({arg i;
			[
				[i + 1, this.makeRandomSymbol((noNumbers: true, noSpaces: true))].choose,
				this.makeRandomValue
			];
		}).flatten;
	}

	*prMakeRandomAttribute{arg key, params;
		var result;
		result = super.prMakeRandomAttribute(key, params);
		if(result.isNil, {
			switch(key,
				\filterRepetitions, { result = this.makeRandomBoolean(params); },
				\value, { result = this.makeRandomValue(params); },
				\defaultValue, { result = this.makeRandomValue(params); },
				\enum, { result = this.makeRandomEnum(params); },
				\restrictValueToEnum, { result = this.makeRandomBoolean(params); }
			);
		});
		^result;
	}

	setUp{
		"Setting up a VTMValueParameterTest".postln;
	}

	tearDown{
		"Tearing down a VTMValueParameterTest".postln;
	}

	test_SetGetValue{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var param = class.new(name);
			testClass = this.class.testclassForType( class.type );
			testValue = testClass.makeRandomValue;
			param.value = testValue;
			this.assertEquals(
				param.value, testValue, "Parameter value was set [%]".format(testClass.name)
			);
			param.free;
		});
	}

	test_SetGetDefaultValue{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var param;
			try{
				testClass = this.class.testclassForType( class.type );
				testValue = testClass.makeRandomValue;
				param = class.new(name, attributes: (defaultValue: testValue));
				this.assertEquals(
					param.defaultValue, testValue, "Parameter defaultValue was set [%]".format(testClass.name)
				);
				this.assertEquals(
					param.value, testValue,
					"Parameter value was set to defined defaultValue when value was not defined [%]".format(testClass.name)
				);
				param.free;
			} {|err|
				this.failed(
					thisMethod,
					"Unknown test fail for %\n\t%".format(class, err.errorString)
				);
			};
		});
	}

	test_ResetSetValueToDefault{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue, wasRun;
			var name = "my%".format(class.name);
			var param;
			testClass = this.class.testclassForType( class.type );
			testValue = testClass.makeRandomValue;
			param = class.new(name);
			param.value = testClass.makeRandomValue;
			param.defaultValue = testClass.makeRandomValue;
			param.reset;
			this.assertEquals(
				param.value, param.defaultValue,
				"Parameter value was set to defaultValue upon reset[%]".format(testClass.name)
			);
			wasRun = false;
			param.action_({arg p; wasRun = true;});
			param.value = testClass.makeRandomValue;
			param.defaultValue = testClass.makeRandomValue;
			param.reset(doActionUponReset: true);
			this.assert(
				wasRun,
				"Parameter action was run upon reset when defined to do so [%]".format(testClass.name)
			);
			param.free;
		});
	}

	test_DefaultValueShouldNotBeNil{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue, wasRun;
			var name = "my%".format(class.name);
			var param;
			testClass = this.class.testclassForType( class.type );
			param = class.new(name);
			this.assert(
				param.defaultValue.notNil,
				"Parameter did not initialize defaultValue to nil [%]".format(
					class.name
				)
			);
			param.free;
		});
	}
	//
	test_Typechecking{
		var wrongValuesForType = (
			integer: \hei,
			decimal: \hei,
			string: 432,
			boolean: 1,
			array: \bingo,
			list: \halo,
			dictionary: -0.93,
			function: 123,
			schema: \hei,
			tuple: \halo
		);
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue, wasRun;
			var name = "my%".format(class.name);
			var param;
			testClass = this.class.testclassForType( class.type );
			param = class.new(name);
			testValue = wrongValuesForType[class.type];
			try{
				this.assert(
					param.isValidType(testValue).not,
					"Parameter value input of wrong type failed validation check corretly [%]".format(class.name)
				);
			} {|err|
				this.failed(
					thisMethod,
					"Parameter input value validation failed by unknown error. [%]\n\t%".format(class.name, err.errorString)
				);
			};
		});
	}

	test_AccessValueInAction{
		TestVTMValueParameter.testClasses.do({arg class;
			try{
				var testClass, testValue, wasRun;
				var name = "my%".format(class.name);
				var param, gotValue = false, gotParamPassed = false;
				testClass = this.class.testclassForType( class.type );
				param = class.new(name);
				testValue = testClass.makeRandomValue;
				param.value = testValue;
				param.action = {arg p;
					gotParamPassed = p === param;
					gotValue = p.value == testValue;
				};
				param.doAction;
				this.assert(gotParamPassed,
					"ValueParameter got param passed in action [%]".format(class.name));
				this.assert(gotValue,
					"ValueParameter got value in action [%]".format(class.name));
				param.free;
			} {|err|
				this.failed(
					thisMethod,
					"Parameter test failed unknown error [%]\n\t%".format(
						class.name,
						err.errorString
					)
				);
			};
		});
	}

	test_ValueAction{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var param, wasRun, gotUpdatedValue;
			wasRun = false;
			gotUpdatedValue = false;
			testClass = this.class.testclassForType( class.type );
			testValue = testClass.makeRandomValue;
			param = class.new(name);
			param.action = {arg p;
				wasRun = true;
				gotUpdatedValue = p.value == testValue;
			};
			param.value = testClass.makeRandomValue;
			param.valueAction_(testValue);
			this.assert(
				wasRun and: {gotUpdatedValue},
				"ValueParameter valueAction set correct value and passed it into action"
			);
			param.free;
		});
	}

	test_FilterRepeatingValues{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var param;
			var wasRun = false;
			testClass = this.class.testclassForType( class.type );
			testValue = testClass.makeRandomValue;
			param = class.new(name);
			param.filterRepetitions = true;
			param.action = {arg p;
				wasRun = true;
			};
			//Action should not be run when input value are equal to current value
			param.value = testValue;
			param.valueAction_(testValue);
			this.assert(
				wasRun.not, "ValueParameter action was prevented to run since values where equal"
			);
		});
	}

	test_SetVariablesFromAttributes{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var param;
			var testAttributes, wasRun = false;
			testClass = this.class.testclassForType( class.type );
			testAttributes = testClass.generateRandomAttributes(
				[
					\value,
					\defaultValue,
					\path,
					\action -> {arg p; wasRun = true; },
					\filterRepetitions,
					\name,
					\type -> class.type
				]
			);

			param = VTMParameter.makeFromAttributes(testAttributes);

			//Change string values to symbols for testing object return values
			testAttributes[\name] = testAttributes[\name].asSymbol;
			testAttributes[\path] = testAttributes[\path].asSymbol;

			[\value, \defaultValue, \path, \name, \filterRepetitions].do({arg item;
				// "CHECKING %: \n\t%[%]\n\t%[%]".format(
				// 	item,
				// 	param.perform(item), param.perform(item).class,
				// 	testAttributes[item], testAttributes[item].class
				// ).debug;
				this.assertEquals(
					param.perform(item), testAttributes[item],
					"Parameter set % through attributes [%]".format(item, class.name)
				);
			});
			param.doAction;
			this.assert(
				wasRun,
				"Parameter action was set through attributes [%]".format(class.name)
			);
			param.free;
		});
	}


	// test_GetAttributes{
	// 	TestVTMValueParameter.testClasses.do({arg class;
	// 		var testClass, testValue;
	// 		var name = "my%".format(class.name);
	// 		var param;
	// 		var testAttributes, testAttributes;
	// 		testClass = this.class.testclassForType( class.type );
	// 		testAttributes = testClass.generateRandomAttributes(
	// 			[
	// 				\value,
	// 				\defaultValue,
	// 				\path,
	// 				\action -> {arg p; 1 + 1 },
	// 				\filterRepetitions,
	// 				\name,
	// 				\type -> class.type,
	// 				\enabled -> true,
	// 				\enum
	// 			]
	// 		);
	// 		param = VTMParameter.makeFromAttributes(testAttributes);
	// 		testAttributes = testAttributes.deepCopy;
	// 		testAttributes.put(\action, testAttributes[\action].asCompileString);
	// 		this.assert(
	// 			testAttributes.keys.sect(param.attributes.keys) == testAttributes.keys,
	// 			"ValueParameter returned correct attribute keys for ValueParameter level [%]".format(class.name)
	// 		);
	// 		//			this.assertEquals(
	// 		//				testAttributes.sect(param.attributes),
	// 		//			   	testAttributes,
	// 		//			   	"ValueParameter returned correct attribute values for ValueParameter level [%]".format(class.name)
	// 		//			);
	// 		param.free;
	// 	});
	// }

	test_Enum{
		TestVTMValueParameter.testClasses.do({arg class;
			var testClass, testValue;
			var name = "my%".format(class.name);
			var param;
			var testEnum;
			var testAttributes;
			testClass = this.class.testclassForType( class.type );
			testAttributes = testClass.generateRandomAttributes(
				[
					\value,
					\defaultValue,
					\path,
					\action -> {arg p; 1 + 1 },
					\filterRepetitions,
					\name,
					\type -> class.type,
					\enabled -> true,
					\enum
				]
			);
			testEnum = testAttributes.at(\enum);
			param = VTMParameter.makeFromAttributes(testAttributes);
			testAttributes.put(\action, testAttributes[\action].asCompileString);
			this.assertEquals(
				param.enum, testEnum,
				"ValueParameter set and returned correct enum[%]".format(class.name)
			);
			param.free;
		});
	}
}

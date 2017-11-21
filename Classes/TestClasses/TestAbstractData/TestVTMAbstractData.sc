TestVTMAbstractData : VTMUnitTest {

	*classesForTesting{
		^[
			//ValueElements
			VTMAttribute,
			VTMCommand,
			VTMReturn,
			VTMSignal,
			//Elements
			VTMMapping,
			VTMDefinitionLibrary,
			VTMRemoteNetworkNode,
			//Contexts
			VTMApplication,
			//ComposableContexts
			VTMCue,
			VTMHardwareDevice,
			VTMScore,
			VTMModule,
			VTMScene
		];
	}

	*makeRandomParameters{arg params;
		var result = VTMParameters[];
		this.findTestedClass.parameterKeys.do({arg attrKey;
			var attrParams, attrVal;
			if(params.notNil and: {params.includesKey(attrKey)}, {
				attrParams = params.at(attrKey);
			});
			attrVal = this.makeRandomParameter(attrKey, attrParams);
			if(attrVal.notNil, {
				result.put(attrKey, attrVal);
			});
		});
		^result;
	}

	*makeRandomParametersForObject{arg object;
		var testClass, class;
		var result = VTMParameters[];
		class = object.class;
		testClass = this.findTestClass(class);
		//omit name
		object.description.keysValuesDo({arg attrKey, attrVal;
			//"Object attr '%' - %".format(attrKey, attrVal).debug;
		});
	}

	*makeRandomDeclaration{arg params;
		^this.makeRandomParameters(params);
	}

	*makeRandomParameter{arg key, params;
		^nil;
	}

	*makeRandomManagerObject{
		var result;
		var managerClass;
		managerClass = this.findTestedClass.managerClass;
		result = managerClass.new();
		^result;
	}

	test_noNameShouldError{
		var obj;
		//Should throw error when no name is given.
		this.class.classesForTesting.do({arg class;
			try{
				obj = class.new(nil);
				this.failed(thisMethod,
					"[%] - Did not throw error when no name was given"
					.format(class)
				);
			} {
				this.passed(thisMethod,
					"[%] - Threw error when no name was given"
					.format(class);
				);
			};
		});
	}

	test_init{
		var obj, testParameters, managerObj;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;
			var managerClass = class.managerClass;
			//managerClass shouldn not be nil
			this.assert(managerClass.notNil,
				"[%] - found manager class for test class".format(class)
			);
			managerObj = managerClass.new;
			this.assert(managerObj.notNil,
				"[%] - made manager obj for test class".format(class)
			);

			testParameters = testClass.makeRandomParameters;
			//"Making with these parameters: %".format(testParameters).debug;
			
			//Testing without manager
			testName = VTMUnitTest.makeRandomSymbol;
			obj = class.new(
				testName,
				testParameters
			);

			// //check if name initialized
			this.assertEquals(
				obj.name,
				testName,
				"[%] - init 'name' correctly".format(class)
			);

			obj.free;

			//Trying to do with manager
			testParameters = testClass.makeRandomParameters;
			obj = class.new(
				testName,
				testParameters,
				managerObj
			);

			// //check if name initialized
			this.assertEquals(
				obj.name,
				testName,
				"[%] - init 'name' correctly".format(class)
			);

			//the manager object must be identical
			this.assert(
				obj.manager.notNil and: { obj.manager === managerObj},
				"[%] - init 'manager' correctly".format(class)
			);

			obj.free;

			//Should now be removed from the manager
			this.assert(
				managerObj.hasItemNamed(obj.name).not,
				"Manager reomved the freed object."
			);
			managerObj.free;
		});
	}

	//Testing parameters may have to be done on class by class basis
	//for the moment.
}

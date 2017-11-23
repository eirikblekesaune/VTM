TestVTMElement : TestVTMAbstractData {
	*classesForTesting{
		^[
			VTMDefinitionLibrary,
			VTMCommand,
			VTMRemoteNetworkNode,
			VTMApplication,
			VTMModule,
			VTMApplication,
			VTMHardwareDevice,
			VTMScore,
			VTMScene
		];
	}

	*makeRandomDeclaration{arg params;
		var result;
		result = super.makeRandomDeclaration(params);
		result.putAll(this.makeRandomAttributes(params));
		^result;
	}

	*makeRandomAttributes{arg params;
		^VTMOrderedIdentityDictionary.new;
	}

	test_DerivedPath{
		var obj, testDeclaration, managerObj;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;
			var managerClass = class.managerClass;
			var testPath;

			managerObj = managerClass.new;

			testDeclaration = testClass.makeRandomParameters;
			obj = class.new(
				testName,
				testDeclaration,
				managerObj
			);

			//should be the manager fullPath
			this.assert(obj.path.notNil,
				"[%] - path initially not nil".format(class)
			);
			this.assert(obj.hasDerivedPath,
				"[%] - has derived path".format(class)
			);
			this.assertEquals(
				obj.path, managerObj.fullPath,
				"[%] - path is the same as manager full path".format(class)
			);

			this.assertEquals(
				obj.fullPath,
				"%%%".format(
					managerObj.path ++ managerObj.leadingSeparator ++ managerObj.name ++
					obj.leadingSeparator ++ obj.name
				).asSymbol,
				"[%] - fullPath is correct".format(class);
			);
			obj.free;
			managerObj.free;
		});
	}

	//Test OSC communication with components
	test_ParameterOSC{}

	test_AttributeOSC{
		var obj, testDeclaration;
		this.class.classesForTesting.do({arg class;
			var testClass = VTMUnitTest.findTestClass(class);
			var testName = VTMUnitTest.makeRandomSymbol;

			testDeclaration = testClass.makeRandomDeclaration;
			obj = class.new(
				testName,
				testDeclaration
			);
			obj.enableOSC;

			class.declarationKeys.do({arg declarationKey;
				var oldVal, attrPath;
				var testVal, oscValReceived = Condition.new, controller;
				oldVal = obj.get(declarationKey);
				attrPath = (obj.fullPath ++ '/' ++ declarationKey).asSymbol;

				testVal = testClass.makeRandomParameter(declarationKey);
				oscValReceived.test = false;
				controller = SimpleController(obj).put(\declaration, {arg ...args;
					var who, what, whichAttr;
					who = args[0];
					what = args[1];
					whichAttr = args[2];
					if(whichAttr == declarationKey, {
						oscValReceived.test = true;
						oscValReceived.unhang;
					});
				});

				VTM.sendLocalMsg(attrPath, testVal);

				oscValReceived.hang(1.0);
				this.assert(oscValReceived.test,
				"[%] - Did not receive declaration set OSC message".format(class));

				controller.removeAt(\declaration);
				controller.remove;
				controller = nil;
				this.assertEquals(
					obj.perform(declarationKey.asGetter), testVal,
					"[%] - Set declaration '%' value through OSC".format(class, declarationKey)
				);
			});
			obj.disableOSC;
			obj.free;
		});
		//changing path manually should update the OSC interface paths.
	}

	test_CommandOSC{}

	test_ReturnOSC{}

	test_SignalOSC{}


}

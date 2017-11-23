TestVTMContext : TestVTMElement {

	*makeRandomContext{arg params;
		var context, name = this.makeRandomString;
		var parameterDeclaration;
		var definition, declaration;
		var numParameters = rrand(3,8);
		var parameterValues = Array.newClear(numParameters);
		var parent;
		params !? { parent = params.at(\parent) };
		parameterDeclaration = numParameters.collect({arg i;
			TestVTMParameter.makeRandomDeclaration(
				[\integer, \decimal, \string, \boolean].choose
			).put(\action, {|p|
				parameterValues[i] = p.value;
			});
		});
		definition	= Environment.make{
			~parameters = parameterDeclaration;
		};
		declaration = (
			path: "/%".format(this.makeRandomString).asSymbol
		);
		context = VTMContext(name, definition, declaration, parent);
		^context;
	}

	test_DefaultConstruction{
		var context, testName;
		//construct without definition and declaration
		testName = this.class.makeRandomString.asSymbol;
		context = VTMContext(testName);
		this.assertEquals(
			context.name, testName,
			"Context initialized name correctly"
		);

		//should init envir as Environment with \self as it itself
		this.assert(
			context.envir.class == Environment and: {
				context.envir == Environment[\self -> context]
			},
			"Context initialized envir as Environment with self reference in self key"
		);

		this.assert(context.parent.isNil,
			"Context parent is nil"
		);

		this.assert(context.children.isNil,
			"Context children is nil"
		);

		//context path is nil
		this.assertEquals(context.path, nil,
			"Context init path to nil"
		);

		//context fullPath is just name prefixed with forward slash
		this.assertEquals(context.fullPath, "/%".format(testName).asSymbol,
			"Context init fullPath to /<name>"
		);

		this.assertEquals(context.state, \didInitialize,
			"Context set state to initialized when constructed"
		);

		this.assertEquals(context.addr, NetAddr.localAddr,
			"Context initialized addr to local address"
		);

		//Constructor extracts definition from declaration if defined
	}

	test_NewAndInitWithDeclaration{
		var context, testName;
		var declaration;
		var definition;
		var paramsAttr, cuesAttr, mappingsAttr;
		var scoresAttr, commandsAttr;
		//Construct with definition and declaration
		testName = this.class.makeRandomString.asSymbol;
		definition = Environment[];
		paramsAttr = TestVTMParameterManager.makeTestDeclaration;
		commandsAttr = TestVTMCommandManager.makeRandomDeclaration(paramsAttr);
		mappingsAttr = TestVTMMappingManager.makeRandomDeclaration(paramsAttr, commandsAttr);

		cuesAttr = TestVTMCueManager.makeRandomDeclaration(paramsAttr, commandsAttr);
		scoresAttr = TestVTMScoreManager.makeRandomDeclaration(
			paramsAttr, commandsAttr, mappingsAttr, cuesAttr
		);

		declaration = (
			path: "/%".format(this.class.makeRandomString).asSymbol,
			parameters: paramsAttr,
			cues: cuesAttr,
			mappings: mappingsAttr,
			scores: scoresAttr,
			commands: commandsAttr
		);
		context = VTMContext(testName, definition, declaration);
		context.free;
	}

	test_DefinitionInitAndPrepareRunFreeAndStateChange{
		var context, testCondition = Condition.new;
		var definition, declaration, name;
		var runtimeSteps = [\prepare, \run, \free];
		var contextStates = [
			\willPrepare, \didPrepare,
			\willRun, \didRun,
			\willFree, \didFree
		];
		var resultDict = IdentityDictionary[
			\wasExecuted -> List.new,
			\args -> IdentityDictionary.new
		];
		var result = IdentityDictionary[
			\definitionFunction -> resultDict.deepCopy,
			\argumentCallback -> resultDict.deepCopy,
			\stateObserver -> resultDict.deepCopy
		];
		var controller;

		//Generate definition functions from runtime step symbols.
		definition = Environment.new;
		runtimeSteps.do({arg runtimeStep;
			definition.put(runtimeStep, {arg ...args;
				result[\definitionFunction][\wasExecuted].add(runtimeStep);
				result[\definitionFunction][\args].put(runtimeStep, args);
			});
		});
		name = this.class.makeRandomString.asSymbol;
		context = VTMContext(name, definition);
		//add dependant for observing state changes
		controller = SimpleController.new(context);
		controller.put(\state, {arg ...args; //should be: theChanged, whatChanged, newState;
			var newState = args[2];
			result[\stateObserver][\wasExecuted].add(newState);
			result[\stateObserver][\args].put(newState, args);
		});

		this.assert(
			context.envir !== definition,
			"Context definition argument is not identical to context envir"
		);

		this.assert(context.envir == definition.put(\self, context),
			"Context envir is equal to definition argument plus self ref."
		);

		//Perform the runtime steps. Do tests after each step.
		runtimeSteps.do({arg runtimeStep;
			context.perform(runtimeStep, testCondition, {arg ...args;
				result[\argumentCallback][\wasExecuted].add(runtimeStep);
				result[\argumentCallback][\args].put(runtimeStep, args);
			});

			//should execute definition function
			this.assert(
				result[\definitionFunction][\wasExecuted].includes(runtimeStep),
				"Context executed definition function for '%'".format(runtimeStep)
			);
			this.assert(
				result[\definitionFunction][\args][runtimeStep][0] === context and: {
					result[\definitionFunction][\args][runtimeStep][1] === testCondition
				},
				"Context propagated correct args to definition function for '%'".format(
					runtimeStep
				)
			);

			//should execute argument callback function
			this.assert(
				result[\argumentCallback][\wasExecuted].includes(runtimeStep),
				"Context argument callback was executed for '%'".format(
					runtimeStep
				)
			);
			this.assert(
				result[\argumentCallback][\args][runtimeStep].size == 1 and: {
					result[\argumentCallback][\args][runtimeStep][0] === context
				},
				"Context argument callback propagated correct arguments for '%'".format(
					runtimeStep
				)
			);

		});
		//should announce state change to observers in the correct order
		this.assertEquals(
			result[\stateObserver][\wasExecuted].asArray,
			contextStates,
			"Context notified dependant on state changes in the correct order."
		);

		contextStates.do({arg contextState;
			this.assert(
				result[\stateObserver][\args][contextState][0] === context and: {
					result[\stateObserver][\args][contextState][1] == \state
				} and: {
					result[\stateObserver][\args][contextState][2] == contextState
				},
				"Context sent correct arguments for state change notification '%'".format(
					contextState
				)
			);
		});


	}

	test_initParametersAndPresets{
		var context, name = this.class.makeRandomString;
		var parameterDeclaration;
		var definition, declaration;
		var numParameters = rrand(3,8);
		var parameterValues = Array.newClear(numParameters);
		parameterDeclaration = numParameters.collect({arg i;
			TestVTMParameter.makeRandomDeclaration(
				[\integer, \decimal, \string, \boolean].choose
			).put(\action, {|p|
				parameterValues[i] = p.value;
			});
		});
		definition	= Environment.make{
			~parameters = parameterDeclaration;
		};
		declaration = (
			path: "/%".format(this.class.makeRandomString).asSymbol
		);
		context = VTMContext(name, definition, declaration);
		context.prepare;

		//Should return parameter names
		this.assertEquals(
			context.parameters,
			parameterDeclaration.collect({arg it; it[\name].asSymbol}),
			"Context initialized parameter names in the right order"
		);


		//check that the param path was built with the context path
		parameterDeclaration.do({arg item;
			var pathShouldBe;
			pathShouldBe = "%/%".format(context.fullPath, item[\name]).asSymbol;
			this.assertEquals(
				pathShouldBe,
				context.getParameter(item[\name].asSymbol).fullPath,
				"Context set Parameter path relative to its own path."
			);
		});

		//should get Parameter values through object API

		//should set Parameter values through object API

		//should free all parameters upon context free.
		context.free;
	}


	test_OSCCommunication{
		var context;
		var subContexts;
		context = this.class.makeRandomContext;
		context.prepare;

		subContexts = 4.collect({arg i;
			this.class.makeRandomContext((parent: context));
		});

		//startingOSC
		context.enableOSC;
		//should activate OSC
		this.assert(context.oscEnabled,
			"Context OSC communication activated."
		);

		//should initialize OSC commands

		{//test the OSC API getters
			[
				'children?', 'parameters?', 'state?'
			].do({arg cmdKey;
				var tempResponder, response, cond;
				var responded = false;
				var respPath = "%:%_testreply".format(context.fullPath, cmdKey).asSymbol;
				cond = Condition.new;
				tempResponder = OSCFunc({arg msg, time, addr, port;
					response = msg[1..].flat;
					responded = true;
					cond.unhang;
				}, respPath);
				context.addr.sendMsg(
					"%:%".format(context.fullPath, cmdKey).asSymbol,
					NetAddr.localAddr.hostname,
					NetAddr.localAddr.port,
					respPath
				);
				cond.hang(0.2);
				this.assert(responded,
					"Context OSC API command '%' responded".format(cmdKey)
				);
				this.assertEquals(
					response,
					context.perform(cmdKey.asString.drop(-1).asSymbol).asArray,
					"Context getter OSC responders responded with correct value for '%'.".format(cmdKey)
				);
				tempResponder.free;
			});
		}.value;

		{//test the OSC API declaration responder
			var tempResponder, response, cond;
			var responded = false;
			var respPath = "%:declaration?_testreply".format(context.fullPath).asSymbol;
			var declaration;

			cond = Condition.new;
			tempResponder = OSCFunc({arg msg, time, addr, port;
				topEnvironment.put(\json, msg[1]);
				response = VTMJSON.parse(msg[1].asString);
				responded = true;
				cond.unhang;
			}, respPath);
			context.addr.sendMsg(
				"%:declaration?".format(context.fullPath).asSymbol,
				NetAddr.localAddr.hostname,
				NetAddr.localAddr.port,
				respPath
			);
			cond.hang(0.2);
			this.assert(responded,
				"Context OSC API command 'declaration?' responded"
			);

			declaration = context.declaration;

			this.assertEquals(
				response.keys.asArray.sort, declaration.keys.asArray.sort,
				"Context OSC API command 'declaration?' returned equal dictionary keys"
			);

			this.assertEquals(
				response, context.declaration,
				"Context OSC API got correct Context declaration"
			);
			/*			declaration.keysValuesDo({arg attrKey, attrVal;

			if(attrVal.isKindOf(Float), {
			this.assertFloatEquals(
			response[attrKey],
			attrVal,
			"Context 'declaration?' OSC getter responded with correct value for '%'[floatEquals].".format(attrKey)
			);
			}, {
			if(attrVal.isArray, {
			var valItemEqualities;
			//convert any symbol values to string, as the OSC response will be an array of string values
			attrVal = attrVal.collect({arg it;
			if(it.isKindOf(Symbol), { it.asString; }, { it; } );
			});
			//compare the elements in the array
			valItemEqualities = attrVal.collect({arg valItem, i;
			if(valItem.isKindOf(Float), {
			valItem.equalWithPrecision(response[attrKey][i]);
			}, {
			valItem == response[attrKey][i];
			});
			});
			this.assert(valItemEqualities.every({arg it; it;}),
			"Context 'declaration?' OSC getter responded with correct value for '%'[array equals].".format(attrKey)
			);
			}, {
			this.assertEquals(
			response[attrKey],
			attrVal,
			"Context 'declaration?' OSC getter responded with correct value for '%'.".format(attrKey)
			);
			});
			});
			});*/

			topEnvironment.put(\response, response);
			topEnvironment.put(\declaration, declaration);

			tempResponder.free;
		}.value;
		//test OSC responders for parameters

		//stoppingOSC

		//restarting OSC

		//free context frees OSC responders, (is tested in VTMUnitTest:tearDown)
		context.free;
	}

	test_addingChildContexts{
		var rootData;
		rootData = IdentityDictionary[\name -> this.class.makeRandomString];

		rootData.put(\obj, VTMContext(rootData[\name]));

		//should return children as nil
		this.assert(
			rootData[\obj].children.isNil,
			"Context return children from context without children as nil"
		);
		this.assert(
			rootData[\obj].isLeaf,
			"Context is considered a leaf when childless"
		);

		this.assert(
			rootData[\obj].root === rootData[\obj],
			"Context root points to itself when being root"
		);

		rootData.put(\children, {
			var childName = this.class.makeRandomString;
			IdentityDictionary[
				\name -> childName,
				\obj -> VTMContext(childName, parent: rootData[\obj]);
			];
		} ! rrand(3,7));

		rootData[\children].do({arg childData;
			childData.put(\children, {
				var grandChildName = this.class.makeRandomString;
				IdentityDictionary[
					\name -> grandChildName,
					\obj -> VTMContext(grandChildName, parent: childData[\obj])
				]
			} ! rrand(3, 7));
		});

		this.assert(
			rootData[\obj].isLeaf.not,
			"Context added children to root object"
		);
		//the root obj should be root and not leafs
		this.assert(
			rootData[\obj].isRoot and: {rootData[\obj].isLeaf.not},
			"Context returned isRoot when being constructed to be so"
		);

		//all children should be both not roots and not leafs
		this.assert(
			rootData[\children].every({arg childData;
				var child = childData[\obj];
				child.isRoot.not and: { child.isLeaf.not};
			}),
			"Context returned all children as non roots and non leafs"
		);

		//all grand children should be non root and leafs
		this.assert(
			rootData[\children].every({arg childData;
				childData[\children].every({arg grandChildData;
					var grandChild = grandChildData[\obj];
					grandChild.isRoot.not and: { grandChild.isLeaf};
				});
			})
		);

		//the children name and paths should match with the test data
		this.assert(
			rootData[\children].every({arg childData;
				var child = childData[\obj];
				"/%/%".format(
					rootData[\name],
					childData[\name]
				).asSymbol == child.fullPath and: {
					"/%".format(rootData[\name]).asSymbol == child.path
				};
			}),
			"Context children paths and fullPaths were correctly returned"
		);

		//the grand children paths and fullPaths should match with the test data
		this.assert(
			rootData[\children].every({arg childData;
				childData[\children].every({arg grandChildData;
					var grandChild = grandChildData[\obj];
					"/%/%/%".format(
						rootData[\name],
						childData[\name],
						grandChildData[\name]
					).asSymbol == grandChild.fullPath and: {
						"/%/%".format(
							rootData[\name],
							childData[\name]
						).asSymbol == grandChild.path
					};
				});
			}),
			"Context grand children paths and fullPaths were correctly returned"
		);

		//all children and grand children point to the root as root
		this.assert(
			rootData[\children].every({arg childData;
				(childData[\obj].root === rootData[\obj]) and: {
					childData[\children].every({arg grandChildData;
						grandChildData[\obj].root === rootData[\obj];
					});
				};
			}),
			"Context children and grand children all point to correct root context"
		);
	}

	//	test_EnvirExecute{
	//		var wasRun = false, itself, theArgs;
	//		var testArgs = [11,22,\hello];
	//		var testDesc = IdentityDictionary[\testObj -> 33];
	//		var testDef = IdentityDictionary[\bongo -> 8383, \brexit -> {|context ...args|"So you wanna leave?".debug; wasRun = true; theArgs = args; itself = context;}];
	//		var context = VTMContext.new('myRoot', testDef, testDesc);
	//
	//		context.execute(\brexit, *testArgs);
	//		this.assert(
	//			wasRun, "Context did run function"
	//		);
	//
	//		this.assert(
	//			itself === context, "Context passed itself to the envir function"
	//		);
	//
	//		this.assertEquals(
	//			theArgs, testArgs, "Context passed correct arguments"
	//		);
	//
	//	}
	//
	//	test_nodeManagement{
	//		var root = VTMContext.new('myRoot');
	//		var app = VTMContext.new('myApp', parent: root);
	//		var module, moduleObj;
	//
	//		this.assert(
	//			root.children.includes(app),
	//			"Context added app to its children"
	//		);
	//
	//		//should notify the parent upon free
	//		app.free;
	//		this.assert(
	//			root.children.includes(app).not,
	//			"Context removed app from its children"
	//		);
	//
	//		//If the root context is freed it should remove all node context, and this should
	//		//propagate down the context tree
	//
	//		//Make three level context tree (chain)
	//		app = VTMContext.new('myOtherApp', parent: root);
	//		module = VTMContext.new('myModule', parent: root);
	//
	//		//Should free all node contexts, i.e. 'myModule' and 'myOtherApp'
	//		root.free;
	//
	//		this.assert(
	//			root.children.includes(app).not,
	//			"Context removed first level node"
	//		);
	//
	//		this.assert(
	//			app.children.includes(module).not,
	//			"Context removed second level node"
	//		);
	//	}
	//
	//	test_MultiLevelChildManagament{
	//		var root;
	//		var children;
	//
	//		//using empty Event as bogus obj
	//		root = VTMContext.new('myRoot');
	//
	//		//Make a three level context tree
	//		3.do({arg i;
	//			var iNode = VTMContext.new("node_%".format(i).asSymbol, parent: root);
	//			children = children.add(iNode);
	//			3.do({arg j;
	//				var jNode = VTMContext.new("node_%_%".format(i, j).asSymbol, parent: iNode);
	//				children = children.add(jNode);
	//				4.do({arg k;
	//					var kNode = VTMContext.new("node_%_%_%".format(i, j, k).asSymbol, parent: jNode);
	//					children = children.add(kNode);
	//				});
	//			});
	//		});
	//
	//		//All nodes must have same root
	//		this.assert(
	//			children.collect({arg item;
	//				item.root === root;
	//			}).every({arg it; it}),
	//			"Context children all have the same root"
	//		);
	//
	//		//freeing root also frees child nodes
	//		root.free;
	//		this.assert(
	//			children.collect({arg item;
	//				//The nodes should no longer have child nodes nor parent node
	//				item.children.isEmpty and: {item.parent.isNil}
	//			}).every({arg it;it}),
	//			"Context children was freed when context root was freed"
	//		);
	//	}
}

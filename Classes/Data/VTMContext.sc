VTMContext : VTMElement {
	var definition;
	var buildFunction;
	var fullPathThunk;
	var envir;
	var <addr; //the address for this object instance.
	var <state;
	var stateChangeCallbacks;
	var cues;
	var scores;
	var condition;
	var library;

	*new{arg name, declaration, manager, definition;
		var defArg, loadedContextDefinition;
		//If no manager defined, use the local network node as manager.
		//TODO?: Will there be problems when a class is listed as manager
		//for multiple type of objects, in the case of Context/LocalNetworkNode?
		manager = manager ? VTM.local.findManagerForContextClass(this);

		//The definition argument takes presedence over the definition named in
		//the declaration.
		//This makes it easier to temporary override context defitnitions in case
		//they need to be worked on or modified on the spot.
		if(declaration.notNil and: {declaration.includesKey(\definition)}, {
			defArg = declaration[\definition];
		});

		//The definition arg can either be a Symbol or an Environment.
		//If a Symbol is used it will try to find the named definition from
		//the manager library.
		//If it is an Environment it will make an unamed ContextDefinition from
		//the Enviroment instance.
		defArg = definition ? defArg;

		//If no definition was declared use an empty Environment for this.
		if(defArg.isNil, {
			defArg = Environment.new;
		});

		//If the definition is an Environment, either by defaut or fraom arg,
		//make a new ContextDefinition from that.
		case
		{defArg.isKindOf(Environment)} {
			loadedContextDefinition = VTMContextDefinition(defArg);
		}
		{defArg.isKindOf(PathName)} {
			loadedContextDefinition = VTMContextDefinition.loadFromFile(
				defArg.fullPath);
		}
		{ //otherwise try to find the ContextDefinition from the managers definition
			//library.
			loadedContextDefinition = manager.findContextDefinition(defArg);
		};

		if(loadedContextDefinition.isNil, {
			VTMError("Failed to make ContextDefinition for '%'".format(name)).throw;
		});
		^super.new(name, declaration, manager).initContext(
			loadedContextDefinition);
	}

	//definition arg must be an instance of VTMContextDefinition
	initContext{arg definition_;
		definition = definition_;
		stateChangeCallbacks = IdentityDictionary.new;

		envir = definition.makeEnvir;
		condition = Condition.new;
//		this.prChangeState(\loadedDefinition);
//		this.prInitCues;
//		this.prInitScores;
//		this.prInitComponentsWithContextDefinition;
//		this.prChangeState(\didInitialize);
	}

	isUnmanaged{
		^manager.parent === VTM.local;
	}

	prInitCues{
		var itemDeclarations = this.class.cueDescriptions.deepCopy;
		cues = VTMCueManager(itemDeclarations, this);
	}

	prInitScores{
		var itemDeclarations = this.class.scoreDescriptions.deepCopy;
		scores = VTMScoreManager(itemDeclarations, this);
	}

	prInitComponentsWithContextDefinition{
//		this.components.select(_.notNil).do({arg component;
//			var compName = component.name;
//			if(envir.includesKey(compName), {
//				var newItem, itemDeclarations;
//				itemDeclarations = envir[compName];
//				component.addItemsFromItemDeclarations(itemDeclarations);
//			});
//		});
		this.prAddComponentsToEnvir(envir);
	}

	prAddComponentsToEnvir{arg componentDeclarations;
		this.components.select(_.notNil).do({arg component;
			var compName;
			compName = component.name;
			if(componentDeclarations.includesKey(compName), {
				var newItem, itemDeclarations;
				itemDeclarations = componentDeclarations[compName];
				//TODO: This is a temporary hack that checks the type of the argument.
				if(itemDeclarations.isKindOf(ArrayedCollection), {
					itemDeclarations = VTMOrderedIdentityDictionary.with(
						*itemDeclarations
					);
				});
				component.addItemsFromItemDeclarations(itemDeclarations);
			});
		});
	}

	// components{
	// 	^super.components ++ [cues, scores];
	// }

	//The context that calls prepare can issue a condition to use for handling
	//asynchronous events. If no condition is passed as argument the context will
	//make its own condition instance.
	//The ~prepare stage is where the module definition defines and creates its
	//parameters.
	prepare{arg condition, action;
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			this.prChangeState(\willPrepare);
			if(envir.includesKey(\prepare), {
				this.execute(\prepare, cond);
			});
			//this.components.select(_.notNil).do({arg it; it.prepare(cond)});
			//this.enableOSC;
			this.prChangeState(\didPrepare);
			action.value(this);
		};
	}

	free{arg condition, action;
		//the stuff that needs to be freed in the envir will happen
		//in a separate thread. Everything else happens synchronously.
		this.prChangeState(\willFree);
		super.free;
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			if(envir.includesKey(\free), {
				this.execute(\free, cond);
			});
			this.prChangeState(\didFree);
			action.value(this);
			definition = nil;
		};
	}

	//	//Determine if this is a root context, i.e. having no parent.
	//	isRoot{
	//		//^parent.isNil;
	//	}
	//
	//	//Determine is this a lead context, i.e. having no children.
	//	isLeaf{
	//		^children.isEmpty;
	//	}
	//
	//	children{
	//		if(children.isEmpty, {
	//			^nil;
	//		}, {
	//			^children.keys.asArray;// safer to return only the children. not the dict.
	//		});
	//	}
	//Find the root for this context.
	//	root{
	//		var result;
	//		//search for context root
	//		result = this;
	//		while({result.isRoot.not}, {
	//			result = result.parent;
	//		});
	//		^result;
	//	}

	prChangeState{ arg val;
		var newState;
		var callback;
		if(state != val, {
			state = val;
			this.changed(\state, state);
			callback = stateChangeCallbacks[state];
			if(callback.notNil, {
				envir.use{
					callback.value(this);
				};
			});
		});
	}

	on{arg stateKey, func;
		var it = stateChangeCallbacks[stateKey];
		it = it.addFunc(func);
		stateChangeCallbacks.put(stateKey, it);
	}

	//Call functions in the runtime environment with this context as first arg.
	execute{arg selector ...args;
		var result;
		envir.use{
			result = currentEnvironment[selector].value(this, *args);
		};
		^result;
	}

	executeWithPrototypes{arg selector ...args;
		var funcList, nextProto, result;
		//Make a function stack of the proto functions
		nextProto = envir;
		while({nextProto.notNil}, {
			if(nextProto.includesKey(selector), {
				funcList = funcList.add(nextProto[selector]);
			});
			nextProto = nextProto.proto;
		});
		envir.use{
			funcList.reverseDo({arg item;
				//last one to evaluate is the the one that returns result
				result = item.valueEnvir(this, *args);
			});
		};
		^result;
	}

	update{arg theChanged, whatChanged, theChanger ...args;
		"[%] Update: %".format(this.name, [theChanged, whatChanged, theChanger, args]).vtmdebug(2);
	}

	enableOSC {
		super.enableOSC();
		this.components.select(_.notNil).do(_.enableOSC());
	}

	disableOSC {
		this.components.select(_.notNil).do({arg item;
			item.disableOSC();
		});
		super.disableOSC();
	}

	//recursive == true pulls declaration from components
	//recursive == false pulls only name declaration of components
	///TODO: Implement gettin gdeclaration from components
	declaration{arg recursive = false;
		var result = super.declaration;
		// var nonEmptyComps = this.components.select({arg item; item.isEmpty.not; });
		// if(recursive, {
		// 	nonEmptyComps.do({arg comp;
		// 		var val;
		// 		val = comp.declaration(recursive: true);
		// 		result.put(comp.name, val);
		// 	});
		// 	}, {
		// 		nonEmptyComps.do({arg comp;
		// 			result.put(comp.name, comp.names);
		// 		});
		// });
		^result;
	}

	*cueDescriptions{  ^VTMOrderedIdentityDictionary[]; }
	*scoreDescriptions{ ^VTMOrderedIdentityDictionary[]; }

	*parameterDescriptions{
		^super.parameterDescriptions.putAll( VTMOrderedIdentityDictionary[
			\definition -> (type: \string, optional: true)
		]);
	}

	*commandDescriptions{
		^super.commandDescriptions.putAll( VTMOrderedIdentityDictionary[
			\prepare -> (type: \none),
			\run -> (type: \none),
			\free -> (type: \none)
		]);
	}

	*returnDescriptions{
		^super.returnDescriptions.putAll( VTMOrderedIdentityDictionary[
			\state -> (type: \string)
		]);
   	}

	description{arg includeDeclaration = false;
		var result;
		result = super.description(includeDeclaration).put(
			\definition, definition.description
		);
		^result;
	}

	//Make a function that evaluates in the envir.
	//This method opens a gaping hole into the context's
	//innards, so it should not be used by other classes
	//than VTMElementComponent.
	prContextualizeFunction{arg func;
		var result;
		envir.use{
			result = func.inEnvir;
		};
		^result;
	}

	cues {
		^cues.names;
	}

	scores {
		^scores.names;
	}
}

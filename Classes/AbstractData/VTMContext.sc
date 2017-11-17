VTMContext : VTMElement {
	var definition;
	var buildFunction;
	var fullPathThunk;
	var envir;
	var <addr; //the address for this object instance.
	var <state;
	var stateChangeCallbacks;
	var cues;
	var mappings;
	var scores;
	var condition;

	classvar <viewClassSymbol = 'VTMContextView';

	*new{arg name, declaration, manager, definition, prototypes;
		var def;
		/*
		The definition can either be specified in the declaration as a symbol, 
		or can be defined in sclang code using the definition argument for this 
		constructor. The definition environment in the declaration argument
		takes presedence over the definition named in the declaration.
		This makes it easier to temporary override context defitnitions in case
		they need to be worked on or modified on the spot.
		*/
		if(declaration.notNil and: {declaration.includesKey(\definition)}, {
			//TODO: Load definition from DefinitionLibrary here.
			//TODO: Throw error if context defintion file not found.
			//TODO: Make the ContextDefinition environment and add it to the def
			//variable.
			//def = someInstanceOfDefinitionLibrary.makeDefinition(
			//   declaration[\definition]); //returns a ContextDefinition obj.
		});
		def = definition ? def;
		^super.new(name, declaration, manager).initContext(def, prototypes);
	}

	initContext{arg definition_;
		stateChangeCallbacks = IdentityDictionary.new;
		if(definition_.notNil, {
			//TODO: Make this into a .newFrom or .makeFrom so
			//that definition could be both an Environemnt and
			//a ContextDefinition.
			definition = VTMContextDefinition.new(definition_, this);
		}, {
			//TODO: make empty ContextDefinition if not defined.
			definition = VTMContextDefinition.new(nil, this);
		});
		envir = definition.makeEnvir;
		condition = Condition.new;
		this.prChangeState(\loadedDefinition);
		this.prInitCues;
		this.prInitMappings;
		this.prInitScores;
		this.prInitComponentsWithContextDefinition;
		this.prChangeState(\didInitialize);
	}

	isUnmanaged{
		^manager.context === VTM.local;
	}

	prInitCues{
		var itemDeclarations = this.class.cueDescriptions.deepCopy;
		cues = VTMCueManager(itemDeclarations, this);
	}

	prInitMappings{
		var itemDeclarations = this.class.mappingDescriptions.deepCopy;
		mappings = VTMMappingManager(itemDeclarations, this);
	}

	prInitScores{
		var itemDeclarations = this.class.scoreDescriptions.deepCopy;
		scores = VTMScoreManager(itemDeclarations, this);
	}

	prInitComponentsWithContextDefinition{
//		this.components.do({arg component;
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

	components{
		^super.components ++ [cues, mappings, scores];
	}

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
			//this.components.do({arg it; it.prepare(cond)});
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
		// "[%] Update: %".format(this.name, [theChanged, whatChanged, theChanger, args]).debug;
	}

	enableOSC {
		super.enableOSC();
		this.components.do(_.enableOSC());
	}

	disableOSC {
		this.components.do(_.disableOSC());
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
	*mappingDescriptions{ ^VTMOrderedIdentityDictionary[]; }
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

	mappings {
		^mappings.names;
	}

	scores {
		^scores.names;
	}
}

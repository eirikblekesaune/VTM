/*
A Context is comething that manages an inner runtime environment
*/
VTMContext : VTMElement {
	var definition;
	var buildFunction;
	var fullPathThunk;
	var envir;
	var <addr; //the address for this object instance.
	var <state;
	var stateChangeCallbacks;
	var condition;
	var library;

	*new{| name, declaration, definition |
		^super.new(name, declaration).initContext(
			definition);
	}

	//definition argument can be either nil, an instance of
	//VTMContextDefinition,  or Environment.
	initContext{| definition_ |
		var def;
		if(definition_.isNil, {
			def = Environment.new;
		}, {
			def = definition_;
		});
		if(def.isKindOf(Environment), {
			def = VTMContextDefinition.newFromEnvir(def);
		});

		//in any case we copy the def
		definition = def.deepCopy;
		envir = definition.makeEnvir;

		stateChangeCallbacks = IdentityDictionary.new;
		manager = VTM.local.findManagerForContextClass(this);

		condition = Condition.new;
		this.prChangeState(\loadedDefinition);
	}

	isUnmanaged{
		^manager.parent === VTM.local;
	}

	addControl{arg newCtrl;
		controls.addItem( newCtrl );
		this.changed(\controls);
	}

	//The context that calls prepare can issue a condition to use for
    //handling asynchronous events. If no condition is passed as
    //argument the context will make its own condition instance.
	//The ~prepare stage is where the module definition defines and
    //creates its parameters.
	prepare{| condition, action |
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			this.prChangeState(\willPrepare);
			if(envir.includesKey(\prepare), {
				this.execute(\prepare, envir, definition, cond);
			});
			//this.controls.select(_.notNil).do({| it | it.prepare(cond)});
			//this.enableOSC;
			this.prChangeState(\didPrepare);
			action.value(this);
		};
	}

	free{| condition, action |
		//the stuff that needs to be freed in the envir will happen
		//in a separate thread. Everything else happens synchronously.
		this.prChangeState(\willFree);
		super.free;
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			if(envir.includesKey(\free), {
				this.execute(\free, envir, definition, cond);
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

	prChangeState{ | val |
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

	on{| stateKey, func |
		var it = stateChangeCallbacks[stateKey];
		it = it.addFunc(func);
		stateChangeCallbacks.put(stateKey, it);
	}

	//Call functions in the runtime environment with this context as first arg.
	execute{| selector ...args |
		var result;
		envir.use{
			result = currentEnvironment[selector].value(this, *args);
		};
		^result;
	}

	executeWithPrototypes{| selector ...args |
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
			funcList.reverseDo({| item |
				//last one to evaluate is the the one that returns result
				result = item.valueEnvir(this, *args);
			});
		};
		^result;
	}

	enableOSC {
		super.enableOSC();
		controls.enableOSC;
	}

	disableOSC {
		controls.disableOSC;
		super.disableOSC();
	}

	*controlDescriptions{
		^super.controlDescriptions.putAll( VTMOrderedIdentityDictionary[
			\prepare -> (type: \none, mode: \command),
			\run -> (type: \none, mode: \command),
			\free -> (type: \none, mode: \command),
			\state -> (type: \string, mode: \return)
		]);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll( VTMOrderedIdentityDictionary[
			\definition -> (type: \string, optional: true)
		]);
	}

	description{| includeDeclaration = false |
		var result;
		result = super.description(includeDeclaration).put(
			\definition, definition.description
		);
		^result;
	}

	//Make a function that evaluates in the envir.
	//This method opens a gaping hole into the context's
	//innards, so it should not be used by other classes
	//than VTMControlManager
	prContextualizeFunction{| func |
		var result;
		envir.use{
			result = func.inEnvir;
		};
		^result;
	}
}

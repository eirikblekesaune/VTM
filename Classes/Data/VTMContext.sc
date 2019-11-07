/*
A Context is comething that manages an inner runtime environment
*/
VTMContext : VTMElement {
	var <definition;//TEMP getter
	var buildFunction;
	var fullPathThunk;
	var <envir; //TEMP getter
	var <addr; //the address for this object instance.
	var <state;
	var stateChangeCallbacks;
	var library;

	*new{| name, declaration, manager, definition, onInit |
		manager = manager ?? { VTM.local.findManagerForContextClass(this) };
		^super.new(name, declaration, manager).initContext(
			definition, onInit);
	}

	//definition argument can be either nil, an instance of
	//VTMContextDefinition,  or Environment.
	initContext{| definition_, onInit_ |
		var def;
		if(definition_.isNil, {
			def = Environment.new;
		}, {
			def = definition_;
		});
		if(def.isKindOf(Environment), {

			def = VTMContextDefinition.new(def);
		});

		//in any case we copy the def
		definition = def.deepCopy;
		envir = definition.makeEnvir(this);

		//Check the parameters from the definition
		"defintion parameters: %".format(definition.parameters).vtmdebug(2, thisMethod);
		if(definition.parameters.notNil, {
			definition.parameters.keysValuesDo({| paramKey, paramProps |
				var tempVal;
				tempVal = this.class.validateParameterValue(paramProps, paramKey, declaration);
				if(tempVal.isKindOf(Error), {
					tempVal.throw;
				}, {
					parameters.put(paramKey, tempVal.deepCopy);
				});
			});
		});

		stateChangeCallbacks = IdentityDictionary.new;
		this.prChangeState(\loadedDefinition);

		//initialize the envir, this is async so register the init callback
		this.on(\didInit, onInit_);
		this.init;
	}

	isUnmanaged{
		^manager.parent === VTM.local;
	}

	declaration{
		^declaration;
	}

	//All calls to envir are async, i.e init, prepare, run, free
	init{| condition, action |
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			this.prChangeState(\willInit);
			if(envir.includesKey(\init), {
				this.execute(\init, envir, definition, cond);
			});
			//at this point it is assumed that control descripitons are
			// ready to be used for building controls
			envir[\controls].keysValuesDo({arg ctrlKey, ctrlDesc;
				var newCtrl;
				newCtrl = VTMControl.makeFromDescription(ctrlKey, ctrlDesc, controls);
				if(newCtrl.action.notNil, {
					newCtrl.action = newCtrl.action.inEnvir(envir);
				});
			});
			this.changed(\controls);

			this.prChangeState(\didInit);
			action.value(this);
		};
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

	definitionControls{
		var result = VTMOrderedIdentityDictionary.new;
		var defControlKeys = definition.controls.keys;
		defControlKeys.do({arg ctrlKey;
			result.put(ctrlKey, controls.at(ctrlKey));
		});
		^result;
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

	parameters{
		^super.parameters.putAll(definition.parameters);
	}

	description{| includeDeclaration = false |
		var result;
		result = super.description(includeDeclaration).put(
			\definition, definition.description
		);
		^result;
	}

	findDefinition{arg defName;
		^VTM.local.library.findDefinition(defName);
	}

}

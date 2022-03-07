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
	var <cues;
	var stateChangeCallbacks;
	var library;

	*new{| name, declaration, manager, definition, onInit |
		manager = manager ?? { VTM.local.findManagerForContextClass(this) };
		^super.new(name, declaration, manager).initContext(
			definition, onInit);
	}

	*fromFile{|name, filepath, declaration, manager, onInit|
		var def;
		try{
			"Trying with this file: %".format(filepath).postln;
			def = VTMContextDefinition.newFromFile(
				filepath
			);
			^this.new(name, declaration, manager, def, onInit);
		} {|err|
			err.throw;
		}
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
		"Context definition parameters: %".format(definition.parameters).vtmdebug(2, thisMethod);
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
		cues = VTMOrderedIdentityDictionary.new;
		if(definition.cues.notEmpty, {
			definition.cues.keysValuesDo({|cueKey, cueDesc|
				var cue = VTMCue(cueKey, cueDesc);
				cues.put(cueKey, cue);
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
			try{
				var cond = condition ?? {Condition.new};
				this.prChangeState(\willInit);
				if(envir.includesKey(\init), {
					this.execute(\init, envir, definition, cond);
				});
				//At this point it is assumed that control descripitons are
				// ready to be used for building controls. Note that this happens after the envir init.
				if(envir[\controls].notNil, {
					var c = VTMOrderedIdentityDictionary.newFromNestedAssociationsArray(envir[\controls]);
					c.keysValuesDo({arg ctrlKey, ctrlDesc;
						var newCtrl;
						newCtrl = VTMControl.makeFromDescription(ctrlKey, ctrlDesc, controls);
						if(newCtrl.action.notNil, {
							newCtrl.action = newCtrl.action.inEnvir(envir);
						});
					});
				});
				this.changed(\controls);

				this.prChangeState(\didInit);
				action.value(this);
			} {|err|
				err.postProtectedBacktrace;
				err.errorString.postln;
			}
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
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			if(envir.includesKey(\free), {
				this.execute(\free, envir, definition, cond);
			});
			this.prChangeState(\didFree);
			action.value(this);
			definition = nil;
			super.free;
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
	//!!! Be very careful when calling functions in the context envir through
	// this method. If they throw an error, there is a chance that the currentEnvironment
	// will be changed, as the internal protect in the envir.use call doesn't always catch
	// the internal errors.
	// The problem seems to appear when the internal enviro function fork a process that
	// throws an error.
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

	runCue{|key|
		if(cues.includesKey(key), {
			cues[key].go;
		}, {
			"Cue '%' not found".format(key).vtmwarn(0, thisMethod);
		});
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
			\definition -> (type: \string, optional: true),
			\definitionPath -> (type: \string, optional: true)
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

	parentApplication{
		if(this.isUnmanaged.not, {
			var p = this.parent;
			while({p != VTM.local}, {
				if(p.isKindOf(VTMApplication), {
					^p;
				}, {
					p = p.parent;
				});
			});
		});
		^nil;
	}

	findDefinition{arg defName;
		var app = this.parentApplication;
		var result;
		if(app.notNil, {
			result = app.findDefinition(defName);
		}, {
			result = VTM.local.library.findDefinition(defName);
		});
		^result;
	}

	// find{|vtmPath|
	// 	var result;
	// 	if(vtmPath.isLocal, {
	// 		var pathSym = vtmPath.first.asSymbol;
	// 		if(controls.names.includes(pathSym), {
	// 			result = controls[pathSym];
	// 		});
	// 	});
	// 	^result;
	// }

	makeView{| parent, bounds, viewDef, settings |
		if(envir.includesKey(\makeView), {
			^this.execute(\makeView, parent, bounds, viewDef, settings);
		}, {
			^super.makeView(parent, bounds, viewDef, settings);
		});
	}

	contextTypeSymbol{
		var result = this.class.asString.drop(3);
		var firstLetter = result.first;
		result.put(0, firstLetter.toLower);
		^result;
	}
}

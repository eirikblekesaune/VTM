VTMContextParameterManager : VTMAbstractDataManager {
	var <parameters;
	var <order;
	var presetList;

	*dataClass{ ^VTMContextParameter; }
	name{ ^\parameters; }

	loadParameterAttributes{arg parameterAttributes;
		if(parameterAttributes.notNil, {
			parameterAttributes.do({arg paramAttributes;
				var newParam;
				paramAttributes.put(\path, context.fullPath);
				newParam = VTMParameter.makeFromAttributes(paramAttributes);
				newParam.envir = context.envir;
				if(newParam.notNil, {
					this.addParameter(newParam);
				}, {
					"Failed to build parameter attributes: '%' for context: '%'".format(
						paramAttributes, context.path;
					).warn;
				});
			});
		});
	}

	//TODO: What if param has no path here?
	addParameter{arg param, index;
		if(parameters.includesKey(param.name).not, {
			parameters.put(param.name, param);
			if(index.isNil, {
				order = order.add(param.name);
			}, {
				order.insert(index, param.name);
			});
			context.changed( \addedParameter, param.name );
		}, {
			"Parameter '%' already exists for context '%'".format(param.name, context.path).warn;
		});
	}

	removeParameter{arg paramName;
		var paramToRemove;
		paramToRemove = parameters.removeAt(paramName);
		if(paramToRemove.notNil, {
			parameters.put( paramToRemove.name, paramToRemove );
			order = order.remove(paramToRemove.name);
			context.changed( \removedParameter, paramToRemove.name );
		}, {
			"Couldn't find parameter: '%' in context '%'".format(paramName).warn;
		});

	}

	free{
		parameters.do(_.free);
		order = nil;
		parameters = nil;
		context = nil;
	}

	enableOSC{
		parameters.do({arg item;
			item.enableOSC;
		});
	}

	disableOSC{
		parameters.do({arg item;
			item.disableOSC;
		});
	}

	//The presets getter only returns the names of the presets.
	//Usage:
	// myContext.presets;// -> returns preset names
	// myContext.set(myContext.getPreset(\myPresetName));
	presets{
		var result = [];
		if(context.envir.includesKey(\presets), {
			result = result.addAll(context.envir[\presets].collect({arg assoc; assoc.key;}));
		});
		if(presetList.notNil, {
			result = result.addAll(presetList.names);
		});
		^result;
	}

	getPreset{arg presetName;
		^presetList.at(presetName);
	}

	addPreset{arg data, presetName, slot;
		//if this is the first preset to be added we have to create
		//a presetList first
		"Adding preset slot %[%]:\n\t%".format(presetName, slot, data).debug;
		if(presetList.isNil, {
			presetList = VTMNamedList.new;
		});
		presetList.addItem(data, presetName, slot);
		this.changed(\presetAdded, presetName);
	}

	removePreset{arg presetName;
		var removedPreset;
		if(presetList.notNil, {
			removedPreset = presetList.removeItem(presetName);
			if(removedPreset.notNil, {
				"Context: % - removed preset '%'".format(this.fullPath, presetName).debug;
			});
		}, {
			"Context: % - no presets to remove".format(this.fullPath).warn
		});
	}

	presetAttributes{
		^presetList.asKeyValuePairs;
	}

	names{
		^this.parameters.collect(_.name);
	}
	//END preset methods
}

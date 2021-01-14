VTMContextDefinition {
	var <name;
	var definition;
	var pathName;

	*new{| envir, name |
		^super.new.initContextDefinition(envir, name);
	}

	*newFromFile{| filepath |
		var pathName = PathName(filepath);
		try{
			var loadedEnvir;
			"filepath to load: %".format(pathName.fullPath).vtmdebug(4, thisMethod);
			if(File.exists(pathName.fullPath).not, {
				VTMError(
					"Definifion file not found at path: '%'".format(pathName.fullPath)
				).throw;
			});
			loadedEnvir = File.loadEnvirFromFile(pathName.fullPath);
			if(loadedEnvir.isNil, {
				VTMError(
					"Could not load environment from definition file: '%'".format(
						pathName.fullPath
					)
				).throw;
			}, {
				var definitionName;
				//FIXME: Do better testing of string here
				definitionName = pathName.fileName.findRegexp("(.+)_definition.scd$");
				if(definitionName.notNil and: {definitionName.notEmpty}, {
					definitionName = definitionName[1];
				});
				if(definitionName.notNil and: {definitionName.notEmpty}, {
					definitionName = definitionName[1].asSymbol;
				});
				^this.new(loadedEnvir, definitionName).filepath_(pathName.fullPath);
			});
		} {|err|
			"Could not compile definition file: '%'".format(pathName).warn;
			err.errorString.vtmdebug(1, thisMethod);
			err.throw;
		};
	}

	initContextDefinition{| env_, name_ |
		var env = VTMOrderedIdentityDictionary.newFrom(env_);
		var ctrls, params;
		name = name_;

		definition = Environment[
			\name -> name
		];

		[\controls, \parameters].do{arg item;
			var cc;
			if(env.includesKey(item), {
				cc = env.removeAt(item);
				cc = VTMOrderedIdentityDictionary.newFromAssociationArray(cc, true);
			}, {
				cc = VTMOrderedIdentityDictionary.new;
			});
			definition.put(item, cc);
		};

		//now put in the rest
		definition.putAll(env);

		"init: %".format(name).vtmdebug(4, thisMethod);
	}

	makeEnvir{| context |
		var result;
		result = definition.deepCopy.put(\self, context);
		^result;
	}

	filepath{
		var result;
		if(pathName.notNil, {
			result = pathName.fullPath;
		});
		^result;
	}

	filepath_{| str |
		pathName = PathName(str);
	}

	parameters{
		^definition[\parameters];
	}

	controls{
		^definition[\controls];
	}

	makeView{|parent, bounds, viewDef, settings|
		^definition[\makeView].value(parent, bounds, viewDef, settings);
	}

	includesKey{|k|
		^definition.includesKey(k);
	}

	description{
		var result = VTMOrderedIdentityDictionary[
			\name -> this.name,
			\parameters -> this.parameters,
			\controls -> this.controls
		];
		^result;
	}
}

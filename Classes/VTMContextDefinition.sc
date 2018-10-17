VTMContextDefinition {
	var <name;
	var definition;
	var pathName;

	*new{| envir, name |
		^super.new.initContextDefinition(envir, name);
	}

	*newFromEnvir{| envir, name |
		^this.new(envir, name);
	}

	*newFromFile{| filepath |
		var pathName = PathName(filepath);
		var definitionName = pathName.fileName.findRegexp("(.+)_definition.scd$")[1][1].asSymbol;
		var loadedEnvir;
		try{
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
				^this.new(loadedEnvir, definitionName).filepath_(filepath);
			});
		} {|err|
			"Could not compile definition file: '%'".format(pathName).warn;
			err.throw;
		};
	}

	initContextDefinition{| env_, name_ |
		name = name_;
		definition = env_.deepCopy;
		definition = Environment[
			\name -> name,
			\parameters -> VTMOrderedIdentityDictionary.new,
			\attributes -> VTMOrderedIdentityDictionary.new,
			\commands -> VTMOrderedIdentityDictionary.new,
			\presets -> VTMOrderedIdentityDictionary.new,
			\returns -> VTMOrderedIdentityDictionary.new,
			\signals -> VTMOrderedIdentityDictionary.new,
			\cues -> VTMOrderedIdentityDictionary.new,
			\mappings -> VTMOrderedIdentityDictionary.new,
			\scores -> VTMOrderedIdentityDictionary.new
		];
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
	attributes{
		^definition[\attributes];
	}
	commands{
		^definition[\command];
	}
	returns{
		^definition[\returns];
	}
	presets{
		^definition[\presets];
	}
	cues{
		^definition[\cues];
	}
	mappings{
		^definition[\mappings];
	}
	scores{
		^definition[\scores];
	}

	description{
		var result = VTMOrderedIdentityDictionary[
			\name -> this.name,
			\parameters -> this.parameters,
			\attributes -> this.attributes,
			\commands -> this.commands,
			\returns -> this.returns,
			\presets -> this.presets,
			\cues -> this.cues,
			\mappings -> this.mappings,
			\scores -> this.scores
		];
		^result;
	}
}

VTMContextDefinition {
	var <name;
	var definition;
	var pathName;

	*new{arg env, name, filepath;
		^super.new.initContextDefinition(env, name, filepath);
	}

	*loadFromFile{arg filepath;
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
				^this.new(loadedEnvir, definitionName, filepath);
			});
		} {|err|
			"Could not compile definition file: '%'".format(pathName).warn;
			err.throw;
		};
	}

	initContextDefinition{arg env_, name_, filepath_;
		name = name_;
		definition = env_.deepCopy;
		if(filepath_.notNil, {
			pathName = PathName(filepath_);
		});
		//		definition = Environment[
		//			\name -> name,
		//			\parameters -> VTMOrderedIdentityDictionary.new,
		//			\attributes -> VTMOrderedIdentityDictionary.new,
		//			\commands -> VTMOrderedIdentityDictionary.new,
		//			\presets -> VTMOrderedIdentityDictionary.new,
		//			\returns -> VTMOrderedIdentityDictionary.new,
		//			\signals -> VTMOrderedIdentityDictionary.new,
		//			\cues -> VTMOrderedIdentityDictionary.new,
		//			\mappings -> VTMOrderedIdentityDictionary.new,
		//			\scores -> VTMOrderedIdentityDictionary.new
		//];
		"init: %".format(name).vtmdebug(4, thisMethod);
		//		if(env_.notNil, {
		//			var envToLoad = env_.deepCopy;
		//			//Turn any arrays of Associations into OrderedIdentityDictionaries
		//			//Get the names of the components our context consists of.
		//			definition.keys.do({arg compName;
		//				if(envToLoad.includesKey(compName), {
		//					var itemDeclarations;
//					//Remove the itemDeclarations for this component key
//					//so that we can add the remaing ones after changes has been made.
//					itemDeclarations = envToLoad.removeAt(compName);
//					//If it is an array of associations we change it to OrderedeIdentotitDictionary
//					if(itemDeclarations.isArray and: {itemDeclarations.every{arg it; it.isKindOf(Association); } }, {
//						itemDeclarations.do({arg itemDeclaration;
//							definition[compName].put(
//								itemDeclaration.key,
//							   	itemDeclaration.value
//							);
//						});
//					}, {
//						//otherwise we assume it is a kind of dictionary.
//						//TODO: Handle dicationaries as arguments
//					});
//				});
//			});
//			definition.putAll(envToLoad);
//		});
	}

	makeEnvir{arg context;
		var result;
		result = definition.deepCopy.put(\self, context);
		^result;
	}

	filepath{
		^pathName.fullPath;
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
}

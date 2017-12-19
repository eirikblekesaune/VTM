VTMContextDefinition {
	var <name;
	var definition;

	*new{arg env, name;
		^super.new.initContextDefinition(env, name);
	}

	initContextDefinition{arg env_, name_;
		name = name_;
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
		"ContextDefinition:% - init".format(name).debug;
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

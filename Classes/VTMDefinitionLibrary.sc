VTMDefinitionLibrary {
	var <definitions;
	var <folderPaths;
	classvar <global;//the definitions that are defined in the class files folder.
	classvar <system;//the definitions loaded from the config file (~/.vtm.conf.yaml)
	classvar <systemPaths;

	*initClass{
		var sysDefPaths;
		Class.initClassTree(VTM);
		Class.initClassTree(VTMOrderedIdentityDictionary);
		//TODO: Read and init global library
		global = this.readLibrary(["%Definitions".format(VTM.vtmPath)]);

		//TODO: Read and init system libraries
		if(VTM.systemConfiguration.includesKey('definitionPaths'), {
			sysDefPaths = VTM.systemConfiguration.at('definitionPaths');
			sysDefPaths = sysDefPaths.collect(_.standardizePath);
			systemPaths = sysDefPaths;
			system = this.readLibrary(systemPaths);
		}, {
			system = VTMOrderedIdentityDictionary.new;
		});
	}

	*new{| folderPaths |
		^super.new.initDefinitionLibrary(folderPaths);
	}

	initDefinitionLibrary{| folderPaths_ |

		folderPaths = folderPaths_;
		if(folderPaths.isString, {
			folderPaths = [folderPaths];
		});
		folderPaths = folderPaths.asArray;
		definitions = this.class.readLibrary(folderPaths);
	}

	findDefinition{| defName |
		var result;
		result = definitions[defName];

		if(result.isNil, {
			result = this.class.system[defName];
		});
		if(result.isNil, {
			//Then lastly try the global library
			result = this.class.global[defName];
		});

		^result.deepCopy;
	}

	*readLibrary{| folderPaths |
		var result = VTMOrderedIdentityDictionary.new;
		var readEntry;
		readEntry = {| entryPathName, res |
			case
			{entryPathName.isFile} {
				var defEnvir;
				if(".+_definition.scd$".matchRegexp(entryPathName.fileName), {
					var loadedEnvir;
					var definitionName = entryPathName.fileName.findRegexp(
						"(.+)_definition.scd$")[1][1].asSymbol;
					try{
						var contextDef = VTMContextDefinition.newFromFile(entryPathName.fullPath);
						"Added definition '%' from file '%'".format(definitionName, entryPathName.fullPath).vtmdebug(2, thisMethod);
						res.put(contextDef.name, contextDef);
					} {
						"Could not compile definition file: '%'".format(entryPathName).warn;
					};
				});
			}
			{entryPathName.isFolder} {
				entryPathName.entries.do({| item |
					readEntry.value(item, res);
				});
			};
		};
		folderPaths.do{| folderPath |
			if(File.exists(folderPath), {
				PathName(folderPath).entries.do({| entry |
					readEntry.value(entry, result);
				});
			}, {
				VTMError("Did not find library folder: '%'".format(folderPath)).throw;
			});
		};
		^result;
	}
}


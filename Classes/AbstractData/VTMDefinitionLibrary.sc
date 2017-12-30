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
		});
	}

	*new{arg folderPaths;
		^super.new.initDefinitionLibrary(folderPaths);
	}

	initDefinitionLibrary{arg folderPaths_;
		folderPaths = folderPaths_;
		if(folderPaths.isString, {
			folderPaths = [folderPaths];
		});
		folderPaths = folderPaths.asArray;
		definitions = this.class.readLibrary(folderPaths);
	}

	findDefinition{arg defName;
		var result;
		var lib;
		//First try the system libraries.
		lib = system.detect({arg item;
			item.includesKey(defName);
		});

		if(lib.notNil, {
			result = lib[defName];
		}, {
			//Then lastly try the global library
			result = this.class.global[defName];
		});
		^result.deepCopy;
	}
	
	*readLibrary{arg folderPaths;
		var result = VTMOrderedIdentityDictionary.new;
		var readEntry;
		readEntry = {arg entryPathName, res;
			case
			{entryPathName.isFile} {
				var defEnvir;
				if(".+_definition.scd$".matchRegexp(entryPathName.fileName), {
					var loadedEnvir;
					var definitionName = entryPathName.fileName.findRegexp(
						"(.+)_definition.scd$")[1][1].asSymbol;
					try{
						loadedEnvir = File.loadEnvirFromFile(entryPathName.fullPath);
						if(loadedEnvir.isNil, {
							Error("Could not load environment from definition file: '%'".format(
								entryPathName
							)).throw;
						}, {
							res.put(definitionName, loadedEnvir);
						});
					} {
						"Could not compile definition file: '%'".format(entryPathName).warn;
					};
				});
			}
			{entryPathName.isFolder} {
				entryPathName.entries.do({arg item;
					readEntry.value(item, res);
				});
			};
		};
		folderPaths.do{arg folderPath;
			if(File.exists(folderPath), {
				PathName(folderPath).entries.do({arg entry;
					readEntry.value(entry, result);
				});
			}, {
				Error("Did not find library folder: '%'".format(folderPath).postln;).throw;
			});
		};
		^result;
	}
}


VTMDefinitionLibrary {
	var <definitions; 
	var <folderPaths;
	classvar <global;//the library that are defined in the class files folder.
	classvar <system;//the libraries loaded from the config file (~/.vtm.conf.yaml)

	*initClass{
		var sysDefPaths;
		Class.initClassTree(VTM);
		//TODO: Read and init global library
		global = this.new( "%/Definitions".format(VTM.vtmPath));

		//TODO: Read and init system libraries
		if(VTM.systemConfiguration.includesKey('definitionPaths'), {
			sysDefPaths = VTM.systemConfiguration.at('definitionPaths');
			sysDefPaths.do({arg sysDefPath;
				system = system.add(
					this.new(sysDefPath);
				);
			});
		})
	}

	*new{arg folderPath;
		^super.new.initDefinitionLibrary(folderPath);
	}

	initDefinitionLibrary{arg folderPath;
		definitions = this.readLibrary(folderPath.standardizePath);
	}

	findDefinition{arg defName;
		var result;
		if(definitions.isEmpty, {
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
		});
		^result.deepCopy;
	}
	
	readLibrary{arg folderPath;
		var result = VTMOrderedIdentityDictionary.new;
		var readEntry;
		readEntry = {arg entryPathName, res;
			case
			{entryPathName.isFile} {
				var defEnvir;
				if(".+_definition.scd$".matchRegexp(entryPathName.fileName), {
					var definitionName = entryPathName.fileName.findRegexp("(.+)_definition.scd$")[1][1].asSymbol;
					var loadedEnvir;
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
		if(File.exists(folderPath), {
			PathName(folderPath).entries.do({arg entry;
				readEntry.value(entry, result);
			});
		}, {
			Error("Did not find library folder: '%'".format(folderPath).postln;).throw;
		});

		^result;
	}
}


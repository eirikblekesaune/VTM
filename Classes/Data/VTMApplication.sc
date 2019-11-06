VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <definitionLibrary;
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMApplicationManager; }

	*new{| name, declaration, manager, definition, onInit|
		^super.new(name, declaration, manager, definition, onInit).initApplication;
	}

	initApplication{
		var defPaths;
		if(declaration.includesKey(\definitionPaths), {
			defPaths = declaration[\definitionPaths];
		});

		//always add the local path for the application definition path
		if(definition.filepath.notNil, {
			var appDefFolder = "%Definitions".format(PathName(definition.filepath).pathOnly);
			if(File.exists(appDefFolder), {
				defPaths = defPaths.add(appDefFolder);
			}, {
				"Did not find definitions folder '%' for application: %".format(appDefFolder, name).vtmwarn(1, thisMethod);
			});
		}, {
			"Did not find folder app defintion: %".format(name).vtmwarn(1, thisMethod);
		});
		definitionLibrary = VTMDefinitionLibrary.new(defPaths, this);

		hardwareDevices = VTMHardwareSetup(this);
		modules = VTMModuleHost(this);
		scenes = VTMSceneOwner(this);
		this.on(\didInit, {
			this.makeComponents;
		});
	}

	makeComponents{
		var meth = thisMethod; //for the vtmwarning method in the catch block
		[
			[\modules, modules],
			[\hardwareDevices, hardwareDevices],
			[\scenes, scenes]
		].do({arg args;
			var compKey, comp;
			#compKey, comp = args;
			"Making components: %".format(compKey).vtmdebug(2, thisMethod);
			if(declaration.includesKey(compKey), {
				declaration[compKey].keysValuesDo({arg itemName, itemDeclaration;
					var newItem;
					try{
						newItem = comp.makeItemFromDeclaration(itemName, itemDeclaration);
						"ADDDING: % to %".format(newItem, comp).postln;
						comp.addItem(newItem);
					} {|err|
						"Failed making component named: % with declaration: %".format(
							itemName, itemDeclaration
						).vtmwarn(0, meth);
						err.errorString.postln;
					}
				});
			});
		});
	}

	components{
		^[modules, hardwareDevices, scenes].collect(_.items).flat;
	}

	findDefinition{arg defName;
		^definitionLibrary.findDefinition(defName);
	}

	free{| condition, action |
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			this.components.do({arg comp;
				comp.free(condition, action);
			});
			super.free(condition, action);
		}
	}
}

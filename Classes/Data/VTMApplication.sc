VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <definitionlibrary;
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
			var appDefFolder = "%/Definitions".format(PathName(definition.filepath).pathOnly);
			if(File.exists(appDefFolder), {
				defPaths = defPaths.add(appDefFolder);
			}, {
				"Did not find definitions folder '%' for application: %".format(appDefFolder, name).vtmwarn(1, thisMethod);
			});
		}, {
			"Did not find folder app defintion: %".format(name).vtmwarn(1, thisMethod);
		});
		definitionlibrary = VTMDefinitionLibrary.new(defPaths, this);

		hardwareDevices = VTMHardwareSetup(this);
		modules = VTMModuleHost(this);
		scenes = VTMSceneOwner(this);
		this.initComponents;
	}

	initComponents{
		if(declaration.includesKey(\modules), {
			"Adding module to module host".vtmdebug(2, thisMethod);
			modules.addItemsFromItemDeclarations(declaration[\modules]);
		});
	}



	// components{
	// 	^super.components;// ++ [hardwareDevices, modules, scenes/*, library*/];
	// }
}

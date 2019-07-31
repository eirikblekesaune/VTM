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
			var appDefFolder = "%/Definitions".format(definition.filepath);
			if(File.exists(appDefFolder), {
				defPaths = defPaths.add(appDefFolder);
			});
		});
		definitionlibrary = VTMDefinitionLibrary.new(defPaths, this);

		hardwareDevices = VTMHardwareSetup(this);
		modules = VTMModuleHost(this);
		scenes = VTMSceneOwner(this);
	}

	// components{
	// 	^super.components;// ++ [hardwareDevices, modules, scenes/*, library*/];
	// }
}

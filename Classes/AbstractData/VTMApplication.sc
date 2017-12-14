VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <library;

	*managerClass{ ^VTMApplicationManager; }

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initApplication;
	}

	initApplication{
		var compItems;
		if(declaration.includesKey(\definitionPaths), {
			compItems = declaration[\definitionPaths];
		});
//		libraries = VTMDefinitionLibraryManager.new(compItems, this);
//		compItems = nil;
//
//		hardwareDevices = VTMHardwareSetup(nil, this);
//		modules = VTMModuleHost(nil, this);
//		scenes = VTMSceneOwner(nil, this);
	}

	components{
		^super.components ++ [hardwareDevices, modules, scenes, library]; }
}

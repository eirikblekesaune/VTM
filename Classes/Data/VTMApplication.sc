VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <libraries;
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMApplicationManager; }

	*new{| name, declaration, definition |
		^super.new(name, declaration, definition).initApplication;
	}

	initApplication{
		// var compItems;
		// if(declaration.includesKey(\definitionPaths), {
		// 	compItems = declaration[\definitionPaths];
		// });
		// libraries = VTMDefinitionLibraryManager.new(compItems, this);
		// libraries = VTMDefinitionLibraryManager.new(compItems, this);
//		compItems = nil;
//
		hardwareDevices = VTMHardwareSetup(this);
		modules = VTMModuleHost(this);
		scenes = VTMSceneOwner(this);
	}

	// components{
	// 	^super.components;// ++ [hardwareDevices, modules, scenes/*, library*/];
	// }
}

VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <libraries;

	*managerClass{ ^VTMLocalNetworkNode; }

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initApplication;
	}

	initApplication{
		libraries = VTMDefinitionLibraryManager.new(nil, this);
		hardwareDevices = VTMHardwareSetup(nil, this);
		modules = VTMModuleHost(nil, this);
		scenes = VTMSceneOwner(nil, this);
	}

	components{ ^super.components ++ [hardwareDevices, modules, scenes, libraries]; }
}

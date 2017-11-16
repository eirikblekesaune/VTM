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
		// //Discover other application on the network
		// if(declaration.includesKey(\openView), {
		// 	if(declaration[\openView], {
		// 		var viewDesc, viewDef;
		// 		this.makeView( declaration[\viewDefinition], declaration[\viewSettings] );
		// 	});
		// });
	}

	components{ ^super.components ++ [hardwareDevices, modules, scenes, libraries]; }
}

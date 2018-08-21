VTMScene : VTMComposableContext {

	*managerClass{ ^VTMSceneOwner; }

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initScene;
	}

	initScene{
	}

}

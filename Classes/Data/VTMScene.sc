/*
A Scene is a context that claims ownership of subcontexts of all kinds.
This means that a Scene can own  (by aggregation) other contexts such
as modules and hardware devices.
Since it is a ComposableContext it also can manage subscenes.
*/
VTMScene : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMSceneOwner; }

	*new{| name, declaration, definition |
		^super.new(name, declaration, definition).initScene;
	}

	initScene{
	}

}

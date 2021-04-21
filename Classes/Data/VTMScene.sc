/*
A Scene is a context that claims ownership of subcontexts of all kinds.
This means that a Scene can own  (by aggregation) other contexts such
as modules and hardware devices.
Since it is a ComposableContext it also can manage subscenes.
*/
VTMScene : VTMComposableContext {
	classvar <isAbstractClass=false;
	var <controlPage;
	var <controlMappings;

	*managerClass{ ^VTMSceneOwner; }

	*new{| name, declaration, manager, definition, onInit |
		^super.new(name, declaration, manager, definition, onInit).initScene;
	}

	initScene{
		controlPage = VTMControlPage.new(this);
		this.on(\didInit, {
			controlMappings = this.definition.controlMappings.copy;
		});
	}

	activateMappings{
		controlMappings.keysValuesDo({|key, desc|
			"Activate mapping: % -> %".format(key, desc).postln;
		});
	}

	deactivateMappings{
	}

}

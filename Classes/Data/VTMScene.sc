/*
A Scene is a context that claims ownership of subcontexts of all kinds.
This means that a Scene can own  (by aggregation) other contexts such
as modules and hardware devices.
Since it is a ComposableContext it also can manage subscenes.
*/
VTMScene : VTMComposableContext {
	classvar <isAbstractClass=false;
	var <controlMappings;
	var mappedControlPage;

	*managerClass{ ^VTMSceneOwner; }

	*new{| name, declaration, manager, definition, onInit |
		^super.new(name, declaration, manager, definition, onInit).initScene;
	}

	initScene{
		this.on(\didInit, {
			controlMappings = this.definition.controlMappings.copy;
		});
	}

	mapToControlPage{|pageName|
		var controlPage = VTM.local.controlPages[pageName];
		if(controlPage.notNil, {
			if(mappedControlPage.notNil, {
				this.unmapControlPage;
			});
			mappedControlPage = controlPage;
			controlPage.mapToScene(this);
		}, {
			"Unknown control page: '%'".format(pageName).postln;
		});
	}

	unmapControlPage{
		if(mappedControlPage.notNil, {
			"\tmappedControlPage was not nil: %".format(mappedControlPage).postln;
			mappedControlPage.unmapFromScene(this);
			mappedControlPage = nil;
		});
	}
}

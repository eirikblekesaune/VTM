VTMControlPage {
	var manager;
	var <controlValues;
	var <mappedScene;
	var >viewBuilder;

	*new{| manager, pageSetup |
		^super.new.initControlPage(manager, pageSetup);
	}

	initControlPage{|manager_, pageSetup|
		manager = manager_;
		pageSetup = VTMOrderedIdentityDictionary.newFromAssociationArray(pageSetup, true);
		controlValues = VTMOrderedIdentityDictionary.new;
		pageSetup.keysValuesDo({|ctrlKey, ctrlDesc|
			controlValues.put(
				ctrlKey,
				VTMValue.makeFromProperties(ctrlDesc);
			);
		});
	}

	mapToScene{|scene|
		if(mappedScene.notNil, {
			this.unmapFromScene(mappedScene);
		});
		if(scene.isKindOf(VTMScene), {
			scene.changed(\willMapToControlPage, this);

			mappedScene = scene;
			this.prSetupSceneMappings;
			mappedScene.addDependant(this);
			this.changed(\mappedScene, scene);
		});
	}

	unmapFromScene{|scene|
		if(mappedScene.notNil, {
			var scene = mappedScene;
			mappedScene = nil;
			scene.removeDependant(this);
			this.changed(\unmappedScene, scene);
		});
	}

	prSetupSceneMappings{
		var controlMappings = mappedScene.controlMappings;
		controlMappings.keysValuesDo({|ctrlKey, mappingDesc|
			"Mapping scene control '%' with '%'".format(ctrlKey, mappingDesc).postln;
		});
	}

	isMapped{
		^mappedScene.notNil;
	}

	update{|theChanged, whatChanged ...args|
		if(this.isMapped and: {mappedScene === theChanged}, {
			if(whatChanged == \freed, {
				this.unmapFromScene(theChanged);
			});
			if(whatChanged == \willMapToControlPage 
				and: {args.first !== this}, {
					//If the mapped scene is about to be
					//mapped to another scene
					//we unmap from this control page
					this.unmapFromScene(theChanged);
				});
		});
	}

	makeView{|parent, bounds, viewSettings|
		var result;
		if(viewBuilder.notNil, {
			result = viewBuilder.value(
				parent, bounds, viewSettings,
				this
			);
		});
		^result;
	}
}

VTMControlPage {
	var manager;
	var <controlValues;
	var <mappedScene;
	var >viewBuilder;
	var controlMappings;

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
		if(mappedScene.notNil and: {mappedScene === scene}, {
			mappedScene.removeDependant(this);
			mappedScene = nil;
			controlMappings.do(_.free);
			this.changed(\unmappedScene, scene);
		});
	}

	prSetupSceneMappings{
		try{
			controlMappings = this.prParseControlMappings(
				mappedScene
			);
		} {|err|
			err.errorString.postln;
			err.dumpBackTrace;
			err.throw;
		}
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

	prParseControlMappings{|scene|
		var result;
		var controlMappings = scene.controlMappings;
		controlMappings.keysValuesDo({|ctrlKey, mappingDesc|
			var cpCv;
			var sceneCvKey, sceneCv;
			var mapping;
			cpCv = controlValues[ctrlKey];
			if(cpCv.isNil, {
				Error("Could not find controlValue cv: '%'".format(ctrlKey)).throw;
			});

			sceneCvKey = mappingDesc.atFail(\destination, {
				Error("Mapping destination not defined").throw;
			});
			sceneCv = scene.find(VTMPath(sceneCvKey));
			if(sceneCv.isNil, {
				Error("Could not find scene cv: '%'".format(sceneCvKey)).throw;
			});
			sceneCv = sceneCv.valueObj;

			mapping = VTMValueMapping((
				source: cpCv,
				destination: sceneCv,
				type: \bind
			));
			mapping.pushToSource;
			mapping.enable;
			"\tMapping scene control '%' with '%'".format(ctrlKey, mappingDesc).postln;
			result = result.add(mapping);
		});
		^result;
	}
}

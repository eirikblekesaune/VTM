VTMControlPage : VTMData {
	var <faders;
	var <knobs;
	var <buttons;
	const <numSlots = 3;
	const <numFadersPerSlot = 8;
	const <numButtonsPerChannel = 3;
	var <mappedScene;

	*managerClass{ ^VTMControlPageManager; }

	*new{| name, declaration, manager |
		manager = manager ?? {
			VTM.local.findManagerForContextClass(this)
		};
		^super.new(name, declaration, manager).initControlPage;
	}

	initControlPage{
		//init the faders and knobs
		faders = this.numChannels.collect({|i|
			var faderNum = i + 1;
			VTMValue.decimal((
				minVal: 0.0,
				maxVal: 1.0,
				defaultValue: 0.0,
				clipmode: \both,
			));
		});
		knobs = this.numChannels.collect({|i|
			var knobNum = i + 1;
			VTMValue.decimal((
				minVal: 0.0,
				maxVal: 1.0,
				defaultValue: 0.0,
				clipmode: \both,
			));
		});

		buttons = (this.numChannels * numButtonsPerChannel).collect({|i|
			var buttonNum = i + 1;
			VTMValue.boolean((doActionOn: \change, defaultValue: false));
		});
	}

	numChannels{
		^numSlots * numFadersPerSlot;
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
}

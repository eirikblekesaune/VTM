VTMControlPage {
	var scene;
	var <faders;
	var <knobs;
	var <buttons;
	const <numSlots = 3;
	const <numFadersPerSlot = 8;
	const <numButtonsPerChannel = 3;

	*new{|scene|
		^super.new.init(scene);
	}

	init{|scene_|
		scene = scene_;

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

	map{|slotNum|
	}

	unmap{}
}

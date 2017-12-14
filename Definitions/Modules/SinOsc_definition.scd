~type = 'Module';
~prototypes = ['AudioSource'];
~prepare = {arg module, condition;
	~casper = {arg val;
		"CASPER CHANGED: %".format(val).postln;
	};
	~source = {|freq, phase|
		// SinOsc.ar(freq.poll(label: \freq), phase.poll(label: \phase));
		SinOsc.ar(freq, phase);
	};

	~initSynthArgs = [\freq, \phase];
};
~free = {arg module, condition;
	fork{1.0.wait; condition.unhang};
	condition.hang;
};
~attributes = [
	(
		name: \freq,
		type: \decimal,
		action: {|p| ~output.set(\freq, p.value.postln);},
		minVal: 20,
		maxVal: 20000,
		defaultValue: 1000
	),
	(
		name: \phase,
		type: \decimal,
		action: {|p| ~output.set(\phase, p.value); },
		minVal: 0,
		maxVal: 2pi,
		defaultValue: 0.0
	)/*,
	(
		name: \sideeffect,
		type: \boolean,
		action: {|p| ~casper.value(p.value); },
		defaultValue: true
	)*/
];
~presets = [
	\coolSound -> (freq: 808, phase: 0.2, comment: "Denne er kul"),
	\evenMoreCoolSound -> (freq: 308, phase: 0.1, comment: "Denne er enda kulere!")
];

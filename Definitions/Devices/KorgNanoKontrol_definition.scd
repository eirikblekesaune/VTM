~prototypes =  [\MIDIDevice];
~prepare = {arg device, cond;
	"Preparing Korg NanoKontrol".postln;
	(1..4).collect({arg sceneNum;
		var result;
		(
			fader: (1..9),
			knob: (10..18),
			lowButton: (19..27),
			highButton: (28..36),
			'button.rewind': 37,
			'button.play': 38,
			'button.forward': 39,
			'button.loop': 40,
			'button.stop': 41,
			'button.record': 42,
		).keysValuesDo({arg key, numbers;
			if(numbers.isKindOf(Collection), {
				numbers.do({arg num, i;
					result = result.add(
						"scene.%/%.%".format(sceneNum, key, i + 1).asSymbol -> (number: num, chan: sceneNum - 1)
					);
				});
			}, {
				result = result.add( "scene.%/%".format(sceneNum, key).asSymbol -> (number: numbers, chan: sceneNum - 1) );
			});
		});
		result;
	}).flat.do({arg item;
		var data = item.value;
		~device.addControl(item.key, data[\chan], data[\number], \control);
	});
	// ~device.trace(true);
};

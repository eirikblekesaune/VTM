~prototypes =  [\MIDIDevice];
~prepare = {arg device, cond;
	var mappings;
	"Preparing Behringer XTouch Mini".postln;
	mappings = [
		[
			'layer.A/rotary.1' -> (number: 1),
			'layer.A/rotary.2' -> (number: 2),
			'layer.A/rotary.3' -> (number: 3),
			'layer.A/rotary.4' -> (number: 4),
			'layer.A/rotary.5' -> (number: 5),
			'layer.A/rotary.6' -> (number: 6),
			'layer.A/rotary.7' -> (number: 7),
			'layer.A/rotary.8' -> (number: 8),
			'layer.A/fader.main' -> (number: 9),
			'layer.B/rotary.1' -> (number: 11),
			'layer.B/rotary.2' -> (number: 12),
			'layer.B/rotary.3' -> (number: 13),
			'layer.B/rotary.4' -> (number: 14),
			'layer.B/rotary.5' -> (number: 15),
			'layer.B/rotary.6' -> (number: 16),
			'layer.B/rotary.7' -> (number: 17),
			'layer.B/rotary.8' -> (number: 18),
			'layer.B/fader.main' -> (number: 10)
		].collect({arg item;
			item.key -> (item.value ++ (msgType: \control));
		}),
		[
			'layer.A/rotaryButton.1' -> (number: 0),
			'layer.A/rotaryButton.2' -> (number: 1),
			'layer.A/rotaryButton.3' -> (number: 2),
			'layer.A/rotaryButton.4' -> (number: 3),
			'layer.A/rotaryButton.5' -> (number: 4),
			'layer.A/rotaryButton.6' -> (number: 5),
			'layer.A/rotaryButton.7' -> (number: 6),
			'layer.A/rotaryButton.8' -> (number: 7),
			'layer.A/button.1/A' -> (number: 16),
			'layer.A/button.2/A' -> (number: 17),
			'layer.A/button.3/A' -> (number: 18),
			'layer.A/button.4/A' -> (number: 19),
			'layer.A/button.5/A' -> (number: 20),
			'layer.A/button.6/A' -> (number: 21),
			'layer.A/button.7/A' -> (number: 22),
			'layer.A/button.8/A' -> (number: 23),
			'layer.A/button.1/B' -> (number: 8),
			'layer.A/button.2/B' -> (number: 9),
			'layer.A/button.3/B' -> (number: 10),
			'layer.A/button.4/B' -> (number: 11),
			'layer.A/button.5/B' -> (number: 12),
			'layer.A/button.6/B' -> (number: 13),
			'layer.A/button.7/B' -> (number: 14),
			'layer.A/button.8/B' -> (number: 15),
			'layer.B/rotaryButton.1' -> (number: 24),
			'layer.B/rotaryButton.2' -> (number: 25),
			'layer.B/rotaryButton.3' -> (number: 26),
			'layer.B/rotaryButton.4' -> (number: 27),
			'layer.B/rotaryButton.5' -> (number: 28),
			'layer.B/rotaryButton.6' -> (number: 29),
			'layer.B/rotaryButton.7' -> (number: 30),
			'layer.B/rotaryButton.8' -> (number: 31),
			'layer.B/button.1/A' -> (number: 40),
			'layer.B/button.2/A' -> (number: 41),
			'layer.B/button.3/A' -> (number: 42),
			'layer.B/button.4/A' -> (number: 43),
			'layer.B/button.5/A' -> (number: 44),
			'layer.B/button.6/A' -> (number: 45),
			'layer.B/button.7/A' -> (number: 46),
			'layer.B/button.8/A' -> (number: 47),
			'layer.B/button.1/B' -> (number: 32),
			'layer.B/button.2/B' -> (number: 33),
			'layer.B/button.3/B' -> (number: 34),
			'layer.B/button.4/B' -> (number: 35),
			'layer.B/button.5/B' -> (number: 36),
			'layer.B/button.6/B' -> (number: 37),
			'layer.B/button.7/B' -> (number: 38),
			'layer.B/button.8/B' -> (number: 39)
		].collect({arg item;
			[
				"%/press".format(item.key).asSymbol -> (item.value ++ (msgType: \noteOn)),
				"%/release".format(item.key).asSymbol -> (item.value ++ (msgType: \noteOff))
			]
		})
	].flat.collect({arg item; item.key -> (item.value ++ (chan: 10))});
	mappings.do({arg item;
		var data = item.value;
		~device.addControl(item.key, data[\chan], data[\number], data[\msgType]);
	});
	// ~device.trace(true);
};

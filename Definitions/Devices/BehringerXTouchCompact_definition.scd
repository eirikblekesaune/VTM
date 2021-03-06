~init = {arg self, envir, definition, cond;
	var deviceControls;
	var midiDevice;
	"Initializing XTOUCH Compact".postln;
	~midiDevice = MIDIDevice(
		~self.get('inDeviceName').value,
		~self.get('inDevicePort').value,
		~self.get('outDeviceName').value,
		~self.get('outDevicePort').value,
		~self.fullPath
	);
	midiDevice = ~midiDevice;
	~controls = VTMOrderedIdentityDictionary.new;
	[
		[
			'layer.A_fader.1' -> (number: 1),
			'layer.A_fader.2' -> (number: 2),
			'layer.A_fader.3' -> (number: 3),
			'layer.A_fader.4' -> (number: 4),
			'layer.A_fader.5' -> (number: 5),
			'layer.A_fader.6' -> (number: 6),
			'layer.A_fader.7' -> (number: 7),
			'layer.A_fader.8' -> (number: 8),
			'layer.B_fader.1' -> (number: 28),
			'layer.B_fader.2' -> (number: 29),
			'layer.B_fader.3' -> (number: 30),
			'layer.B_fader.4' -> (number: 31),
			'layer.B_fader.5' -> (number: 32),
			'layer.B_fader.6' -> (number: 33),
			'layer.B_fader.7' -> (number: 34),
			'layer.B_fader.8' -> (number: 35),
			'layer.B_fader.main' -> (number: 36),
			'layer.A_fader.main' -> (number: 9),
			'layer.A_faderTouch.1' -> (number: 101),
			'layer.A_faderTouch.2' -> (number: 102),
			'layer.A_faderTouch.3' -> (number: 103),
			'layer.A_faderTouch.4' -> (number: 104),
			'layer.A_faderTouch.5' -> (number: 105),
			'layer.A_faderTouch.6' -> (number: 106),
			'layer.A_faderTouch.7' -> (number: 107),
			'layer.A_faderTouch.8' -> (number: 108),
			'layer.A_faderTouch.main' -> (number: 109),
			'layer.A_rotary.1' -> (number: 10),
			'layer.A_rotary.2' -> (number: 11),
			'layer.A_rotary.3' -> (number: 12),
			'layer.A_rotary.4' -> (number: 13),
			'layer.A_rotary.5' -> (number: 14),
			'layer.A_rotary.6' -> (number: 15),
			'layer.A_rotary.7' -> (number: 16),
			'layer.A_rotary.8' -> (number: 17),
			'layer.A_rotary.9' -> (number: 18),
			'layer.A_rotary.10' -> (number: 19),
			'layer.A_rotary.11' -> (number: 20),
			'layer.A_rotary.12' -> (number: 21),
			'layer.A_rotary.13' -> (number: 22),
			'layer.A_rotary.14' -> (number: 23),
			'layer.A_rotary.15' -> (number: 24),
			'layer.A_rotary.16' -> (number: 25),
			'layer.B_faderTouch.1' -> (number: 111),
			'layer.B_faderTouch.2' -> (number: 112),
			'layer.B_faderTouch.3' -> (number: 113),
			'layer.B_faderTouch.4' -> (number: 114),
			'layer.B_faderTouch.5' -> (number: 115),
			'layer.B_faderTouch.6' -> (number: 116),
			'layer.B_faderTouch.7' -> (number: 117),
			'layer.B_faderTouch.8' -> (number: 118),
			'layer.B_faderTouch.main' -> (number: 119),
			'layer.B_rotary.1' -> (number: 37),
			'layer.B_rotary.2' -> (number: 38),
			'layer.B_rotary.3' -> (number: 39),
			'layer.B_rotary.4' -> (number: 40),
			'layer.B_rotary.5' -> (number: 41),
			'layer.B_rotary.6' -> (number: 42),
			'layer.B_rotary.7' -> (number: 43),
			'layer.B_rotary.8' -> (number: 44),
			'layer.B_rotary.9' -> (number: 45),
			'layer.B_rotary.10' -> (number: 46),
			'layer.B_rotary.11' -> (number: 47),
			'layer.B_rotary.12' -> (number: 48),
			'layer.B_rotary.13' -> (number: 49),
			'layer.B_rotary.14' -> (number: 50),
			'layer.B_rotary.15' -> (number: 51),
			'layer.B_rotary.16' -> (number: 52),
		].collect({arg item;
			item.key -> (item.value ++ (msgType: \control));
		}),
		[
			'layer.A_button.1_A' -> (number: 32),
			'layer.A_button.2_A' -> (number: 33),
			'layer.A_button.3_A' -> (number: 34),
			'layer.A_button.4_A' -> (number: 35),
			'layer.A_button.5_A' -> (number: 36),
			'layer.A_button.6_A' -> (number: 37),
			'layer.A_button.7_A' -> (number: 38),
			'layer.A_button.8_A' -> (number: 39),
			'layer.A_button.1_B' -> (number: 24),
			'layer.A_button.2_B' -> (number: 25),
			'layer.A_button.3_B' -> (number: 26),
			'layer.A_button.4_B' -> (number: 27),
			'layer.A_button.5_B' -> (number: 28),
			'layer.A_button.6_B' -> (number: 29),
			'layer.A_button.7_B' -> (number: 30),
			'layer.A_button.8_B' -> (number: 31),
			'layer.A_button.1_C' -> (number: 16),
			'layer.A_button.2_C' -> (number: 17),
			'layer.A_button.3_C' -> (number: 18),
			'layer.A_button.4_C' -> (number: 19),
			'layer.A_button.5_C' -> (number: 20),
			'layer.A_button.6_C' -> (number: 21),
			'layer.A_button.7_C' -> (number: 22),
			'layer.A_button.8_C' -> (number: 23),
			'layer.A_button.1_D' -> (number: 40),
			'layer.A_button.2_D' -> (number: 41),
			'layer.A_button.3_D' -> (number: 42),
			'layer.A_button.4_D' -> (number: 43),
			'layer.A_button.5_D' -> (number: 44),
			'layer.A_button.6_D' -> (number: 45),
			'layer.A_button.7_D' -> (number: 46),
			'layer.A_button.8_D' -> (number: 47),
			'layer.A_button.main' ->  (number: 48),
			'layer.A_button.rewind' ->  (number: 49),
			'layer.A_button.forward' ->  (number: 50),
			'layer.A_button.loop' -> (number: 51),
			'layer.A_button.record' ->  (number: 52),
			'layer.A_button.stop' -> (number: 53),
			'layer.A_button.play' -> (number: 54),
			'layer.A_rotaryButton.1' -> (number: 0),
			'layer.A_rotaryButton.2' -> (number: 1),
			'layer.A_rotaryButton.3' -> (number: 2),
			'layer.A_rotaryButton.4' -> (number: 3),
			'layer.A_rotaryButton.5' -> (number: 4),
			'layer.A_rotaryButton.6' -> (number: 5),
			'layer.A_rotaryButton.7' -> (number: 6),
			'layer.A_rotaryButton.8' -> (number: 7),
			'layer.A_rotaryButton.9' -> (number: 8),
			'layer.A_rotaryButton.10' -> (number: 9),
			'layer.A_rotaryButton.11' -> (number: 10),
			'layer.A_rotaryButton.12' -> (number: 11),
			'layer.A_rotaryButton.13' -> (number: 12),
			'layer.A_rotaryButton.14' -> (number: 13),
			'layer.A_rotaryButton.15' -> (number: 14),
			'layer.A_rotaryButton.16' -> (number: 15),
			'layer.B_button.1_A' -> (number: 87),
			'layer.B_button.2_A' -> (number: 88),
			'layer.B_button.3_A' -> (number: 89),
			'layer.B_button.4_A' -> (number: 90),
			'layer.B_button.5_A' -> (number: 91),
			'layer.B_button.6_A' -> (number: 92),
			'layer.B_button.7_A' -> (number: 93),
			'layer.B_button.8_A' -> (number: 94),
			'layer.B_button.1_B' -> (number: 79),
			'layer.B_button.2_B' -> (number: 80),
			'layer.B_button.3_B' -> (number: 81),
			'layer.B_button.4_B' -> (number: 82),
			'layer.B_button.5_B' -> (number: 83),
			'layer.B_button.6_B' -> (number: 84),
			'layer.B_button.7_B' -> (number: 85),
			'layer.B_button.8_B' -> (number: 86),
			'layer.B_button.1_C' -> (number: 71),
			'layer.B_button.2_C' -> (number: 72),
			'layer.B_button.3_C' -> (number: 73),
			'layer.B_button.4_C' -> (number: 74),
			'layer.B_button.5_C' -> (number: 75),
			'layer.B_button.6_C' -> (number: 76),
			'layer.B_button.7_C' -> (number: 77),
			'layer.B_button.8_C' -> (number: 78),
			'layer.B_button.1_D' -> (number: 95),
			'layer.B_button.2_D' -> (number: 96),
			'layer.B_button.3_D' -> (number: 97),
			'layer.B_button.4_D' -> (number: 98),
			'layer.B_button.5_D' -> (number: 99),
			'layer.B_button.6_D' -> (number: 100),
			'layer.B_button.7_D' -> (number: 101),
			'layer.B_button.8_D' -> (number: 102),
			'layer.B_button.main' -> (number: 103),
			'layer.B_button.rewind' -> (number: 104),
			'layer.B_button.forward' -> (number: 105),
			'layer.B_button.loop' -> (number: 106),
			'layer.B_button.record' -> (number: 107),
			'layer.B_button.stop' -> (number: 108),
			'layer.B_button.play' -> (number: 109),
			'layer.B_rotaryButton.1' -> (number: 55),
			'layer.B_rotaryButton.2' -> (number: 56),
			'layer.B_rotaryButton.3' -> (number: 57),
			'layer.B_rotaryButton.4' -> (number: 58),
			'layer.B_rotaryButton.5' -> (number: 59),
			'layer.B_rotaryButton.6' -> (number: 60),
			'layer.B_rotaryButton.7' -> (number: 61),
			'layer.B_rotaryButton.8' -> (number: 62),
			'layer.B_rotaryButton.9' -> (number: 63),
			'layer.B_rotaryButton.10' -> (number: 64),
			'layer.B_rotaryButton.11' -> (number: 65),
			'layer.B_rotaryButton.12' -> (number: 66),
			'layer.B_rotaryButton.13' -> (number: 67),
			'layer.B_rotaryButton.14' -> (number: 68),
			'layer.B_rotaryButton.15' -> (number: 69),
			'layer.B_rotaryButton.16' -> (number: 70)
		].collect({arg item;
			[
				"%_press".format(item.key).asSymbol -> (item.value ++ (msgType: \noteOn)),
				"%_release".format(item.key).asSymbol -> (item.value ++ (msgType: \noteOff))
			]
		})
	].flat.collect({arg item; item.key -> (item.value ++ (chan: 0))}).do({arg item;
		var data = item.value;
		~midiDevice.addComponent(item.key, data[\chan], data[\number], data[\msgType]);
	});
	deviceControls = ~midiDevice.makeControlDescriptions;
	deviceControls.keysValuesDo({arg ctrlKey, ctrlDesc;
		var func = switch(ctrlDesc[\mode],
			\attribute, {
				{| comp |
					self.set(ctrlKey, comp.value);
				}
			},
			\signal, {
				{| comp |
					self.emit(ctrlKey, comp.value);
				}
			}
		);
		~midiDevice.components[ctrlKey].action = func;
		~midiDevice.components[ctrlKey].action = func;
	});
	~syncListeners = [];
	~self.on(\didInit, {
		deviceControls.keysValuesDo({arg ctrlKey, ctrlDesc;
			~syncListeners = ~syncListeners.add(
				SimpleController(~self.controls[ctrlKey]).put(\value, {|...args|
					var newVal = args[2];
					if(midiDevice.components[ctrlKey].value != newVal, {
						midiDevice.components[ctrlKey].value = newVal;
						midiDevice.components[ctrlKey].refresh;
					});
				}.inEnvir);
			);
		});
	}.inEnvir);
	~controls = ~controls.putAll(deviceControls);
	~midiDevice.trace(true);
};
~free = {arg cond;
	"Freeing Behringer XTouch Compact: %".format(~self.fullPath).postln;
	~syncListeners.do({arg item;
		item.remove;
	});
	~midiDevice.free;
};
~parameters = [
	'inDeviceName' -> ( type: 'string',	optional: false ),
	'inDevicePort' -> ( type: 'string',	optional: false ),
	'outDeviceName' -> ( type: 'string', optional: false ),
	'outDevicePort' -> ( type: 'string', optional: false )
];

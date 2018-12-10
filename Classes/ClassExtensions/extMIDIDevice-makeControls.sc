+ MIDIDevice {
	makeControlDescriptions{
		var result = VTMOrderedIdentityDictionary.new;
		components.keysValuesDo({| key, component |
			var desc = component.makeControlDescription;
			result.put(key, desc);
		});
		^result;
	}
}

+ MIDIDeviceComponent {
	makeControlDescription{
		var result;
		result = (
			type: \integer,
			range: [0, 127]
		);
		switch(msgType,
			\control, {
				result.putAll((
					mode: \attribute
				));
			},
			// \noteOn, {
			// 	result.putAll((
			// 		mode: \signal
			// 	));
			// },
			// \noteOff, {
			// 	result.putAll((
			// 		mode: \signal
			// 	));
			// },
			{
				result.putAll((
					mode: \attribute
				));
			}
		);
		^result;
	}
}
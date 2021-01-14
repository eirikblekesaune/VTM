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
			minVal: 0,
			maxVal: 127
		);
		switch(msgType,
			\control, {
				result.putAll((
					mode: \attribute
				));
			},
			\noteOn, {
				result.putAll((
					mode: \signal
				));
			},
			\noteOff, {
				result.putAll((
					mode: \signal
				));
			},
			{
				result.putAll((
					mode: \attribute
				));
			}
		);
		^result;
	}
}

+ MIDIDeviceNoteComponent {
	makeControlDescription{
		var result;
		result = (
			type: \array,
			size: 2,
			fixedSize: true,
			itemType: \integer,
			mode: \signal
		);
		^result;
	}
}

+ MIDIDevicePolytouchComponent {
	makeControlDescription{
		var result;
		result = (
			type: \integer,
			minVal: 0,
			maxVal: 127,
			mode: \return
		);
		^result;
	}
}

+ MIDIDeviceIncrementComponent {
	makeControlDescription{
		var result;
		result = (
			type: \integer,
			minVal: spec.minval,
			maxVal: spec.maxval,
			mode: \attribute
		);
		^result;
	}
}

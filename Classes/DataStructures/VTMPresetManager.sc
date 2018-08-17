VTMPresetManager : VTMNamedList{
	name{ ^\presets; }

//	loadPreset{arg presetName, ramping;
//		var newPreset;
//		newPreset = items.detect({arg item; item.key == presetName;});
//		if(newPreset.notNil, {
//			newPreset = newPreset.value;
//			newPreset.removeAt(\comment);
//			model.parameters.set(*newPreset.asKeyValuePairs);
//			if(ramping.isNil, {
//				model.parameters.set(*newPreset.asKeyValuePairs);
//			}, {
//				newPreset = newPreset.asKeyValuePairs.flop.collect({arg item;
//					item.add(ramping);
//				}).flatten;
//				model.parameters.ramp(*newPreset);
//			});
//		}, {
//			"Preset '%' for '%' not found".format(presetName, model.fullPath).warn;
//		});
//	}
}

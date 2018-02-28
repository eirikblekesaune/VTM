VTMAbstractDataView : VTMView {
	var dataObj;
	var definition;

	*new{arg parent, bounds, dataObj, definition, settings;
		^super.new(parent, bounds).initAbstractDataView(
			dataObj, definition, settings);
	}

	initAbstractDataView{arg dataObj_, definition_, settings_;
		dataObj = dataObj_;
		definition = definition_;
		if(settings_.notNil, {
			settings = VTMOrderedIdentityDictionary.newFrom(settings_);
		}, {
			settings = VTMOrderedIdentityDictionary.new;
		});
	}
}
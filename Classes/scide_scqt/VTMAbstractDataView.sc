VTMAbstractDataView : VTMView {
	var dataObj;
	var definition;

	*new{arg parent, bounds, definition, settings, dataObj;
		^super.new(parent, bounds, definition, settings).initAbstractDataView(
			dataObj
		);
	}

	initAbstractDataView{arg dataObj_;
		dataObj = dataObj_;
	}
}
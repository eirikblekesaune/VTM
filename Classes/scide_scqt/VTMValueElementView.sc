VTMValueElementView : VTMAbstractDataView {
	var valueView;
	*new{arg parent, bounds, dataObj, definition, settings;
		var result;
		result = super.new(parent, bounds, dataObj, definition, settings);
		result.initValueElementView();
		^result;
	}

	initValueElementView{
		valueView = dataObj.valueObj.makeView(
			this, this.bounds,
			definition: definition,
			settings: (
				label: dataObj.name
			)
		);
		this.layout = VLayout(
			valueView
		);
	}
}
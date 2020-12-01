VTMControlView : VTMDataView {
	var valueView;

	prMakeChildViews{
		valueView = model.valueObj.makeView(
			this, this.bounds,
			definition: definition,
			settings: (
				label: model.name
			)
		);
	}

	prMakeLayout{
		^VLayout(
			valueView
		);
	}
}

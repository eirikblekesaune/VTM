VTMValueElementView : VTMAbstractDataView {
	var valueView;

	prMakeValueView{
		var result;
		result = model.valueObj.makeView(
			this, this.bounds,
			definition: definition,
			settings: (
				label: model.name
			)
		);
		^result;
	}

	prMakeLayout{
		valueView = this.prMakeValueView;
		^VLayout(
			valueView
		);
	}
}
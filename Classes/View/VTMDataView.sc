VTMDataView : VTMView {
	prInitLabel{
		label = settings[\label] ?? {model.name};
	}

	toolTip{
		^toolTip ?? {"%".format(model.fullPath);};
	}
}

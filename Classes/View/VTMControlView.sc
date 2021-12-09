VTMControlView : VTMDataView {
	var valueView;

	settings{ valueView.settings }

	definition {valueView.definition }

	prMakeChildViews{
		valueView = model.valueObj.makeView(
			this, this.bounds,
			definition: definition,
			settings: settings
		);
	}

	prMakeLayout{
		^valueView.prMakeLayout;
	}

	label{ ^valueView.label; }

	label_{| str |
		valueView.label = str;
	}

	toolTip{
		^toolTip ?? {"% [%]".format(model.fullPath, model.type);};
	}

	refreshLabel{
		valueView.refreshLabel;
	}
}

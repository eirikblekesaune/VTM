VTMElementView : VTMDataView {
	var controlsView;
	var showControls = false;
	var showControlsButton;
	var showNumControlsLabel;

	showControls_{| bool |
		showControls = bool;
		{
			controlsView.visible = showControls;
		}.defer;
	}

	rebuildControlsView{
		controlsView.children.do(_.remove);
		"These are children: %".format(model.controls).vtmdebug(3, thisMethod);
		controlsView.layout_(
			VLayout(
				*model.controls.reject(_.isNil).collect({| item |
					[
						item.makeView,
						\align,
						\topLeft
					]
				}).add(nil)
			).spacing_(2).margins_(2)
		);
		showNumControlsLabel.string_(model.numControls);
	}

	prMakeChildViews{
		labelView = this.prMakeLabelView;
		controlsView = this.prMakeControlsView;
		showControlsButton = Button()
		.states_([
			["[+]", Color.black, Color.white.alpha_(0.1)],
			["[—]", Color.black, Color.white.alpha_(0.1)]
		])
		.value_(showControls.asInt)
		.action_({| butt | this.showControls_(butt.value.booleanValue); })
		.font_(this.font)
		.background_(labelView.background)
		.fixedSize_(Size(15,15))
		.canFocus_(false);
		showNumControlsLabel = StaticText()
		.string_(model.numControls)
		.font_(this.font.italic_(true))
		.fixedSize_(Size(15,15));
		this.rebuildControlsView();
	}

	prMakeLayout{
		^VLayout(
			View().layout_(
				HLayout(
					[showControlsButton, \align: \left],
					[labelView, \align: \left],
					nil,
					[showNumControlsLabel, \align: \right]
				).spacing_(0).margins_(1)
			)
			.maxHeight_(15)
			.background_(labelView.background),
			controlsView.visible_(showControls)
		);
	}


	prMakeControlsView{
		var result;
		result = View()
		.background_(Color.yellow.alpha_(0.1));
		^result;
	}

	//pull style update
	update{| theChanged, whatChanged, whoChangedIt, toValue |
		"Dependant update: % % % %".format(
			theChanged, whatChanged, whoChangedIt, toValue
		).vtmdebug(3, thisMethod);

		//only update the view if the valueObj changed
		if(theChanged === model, {
			switch(whatChanged,
				\controls, { { this.rebuildControlsView; }.defer; },
				\freed, { { this.remove; }.defer; }
			);
			{this.refresh;}.defer;
		});
	}

}

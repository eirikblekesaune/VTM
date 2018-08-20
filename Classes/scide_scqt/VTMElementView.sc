VTMElementView : VTMAbstractDataView {
	var componentsView;
	var showComponents = false;
	var showComponentsButton;
	var showNumComponentsLabel;

	showComponents_{arg bool;
		showComponents = bool;
		{
			componentsView.visible = showComponents;
		}.defer;
	}

	rebuildComponentsView{
		componentsView.children.do(_.remove);
		"These are children: %".format(model.components).vtmdebug(3, thisMethod);
		componentsView.layout_(
			VLayout(
				*model.components.reject(_.isNil).collect({arg item;
					[
						item.makeView,
						\align,
						\topLeft
					]
				}).add(nil)
			).spacing_(2).margins_(2)
		);
		showNumComponentsLabel.string_(model.numComponents);
	}

	prMakeChildViews{
		labelView = this.prMakeLabelView;
		componentsView = this.prMakeComponentsView;
		showComponentsButton = Button()
		.states_([
			["[+]", Color.black, Color.white.alpha_(0.1)],
			["[—]", Color.black, Color.white.alpha_(0.1)]
		])
		.value_(showComponents.asInt)
		.action_({arg butt; this.showComponents_(butt.value.booleanValue); })
		.font_(this.font)
		.background_(labelView.background)
		.fixedSize_(Size(15,15))
		.canFocus_(false);
		showNumComponentsLabel = StaticText()
		.string_(model.numComponents)
		.font_(this.font.italic_(true))
		.fixedSize_(Size(15,15));
		this.rebuildComponentsView();
	}

	prMakeLayout{
		^VLayout(
			View().layout_(
				HLayout(
					[showComponentsButton, \align: \left],
					[labelView, \align: \left],
					nil,
					[showNumComponentsLabel, \align: \right]
				).spacing_(0).margins_(1)
			)
			.maxHeight_(15)
			.background_(labelView.background),
			componentsView.visible_(showComponents)
		);
	}


	prMakeComponentsView{
		var result;
		result = View()
		.background_(Color.yellow.alpha_(0.1));
		^result;
	}

	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		"Dependant update: % % % %".format(
			theChanged, whatChanged, whoChangedIt, toValue
		).vtmdebug(3, thisMethod);

		//only update the view if the valueObj changed
		if(theChanged === model, {
			switch(whatChanged,
				\components, { { this.rebuildComponentsView; }.defer; },
				\freed, { { this.remove; }.defer; }
			);
			{this.refresh;}.defer;
		});
	}

}
VTMAbstractDataManagerView : VTMView {
	var itemsView;
	var showItems = false;
	var showItemsButton;
	var showNumItemsLabel;

	rebuildItemsView{
		itemsView.children.do(_.remove);
		itemsView.layout_(
			VLayout(
				*model.items.collect({arg item;
					[
						item.makeView,
						\align,
						\topLeft
					];
				}).add(nil)
			).spacing_(2).margins_(2)
		);
		showNumItemsLabel.string_(model.numItems);
	}

	prMakeLayout{
		labelView = this.prMakeLabelView();
		itemsView = this.prMakeItemsView();
		showItemsButton = Button()
		.states_([
			["[+]", Color.black, Color.white.alpha_(0.1)],
			["[—]", Color.black, Color.white.alpha_(0.1)]
		])
		.value_(showItems.asInt)
		.action_({arg butt; this.showItems_(butt.value.booleanValue); })
		.font_(this.font)
		.background_(labelView.background)
		.fixedSize_(Size(15,15))
		.canFocus_(false);

		showNumItemsLabel = StaticText()
		.string_(model.numItems)
		.font_(this.font.italic_(true))
		.fixedSize_(Size(15,15));
		this.rebuildItemsView();
		^VLayout(
			View().layout_(
				HLayout(
					[labelView, \align: \left],
					nil,
					[showNumItemsLabel, \align: \right],
					[showItemsButton, \align: \right]
				).spacing_(0).margins_(1)
			)
			.maxHeight_(15)
			.background_(labelView.background),
			itemsView.visible_(showItems)
		);
	}

	showItems_{arg bool;
		showItems = bool;
		{
			itemsView.visible = showItems;
		}.defer;
	}

	prMakeItemsView{
		var result;
		result = View()
		.background_(Color.yellow.alpha_(0.1));
		^result;
	}

	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		// "Dependant update: % % % %".format(
		// theChanged, whatChanged, whoChangedIt, toValue).postln;

		//only update the view if the valueObj changed
		if(theChanged === model, {
			switch(whatChanged,
				\items, { { this.rebuildItemsView; }.defer; },
				\freed, { { this.remove; }.defer; }
			);
			{this.refresh;}.defer;
		});
	}

}
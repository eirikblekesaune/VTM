VTMAbstractDataManagerView : VTMView {
	var itemsView;

	rebuildItemsView{
		itemsView.children.do(_.remove);
		itemsView.layout_(
			VLayout(
				*model.items.collect({arg item;
					[
						/*StaticText().string_(item.name)
						.maxHeight_(this.class.unitHeight)
						.background_(Color.green.alpha_(0.7))
						.mouseDownAction_({item.debugString.postln;}),*/
						item.makeView,
						\align,
						\topLeft
					];
				})
			).spacing_(2).margins_(0)
		);
	}

	prMakeLayout{
		labelView = this.prMakeLabelView();
		itemsView = this.prMakeItemsView();
		this.rebuildItemsView();
		^VLayout(
			labelView,
			itemsView;
		);
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
VTMAbstractDataManagerView : VTMView {
	var managerObj;
	var labelView;
	var itemsView;

	*new{arg parent, bounds, definition, settings, managerObj;
		^super.new(
			parent, bounds, definition, settings
		).initAbstractDataManagerView(managerObj);
	}

	initAbstractDataManagerView{arg managerObj_;
		managerObj = managerObj_;
		managerObj.addDependant(this);
		labelView = StaticText(
			this,
			Rect(0, 0, this.bounds.width, this.class.unitHeight)
		)
		.maxHeight_(this.class.unitHeight)
		.string_(managerObj.name)
		.background_(Color.cyan.alpha_(0.1))
		.mouseDownAction_({
			managerObj.debugString.postln;
		});

		itemsView = View()
		.background_(Color.yellow.alpha_(0.1));
		this.refreshItemsView();

		this.layout_(
			VLayout(
				labelView,
				itemsView
			)
		);

		this.addAction({arg ...args;
			managerObj.removeDependant(this);
		}, \onClose);

		this.bounds_(this.bounds);
	}

	refreshItemsView{
		itemsView.children.do(_.remove);
		itemsView.layout_(
			VLayout(
				*managerObj.items.collect({arg item;
					[
						StaticText().string_(item.name)
						.maxHeight_(this.class.unitHeight)
						.background_(Color.green.alpha_(0.7))
						.mouseDownAction_({item.debugString.postln;}),
						\align, \topLeft
					];
				})
			).spacing_(2).margins_(0)
		);
	}


	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		// "Dependant update: % % % %".format(
		// theChanged, whatChanged, whoChangedIt, toValue).postln;

		//only update the view if the valueObj changed
		if(theChanged === managerObj, {
			switch(whatChanged,
				\items, { { this.refreshItemsView; }.defer; },
				\freed, { { this.remove; }.defer; }
			);
			{this.refresh;}.defer;
		});
	}

}
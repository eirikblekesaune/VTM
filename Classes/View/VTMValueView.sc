VTMValueView : VTMView {
	var outlineView;
	var valueView;
	var backgroundView;

	classvar <labelOffset = 5;
	classvar <viewTypeToClassMappings;


	init{| definition_, settings_, model_ |
		super.init(definition_, settings_, model_);

		this.updateValue;
		this.refreshLabel;
		this.deleteOnClose_(true);
		this.addAction({| ...args |
			this.action = nil;
		}, \onClose);
	}


	prMakeLayout{
		^VLayout(
			backgroundView,
			valueView
		);
	}

	prMakeChildViews{
		labelView = this.prMakeLabelView(this.label);
		backgroundView = this.prMakeBackgroundView;
		valueView = this.prMakeValueView;
	}

	prMakeBackgroundView{
		var bView = View.new(this, this.bounds);
		outlineView !? {outlineView.clearDrawing; outlineView.remove;};
		outlineView = UserView(bView, bView.bounds).canFocus_(false);
		outlineView.drawFunc = {|uview|
			Pen.use {
				Pen.addRoundedRect(uview.bounds.insetBy(1,1), 5, 5);
				Pen.strokeColor_(settings[\outlineColor] ? Color.black);
				Pen.width_(settings[\outlineWidth] ? 2);
				Pen.fillColor_(
					settings.atFail(\color, {Color.cyan.alpha_(0.0)})
				);
				Pen.draw(3);//draw both stroke and fill
			}
		};
		^bView;
	}

	prMakeValueView{
		^TextField(this,
			this.bounds.insetAll(0, 0, 5, 0)//inset for nice things
		)
		.font_(this.font)
		.setBoth_(true)
		.align_(\right)
		.object_(model.value)
		.background_(Color.white.alpha_(0.0))
		.action_({| v |
			model.valueAction_(v.string);
		});
	}

	label{
		if(settings.notNil and: {settings.includesKey(\label)}, {
			^settings['label'];
		});
		^"";
	}

	label_{| str |
		settings[\label] = str;
		this.refreshLabel;
	}

	refreshLabel{
		{
			labelView.string_(this.label)
			.toolTip_("% [%]".format(this.label, model.type))
		}.defer;
	}

	bounds_{| argBounds |
		this.fixedSize_(argBounds.size);
		super.bounds_(argBounds);
	}

	updateValue{
		{
			valueView.object_(model.value);
		}.defer;
	}

	//pull style update
	update{| theChanged, whatChanged, whoChangedIt, toValue |
		"Dependant update: % % % %".format(
			theChanged, whatChanged, whoChangedIt, toValue
		).vtmdebug(3, thisMethod);

		//only update the view if the model changed
		if(theChanged === model, {
			switch(whatChanged,
				\enabled, { this.enabled_(model.enabled); },
				\value, { this.updateValue; },
				{
					super.update(theChanged, whatChanged, whoChangedIt, toValue)
				}
			);
			{this.refresh;}.defer;
		});
	}
}

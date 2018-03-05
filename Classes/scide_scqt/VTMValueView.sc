VTMValueView : VTMView {
	var valueObj;//the VTMValue instance
	var <label;
	var labelView, outlineView, valueView, backgroundView;
	var settings;

	classvar <labelOffset = 5;
	classvar <viewTypeToClassMappings;

	*new{arg parent, bounds, valueObj, definition, settings;
		^super.new( parent, bounds).initValueView(
			valueObj, settings);
	}

	initValueView{arg valueObj_, settings_;
		if(valueObj_.isNil, {VTError("VTMValueView - needs valueObj").throw;});
		valueObj = valueObj_;
		valueObj.addDependant(this);

		settings = settings_ ? ();

		backgroundView = this.prMakeBackgroundView;
		valueView = this.prMakeValueView;
		this.label = settings[\label] ? "";
		this.updateValue;
		this.refreshLabel;
		this.deleteOnClose_(true);
		this.addAction({arg ...args;
			this.action = nil;
			valueObj.removeDependant(this);
		}, \onClose);
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
		// labelView !? {labelView.remove;};
		labelView = StaticText(bView,
			this.class.prCalculateSize(1).asRect.insetAll(labelOffset, 0, 0, 0)
		)
		.stringColor_(settings[\stringColor] ? this.class.stringColor)
		.font_(settings[\font] ? this.class.font.bold_(true).italic_(true))
		.acceptsMouse_(false)
		.focusColor_(Color.white.alpha_(0.0))
		.background_(Color.white.alpha_(0.0))
		.canFocus_(false);
		^bView;
	}

	prMakeValueView{
		^TextField(this,
			this.bounds.insetAll(0, 0, 5, 0)//inset for nice things
		)
		.font_(Font("Menlo", 9))
		.setBoth_(true)
		.align_(\right)
		.object_(valueObj.value)
		.background_(Color.white.alpha_(0.0))
		.action_({arg v;
			valueObj.valueAction_(v.string);
		});
	}

	label_{arg str;
		label = str;
		this.refreshLabel;
	}

	refreshLabel{
		{
			labelView.string_(label)
			.toolTip_("% [%]".format(label, valueObj.type))
		}.defer;
	}

	bounds_{arg argBounds;
		this.fixedSize_(argBounds.size);
		super.bounds_(argBounds);
	}

	updateValue{
		{
			valueView.object_(valueObj.value);
		}.defer;
	}

	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		// "Dependant update: % % % %".format(
		// theChanged, whatChanged, whoChangedIt, toValue).postln;

		//only update the view if the valueObj changed
		if(theChanged === valueObj, {
			switch(whatChanged,
				\enabled, { this.enabled_(valueObj.enabled); },
				\value, { this.updateValue; }
			);
			{this.refresh;}.defer;
		});
	}

	*font{^Font("Menlo", 10).bold_(true);}
	*stringColor{^Color.black}
	*elementColor{^Color.white.alpha_(0.0)}

}

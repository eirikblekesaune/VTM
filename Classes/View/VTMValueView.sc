VTMValueView : VTMView {
	var valueView;
	var backgroundView;
	var valueUpdateFunction;

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
		var result;
		var topMargin = this.font.pixelSize * 0.5;
		//StackLayout needs the sub views wrapped in a View to work.
		result = StackLayout(
			View().layout_(VLayout([valueView, align: \bottomRight]).margins_([0,0,2,3]).spacing_(0)),
			View().layout_(HLayout([labelView, align: \topLeft]).margins_([5,topMargin + 1,0,0]).spacing_(0)),
			View().layout_(HLayout(backgroundView).margins_(1).spacing_(0)),
		);
		result.mode_(1);
		^result;
	}

	prMakeChildViews{
		labelView = this.prMakeLabelView(this.label);
		backgroundView = this.prMakeBackgroundView;
		valueView = this.prMakeValueView(settings[\viewType] ? \textfield);
		valueUpdateFunction = this.prMakeValueViewUpdateFunction(valueView);
	}

	prMakeBackgroundView{
		^UserView()
		.canFocus_(false)
		.drawFunc = {|uview|
			Pen.use {
				var outlineWidth = settings[\outlineWidth] ? 2;
				Pen.addRoundedRect(uview.bounds.insetAll(0,0,outlineWidth,outlineWidth), 5, 5);
				Pen.strokeColor_(settings[\outlineColor] ? Color.black);
				Pen.width_(outlineWidth);
				Pen.fillColor_(
					settings.atFail(\color, {Color.cyan.alpha_(0.5)})
				);
				Pen.draw(3);//draw both stroke and fill
			}
		};
	}

	prMakeValueView{|type|
		^switch(type, 
			\textfield, {this.buildTextFieldWidget()},
			\slider, {this.buildSliderWidget()}
		);
	}

	buildSliderWidget{
		^Slider()
		.maxHeight_(this.class.unitHeight)
		.background_(Color.white.alpha_(0.0))
		.thumbSize_(3.0)
		.orientation_(\horizontal)
		.value_(0.1)
		.action_({|v|
			model.valueAction_(v.value);
		});
	}

	buildTextFieldWidget{
		^TextField()
		.font_(this.font)
		.setBoth_(true)
		.align_(\right)
		.maxHeight_(this.class.unitHeight)
		.object_(model.value)
		.background_(Color.white.alpha_(0.0))
		.action_({| v |
			var val = v.string;
			//Parse the string for the given value type
			val = model.parseStringValue(val);
			if(model.isValidValue(val), {
				model.valueAction_(val);
			}, {
				"Invalid input value type format: '%'".format(
					v.string;
				).warn;
			})
		});
	}

	prMakeValueViewUpdateFunction{|type, valView|
		switch(type,
			\textfield, {
				{ valueView.object_(model.value); }
			},
			\slider, {
				{ valueView.value_(model.value); }
			}
		);
	}

	prInitLabel{
		label = settings[\label] ? "";
	}

	refreshLabel{
		{
			labelView !? {
				labelView.string_(this.label)
				.toolTip_(this.toolTip);
			}
		}.defer;
	}

	toolTip{ ^toolTip ?? {"% [%]".format(this.label, model.type)} }

	bounds_{| argBounds |
		this.fixedSize_(argBounds.size);
		super.bounds_(argBounds);
	}

	updateValue{
		{
			valueView !? {valueUpdateFunction.value() };
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

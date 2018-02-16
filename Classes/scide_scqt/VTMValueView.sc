VTMValueView : VTMView {
	var valueObj;//the VTMValue instance
	var <label;
	var labelView, outlineView, valueView, backgroundView;
	var settings;
	var <color;
	var <units = 1;
	classvar <unitWidth = 150, <unitHeight = 25;
	classvar <labelOffset = 5;
	classvar <viewTypeToClassMappings;

	*prCalculateSize{arg units;
		^Size(unitWidth, unitHeight * units);
	}

	*new{arg parent, bounds, valueObj, settings;
		var viewBounds;
		viewBounds = bounds ?? { this.prCalculateSize(1).asRect; };
		^super.new( parent: parent, bounds: viewBounds ).initValueView(valueObj, settings);
	}

	initValueView{arg valueObj_, settings_;
		if(valueObj_.isNil, {VTError("VTMValueView - needs valueObj").throw;});
		valueObj = valueObj_;
		valueObj.addDependant(this);

		settings = settings_ ? ();

		this.addAction(
			{arg v,x,y,mod;
				var result = false;
				//if alt key is pressed when pressing down the view, the valueObj setting window
				//for this ibjet will open.
				if(mod == 524288, {
					"Opening valueObj settings window: %".format(valueObj).postln;
					result = true;
				});
				result;
			},
			\mouseDownAction
		);

		//This is needed to set the fixedSize
		this.bounds_(this.bounds);
		this.layout_(

		);
	}

	prAddAltClickInterceptor{arg argView;
		//if alt key is pressed when pressing down the view, the action will be propagated to the next view
		argView.addAction( {arg v,x,y,mod; mod != 524288 }, \mouseDownAction);
	}

	prMakeValueView{
		^StaticText().font_(this.font);
	}

	close{
		action = nil;
		valueObj = nil;
		valueObj.removeDependant(this);
	}

	label_{arg str;
		label = str;
		this.refreshLabel;
	}

	drawBackground{
		outlineView !? {outlineView.clearDrawing; outlineView.remove;};
		outlineView = UserView(this, this.bounds).canFocus_(false);
		outlineView.drawFunc = {|uview|
			Pen.use {
				Pen.addRoundedRect(uview.bounds.insetBy(1,1), 5, 5);
				Pen.strokeColor_(settings[\outlineColor] ? Color.black);
				Pen.width_(settings[\outlineWidth] ? 2);
				Pen.fillColor_(settings.atFail(\color, {Color.cyan.alpha_(0.0)}));
				Pen.draw(3);//draw both stroke and fill
			}
		};
		labelView !? {labelView.remove;};
		labelView = StaticText(this, this.class.prCalculateSize(1).asRect.insetAll(labelOffset, 0, 0, 0))
		.stringColor_(settings[\stringColor] ? this.class.stringColor)
		.font_(settings[\font] ? this.class.font.bold_(true).italic_(true))
		.acceptsMouse_(false)
		.focusColor_(Color.white.alpha_(0.0))
		.background_(Color.white.alpha_(0.0))
		.canFocus_(false);
		this.prAddAltClickInterceptor(labelView);
		this.prAddAltClickInterceptor(outlineView);
		this.refreshLabel;
	}

	drawValue{

	}

	refreshLabel{
		{labelView.string_(label).toolTip_("% [%]".format(label, valueObj.type))}.defer;
	}

	bounds_{arg argBounds;
		this.fixedSize_(argBounds.size);
		super.bounds_(argBounds);
		this.drawBackground;
	}

	//the view value
	value_{arg val;
		//this needs to be implemented in subclasses
		this.subclassResponsibility(thisMethod);
	}

	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		//"Dependant update: % % % %".format(theChanged, whatChanged, whoChangedIt, toValue).postln;
		if(theChanged == valueObj, {//only update the view if the valueObj changed
			switch(whatChanged,
				\enabled, { this.enabled_(valueObj.enabled); },
				\value, { this.value_(valueObj.value); }
			);
			this.refresh;
		});
	}

	*font{^Font("Menlo", 10).bold_(true);}
	*stringColor{^Color.black}
	*elementColor{^Color.white.alpha_(0.0)}
}

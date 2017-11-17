VTMParameterView : View {
	var parameter;//the VTParameter instance
	var labelView, outlineView;
	var attributes;
	var definition;
	var <labelMode = \name;
	var <color;
	var <units = 1;
	classvar <unitWidth = 150, <unitHeight = 25;
	classvar <labelOffset = 5;
	classvar <viewTypeToClassMappings;

	*initClass{
		viewTypeToClassMappings = IdentityDictionary[
			\slider -> VTMSliderView,
			\number -> VTMNumberView,
			\toggle -> VTMToggleView
			/*\button -> VTMButtonView,
			\textfield -> VTMTextFieldView,
			\textedit -> VTMTextEditView,
			\label -> VTMLabelView,
			\option -> VTMOptionParameterView,
			\message -> VTMMessageParameterView,
			\timecode -> VTMTimecodeView*/
		];
	}

	*getClassForType{arg type;
		^viewTypeToClassMappings.at(type);
	}

	type{
		^this.class.viewTypeToClassMappings.findKeyForValue(this.class);
	}

	*makeFromAttributes{arg parent, bounds, definition, attributes, parameter;
		var viewClass;
		if(attributes.notNil, {
			if(attributes.includesKey(\type), {
				viewClass = this.viewTypeToClassMappings[attributes[\type]];
			});
		});

		if(viewClass.isNil, {
			viewClass = this.viewTypeToClassMappings[parameter.class.defaultViewType];
		});
		^viewClass.new(parent, bounds, definition, attributes, parameter);
	}

	*prCalculateSize{arg units;
		^Size(unitWidth, unitHeight * units);
	}

	*new{arg parent, bounds, definition, attributes, parameter;
		var viewBounds;
		viewBounds = bounds ?? { this.prCalculateSize(1).asRect; };
		^super.new( parent: parent, bounds: viewBounds ).initParameterView(definition, attributes, parameter);
	}

	initParameterView{arg definition_, attributes_, parameter_;
		if(parameter_.isNil, {Error("VTMParameterView - needs parameter").throw;});
		parameter = parameter_;
		parameter.addDependant(this);

		attributes = attributes_ ? IdentityDictionary.new;
		definition = definition_ ? Environment.new;

		if(attributes.notNil, {
			if(attributes.includesKey(\labelMode), {
				labelMode = attributes[\labelMode];
			});
		});

		this.addAction(
			{arg v,x,y,mod;
				var result = false;
				//if alt key is pressed when pressing down the view, the parameter setting window
				//for this ibjet will open.
				if(mod == 524288, {
					"Opening parameter attributes window: %".format(parameter.path).debug;
					result = true;
				});
				result;
			},
			\mouseDownAction
		);

		//This is needed to set the fixedSize
		this.bounds_(this.bounds);
	}

	// prAddAltClickInterceptor{arg argView;
	// 	//if alt key is pressed when pressing down the view, the action will be propagated to the next view
	// 	argView.addAction( {arg v,x,y,mod; mod != 524288 }, \mouseDownAction);
	// }

	close{
		action = nil;
		parameter.removeDependant(this);
		parameter = nil;
	}

	labelMode_{arg newMode;
		if([\name, \path].includesEqual(newMode), {
			labelMode = newMode;
			this.refreshLabel;
		});
	}

	drawBackground{
		outlineView !? {outlineView.clearDrawing; outlineView.remove;};
		outlineView = UserView(this, this.bounds).canFocus_(false);
		outlineView.drawFunc = {|uview|
			Pen.use {
				Pen.addRoundedRect(uview.bounds.insetBy(1,1), 5, 5);
				Pen.strokeColor_(attributes[\outlineColor] ? Color.black);
				Pen.width_(attributes[\outlineWidth] ? 2);
				Pen.fillColor_(attributes.atFail(\color, {Color.cyan.alpha_(0.0)}));
				Pen.draw(3);//draw both stroke and fill
			}
		};
		labelView !? {labelView.remove;};
		labelView = StaticText(this, this.class.prCalculateSize(1).asRect.insetAll(labelOffset, 0, 0, 0))
		.stringColor_(attributes[\stringColor] ? this.class.stringColor)
		.font_(attributes[\font] ? this.class.font.bold_(true).italic_(true))
		.acceptsMouse_(false)
		.focusColor_(Color.white.alpha_(0.0))
		.background_(Color.white.alpha_(0.0))
		.canFocus_(false);
		// this.prAddAltClickInterceptor(labelView);
		// this.prAddAltClickInterceptor(outlineView);
		this.refreshLabel;
	}

	refreshLabel{
		var label;
		switch(labelMode,
			\name, { label = parameter.name; },
			\path, { label = parameter.fullPath; }
		);
		attributes[\labelPrepend] !? {label = attributes[\labelPrepend] ++ label; };
		{labelView.string_(label).toolTip_("% [%]".format(label, this.type))}.defer;
	}

	bounds_{arg argBounds;
		this.fixedSize_(argBounds.size);
		super.bounds_(argBounds);
		this.drawBackground;
	}

	//pull style update
	update{arg theChanged, whatChanged, whoChangedIt, toValue;
		//"Dependant update: % % % %".format(theChanged, whatChanged, whoChangedIt, toValue).debug;
		if(theChanged == parameter, {//only update the view if the parameter changed
			switch(whatChanged,
				\enabled, { this.enabled_(parameter.enabled); },
				\path, { this.refreshLabel; },
				\name, { this.refreshLabel; }
			);
			this.refresh;
		});
	}

	*font{^Font("Menlo", 10).bold_(true);}
	*stringColor{^Color.black}
	*elementColor{^Color.white.alpha_(0.0)}
}

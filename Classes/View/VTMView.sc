VTMView : View {
	var <settings;
	var <definition;
	var model;
	var labelView;
	var label;
	var toolTip;

	var <units = 1;
	classvar <unitWidth = 150, <unitHeight = 25;
	var <color;

	font{ ^font ? Font("Iosevka Heavy", 12); }
	background{
		^Color(0.823, 0.757, 0.486)
	}

	*prCalculateSize{| units |
		^Size(unitWidth, unitHeight * units);
	}

	*new{| parent, bounds, definition, settings, model |
		var viewBounds;
		viewBounds = bounds ?? { this.prCalculateSize(1).asRect; };
		^super.new(parent, viewBounds).init(definition, settings, model);
	}

	init{| definition_, settings_, model_ |
		settings = settings_ ?? {IdentityDictionary.new};
		definition = definition_;
		model = model_;
		model.addDependant(this);

		this.prInitLabel;
		toolTip = "";
		this.prMakeChildViews;

		this.addAction({| ...args |
			model.removeDependant(this);
		}, \onClose);

		this.layout_( this.prMakeLayout );
		//This is needed to set the fixedSize
		this.bounds_(this.bounds);
		this.mouseDownAction_({|...args| args.postln; true;})
	}

	prInitLabel{
		this.subclassResponsibility(thisMethod);
	}

	prMakeChildViews{
		this.subclassResponsibility(thisMethod);
	}

	label{ ^label; }

	label_{| str |
		label = str;
		this.refreshLabel;
	}

	refreshLabel{
		{
			labelView
			.string_(label)
			.toolTip_(this.toolTip);
		}.defer;
	}

	toolTip{ ^toolTip; }
	toolTip_{|str|
		{
			toolTip = str;
			this.refreshLabel;
		}.defer;
	}

	prMakeLabelView{| str |
		var result;
		var labelStr = str ?? "";
		result = StaticText()
		// .maxHeight_(this.class.unitHeight)
		.string_(labelStr)
		// .background_(this.background)
		.mouseDownAction_({
			model.debugString.vitmdebug(0, thisMethod);
		})
		.font_(this.font);

		^result;
	}

	prMakeLayout{
		this.subclassResponsibility(thisMethod);
	}
}

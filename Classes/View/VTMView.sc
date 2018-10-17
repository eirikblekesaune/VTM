VTMView : View {
	var <settings;
	var <definition;
	var model;
	var labelView;

	var <units = 1;
	classvar <unitWidth = 150, <unitHeight = 25;
	var <color;

	font{ ^font ? Font("Avenir Next", 10); }
	background{
		^Color(0.823, 0.757, 0.486)
	}

	*prCalculateSize{| units |
		^Size(unitWidth, unitHeight * units);
	}

	*new{| parent, bounds, definition, settings, model |
		var viewBounds;
		viewBounds = bounds ?? { this.prCalculateSize(1).asRect; };
		"Making VTM view with parent: %".format(parent).vtmdebug(4, thisMethod);
		^super.new(parent, viewBounds).init(definition, settings, model);
	}

	init{| definition_, settings_, model_ |
		settings = settings_;
		definition = definition_;
		model = model_;
		model.addDependant(this);

		this.prMakeChildViews;

		this.layout_( this.prMakeLayout.spacing_(2).margins_([5, 2]) );
		//This is needed to set the fixedSize
		this.bounds_(this.bounds);

		this.addAction({| ...args |
			model.removeDependant(this);
		}, \onClose);
	}

	prMakeChildViews{
		labelView = this.prMakeLabelView;
	}

	prMakeLabelView{| str |
		var result;
		var labelStr = str ?? {model.name};
		result = StaticText(
			this,
			Rect(0, 0, this.bounds.width, this.class.unitHeight)
		)
		.maxHeight_(this.class.unitHeight)
		.string_(labelStr)
		.background_(this.background)
		.mouseDownAction_({
			model.debugString.postln;
		})
		.font_(this.font);

		^result;
	}

	prMakeLayout{
		^VLayout(
			labelView
		)
	}
}

VTMView : View {
	var <settings;
	var <definition;
	var <units = 1;
	classvar <unitWidth = 150, <unitHeight = 25;
	var <color;

	font{ ^font ? Font("Menlo", 12); }

	*prCalculateSize{arg units;
		^Size(unitWidth, unitHeight * units);
	}

	*new{arg parent, bounds, definition, settings;
		var viewBounds;
		viewBounds = bounds ?? { this.prCalculateSize(1).asRect; };

		//"Making VTM view with parent: %".format(parent).debug;
		^super.new(parent, viewBounds).initVTMView(definition, settings);
	}

	initVTMView{arg definition_, settings_;
		settings = settings_;
		definition = definition_;

		//This is needed to set the fixedSize
		this.bounds_(this.bounds);

	}

}

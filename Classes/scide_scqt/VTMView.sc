VTMView : View {
	var <settings;
	var <definition;

	font{ ^font ? Font("Menlo", 12); }


	*new{arg parent, bounds, definition, settings;
		//"Making VTM view with parent: %".format(parent).debug;
		^super.new(parent, bounds).initVTMView(definition, settings);
	}

	initVTMView{arg definition_, settings_;
		settings = settings_;
		definition = definition_;

		//This is needed to set the fixedSize
		this.bounds_(this.bounds);

	}

}

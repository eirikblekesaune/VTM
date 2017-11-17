VTMView : View {
	var <settings;
	var <definition;
	var >font;

	font{ ^font ? Font("Menlo", 12); }


	*new{arg parent, bounds, definition, settings;
		//"Making VTM view with parent: %".format(parent).debug;
		^super.new(parent, bounds).initView(definition, settings);
	}

	initView{arg definition_, settings_;
		settings = settings_;
		definition = definition_;
	}

}

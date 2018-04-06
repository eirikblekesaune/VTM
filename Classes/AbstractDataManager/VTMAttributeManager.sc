VTMAttributeManager : VTMComponent {
	var presets;

	*dataClass{ ^VTMAttribute; }
	name{ ^\attributes; }

	*new{arg itemDeclarations, element;
		^super.new(itemDeclarations, element).initAttributeManager;
	}

	initAttributeManager{
		//presets = VTMPresetManager(itemDeclarations[\presets]);
	}

	set{arg key...args;
		items[key].valueAction_(*args);
	}

	get{arg key;
		^items[key].value;
	}
}

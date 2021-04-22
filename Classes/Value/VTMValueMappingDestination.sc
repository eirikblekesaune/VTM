VTMValueMappingDestination{
	var <valueObj;
	var <mapping;

	*new{arg valueObj, mapping;
		^super.new.init(valueObj, mapping);
	}

	init{arg valueObj_, mapping_;
		valueObj = valueObj_;
		mapping = mapping_;
	}
}

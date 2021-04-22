/*
ArrayValues has items with a defined type.
Differing from ListValue in that it doesn't make the actual internal
sub-Values, but rather constructs a subValue interface
to its internal items(i.e. array elements).
*/
VTMArrayValue : VTMCollectionValue {
	var <size = 0;
	var <fixedSize = false;
	var <itemType;

	isValidType{| val |
		^(val.isString.not and: {val.isArray});
	}

	*type{ ^\array; }

	*prDefaultValueForType{
		^[];
	}

	*new{| properties |
		^super.new(properties).initArrayValue;
	}

	initArrayValue{
		if(properties.notEmpty, {
			if(properties.includesKey(\size), {
				size = properties[\size];
			});
			if(properties.includesKey(\fixedSize), {
				fixedSize = properties[\fixedSize];
			});
			if(properties.includesKey(\itemType), {
				itemType = properties[\itemType];
			});
		});
		properties[\value] = this.get(\defaultValue) ! size;
	}

	*propertyKeys{
		^super.propertyKeys.addAll([
			\size, \fixedSize, \itemType
		]);
	}

	value_{| val |
		if(fixedSize, {
			if(val.size != this.size, {
				^this; // return early
			})
		});
		super.value_(val);
	}

}

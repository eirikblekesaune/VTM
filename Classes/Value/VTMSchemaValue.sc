/*
JSON Schema

Six types: null, boolean, object, array, number, string
*/
VTMSchemaValue : VTMDictionaryValue {
	var schema;
	var description;
	var required;

	*new{| properties |
		^super.new(properties).initSchemaValue;
	}

	initSchemaValue{| attrs_ |
		if(properties.notEmpty, {
			if(properties.includesKey(\schema), {
				schema = properties[\schema].copy;
			});
		});
	}

	isValidType{| val |
		var result = false;
		if(super.isValidType(val), {
			result = this.validate(val);
		});
		^result;
	}
	*type{ ^\schema; }
	*prDefaultValueForType{
		^();
	}

	*propertyKeys{
		^super.propertyKeys ++ [\schema];
	}

	schema{ ^schema.copy; }

	validate{| inval |
		^inval.isKindOf(Dictionary) and: {this.validateSchema(inval);};
	}

	validateSchema{|inval|
		//TODO: Much more to add here, but at least checking non-optional values for now.
		var nonOptionalItems = schema.select({|item|
			item.includesKey(\optional) and: {item[\optional] == false};
		});
		//If any of the non-optional keys are missing, exit early
		if(nonOptionalItems.keys.difference(inval.keys).notEmpty, {
			^false;
		});
		^true;
	}

	value_{| val |
		var inval = val.copy;
		if(this.validate(inval), {
			super.value_(inval);
		}, {
			"SchemaValue:value_ - ignoring val because of failed schema validation: '%'".format(
				inval
			).warn;
		});
	}

	//non-DRY version of value_ with doAction call if value is valid.
	//  Doing this to avoid copy and validation twice
	valueAction_{| val |
		var inval = val.copy;
		if(this.validate(inval), {
			super.value_(inval);
			this.doAction;
		}, {
			"SchemaValue:valueAction_ - ignoring val because of failed schema validation: '%'".format(
				inval
			).warn;
		});
	}

	parseStringValue{|str|
		//Expects sclang key val notation in event syntax: (hei: 123, hallo: 'kjubbing')
	}
}

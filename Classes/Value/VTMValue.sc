VTMValue {
	var enum;
	var <>action;
	var <selectedEnum;
	var scheduler;//TEMP setter
	var properties;

	*prDefaultValueForType{
		^nil;
	}

	*typeToClass{| val |
		^"VTM%Value".format(val.asString.capitalize).asSymbol.asClass;
	}

	*classToType{| val |
		^val.name.asString.findRegexp("^VTM(.+)Value$")[1][1].toLower;
	}

	*type{
		this.subclassResponsibility(thisMethod);
	}

	type{
		^this.class.type;
	}


	*none{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*string{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*boolean{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*timecode{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*integer{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*decimal{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*array{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*dictionary{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*schema{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*list{| properties | ^this.makeFromType(thisMethod.name, properties); }
	*tuple{| properties | ^this.makeFromType(thisMethod.name, properties); }

	*makeFromProperties{| properties |
		var dec = properties.deepCopy;
		var type = dec.removeAt(\type);
		^this.makeFromType(type, dec);
	}

	*makeFromType{| type, properties |
		var class;
		class = this.typeToClass(type);
		if(class.notNil, {
			^class.new(properties);
		}, {
			^this.new(properties);
		});
	}

	*new{| properties |
		^super.new.initValue(properties);
	}

	initValue{| properties_ |
		properties = VTMValueProperties.newFrom(properties_ ? []);
		if(properties.notEmpty, {
			if(properties.includesKey(\value), {
				this.value_(properties[\value]);
			});
			if(properties.includesKey(\defaultValue), {
				this.defaultValue_(properties[\defaultValue]);
			});
			if(properties.includesKey(\enum), {
				this.enum_(properties[\enum]);
			});
		});

	}

	//only non-abstract sub classes will implement this.
	isValidType{| val |
		this.subclassResponsibility(thisMethod);
	}

	//set value to default
	reset{| doActionUponReset = false |
		if(this.defaultValue.notNil, {
			this.value_(this.defaultValue);
			if(doActionUponReset, {
				this.doAction;
			});
		});
	}

	valueAction_{| val |
		if(this.filterRepetitions, {
			var willDoAction = val != this.value; //check if new value is different
			this.value_(val);
			if(willDoAction, {
				this.doAction;
			});
		}, {
			this.value_(val);
			this.doAction;
		});
	}

	//Enabled by default.
	//Will enable action to be run
	enable{| doActionWhenEnabled = false |
		this.enabled_(true);
		if(doActionWhenEnabled, {
			this.doAction;
		});
	}

	//Will disable action from being run
	disable{
		this.enabled_(false);
	}

	doAction{
		if(this.enabled, {
			action.value(this);
		});
	}

	ramp{| val, time |
		if(scheduler.isPlaying, {
			scheduler.stop;
		});
		scheduler = fork{
			time.wait;
			this.valueAction_(val);
		};
	}

	addEnum{| val, name, slot |
		enum.addItem(val, name, slot);
		this.changed(\enum);
	}

	removeEnum{| slotOrName |
		if(enum.removedItem(slotOrName).notNil, {
			this.changed(\enum);
		});
	}

	moveEnum{| slotOrName, toSlot |
		enum.moveItem(slotOrName, toSlot);
		this.changed(\enum);
	}

	setEnumName{| slotOrName, itemName |
		enum.setItemName(slotOrName, itemName);
		this.changed(\enum);
	}

	changeEnum{| slotOrName, newValue |
		enum.changeItem(slotOrName, newValue);
		this.changed(\enum);
	}

	getEnumValue{| slotOrName |
		^enum[slotOrName];
	}

	*propertyKeys{
		^[\enabled, \filterRepetitions, \value, \defaultValue, \enum, \restrictValueToEnum];
	}

	set{| key, val |
		properties.put(key, val);
		this.changed(\properties, key);
	}

	get{| key |
		^properties[key];
	}

	properties{| includeDefaultValues = true, includeType = true |
		var result = properties.class.new;
		var propKeys;
		propKeys = result.keys;
		if(includeDefaultValues, {
			propKeys = this.class.propertyKeys;
		});
		if(includeType, {
			propKeys = propKeys.add(\type);
		});
		propKeys.do({| attrKey |
			var attrVal = this.perform(attrKey);
			//don't use the ones that are nil
			if(attrVal.notNil, {
				result.put(attrKey, attrVal.deepCopy);
			});
		});

		^result;
	}

	//Property getters
	enabled{ ^this.get(\enabled) ? true; }
	enabled_{| val | this.set(\enabled, val); }

	filterRepetitions{ ^this.get(\filterRepetitions) ? false; }
	filterRepetitions_{| val | this.set(\filterRepetitions, val); }

	value{ ^this.get(\value) ? this.defaultValue; }
	value_{| val |
		if(this.restrictValueToEnum, {
			if(this.enum.includes(val), {
				this.set(\value, val);
				this.changed(\value);
			});
		}, {
			this.set(\value, val);
			this.changed(\value);
		});
	}

	defaultValue{ ^this.get(\defaultValue) ? this.class.prDefaultValueForType; }
	defaultValue_{| val | this.set(\defaultValue, val); }

	enum{ ^this.get(\enum); }
	enum_{| val | this.set(\enum, val); }

	restrictValueToEnum{ ^this.get(\restrictValueToEnum) ? false; }
	restrictValueToEnum_{| val | this.set(\restrictValueToEnum, val); }


	//views
	*getViewClass{
		^'VTMValueView'.asClass;
	}

	makeView{| parent, bounds, definition, settings |
		var viewClass = this.class.getViewClass;
		//override class if defined in settings.
		^viewClass.new(parent, bounds, definition, settings, this);
	}
}

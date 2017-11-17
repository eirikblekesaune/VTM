//base class for parameter classes.
//Objects of this type has no arguments/values but works
//as a 'command' to perform the defined action.

VTMParameter {
	var <name;
	var <path, fullPathThunk; //an OSC valid path.
	var action, hiddenAction;
	var <enabled = true;
	var <mappings;
	var <oscInterface;
	var <>willStore = true;
	var <>onlyReturn = false;
	var <isSubParameter = false;
	var >envir;
	var attributes;
	var attributeGetterFunctions;

	*typeToClass{arg val;
		^"VTM%Parameter".format(val.asString.capitalize).asSymbol.asClass;
	}

	*classToType{arg val;
		^val.name.asString.findRegexp("^VTM(.+)Parameter$")[1][1].toLower;
	}

	*type{
		this.subclassResponsibility(thisMethod);
	}

	type{
		^this.class.type;
	}

	//factory type constructor
	//In attributes dict 'name' and 'type' is mandatory.
	*makeFromAttributes{arg attributes;
		var decl = attributes.deepCopy;
		//if 'type' and 'name' is defined in attributes
		if(decl.includesKey(\name), {
			if(decl.includesKey(\type), {
				var paramClass = decl.removeAt(\type);
				var paramName = decl.removeAt(\name);
				^VTMParameter.typeToClass(paramClass).new(paramName, decl);
			}, {
				Error("VTMParameter attributes needs type").throw;
			});
		}, {
			Error("VTMParameter attributes needs name").throw;
		});
	}

	//This constructor is not used directly, only for testing purposes
	*new{arg name, attributes;
		if(name.notNil, {
			^super.new.initParameter(name, attributes);
		}, {
			Error("VTMParameter needs name").throw;
		});
	}

	initParameter{arg name_, attributes_;
		var tempName = name_.copy.asString;
		if(tempName.first == $/, {
			tempName = tempName[1..];
			"Parameter : removed leading slash from name: %".format(tempName).warn;
		});
		name = tempName.asSymbol;
		attributes = attributes_ ? IdentityDictionary.new;

		fullPathThunk = Thunk.new({
			if(isSubParameter, {
				".%".format(name).asSymbol;
			}, {
				"/%".format(name).asSymbol;
			});
		});
		if(attributes.notEmpty, {
			if(attributes.includesKey(\isSubParameter), {
				isSubParameter = attributes[\isSubParameter];
			});
			if(attributes.includesKey(\path), {
				this.path = attributes[\path];
			});
			if(attributes.includesKey(\action), {
				// "Setting action from attributes: %".format(attributes[\action]).debug;
				this.action_(attributes[\action]);
			});
			if(attributes.includesKey(\enabled), {
				//Is enabled by default so only disabled if defined
				if(attributes[\enabled].not, {
					this.disable;
				})
			});
			if(attributes.includesKey(\willStore), {
				willStore = attributes[\willStore];
			});
			if(attributes.includesKey(\onlyReturn), {
				onlyReturn = attributes[\onlyReturn];
			});
		});
	}

	doAction{
		if(envir.notNil, {
			envir.use{action.value(this)};
		}, {
			action.value(this);
		});
	}

	//If path is not defined the name is returned with a leading slash
	fullPath{
		^fullPathThunk.value;
	}

	path_{arg str;
		var newPath = str.copy.asString;
		//add leading slash if not defined
		if(newPath.first != $/, {
			newPath = newPath.addFirst("/");
			"Added leading slash for parameter '%'".format(name).warn;
		});
		path = newPath.asSymbol;
		fullPathThunk = Thunk.new({
			if(isSubParameter, {
				"%.%".format(path, name).asSymbol;
			}, {
				"%/%".format(path, name).asSymbol;
			});
		});
	}

	action_{arg func;
		//add to hidden action if disabled
		if(enabled, {
			action = func;
		}, {
			hiddenAction = func;
		});
	}

	action{
		if(enabled, {
			^action;
		}, {
			^hiddenAction;
		});
	}

	//Enabled by default.
	//Will enable action to be run
	enable{arg doActionWhenEnabled = false;
		if(hiddenAction.notNil, {
			action = hiddenAction;
		});
		hiddenAction = nil;
		enabled = true;
		if(doActionWhenEnabled, {
			this.doAction;
		});
	}

	//Will disable action from being run
	disable{
		//only happens when action is defined
		if(action.notNil, {
			hiddenAction = action;
			action = nil;
		});
		enabled = false;
	}

	free{
		action = nil;
		hiddenAction = nil;
		if(mappings.notNil, {
			mappings.do(_.free);
		});
		mappings = nil;

		if(oscInterface.notNil, {
			oscInterface.free;
		});
		oscInterface = nil;
		super.free;

		this.changed(\freed);
	}


	makeView{arg parent, bounds, definition, attributes;
		^VTMParameterView.makeFromAttributes(parent, bounds, definition, attributes, this);
	}

	attributes{
		var result = IdentityDictionary.new;
		if(attributeGetterFunctions.isNil, {
			attributeGetterFunctions = this.class.makeAttributeGetterFunctions(this);
		});

		this.class.attributeKeys.collect({arg key;
			result.put(key, attributeGetterFunctions[key].value.deepCopy);
		});
		^result;
	}

	*attributeKeys{
		^[\name, \path, \action, \enabled, \type];
	}

	*makeAttributeGetterFunctions{arg param;
		var result;
		result = IdentityDictionary[
			\name -> {param.name;},
			\path -> {param.path;},
			\action -> {
				var aFunction;
				aFunction = param.action;
				if(aFunction.notNil and: {aFunction.isKindOf(Function)} and: {aFunction.isClosed}, {
					//Only return closed functions as attributes
					aFunction = aFunction.asCompileString;
				}, {
					aFunction = nil;
				});
				aFunction;
			},
			\enabled -> {param.enabled;},
			\type -> {param.type;}
		];
		^result;
	}

	*makeAttributeSetterFunctions{arg param;
		var result;
		result = IdentityDictionary.new;
		^result;
	}

	*makeOSCAPI{arg param;
		var result = IdentityDictionary.new;

		//make query getters for attributes
		param.attributeGetterFunctions.keysValuesDo({arg key, getFunc;
			result.put(
				"%?".format(key).asSymbol,
				getFunc
			);
		});
		//make setters for attributes
		result.putAll(param.attributeSetterFunctions);
		^result;
	}

	enableOSC{
		if(oscInterface.isNil, {
			oscInterface = VTMParameterOSCInterface(this);
		});
		oscInterface.enable;
	}

	disableOSC{
		if(oscInterface.notNil, {
			oscInterface.free;
			oscInterface = nil;
		});
	}
}

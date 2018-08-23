/*
A VTMControl is something that controls an instance of data
*/
VTMControl : VTMData {
	var <valueObj;
	var forwardings;
	var forwarder;

	*viewClass{
		^\VTMControlView.asClass;
	}
	*managerClass{ ^VTMControlManager; }

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initControl;
	}

	initControl{
		var valueClass = VTMValue.typeToClass(declaration[\type]) ? VTMValue;
		var valueProperties = VTMOrderedIdentityDictionary.new;
		//extract property values from declaration
		valueClass.propertyKeys.do({arg propKey;
			if(declaration.includesKey(propKey), {
				valueProperties.put(propKey, declaration[propKey]);
			});
		});
		valueObj = VTMValue.makeFromType(declaration[\type], valueProperties);
		forwardings = VTMOrderedIdentityDictionary.new;
		this.enableForwarding;
	}

	action_{arg func;
		valueObj.action_(func);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\type -> (type: \string, optional: true, defaultValue: \none)
			]
		);
	}

	valueAction_{arg ...args;
		valueObj.valueAction_(*args);
	}

	value_{arg ...args;
		valueObj.value_(*args);
	}

	value{
		^valueObj.value;
	}

	free{
		forwardings.clear;
		forwarder.remove(\value);
		valueObj.release;
		super.free;
	}

	addForwarding{arg key, addr, path, vtmJson = false, mapFunc;
		//Observe value object for changng values
		forwardings.put(key, (addr: addr, path: path, vtmJson: vtmJson, mapFunc: mapFunc));
	}

	removeForwarding{arg key;
		forwardings.removeAt(key);
	}

	removeAllForwardings{
		forwardings.clear;
	}

	disableForwarding{
		forwarder.remove(\value);
		forwarder.clear;
	}

	type{
		^this.get(\type);
	}

	declaration{
		^valueObj.properties.putAll(parameters);
	}

	//setting the value object properties.
	set{arg key...args;
		valueObj.set(key, *args);
	}

	//getting the vaue object properties, or if not found
	//try getting the Element parameters
	get{arg key;
		var result;
		result = valueObj.get(key);
		if(result.isNil, {
			result = super.get(key);
		});
		^result;
	}

	disable{
		valueObj.disable;
		super.disable;
	}

	enable{
		super.enable;
		valueObj.enable;
	}

	enableForwarding{
		forwarder = SimpleController(valueObj).put(\value, {arg theChanged;
			forwardings.do({arg item;
				var outputValue, mapFunc;
				//TODO: Change this so it supports other value types
				mapFunc = item[\mapFunc] ? {|val| val};
				outputValue = mapFunc.value(this.value);
				if(item[\vtmJson], {
					VTM.sendMsg(item[\addr].hostname, item[\addr].port, item[\path], outputValue);
				}, {
					if(this.type==\dictionary, {
						"VTMControl, forwarding, dictionaries must be sent as JSON".warn;
					});
					item[\addr].sendMsg(item[\path], outputValue);
				});
			});
		});
	}

	makeView{arg parent, bounds, viewDef, settings;
		^this.class.viewClass.new(parent, bounds, viewDef, settings, this);
	}
}

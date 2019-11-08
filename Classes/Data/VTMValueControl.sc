VTMValueControl : VTMControl {
	var <valueObj;
	var valueListener;
	var forwardings;
	var forwarder;
	var traceResponder;

	*new{| name, declaration, manager |
		var action = declaration[\action];
		^super.new(name, declaration, manager).initValueControl(action);
	}

	initValueControl{| action_ |
		var valueClass = VTMValue.typeToClass(
			declaration[\type]
		) ? VTMValue;
		var valueProperties = VTMOrderedIdentityDictionary.new;
		//extract property values from declaration
		valueClass.propertyKeys.do({| propKey |
			if(declaration.includesKey(propKey), {
				valueProperties.put(propKey, declaration[propKey]);
			});
		});
		valueObj = VTMValue.makeFromType(
			declaration[\type], valueProperties
		);
		this.action = action_;

		valueListener = SimpleController(valueObj).put(\value, {
			this.changed(\value, valueObj.value);
		});

		forwardings = VTMOrderedIdentityDictionary.new;
		this.enableForwarding;
	}

	free{
		forwardings.clear;
		forwarder.remove(\value);
		valueObj.release;
		super.free;
	}

	action_{| func |
		valueObj.action_(func);
	}

	action{
		^valueObj.action;
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\type -> (
					type: \string,
					optional: true,
					defaultValue: \none
				)
			]
		);
	}

	//setting the value object properties.
	set{| key...args |
		valueObj.set(key, *args);
	}

	//getting the vaue object properties, or if not found
	//try getting the Element parameters
	get{| key |
		var result;
		result = valueObj.get(key);
		if(result.isNil, {
			result = super.get(key);
		});
		^result;
	}

	type{
		^this.get(\type);
	}

	declaration{
		^valueObj.properties.putAll(parameters);
	}

	disable{
		valueObj.disable;
		super.disable;
	}

	enable{
		super.enable;
		valueObj.enable;
	}

	enabled{
		^valueObj.enabled;
	}

	enableForwarding{
		forwarder = SimpleController(valueObj).put(\value, {| theChanged |
			forwardings.do({| item |
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

	addForwarding{| key, addr, path, vtmJson = false, mapFunc |
		//Observe value object for changng values
		forwardings.put(key, (addr: addr, path: path, vtmJson: vtmJson, mapFunc: mapFunc));
	}

	removeForwarding{| key |
		forwardings.removeAt(key);
	}

	removeAllForwardings{
		forwardings.clear;
	}

	disableForwarding{
		forwarder.remove(\value);
		forwarder.clear;
	}

	trace{ arg bool = true;
		if(bool, {
			if(traceResponder.isNil, {
				traceResponder = {arg theChanged, what ...args;
					var str;
					switch(what,
						\value, {
							str = "value: %".format(theChanged.value);
						},
						\properties, {
							str = "property: % - %".format(
								args[0], theChanged.get(args[0])
							);
						},
						{
							str = [what] ++ args;
						}
					);
					"control trace: % - %".format(this.fullPath, str).vtmdebug(0, thisMethod);
				};
				valueObj.addDependant(traceResponder);
			});
		}, {
			if(traceResponder.notNil, {
				valueObj.removeDependant(traceResponder);
			})
		});
	}
}

VTMValueMapping {
	var <source;
	var <destination;
	var <type = \forwarding;
	var <condition;

	var mapFunc;
	var unmapFunc;

	var forwardingFunc;
	var forwardingListener;
	var isForwardingValue = false;

	var subscriptionListener;
	var subscriptionFunc;
	var isUpdatingSubscriptionValue = false;

	*new{|properties|
		^super.new.initValueMapping(properties);
	}

	initValueMapping{| properties_ |
		var properties = VTMValueProperties.newFrom(properties_ ? []);
		var hasMandatoryKeys = {|p|
			var mandatoryPropertyKeys = Set[\source, \destination];
			p.keys.as(Set).sect(mandatoryPropertyKeys) == mandatoryPropertyKeys;
		};
		if(properties.notEmpty and: {hasMandatoryKeys.value(properties)}, {
			if(properties.includesKey(\source), {
				source = properties[\source];
				if(source.isKindOf(VTMValue).not, {
					Error(
						"ValueMapping source must be of kind VTMValue: %[%]".format(
							source, source.class
						)
					).throw;
				});
			});
			if(properties.includesKey(\destination), {
				destination = properties[\destination];
				if(destination.isKindOf(VTMValue).not, {
					Error(
						"ValueMapping destination must be of kind VTMValue: %[%]".format(
							destination, destination.class
						)
					).throw;
				});
			});
			if(properties.includesKey(\type), {
				type = properties[\type];
			});
			if(properties.includesKey(\condition), {
				condition = properties[\condition];
			});
			if(properties.includesKey(\forwardingFunc), {
				forwardingFunc = properties[\forwardingFunc];
			});
			if(properties.includesKey(\subscriptionFunc), {
				subscriptionFunc = properties[\subscriptionFunc];
			});
			if(properties.includesKey(\mapFunc), {
				mapFunc = properties[\mapFunc];
			});
			if(properties.includesKey(\unmapFunc), {
				unmapFunc = properties[\unmapFunc];
			});
		}, {
			Error("Value Mapping must have both source: % and destination: %".format(
				properties[\source], properties[\destination]
			)).throw;
		});

		if(mapFunc.isNil, {
			mapFunc = this.class.getDefaultMappingFunc(source, destination);
		});

		if(unmapFunc.isNil, {
			unmapFunc = this.class.getDefaultMappingFunc(destination, source);
		});
	}

	enable{
		if(source.notNil and: {destination.notNil}, {
			switch(type,
				\forwarding, {
					//send the data from the source to the destination
					this.startForwarding;
				},
				\bind, {
					//both source and destination subscribes to eachother
					this.startForwarding;
					this.startSubscribing;
				},/*
				\subscription, {
					//subscribe to the data from the destination
					source.addSubscription(destination);
				},

				\exclusiveBind, {
					//both source and destination subscribes to eachother exclusively
					source.addForwarding(destination, exclusive: true);
					source.addSubscriptions(destination, exclusive: true);
				}*/
				{
					"unknown mapping type '%'".format(type).warn;
				}
			);
		}, {
			"Both source and destination must not be nil".warn;
		});
	}

	disable{
		if(forwardingListener.notNil, {
			forwardingListener.remove;
		});
		if(subscriptionListener.notNil, {
			subscriptionListener.remove;
		});
	}

	free{
		this.disable;
		source = nil;
		destination = nil;
	}

	startForwarding{
		var func;
		if(forwardingFunc.isNil, {
			func = {|val, m|
				destination.valueAction_(val);
			};
		}, {
			func = {|val, m|
				forwardingFunc.value(val, m);
			};
		});
		forwardingListener = SimpleController(source).put(\value, {
			| theChanged, whatChanged |
			if(isUpdatingSubscriptionValue.not, {
				isForwardingValue = true;
				func.value(this.mappedValue, this);
				isForwardingValue = false;
			})
		})
	}

	startSubscribing{
		var func;
		if(subscriptionFunc.isNil, {
			func = {|val, m|
				source.valueAction_(val);
			};
		}, {
			func = {|val, m|
				subscriptionFunc.value(val, m);
			};
		});
		subscriptionListener = SimpleController(destination).put(\value, {
			| theChanged, whatChanged|
			if(isForwardingValue.not, {
				isUpdatingSubscriptionValue = true;
				func.value(this.unmappedValue, this);
				isUpdatingSubscriptionValue = false;
			});
		});
	}

	//the source value mapped to
	//the destination value range
	mappedValue{
		^mapFunc.value(source.value, this);
	}

	//the destination value mapped to
	//the source value range
	unmappedValue{
		^unmapFunc.value(destination.value, this);
	}

	pushToDestination{
		destination.value = this.mappedValue;
	}

	pullFromDestination{
		source.value = this.unmappedValue;
	}

	pushToSource{
		this.pullFromDestination;
	}

	pullFromSource{
		this.pushToDestination;
	}

	*getDefaultMappingFunc{|fromValue, targetValue|
		var result;
		result = this.prGetMappingFunc(fromValue, targetValue);
		^result;
	}

	*prGetMappingFunc{|fromValue, targetValue|
		var typeMap = Dictionary[
			[VTMIntegerValue, VTMIntegerValue] -> \prNumberToNumberFunc,
			[VTMDecimalValue, VTMIntegerValue] -> \prNumberToNumberFunc,
			[VTMIntegerValue, VTMDecimalValue] -> \prNumberToNumberFunc,
			[VTMDecimalValue, VTMDecimalValue] -> \prNumberToNumberFunc,
			[VTMBooleanValue, VTMIntegerValue] -> \prBooleanToNumberFunc,
			[VTMBooleanValue, VTMDecimalValue] -> \prBooleanToNumberFunc,
			[VTMIntegerValue, VTMBooleanValue] -> \prNumberToBooleanFunc,
			[VTMDecimalValue, VTMBooleanValue] -> \prNumberToBooleanFunc
		];
		var methodName = typeMap[[fromValue.class, targetValue.class]];
		var func = this.perform(
			methodName,
			fromValue,
			targetValue
		);
		if(func.isNil, {
			Error(
				"Non-implemented default mapping func for relation: % => %".format(
					fromValue, targetValue
				)
			).throw;
		});
		^func;
	}

	*prNumberToNumberFunc{|fromValue, targetValue|
		^{arg inVal;
			var inMin, inMax, outMin, outMax;
			var outVal;
			inMin = fromValue.minVal;
			inMax = fromValue.maxVal;
			outMin = targetValue.minVal;
			outMax = targetValue.maxVal;
			outVal = inVal.linlin(inMin, inMax, outMin, outMax);
			outVal;
		};
	}

	*prNumberToBooleanFunc{|fromValue, targetValue|
		^{arg inVal;
			var inMin, inMax, outMin, outMax;
			var outVal;
			outVal = inVal.booleanValue;
			outVal;
		};
	}

	*prBooleanToNumberFunc{|fromValue, targetValue|
		^{arg inVal;
			var inMin, inMax, outMin, outMax;
			var outVal;
			outVal = inVal.asInteger;
			outVal;
		};
	}
}

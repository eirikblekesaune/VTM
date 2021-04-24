VTMValueMapping {
	var <source;
	var <destination;
	var <type = \forwarding;
	var <condition;

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
			});
			if(properties.includesKey(\destination), {
				destination = properties[\destination];
			});
			if(properties.includesKey(\type), {
				type = properties[\type];
			});
			if(properties.includesKey(\condition), {
				condition = properties[\condition];
			});
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
		source = nil;
		destination = nil;
	}

	startForwarding{
		var func = forwardingFunc;
		if(func.isNil, {
			func = this.class.getDefaultForwardingFunc(this);
		});
		forwardingListener = SimpleController(source).put(\value, {
			| theChanged, whatChanged |
			if(isUpdatingSubscriptionValue.not, {
				isForwardingValue = true;
				forwardingFunc.value(theChanged.value);
				isForwardingValue = false;
			})
		})
	}

	startSubscribing{
		var func = subscriptionFunc;
		if(func.isNil, {
			func = this.class.getDefaultSubscriptionFunc(this);
		});
		subscriptionListener = SimpleController(destination).put(\value, {
			| theChanged, whatChanged|
			if(isForwardingValue.not, {
				isUpdatingSubscriptionValue = true;
				subscriptionFunc.value(theChanged.value);
				isUpdatingSubscriptionValue = false;
			});
		});
	}

	//the destination value mapped to
	//the source value range
	unmappedValue{
		var result;
		case
		{destination.isKindOf(VTMNumberValue)} {
			var inMin, inMax, outMin, outMax;
			inMin = destination.minVal;
			inMax = destination.maxVal;
			outMax = source.maxVal;
			outMin = source.minVal;
			result = destination.value.linlin(inMin, inMax, outMin, outMax);
		} {
			"No know the thing: %".format(destination).vtmwarn(0, thisMethod);
		};
		^result;
	}

	//the source value mapped to
	//the destination value range
	mappedValue{
		var result;
		case
		{source.isKindOf(VTMNumberValue)} {
			var inMin, inMax, outMin, outMax;
			outMin = destination.minVal;
			outMax = destination.maxVal;
			inMin = source.minVal;
			inMax = source.maxVal;
			result = source.value.linlin(inMin, inMax, outMin, outMax);
		} {
			"No know the thing: %".format(destination).vtmwarn(0, thisMethod);
		};
		^result;
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

	*getDefaultForwardingFunc{arg mapping;
		var result;
		case
		{mapping.source.isKindOf(VTMNumberValue)} {
			result = {arg inVal;
				var outVal;
				var inMin, inMax, outMin, outMax;
				inMin = mapping.source.minVal;
				inMax = mapping.source.maxVal;
				outMin = mapping.destination.minVal;
				outMax = mapping.destination.maxVal;
				outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				mapping.destination.valueAction_(outVal);
			};
		} {
			"No know the thing: %".format(mapping.source).vtmwarn(0, thisMethod);
			result = {};
		};
		^result;
	}

	*getDefaultSubscriptionFunc{|mapping|
		var result;
		case
		{mapping.destination.isKindOf(VTMNumberValue)} {
			result = {arg inVal;
				var outVal;
				var inMin, inMax, outMin, outMax;
				inMin = mapping.destination.minVal;
				inMax = mapping.destination.maxVal;
				outMin = mapping.source.minVal;
				outMax = mapping.source.maxVal;
				outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				mapping.source.valueAction_(outVal);
			};
		} {
			"No know the thing: %".format(mapping.destination).vtmwarn(0, thisMethod);
		};
		^result;
	}
}

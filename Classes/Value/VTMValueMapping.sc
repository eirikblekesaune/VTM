VTMValueMapping {
	var <source;
	var <destination;
	var <type = \forwarding;

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
				source = VTMValueMappingSource(properties[\source], this);
			});
			if(properties.includesKey(\destination), {
				destination = VTMValueMappingDestination(properties[\destination], this);
			});
			if(properties.includesKey(\type), {
				type = properties[\type];
			});
		});
	}

	enable{
		if(source.notNil and: {destination.notNil}, {
			switch(type,
				\forwarding, {
					//send the data from the source to the destination
					source.startForwarding;
				},
				\bind, {
					//both source and destination subscribes to eachother
					source.startForwarding;
					source.startObserving;
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
		source.free;
	}

	//the destination value mapped to
	//the source value range
	unmappedValue{
		var result;
		var destinationValueObj = destination.valueObj;
		var sourceValueObj = source.valueObj;
		case
		{destinationValueObj.isKindOf(VTMNumberValue)} {
			var inMin, inMax, outMin, outMax;
			inMin = destinationValueObj.minVal;
			inMax = destinationValueObj.maxVal;
			outMin = sourceValueObj.minVal;
			outMax = sourceValueObj.maxVal;
			result = destinationValueObj.value.linlin(inMin, inMax, outMin, outMax);
		} {
			"No know the thing: %".format(destinationValueObj).vtmwarn(0, thisMethod);
		};
		^result;
	}

	//the source value mapped to
	//the destination value range
	mappedValue{
		var result;
		var destinationValueObj = destination.valueObj;
		var sourceValueObj = source.valueObj;
		case
		{sourceValueObj.isKindOf(VTMNumberValue)} {
			var inMin, inMax, outMin, outMax;
			outMin = destinationValueObj.minVal;
			outMax = destinationValueObj.maxVal;
			inMin = sourceValueObj.minVal;
			inMax = sourceValueObj.maxVal;
			result = sourceValueObj.value.linlin(inMin, inMax, outMin, outMax);
		} {
			"No know the thing: %".format(destinationValueObj).vtmwarn(0, thisMethod);
		};
		^result;
	}

	pushToDestination{
		destination.valueObj.value = this.mappedValue;
	}

	pullFromDestination{
		source.valueObj.value = this.unmappedValue;
	}

	pushToSource{
		this.pullFromDestination;
	}

	pullFromSource{
		this.pushToDestination;
	}
}

VTMValueMappingSource{
	var <valueObj;
	var forwardingFunc;
	var forwardingListener;
	var isForwardingValue = false;

	var destinationListener;
	var listenFunc;
	var isUpdatingListeningValue = false;
	var mapping;

	*new{arg valueObj, mapping;
		^super.new.init(valueObj, mapping);
	}

	init{arg valueObj_, mapping_;
		valueObj = valueObj_;
		mapping = mapping_;
	}

	destination{
		^mapping.destination;
	}

	startForwarding{
		forwardingFunc = this.class.getForwardingFunc(mapping);
		forwardingListener = SimpleController(valueObj).put(\value, {
			| theChanged, whatChanged |
			if(isUpdatingListeningValue.not, {
				isForwardingValue = true;
				forwardingFunc.value(theChanged.value);
				isForwardingValue = false;
			})
		})
	}

	startObserving{
		// "Starting observing: %".format(this.destination).vtmdebug(0, thisMethod);
		case
		{valueObj.isKindOf(VTMNumberValue)} {
			var inMin, inMax, outMin, outMax;
			inMin = this.destination.valueObj.minVal;
			inMax = this.destination.valueObj.maxVal;
			outMin = valueObj.minVal;
			outMax = valueObj.maxVal;
			listenFunc = {arg inVal;
				var outVal;
				outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				if(isForwardingValue.not, {
					isUpdatingListeningValue = true;
					valueObj.valueAction_(outVal);
					isUpdatingListeningValue = false;
				});
			};
		} {
			"No know the thing: %".format(valueObj).vtmwarn(0, thisMethod);
		};

		destinationListener = SimpleController(this.destination.valueObj).put(\value, {|whoChanged, whatChanged|
			// "pysa majore: %".format([whoChanged, whatChanged, val]).vtmdebug(0, thisMethod);
			listenFunc.value(whoChanged.value);
		});
	}

	free{
		if(forwardingListener.notNil, {
			forwardingListener.remove;
		});
		if(destinationListener.notNil, {
			destinationListener.remove;
		});
	}

	*getForwardingFunc{arg mapping;
		var result;

		case
		{mapping.source.valueObj.isKindOf(VTMNumberValue)} {
			result = {arg inVal;
				var outVal;
				var inMin, inMax, outMin, outMax;
				inMin = mapping.source.valueObj.minVal;
				inMax = mapping.source.valueObj.maxVal;
				outMin = mapping.destination.valueObj.minVal;
				outMax = mapping.destination.valueObj.maxVal;
				outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				// "bbb: %".format([outVal]).vtmdebug(0, thisMethod);
				mapping.destination.valueObj.valueAction_(outVal);
			};
		} {
			"No know the thing: %".format(mapping.source.valueObj).vtmwarn(0, thisMethod);
			result = {};
		};
		^result;
	}

}


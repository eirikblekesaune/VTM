VTMMappingSource{
	var <obj;
	var forwardingFunc;
	var forwardingListener;
	var isForwardingValue = false;

	var destinationListener;
	var listenFunc;
	var isUpdatingListeningValue = false;
	var mapping;


	map{arg destination;}

	*new{arg obj, mapping;
		^super.new.init(obj, mapping);
	}

	init{arg obj_, mapping_;
		obj = obj_;
		mapping = mapping_;
	}

	destination{
		^mapping.destination;
	}

	startForwarding{
		// "Setting up forwarding: % -> %".format(this.destination, obj).vtmdebug(0, thisMethod);
		forwardingFunc = this.class.getForwardingFunc(mapping);
		forwardingListener = SimpleController(obj).put(\value, {
			| theChanged, whatChanged, val |
			// "aaa: %".format([theChanged, whatChanged, val]).vtmdebug(0, thisMethod);
			if(isUpdatingListeningValue.not, {
				isForwardingValue = true;
				forwardingFunc.value(val);
				isForwardingValue = false;
			})
		})
	}

	startObserving{
		// "Starting observing: %".format(this.destination).vtmdebug(0, thisMethod);
		case
		{obj.isKindOf(VTMAttribute)} {
			var inMin, inMax, outMin, outMax;
			inMin = this.destination.obj.get(\minVal);
			inMax = this.destination.obj.get(\maxVal);
			outMin = obj.get(\minVal);
			outMax = obj.get(\maxVal);
			listenFunc = {arg inVal;
				var outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				// "listen: %".format(outVal).vtmdebug(0, thisMethod);
				if(isForwardingValue.not, {
					isUpdatingListeningValue = true;
					obj.valueAction_(outVal);
					isUpdatingListeningValue = false;
				});
			};
		} {
			"No know the thing: %".format(obj).vtmwarn(0, thisMethod);
		};

		destinationListener = SimpleController(this.destination.obj).put(\value, {|whoChanged, whatChanged, val|
			// "pysa majore: %".format([whoChanged, whatChanged, val]).vtmdebug(0, thisMethod);
			listenFunc.value(val);
		});
	}

	free{
		if(forwardingListener.notNil, {
			forwardingListener.remove;
		});
	}

	*getForwardingFunc{arg mapping;
		var result;

		case
		{mapping.source.obj.isKindOf(VTMAttribute)} {
			var inMin, inMax, outMin, outMax;
			inMin = mapping.source.obj.get(\minVal);
			inMax = mapping.source.obj.get(\maxVal);
			outMin = mapping.destination.obj.get(\minVal);
			outMax = mapping.destination.obj.get(\maxVal);
			result = {arg inVal;
				var outVal = inVal.linlin(inMin, inMax, outMin, outMax);
				// "bbb: %".format([outVal]).vtmdebug(0, thisMethod);
				mapping.destination.obj.valueAction_(outVal);
			};
		}
		{mapping.source.obj.isKindOf(VTMSignal)} {
			var destValue;
			destValue = mapping.get(\destinationValue);
			if(destValue.notNil, {
				destValue = destValue.value;
				result = {arg inVal;
					// "XXX: %".format(destValue).vtmdebug(0, thisMethod);
					mapping.destination.obj.valueAction_(*destValue);
				}
			}, {
				result = {arg inVal;
					// "YYY: %".format(mapping.source.obj.value).vtmdebug(0, thisMethod);
					mapping.destination.obj.valueAction_(*mapping.source.obj.value);
				};
			});
		} {
			"No know the thing: %".format(mapping.source.obj).vtmwarn(0, thisMethod);
			result = {};
		};
		^result;
	}

}

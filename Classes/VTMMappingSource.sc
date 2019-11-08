VTMMappingSource{
	var <obj;
	var destination;
	var mappingFunc;

	map{arg destination;}

	*new{arg obj;
		^super.new.init(obj);
	}

	init{arg obj_;
		obj = obj_;
	}

	forwardTo{arg destination;
		var inMin, inMax, outMin, outMax;
		inMin = obj.get(\minVal);
		inMax = obj.get(\maxVal);
		outMin = destination.obj.get(\minVal);
		outMax = destination.obj.get(\maxVal);
		mappingFunc = {arg inVal;
			var outVal = inVal.linlin(inMin, inMax, outMin, outMax);
			destination.obj.valueAction_(outVal);
		};
		obj.addDependant(this);
	}

	free{
		obj.removeDependant(this);
	}

	update{arg whoChanged, whatChanged, val;
		if(whatChanged == \value, {
			mappingFunc.value(val);
		});
	}

}

VTMMappingSource{
	var obj;
	var destination;

	map{arg destination;}

	*from{arg obj;
		^super.new.init(obj);
	}

	init{arg obj_;
		obj = obj_;
	}

	forwardTo{arg destination;
		obj.addDependant(this);

	}

	free{
		obj.removeDependant(this);
	}

	update{arg ...args;
		"Got update: %".format(args).vtmdebug(0, thisMethod);
	}

}

VTMMappingSource{
	var obj;
	var destination;

	map{arg destination;}

	*new{arg obj;
		^super.new.init(obj);
	}

	init{arg obj_;
		obj = obj_;
		"AAA".vtmdebug(0, thisMethod);
	}

	forwardTo{arg destination;
		"AAA".vtmdebug(0, thisMethod);
		obj.addDependant(this);
	}

	free{
		obj.removeDependant(this);
	}

	update{arg ...args;
		"AAA".vtmdebug(0, thisMethod);
		"Got update: %".format(args).vtmdebug(0, thisMethod);
	}

}

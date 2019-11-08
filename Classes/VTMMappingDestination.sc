VTMMappingDestination{
	var <obj;

	map{arg source;}

	*from{arg obj;
		^super.new.init(obj);
	}

	init{arg obj_;
		obj = obj_;
	}


}

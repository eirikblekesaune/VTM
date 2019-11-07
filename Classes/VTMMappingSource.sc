VTMMappingSource{
	var obj;

	map{arg destination;}

	*from{arg obj;
		^super.new.init(obj);
	}

	init{arg obj_;
		obj = obj_;
	}

}

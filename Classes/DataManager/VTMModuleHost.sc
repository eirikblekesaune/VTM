VTMModuleHost : VTMComposableContextManager {
	var <factory;

	*dataClass{ ^VTMModule; }
	name{ ^\modules; }

	initModuleHost {
		factory = VTMModuleFactory.new(this);
	}

	free{
		factory.free;
		super.free;
	}

}

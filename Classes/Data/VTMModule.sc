//children may be Module
VTMModule : VTMComposableContext {

	*managerClass{ ^VTMModuleHost; }

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initModule;
	}

	initModule{
	}

	play{arg ...args;
		this.execute(\play, *args);
	} //temp for module definition hackaton

	stop{arg ...args;
		this.execute(\stop, *args);
	} //temp for module definition hackaton

}

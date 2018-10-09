//children may be Module
VTMModule : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMModuleHost; }

	*new{arg name, declaration, definition;
		^super.new(name, declaration, definition).initModule;
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

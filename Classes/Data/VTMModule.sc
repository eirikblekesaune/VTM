//children may be Module
VTMModule : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMModuleHost; }

	*new{| name, declaration, definition |
		^super.new(name, declaration, definition).initModule;
	}

	initModule{
	}

	play{| ...args |
		this.execute(\play, *args);
	} //temp for module definition hackaton

	stop{| ...args |
		this.execute(\stop, *args);
	} //temp for module definition hackaton

}

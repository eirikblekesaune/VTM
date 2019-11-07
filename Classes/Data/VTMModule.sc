VTMModule : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMModuleHost; }

	*new{| name, declaration, manager, definition, onInit|
		^super.new(name, declaration, manager, definition, onInit).initModule;
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

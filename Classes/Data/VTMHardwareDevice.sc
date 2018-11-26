VTMHardwareDevice : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMHardwareSetup; }

	*new{| name, declaration, manager, definition, onInit|
		^super.new(name, declaration, manager, definition, onInit).initHardwareDevice;
	}

	initHardwareDevice{
		"VTMHardwareDevice initialized".vtmdebug(3, thisMethod);
	}
}

VTMHardwareDevice : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMHardwareSetup; }

	*new{arg name, declaration, manager, definition;
		^super.new(name, declaration, manager, definition).initHardwareDevice;
	}

	initHardwareDevice{
		"VTMHardwareDevice initialized".vtmdebug(3, thisMethod);
	}
}

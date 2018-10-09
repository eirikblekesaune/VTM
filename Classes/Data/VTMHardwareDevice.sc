VTMHardwareDevice : VTMComposableContext {
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMHardwareSetup; }

	*new{arg name, declaration, definition;
		^super.new(name, declaration, definition).initHardwareDevice;
	}

	initHardwareDevice{
		"VTMHardwareDevice initialized".vtmdebug(3, thisMethod);
	}
}

VTMSignal : VTMValueControl{
	classvar <isAbstractClass=false;

	emit{arg ...args;
		valueObj.valueAction_(*args);
	}
}

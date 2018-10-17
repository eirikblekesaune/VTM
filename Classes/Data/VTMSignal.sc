VTMSignal : VTMValueControl{
	classvar <isAbstractClass=false;

	emit{| ...args |
		valueObj.valueAction_(*args);
	}
}

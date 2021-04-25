VTMControlPageButton{
	var controlValue;
	var <>inverted;
	*new{|controlValue, inverted|
		^super.new.init(controlValue, inverted);
	}

	init{|cv, inv|
		controlValue = cv;
		inverted = inv ? false;
	}

	handleButtonValue{|val|
		this.subclassResponsibility(thisMethod);
	}
}


VTMControlPageMomentaryButton : VTMControlPageButton {
	handleButtonValue{|val|
		if(val.booleanValue == inverted.not, {
			controlValue.value = val.booleanValue;
		});
	}
}

VTMControlPageToggleButton : VTMControlPageButton {
	handleButtonValue{|val|
		if(val.booleanValue == inverted.not, {
			controlValue.value = controlValue.value.not;
		});
	}
}

VTMControlPageGateButton : VTMControlPageButton {
	handleButtonValue{|val|
		if(inverted, {
			controlValue.value = val.booleanValue.not;
		}, {
			controlValue.value = val.booleanValue;
		});
	}
}

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
		"\tmomentary button value after handling: %".format(controlValue.value).postln;
	}
}

VTMControlPageToggleButton : VTMControlPageButton {
	var <>triggerValue = true;
	handleButtonValue{|val|
		if(val.booleanValue == inverted.not, {
			controlValue.value = controlValue.value.not;
		});
		"\ttoggle button value after handling: %".format(controlValue.value).postln;
	}
}

VTMControlPageGateButton : VTMControlPageButton {
	handleButtonValue{|val|
		if(inverted, {
			controlValue.value = val.booleanValue.not;
		}, {
			controlValue.value = val.booleanValue;
		});
		"\tgate button value after handling: %".format(controlValue.value).postln;
	}
}

VTMControlPageRadioButtons {
	var buttons;
	*new{|buttons|
		^super.new.init(buttons);
	}
	init{|buttons_|
		buttons = buttons_;
	}
	handleButtonValue{|val|
		this.subclassResponsibility(thisMethod);
	}
}

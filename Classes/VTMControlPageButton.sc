VTMControlPageButton{
	var controlValue;
	var <>inverted = false;
	*new{|controlValue, inverted|
		^super.new.init(controlValue, inverted);
	}

	init{|cv, inv|
		controlValue = cv;
		inverted = inv;
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
	var <>triggerValue = true;
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

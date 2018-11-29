VTMControlManager : VTMDataManager{
	*dataClass{
		^VTMControl;
	}

	name{ ^'controls'; }

	trace{arg bool = true;
		items.do({arg item;
			item.trace(bool);
		});
	}

	path{
		if(this.parent.notNil, {
			^this.parent.fullPath;
		});
		^'/';
	}
}

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
}

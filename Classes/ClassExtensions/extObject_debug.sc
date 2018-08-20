//debug levels usage:
//
//1 - runtime info and VTMErrors
//2 - adding/freeing abstract data items.
//3 - dependancy updates
//4 - object initialization
//5 - runtime messages, OSC messages received

+ Object {
	vtmdebug{arg level = 0, aMethod;
		if(VTM.debugLevel >= level, {
			if(VTM.debugFilterFunction.notNil, {
				if(VTM.debugFilterFunction.value(this, level, aMethod), {
					^this; //early return
				});
			});
			this.debug("[VTM debug %] %::%".format(level, aMethod.ownerClass, aMethod.name));
		});
	}
}
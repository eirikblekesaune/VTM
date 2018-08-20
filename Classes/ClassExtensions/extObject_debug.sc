//debug levels usage:
//
//1 - runtime info
//2 - adding/freeing abstract data items.
//3 - dependancy updates
//4 - object initialization
//5 - runtime messages


+ Object {
	vtmdebug{arg level = 0, aMethod;
		if(VTM.debugLevel >= level, {
			this.debug("[VTM debug] %::%".format(aMethod.ownerClass, aMethod.name));
		});
	}
}
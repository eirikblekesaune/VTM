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
			var ownerClass = aMethod.ownerClass;
			var methodName = aMethod.name;
			if(VTM.debugFilterFunction.notNil, {
				if(VTM.debugFilterFunction.value(this, level, aMethod), {
					^this; //early return
				});
			});
			if(ownerClass.isMetaClass, { //if is it a class method
				//turn into class method syntax
				methodName = "*%".format(methodName);
				ownerClass = ownerClass.name.asString.drop(5); //this trick gives us the classmethods classname again
			});
			this.debug("[VTM debug %] %::%".format(level, ownerClass, methodName));
		});
	}
}

//warning levels usage:
//
//1 - non critical warnings

+ Object {
	vtmwarn{arg level = 0, aMethod;
		if(VTM.debugLevel >= level, {
			var ownerClass = aMethod.ownerClass;
			var methodName = aMethod.name;
			if(VTM.debugFilterFunction.notNil, {
				if(VTM.debugFilterFunction.value(this, level, aMethod), {
					^this; //early return
				});
			});
			if(ownerClass.isMetaClass, { //if is it a class method
				//turn into class method syntax
				methodName = "*%".format(methodName);
				ownerClass = ownerClass.name.asString.drop(5); //this trick gives us the classmethods classname again
			});
			"[VTM warning %] %::% %".format(level, ownerClass, methodName, this).warn;
		});
	}
}

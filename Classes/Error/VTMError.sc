VTMError : Error {
	errorString {
		^"VTMERROR: " ++ what
	}
	errorPathString {
		^if(path.isNil) { "" } { "PATH:" + path ++ "\n" }
	}

	throw{
		this.errorString.vtmdebug(1, thisMethod);
		super.throw;
	}
}

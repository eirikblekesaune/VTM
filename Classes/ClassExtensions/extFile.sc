+ File {
	*loadEnvirFromFile{arg pathName;
		var defFunc;
		if(this.exists(pathName).not, {
			VTMError("Filename not found").throw;
		});

		defFunc = thisProcess.interpreter.compileFile(pathName.asAbsolutePath);
		if(defFunc.isNil, { VTMError("Could not compile envir from file").throw; });
		^Environment.make(defFunc);
	}
}

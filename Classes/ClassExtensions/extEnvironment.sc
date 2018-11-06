+ Environment {
	*loadFromFile{arg pathName;
		var defFunc;
		var result;
		if(File.exists(pathName).not, {
			VTMError("Filename not found").throw;
		});

		defFunc = thisProcess.interpreter.compileFile(pathName.asAbsolutePath);
		if(defFunc.isNil, { VTMError("Could not compile envir from file").throw; });
		result = Environment.make(defFunc);
		if(result.isNil or: {result.isKindOf(Environment).not}, {
			^nil;
		});
		^result;
	}
}

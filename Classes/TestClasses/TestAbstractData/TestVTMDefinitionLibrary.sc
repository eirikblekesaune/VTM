TestVTMDefinitionLibrary : TestVTMElement {
	*makeRandomParameter{arg key, params;
		var result;
		result = super.makeRandomParameter(key, params);
		result = switch(key,
			\folderPath, {VTM.vtmPath +/+ 'Classes/TestClasses/DataForTests/Definitions'},
			\includedPaths, { { this.makeRandomPath } ! rrand(1,7)},
			\excludedPaths, { { this.makeRandomPath } ! rrand(1,7)}
		);
		^result;
	}

}

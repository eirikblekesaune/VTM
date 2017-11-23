TestVTMAbstractDataManager : VTMUnitTest {

	*dataClass{ ^this.findTestedClass.dataClass; }

	*dataTestClass{ ^this.findTestClass(this.dataClass);}


	//settings: minItems, maxItems
	//if declaration are defined the settings are overriden, and the
	//declaration array size defines the number of data item declaration
	//that are made.

	*makeRandomDeclaration{arg settings ...args;
		var result;
		var numItems;
		if(settings.isNil, {
			numItems = rrand(1,7);
		}, {
			var minItems = settings[\minItems] ? 1;
			var maxItems = settings[\maxItems] ? 7;
			numItems = rrand(minItems, maxItems);
		});
		result = numItems.collect({arg i;
			[
				[i + 1, {this.makeRandomString}].choose.value,
				this.dataTestClass.makeRandomDeclaration(*args)
			]
		}).flatten;
		^result;
	}
}

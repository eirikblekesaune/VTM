TestVTMContextDefinition : VTMUnitTest {
	*makeRandomDefinitionEnvironment{arg params;
		var result = Environment.new;
		result.use{
			//Make prepare function
			~prepare = {
				"ContextDefinition '%' - prepare".format(~name).postln;
			};
			//Make run function
			~run = {
				"ContextDefinition '%' - run".format(~name).postln;
			};
			//Make free function
			~free = {
				"ContextDefinition '%' - free".format(~name).postln;
			};
			//Make parameter descriptions

			TestVTMAttribute.makeRandomDeclaration;
			TestVTMAttribute.makeRandomDescription;
			TestVTMIntegerValue.makeRandomProperties
			//Make attribute descriptions
			//Make command descriptions
			//Make signal description
			//Make return descriptions
		};
		^result;
	}

	*makeRandomContextDefinition{arg params, context;
		var result;
		var envir = this.makeRandomDefinitionEnvironment(params);
		var defname = this.makeRandomSymbol;


		result = VTMContextDefinition(defname, envir, context);
		^result;
	}
}

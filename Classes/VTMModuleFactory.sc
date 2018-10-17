VTMModuleFactory{
	var <>host;

	*new{| host |
		^super.new.init(host);
	}

	init{| host_ |
		host = host_;
	}

	build{| definition, declaration |
		var moduleDefinition;
		var newModule;

		//Load module definition from file if not defined in arg
		if(definition.isNil, {
			var defName;
			//Module declaration must define a def name, if not defined in arg
			if(declaration.includesKey(\definition).not, {
				VTMError("Module declaration for '%' is missing module definition name".format(declaration[\name])).throw;
				^nil;
			});

			//Load def from file
			defName = declaration[\definition];
			//Search for mod def file
			moduleDefinition = this.host.getDefinition(defName);

			if(moduleDefinition.isNil) {
				VTMError(
					"Could not find module definition for '%'".format(
						defName
					)
				).throw;
				^nil;
			};

			// TODO: Change this to compile on runtime.
			// // Check if module definition compiles
			// try{
			// 	moduleDefinition = Environment.make(moduleDefinition);
			// } {|err|
			// 	VTMError(
			// 		"Could not compile module definition '%'".format(
			// 			defName
			// 		)
			// 	).throw;
			// 	^nil;
			// };
			//
		}, {
			moduleDefinition = definition;
		});

		newModule = VTMModule.new(declaration[\name], moduleDefinition, declaration, host);
		^newModule;

	}

	loadModuleDefinitionFromFile{| filepath |

	}

	moduleDefinitionsFolder{
		^host.node.getFilePathFor(\moduleDefinition);
	}

	*isDeclarationForRemoteModule{| desc |
		^desc.includesKey(\app);
	}

	*isDeclarationForExistingModule{| desc |
		^desc.includesKey(\path);
	}
}

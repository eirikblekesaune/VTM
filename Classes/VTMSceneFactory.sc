VTMSceneFactory{
	var <sceneOwner;

	*new{| sceneOwner |
		^super.new.init(sceneOwner);
	}

	init{| sceneOwner_ |
		sceneOwner = sceneOwner_;
	}

	moduleHost{
		^sceneOwner.application.moduleHost;
	}

	hardwareSetup{
		^sceneOwner.application.hardwareSetup;
	}

	build{| sceneDefinition, sceneDeclaration |
		var newScene, buildResult;
		var modules = (
			local: (static: [], dynamic: []),
			remote: (static: [], dynamic: [])
		);
		buildResult = (
			dependancies: (
				remote: [],
				local: []
			)
			success: false,
			errors: []
		);
		//>Check if scene declaration contains a name. Issue error if not.
		if(sceneDeclaration.includesKey(\name).not, {
			buildResult[\errors] = buildResult[\error].add(VTMError("Scene declaration must have name"));
		});

		//>determine the build order by searching the scene declaration for references
		//to other modules and find a resolving build order.
		//>if finding a build order fails
		//	>then throw build error.

		//>Find out if the scene declaration has sub scenes.
		//>For all sub scenes
		//	>if this context overrides any values for the subscene
		//		>then overwrite the overriden values in the subscene declaration

		//if scene declaration has any module declaration
		if(sceneDeclaration.includesKey(\modules), {
			//>Separate local and remote modules.
			//Determine if they are existing or dynamic.
			//An existing module is referred to using the 'path' keyword. This indicates
			//that the module already exists. An existing module may be either static
			//or dynamic.

			//>For all remote scenes
			//	>contact the host
			//	>if the host responds
			//		>then send a remote hosting scene cue to the hosts.
			//			>if no response throw build error

			//>Separate already existing modules and new modules to be created.
			//Modules that are already existing on an application will be hosted by the apps module host.
			//Otherwise they will be hosted by its scene, or scene proxy
			//>For each existing local module
			//	>send scene ownership message to module
			//	>if module host replies with 'module busy/reserved'
			//		>then throw build error
			//>For each new local module to be created
			//	>send module cue to local module host
			//	>module host replies with a build result, stating the dependancies for that module.

		});


		newScene = VTMScene.new(sceneDeclaration[\name], sceneDefinition, sceneDeclaration, sceneOwner);
		buildResult.put(\scene, newScene);
		^buildResult;
	}

}

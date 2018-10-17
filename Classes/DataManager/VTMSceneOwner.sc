VTMSceneOwner : VTMContextManager {
	var <sceneFactory;
	*dataClass{ ^VTMScene; }
	name{ ^\scenes; }

	initSceneOwner{
		sceneFactory = VTMSceneFactory.new(this);
	}

	free{
		sceneFactory.free;
		super.free;
	}

	addScene{| newScene |
	}

	loadSceneCue{| cue |
		var newScene;
		try{
			newScene = sceneFactory.build(cue);
		} {|err|
			"Scene cue build error".warn;
			err.throw;
		};
		this.addScene(newScene);
	}
}

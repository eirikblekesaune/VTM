TestVTMLocalNetworkNode : TestVTMAbstractDataManager {

	test_singleton{
		var aa, bb;
		aa = VTMLocalNetworkNode.new;
		bb = VTMLocalNetworkNode.new;
		this.assert(aa === bb,
			"Constructor returned singleton object"
		);
	}

	test_autoAddUnmanagedContexts{
		var obj, path;

		//Test Module
		path = this.class.makeRandomSymbol;
		obj = VTMModule.new(path);
		this.assert(
			obj === VTM.local.moduleHost[path],
			"Unmanaged Module is managed by local network node object."
		);
		obj.free;
		this.assert(
			VTM.local.moduleHost.items.isEmpty,
			"Freeing umanaged Module removed it from local network node object."
		);

		//Test Scene
		path = this.class.makeRandomSymbol;
		obj = VTMScene.new(path);
		this.assert(
			obj === VTM.local.sceneOwner[path],
			"Unmanaged Scene is managed by local network node object."
		);
		obj.free;
		this.assert(
			VTM.local.sceneOwner.items.isEmpty,
			"Freeing umanaged Scene removed it from local network node object."
		);

		//Test HardwareDevice
		path = this.class.makeRandomSymbol;
		obj = VTMHardwareDevice.new(path);
		this.assert(
			obj === VTM.local.hardwareSetup[path],
			"Unmanaged HardwareDevice is managed by local network node object."
		);
		obj.free;
		this.assert(
			VTM.local.hardwareSetup.items.isEmpty,
			"Freeing umanaged HardwareDevice removed it from local network node object."
		);

		//Test Score
		path = this.class.makeRandomSymbol;
		obj = VTMScore.new(path);
		this.assert(
			obj === VTM.local.scoreManager[path],
			"Unmanaged Score is managed by local network node object."
		);
		obj.free;
		this.assert(
			VTM.local.scoreManager.items.isEmpty,
			"Freeing umanaged Score removed it from local network node object."
		);
	}

	test_removeComposableContextShouldAddItToLocal{
		var testManager;
		var testContext;
	}
}

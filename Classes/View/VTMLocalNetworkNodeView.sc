VTMLocalNetworkNodeView : VTMView{
	var applicationManagerView;
	var hardwareSetupView;
	var moduleHostView;
	var sceneOwnerView;
	var scoreManagerView;
	var networkNodeManagerView;


	prMakeChildViews{
		applicationManagerView = model.applicationManager.makeView;
		hardwareSetupView = model.hardwareSetup.makeView;
		moduleHostView = model.moduleHost.makeView;
		sceneOwnerView = model.sceneOwner.makeView;
		scoreManagerView = model.scoreManager.makeView;
		networkNodeManagerView = model.networkNodeManager.makeView;
	}

	prMakeLayout{
		^VLayout(
			applicationManagerView,
			hardwareSetupView,
			moduleHostView,
			sceneOwnerView,
			scoreManagerView,
			networkNodeManagerView,
			nil
		);
	}
}

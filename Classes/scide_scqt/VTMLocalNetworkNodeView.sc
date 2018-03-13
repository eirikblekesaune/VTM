VTMLocalNetworkNodeView : VTMView{

	prMakeLayout{
		^VLayout(
			model.applicationManager.makeView,
			model.hardwareSetup.makeView,
			model.moduleHost.makeView,
			model.sceneOwner.makeView,
			model.scoreManager.makeView,
			model.networkNodeManager.makeView,
			nil
		);
	}
}
VTMApplicationView : VTMView {
	var application;
	var showContentButtons, shownInContentView;
	var stageView, definitionsView, menuView, contextView, appNameView;
	var headerView, contentView, bottomView, statusView;
	var sceneOwnerView, moduleHostView, hardwareSetupView, networkView;

	*new{arg parent, bounds, application, definition, settings;
		^super.new(parent, bounds, definition, settings).initApplicationView(application);
	}

	initApplicationView{arg application_;
		application = application_;
		//make views
		appNameView = View()
		.layout_(HLayout(StaticText().string_("aaa")).spacing_(3).margins_(3))
		.background_(Color.cyan);

		menuView = View()
		.layout_(HLayout(StaticText().string_("menuView")).spacing_(3).margins_(3))
		.background_(Color(0.0, 0.1, 1.0));

		stageView = View()
		.layout_(HLayout(StaticText().string_("stageView")).spacing_(3).margins_(3))
		.background_(Color(0.0, 1.0, 0.2));

		contextView = View()
		.background_(Color(0.0, 0.1, 1.0));

		definitionsView = View()
		.layout_(HLayout(StaticText().string_("definitionsView")).spacing_(3).margins_(3))
		.background_(Color(0.5, 0.5, 0.1));

		sceneOwnerView = VTMSceneOwnerView(this, nil, application.sceneOwner)
		.background_(Color(1.0, 0.0, 0.0));

		moduleHostView = VTMModuleHostView(this, nil, application.moduleHost)
		.background_(Color(0.0, 0.0, 1.0));

		hardwareSetupView = VTMHardwareSetupView(this, nil, application.hardwareSetup)
		.background_(Color(0.1, 0.1, 0.7));

		networkView = VTMNetworkView(this, nil, application.network)
		.background_(Color(0.0, 0.0, 0.3));

		statusView = View().layout_(VLayout(StaticText().string_("STATUS")));

		headerView = View().layout_(
			HLayout(
					[appNameView, align: \topLeft],
					[menuView, align: \topLeft]
			).spacing_(3).margins_(3)
		);

		contentView = View().layout_(HLayout(
			[contextView.maxWidth_(155), align: \leftTop],
			[stageView, align: \center],
			[definitionsView.fixedWidth_(150), align: \rightTop]
		).spacing_(3).margins_(3));

		bottomView = View().layout_(
			VLayout(
				[statusView, align: \right]
			).spacing_(3).margins_(3)
		);

		contextView.layout_(
			VLayout(
				[sceneOwnerView],
				[moduleHostView],
				[hardwareSetupView],
				[networkView]
			).spacing_(3).margins_(3)
		);

		this.layout_(
			VLayout(
				[headerView.maxHeight_(25)],
				[contentView],
				[bottomView.maxHeight_(25)]
				/*[bottomView, align: \bottom]*/
			).spacing_(3).margins_(3)
		);
		this.layout.spacing_(3).margins_(3);

	}

	showInStageView{arg val;

	}
}

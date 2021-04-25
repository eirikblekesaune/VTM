VTMControlPage {
	var manager;
	var <controlValues;
	var <mappedScene;
	var >viewBuilder;
	var controlMappings;
	var <buttons;

	*new{| manager, pageSetup |
		^super.new.initControlPage(manager, pageSetup);
	}

	initControlPage{|manager_, pageSetup|
		manager = manager_;
		pageSetup = VTMOrderedIdentityDictionary.newFromAssociationArray(pageSetup, true);
		controlValues = VTMOrderedIdentityDictionary.new;
		buttons = IdentityDictionary.new;
		pageSetup.keysValuesDo({|ctrlKey, ctrlDesc|
			var cv = VTMValue.makeFromProperties(ctrlDesc);
			controlValues.put( ctrlKey, cv );
			if(ctrlKey.asString.contains("button"), {
				buttons.put(ctrlKey,
					VTMControlPageGateButton.new(cv)
				);
			});
		});
	}

	mapToScene{|scene|
		if(mappedScene.notNil, {
			this.unmapFromScene(mappedScene);
		});
		if(scene.isKindOf(VTMScene), {
			scene.changed(\willMapToControlPage, this);

			mappedScene = scene;
			this.prSetupSceneMappings;
			mappedScene.addDependant(this);
			this.changed(\mappedScene, scene);
		});
	}

	unmapFromScene{|scene|
		if(mappedScene.notNil and: {mappedScene === scene}, {
			mappedScene.removeDependant(this);
			mappedScene = nil;
			controlMappings.do(_.free);
			buttons.keys.do({|k|
				this.setButtonType(k, \gate);
			});
			this.changed(\unmappedScene, scene);
		});
	}

	setButtonType{|buttonKey, buttonType, inverted=false|
		var buttonClass;
		if(buttons.includesKey(buttonKey), {
		}, {
			"Unknown button key '%'".format(buttonKey).warn;
		});
		buttonClass = switch(buttonType,
			\gate, VTMControlPageGateButton,
			\toggle, VTMControlPageToggleButton,
			\momentary, VTMControlPageMomentaryButton
		);
		if(buttonClass.notNil, {
			buttons.put(
				buttonKey, 
				buttonClass.new(controlValues[buttonKey], inverted)
			);
			this.changed(\buttonType, buttonKey, buttonType);
		}, {
			"Uknown button type: '%'".format(buttonType).warn;
		});
	}

	getButtonType{|buttonKey|
		var result;
		if(buttons.includesKey(buttonKey), {
			var buttonClass = buttons[buttonKey].class;
			buttonClass = switch(buttonClass,
				VTMControlPageGateButton, \gate,
				VTMControlPageToggleButton,\toggle,
				VTMControlPageMomentaryButton, \momentary
			);
		});
		^result;
	}

	prSetupSceneMappings{
		try{
			controlMappings = this.prParseControlMappings(
				mappedScene
			);
			controlMappings.do(_.pushToSource);
			controlMappings.do(_.enable);
		} {|err|
			err.errorString.postln;
			err.dumpBackTrace;
			err.throw;
		}
	}

	isMapped{
		^mappedScene.notNil;
	}

	update{|theChanged, whatChanged ...args|
		if(this.isMapped and: {mappedScene === theChanged}, {
			if(whatChanged == \freed, {
				this.unmapFromScene(theChanged);
			});
			if(whatChanged == \willMapToControlPage 
				and: {args.first !== this}, {
					//If the mapped scene is about to be
					//mapped to another scene
					//we unmap from this control page
					this.unmapFromScene(theChanged);
				});
		});
	}

	makeView{|parent, bounds, viewSettings|
		var result;
		if(viewBuilder.notNil, {
			result = viewBuilder.value(
				parent, bounds, viewSettings,
				this
			);
		});
		^result;
	}

	handleButtonValue{|buttonKey, val|
		if(buttons.includesKey(buttonKey), {
			buttons[buttonKey].handleButtonValue(val);
		});
	}

	prParseControlMappings{|scene|
		var result;
		var sceneControlMappings = scene.controlMappings;
		scene.controlMappings.keysValuesDo({|ctrlKey, mappingDesc|
			var cpCv;
			var sceneCvKey, sceneCv;
			var mapping;
			var type;
			cpCv = controlValues[ctrlKey];
			if(cpCv.isNil, {
				Error("Could not find controlValue cv: '%'".format(ctrlKey)).throw;
			});

			sceneCvKey = mappingDesc.atFail(\destination, {
				Error("Mapping destination not defined").throw;
			});
			sceneCv = scene.find(VTMPath(sceneCvKey));
			if(sceneCv.isNil, {
				Error("Could not find scene cv: '%'".format(sceneCvKey)).throw;
			});
			sceneCv = sceneCv.valueObj;
			
			type = mappingDesc[\type];
			mapping = switch(type,
				\button, {
					this.prMakeButtonMapping(
						cpCv, sceneCv, ctrlKey,
						mappingDesc, sceneCvKey
					);
				},
				{
					VTMValueMapping((
						source: cpCv,
						destination: sceneCv,
						type: \bind
					));
				}
			);
			result = result.add(mapping);
		});
		^result;
	}

	prMakeButtonMapping{|cpCv, sceneCv, cpCvKey, mappingDesc, sceneCvKey|
		var result;
		var type = mappingDesc[\type] ? \gate;
		var val = mappingDesc[\value];
		result = VTMValueMapping((
			source: cpCv,
			destination: sceneCv,
			type: \forwarding,
			mapFunc: {|v, m|
				val ? v;
			}
		));
		^result;
	}

	*prDefaultControlPageSetup{|numChannels|
		var pageSetup;
		pageSetup = pageSetup.add(
			{|i| "fader.%".format(i+1).asSymbol -> (
				type: \decimal, minVal: 0.0, maxVal: 1.0, clipmode: 'both'
			) } ! numChannels
		);
		pageSetup = pageSetup.add(
			{|i| "knob.%".format(i+1).asSymbol -> (
				type: \decimal, minVal: 0.0, maxVal: 1.0, clipmode: 'both'
			) } ! numChannels
		);
		pageSetup = pageSetup.add(
			{|i| ['A', 'B', 'C'].collect({|buttonKey|
				"button.%/%".format(i+1, buttonKey).asSymbol -> (
					type: \boolean
				);
			}); } ! numChannels
		);
		pageSetup = pageSetup.flat;
		^pageSetup;
	}

	*prDefaulControlPageViewBuilder{|numChannels, pageName, controlPage|
		^{|parent, bounds, viewSettings, page|
			var v = View();
			var channelViews;
			var listeners;
			var headerView, updateHeaderView;
			numChannels.do({|i|
				var channelNum = i + 1;
				var makeKnobView = {
					var knobKey = "knob.%".format(channelNum).asSymbol;
					var cv = page.controlValues[knobKey];
					var knobView = Knob()
					.action_({|k| 
						var val = k.value;
						cv.value_(val);
					});
					listeners = listeners.add(
						SimpleController(cv).put(\value, {
							{ knobView.value_(cv.value) }.defer;
						});
					);
					knobView;
				};
				var makeButtonViews = {
					[\A, \B, \C].collect({|buttonLetter|
						var buttonKey = "button.%/%".format(
							channelNum, buttonLetter
						).asSymbol;
						var cv = page.controlValues[buttonKey];
						var buttonView = Button()
						.states_([[buttonLetter], [buttonLetter, nil, Color.red]])
						.action_({|k| 
							var val = k.value;
							controlPage.handleButtonValue(val);
						});
						listeners = listeners.add(
							SimpleController(cv).put(\value, {
								{ buttonView.value_(cv.value) }.defer;
							});
						);
						listeners = listeners.add(
							SimpleController(controlPage).put(\buttonType, {|...args|
								var key = args[2];
								if(key == buttonKey, {
									var type = args[3];
									switch(
										\momentary, {
											{
												buttonView.states_([
													[buttonLetter, nil, Color.yellow]
												]);
											}.defer;
										},
										\gate, {
											{
												buttonView.states_([
													[buttonLetter, nil, Color.orange]
													[buttonLetter, nil, Color.cyan]
												]);
											}.defer;
										},
										\toggle, {
											{
												buttonView.states_([
													[buttonLetter],
													[buttonLetter, nil, Color.red]
												]);
											}.defer;
										}
									);
								});
							})
						);
						buttonView;
					});
				};
				var makeFaderView = {
					var faderKey = "fader.%".format(channelNum).asSymbol;
					var cv = page.controlValues[faderKey];
					var faderView = Slider()
					.action_({|k| 
						var val = k.value;
						cv.value_(val);
					});
					listeners = listeners.add(
						SimpleController(cv).put(\value, {
							{ faderView.value_(cv.value) }.defer;
						});
					);
					faderView.minHeight_(200);
				};
				channelViews = channelViews.add(
					VLayout(
						StaticText().string_(channelNum),
						makeKnobView.value,
						VLayout(*makeButtonViews.value),
						makeFaderView.value
					)
				);
			});
			headerView = StaticText().string_("Control Page %".format(pageName ? ""));
			updateHeaderView = {
				var str = "Control Page %".format(pageName ? "");
				if(controlPage.isMapped, {
					str = str ++ " - Mapped to scene '%'".format(controlPage.mappedScene.fullPath);
				});
				headerView.string_(str);
				headerView.refresh;
			};
			listeners = listeners.add(
				SimpleController(controlPage)
				.put(\mappedScene, { {updateHeaderView.value}.defer; })
				.put(\unmappedScene, { {updateHeaderView.value}.defer; })
			);
			v.layout_(VLayout(
				headerView.maxHeight_(50),
				HLayout(*channelViews)
			));
			v.onClose = {
				listeners.do(_.remove);
			};
			v;
		}
	}

}

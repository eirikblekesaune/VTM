VTMApplication : VTMContext {
	var <scenes;
	var <modules;
	var <hardwareDevices;
	var <definitionLibrary;
	classvar <isAbstractClass=false;

	*managerClass{ ^VTMApplicationManager; }

	*new{| name, declaration, manager, definition, onInit|
		^super.new(name, declaration, manager, definition, onInit).initApplication;
	}

	initApplication{
		var defPaths;
		if(declaration.includesKey(\definitionPaths), {
			defPaths = declaration[\definitionPaths];
		});

		//always add the local path for the application definition path
		if(definition.filepath.notNil, {
			var appDefFolder = "%Definitions".format(PathName(definition.filepath).pathOnly);
			if(File.exists(appDefFolder), {
				defPaths = defPaths.add(appDefFolder);
			}, {
				"Did not find definitions folder '%' for application: %".format(appDefFolder, name).vtmwarn(1, thisMethod);
			});
		}, {
			"Did not find folder app defintion: %".format(name).vtmwarn(1, thisMethod);
		});
		definitionLibrary = VTMDefinitionLibrary.new(defPaths, this);

		hardwareDevices = VTMHardwareSetup(this);
		modules = VTMModuleHost(this);
		scenes = VTMSceneOwner(this);
		this.registerChild(hardwareDevices);
		this.registerChild(modules);
		this.registerChild(scenes);
		this.on(\didInit, {
			this.makeComponents;
		});
	}

	makeComponents{
		var meth = thisMethod; //for the vtmwarning method in the catch block
		[
			[\hardwareDevices, hardwareDevices],
			[\modules, modules],
			[\scenes, scenes]
		].do({arg args;
			var compKey, comp;
			#compKey, comp = args;
			if(definition.includesKey(compKey), {
				var overrideDeclaration, ignored = false;
				var compDeclarations;
				compDeclarations = definition.perform(compKey);
				if(compDeclarations.notNil, {
					compDeclarations.keysValuesDo({arg itemName, itemDeclaration;
						var makeComponent = {|iName, iDecl|
							var newItem;
							try{
								newItem = comp.makeItemFromDeclaration(
									iName, iDecl
								);
								"ADDDING: % to %".format(
									newItem, comp
								).vtmdebug(2, thisMethod);
								comp.addItem(newItem);
							} {|err|
								"Failed making component named: % with declaration: %".format(
									iName, iDecl
								).vtmwarn(0, meth);
								err.errorString.vtmdebug(1, thisMethod);
							}
						};
						//You can override the settings from the definition file with 
						//settings from the declaration.
						if(declaration.includesKey(compKey), {
							if(declaration[compKey].includesKey(itemName), {
								itemDeclaration.putAll(declaration[compKey][itemName]);
								ignored = itemDeclaration.atFail(\ignored, ignored);
							})
						});
						if(ignored.not, {
							//check if comp name is in expansion format
							if( "^.+\.\{.+\}$".matchRegexp(itemName.asString), {
								var items = itemName.asString.expandNumberingPostfix;
								var args = itemDeclaration['args'];
								//we parse the expansion here.
								items.do({|subItem|
									var name, subName, subDecl;
									#name, subName = subItem.split($.).collect(_.asSymbol);
									subDecl = itemDeclaration.deepCopy.reject({|v,k|
										[\args].matchItem(k);
									});
									//The declaration can optionally have an 'args' value
									//with individual declaration values for each component.
									//This arg value should be stored by the subname.
									if(args.notNil and: {args.includesKey(subName)}, {
										subDecl.putAll(args[subName]);
									});
									makeComponent.value(subItem, subDecl);
								});
							}, {
								var item;
								makeComponent.value(itemName, itemDeclaration);
							});
						});
					});
				});
			});
		});
	}

	components{
		^[modules, hardwareDevices, scenes].collect(_.items).flat;
	}

	findDefinition{arg defName;
		^definitionLibrary.findDefinition(defName);
	}

	free{| condition, action |
		forkIfNeeded{
			var cond = condition ?? {Condition.new};
			this.components.do({arg comp;
				comp.free(condition, action);
			});
			super.free(condition, action);
		}
	}

	//to make it act as a manager
	addItem{|comp|
		var compManager;
		compManager = case
		{comp.isKindOf(VTMScene)} { scenes; }
		{comp.isKindOf(VTMHardwareDevice)} { hardwareDevices; }
		{comp.isKindOf(VTMModule)} { modules; }
		{
			Error("Unknown component type: %[%]".format(comp, comp.class)).throw;
		};
		compManager.addItem(comp);
	}
}

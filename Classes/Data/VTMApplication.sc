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
			"Making components: %".format(compKey).vtmdebug(2, thisMethod);
			if(declaration.includesKey(compKey), {
				declaration[compKey].keysValuesDo({arg itemName, itemDeclaration;
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
						makeComponent.value(itemName, itemDeclaration);
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

	*childKeys{
		^#[':scenes', ':hardwareDevices', ':modules'];
	}

	hasChildKey{arg key;
		if(this.class.childKeys.includes(key), {
			^true;
		}, {
			^super.hasChildKey(key);
		});
	}

	getChild{arg key;
		var result;
		switch(key,
			':scenes', {^scenes},
			':modules', {^modules},
			':hardwareDevices', {^hardwareDevices}
		);
		result = super.getChild(key);
		^result;
	}

}

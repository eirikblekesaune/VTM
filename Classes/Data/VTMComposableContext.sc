/*
A ComposableContext is something that can manage instances of its own kind.
e.g a Module can submodules.
*/
VTMComposableContext : VTMContext {
	var <subContexts;

	*new{| name, declaration, manager, definition, onInit|
		^super.new(name, declaration, manager, definition, onInit).initComposableContext;
	}

	initComposableContext{
		//TODO: init subContexts here
		subContexts = this.class.managerClass.new(this);
		this.loadSubcontexts;
	}

	loadSubcontexts{
		var meth = thisMethod;
		var compKey = this.contextTypeSymbol.asString.add($s).asSymbol;
		if(definition.includesKey(compKey), {
			var overrideDeclaration, ignored = false;
			var compDeclarations;
			compDeclarations = definition.perform(compKey);
			if(compDeclarations.notNil, {
				compDeclarations.keysValuesDo({arg itemName, itemDeclaration;
					var makeComponent = {|iName, iDecl|
						var newItem;
						try{
							newItem = subContexts.makeItemFromDeclaration(
								iName, iDecl
							);
							"ADDDING: % to %".format(
								newItem, subContexts
							).vtmdebug(2, thisMethod);
							this.addItem(newItem);
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
	}

	free{
		subContexts.do(_.free);
		super.free;
	}

	isSubcontext{
		if(manager.notNil, {
			^this.manager.isKindOf(this.class);
		});
		^false;
	}

	path{
		if(this.isSubcontext, {
			^this.parent.fullPath;
		}, {
			^super.path;
		})
	}

	leadingSeparator{
		if(this.isSubcontext,
			{
				^'.';
			}, {
				^'/'
			}
		);
	}

	addItem{|comp|
		subContexts.addItem(comp);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\exclusivelyOwned -> (type: \boolean, defaultValue: true)
		]);
	}

	*controlDescriptions{
		^super.controlDescriptions.putAll(VTMOrderedIdentityDictionary[
			\takeOwnership -> (type: \string, mode: \command), //which type to describe scene or application here?
			\releaseOwnership -> (type: \string, mode: \command),
			\owner -> (type: \string, mode: \return)
		]);
	}
}

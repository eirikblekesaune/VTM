VTMComposableContext : VTMContext {
	var <children;

	*new{arg name, declaration, manager, definition;
		//If no manager defined, use the local network node as manager.
		//TODO?: Will there be problems when a class is listed as manager
		//for multiple type of objects, in the case of Context/LocalNetworkNode?
		manager = manager ? VTM.local.findManagerForContextClass(this);

		^super.new(name, declaration, manager, definition).initComposableContext;
	}

	initComposableContext{
		//TODO: init children here
	}

	free{
		children.do(_.free);
		super.free;
	}

	isSubcontext{
		if(manager.notNil, {
			^this.manager.isKindOf(this.class);
		});
		^false;
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

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\exclusivelyOwned -> (type: \boolean, defaultValue: true)
		]);
	}

	*commandDescriptions{
		^super.commandDescriptions.putAll(VTMOrderedIdentityDictionary[
			\takeOwnership -> (type: \string), //which type to describe scene or application here?
			\releaseOwnership -> (type: \string)
		]);
	}

	*returnDescriptions{
		^super.returnDescriptions.putAll(VTMOrderedIdentityDictionary[
			\owner -> (type: \string)
		]);
	}
}

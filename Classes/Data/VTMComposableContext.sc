/*
A ComposableContext is something that can manage instances of its own kind.
e.g a Module can submodules.
*/
VTMComposableContext : VTMContext {
	var <children;

	*new{arg name, declaration, manager, definition;

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

	*controlDescriptions{
		^super.controlDescriptions.putAll(VTMOrderedIdentityDictionary[
			\takeOwnership -> (type: \string, mode: \command), //which type to describe scene or application here?
			\releaseOwnership -> (type: \string, mode: \command),
			\owner -> (type: \string, mode: \return)
		]);
	}
}

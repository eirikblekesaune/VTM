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

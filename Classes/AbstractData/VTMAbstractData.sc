VTMAbstractData{
	var <name;
	var attributes;
	var <manager;
	var attributeGetterFunctionsThunk;
	var attributeSetterFunctionsThunk;
	classvar viewClassSymbol = \VTMAbstractDataView;

	*managerClass{
		^this.subclassResponsibility(thisMethod);
	}

	*newFromAttributes{arg attributes;
		var name, manager, attr;
		attr = attributes.deepCopy;
		name = attr.removeAt(\name);
		manager = attr.removeAt(\manager);
		^this.new(name, attr, manager);
	}

	*new{arg name, attributes, manager;
		^super.new.initAbstractData(name, attributes, manager);
	}

	initAbstractData{arg name_, attributes_, manager_;
		name = name_;
		manager = manager_;
		// attributes = VTMAttributeList.new(attributes_);
		if(attributes_.isNil, {
			attributes = IdentityDictionary.new;
		}, {
			attributes = attributes_.deepCopy;
		});

		//lazy attributesGetters and setters
		attributeGetterFunctionsThunk = Thunk({
			this.class.makeAttributeGetterFunctions(this);
		});
		attributeSetterFunctionsThunk = Thunk({
			this.class.makeAttributeSetterFunctions(this);
		});
	}

	free{
		attributes.free;
		this.releaseDependants;
		attributes = nil;
		manager = nil;
	}

	prComponents{ ^nil; }

	*attributeKeys{
		^[\name];
	}

	attributes{
		var result;
		result = IdentityDictionary.new;
		this.class.attributeKeys.do({arg attrKey;
			var val;
			val = this.attributeGetterFunctions[attrKey].value;
			result.put(
				attrKey,
				val
			);
		});
		^result;
	}

	attributeGetterFunctions{
		^attributeGetterFunctionsThunk.value;
	}

	attributeSetterFunctions{
		^attributeSetterFunctionsThunk.value;
	}

	*makeAttributeGetterFunctions{arg data;
		this.subclassResponsibility(thisMethod);
	}

	*makeAttributeSetterFunctions{arg param;
		this.subclassResponsibility(thisMethod);
	}

	makeView{arg parent, bounds, definition, attributes;
		var viewClass = this.class.viewClassSymbol.asClass;
		//override class if defined in attributes.
		if(attributes.notNil, {
			if(attributes.includesKey(\viewClass), {
				viewClass = attributes[\viewClass];
			});
		});
		^viewClass.new(parent, bounds, definition, attributes, this);
	}
}
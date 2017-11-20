VTMElementComponent : VTMAbstractDataManager{
	var <element;

	*new{arg itemDeclaration, element;
		^super.new(itemDeclaration).initElementComponent(element);
	}

	initElementComponent{arg element_;
		element = element_;
	}
}

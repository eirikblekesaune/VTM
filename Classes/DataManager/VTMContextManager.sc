VTMContextManager : VTMDataManager {

	context{
		^parent;
	}

	free{
		super.free;
	}

	path{
		if(this.context.notNil, {
			if(this.context.isKindOf(VTMContext), {
				^this.context.fullPath;
			}, {
				"/%".format(this.context).asSymbol;
			});
		});
		^'/';
	}

	addItemFromDeclaration{arg itemName, itemDeclaration;
		var meth = thisMethod; //for getting the method ref to the error throw below
		var newItem;
		var itemDefinition;
		try{
			case 
			{itemDeclaration.includesKey(\definition)} {
				var defName = itemDeclaration[\definition].asSymbol;
				"context: % %".format(this.context.fullPath, 
					this.context.class).vtmdebug(2, thisMethod);
				itemDefinition = this.context.findDefinition(defName);
				if(itemDefinition.isNil, {
					VTMContextDefinitionError("Context definition '%' not found in context: '%'".format(
						defName, this.context.fullPath
					)).throw;
				});
				"Definition name: %[%]".format(
					defName, defName.class
				).vtmdebug(2, thisMethod);
			}
			{itemDeclaration.includesKey(\definitionPath)} {
				var defName;
				"Tryinging to load definition path: %".format(
					itemDeclaration[\definitionPath]
				).vtmdebug(2, thisMethod);
				itemDefinition = VTMContextDefinition.newFromFile(
					itemDeclaration[\definitionPath].resolveRelative
				);
			}
			{
				"No definition for item: %".format(
					itemDeclaration
				).vtmdebug(2, thisMethod);
				VTMContextDefinitionError("No definition for: '%' in context".format(
					itemName, this.context.fullPath
				)).throw;
			};
			"Definition: %".format(itemDefinition).vtmdebug(2, thisMethod);
			//Check if any of the declaration keys are functions.
			//If they are, they are meant to be evaluated in the context they were defined
			//in.
			itemDeclaration.keysValuesChange({|k,v|
				if(v.isKindOf(Function), {
					v = this.parent.envir.use{ v.value(this.parent);};
				});
				v;
			});
			newItem = this.class.dataClass.new(
				name: itemName,
				declaration: itemDeclaration,
				definition: itemDefinition,
				manager: this
			);
		} {|err|
			var errString = "Failed to build item named: % with declaration: %".format(
				itemName, itemDeclaration
			);
			errString = errString ++ "\n\tError string: %".format(err.errorString);
			VTMError(errString).throw;
		};
		^newItem;
	}

	addItem{arg newItem;
		var itemAction;
		super.addItem(newItem);
		if(newItem.isKindOf(VTMControl), {
			//The action is a part of the Controls declaration
			//but only relevant to the Element it is declared in.
			//Therefor we extract the action here and wrap it into a function
			//that also includes the context as the second argument.
			itemAction = newItem.action;
			if(itemAction.notNil, {
				//If this object is in a context we bind the item action
				//to the context environment so that environment variables
				//can be used inside the defined action.
				if(this.context.notNil, {
					itemAction = this.context.prContextualizeFunction(itemAction);
				});
				newItem.action_({| item |
					itemAction.value(newItem, this.context);
				});
			});
		});
	}
}

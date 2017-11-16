VTMContextComponent : VTMElementComponent {
	var <context;

	*new{arg itemDeclarations, context;
		^super.new(itemDeclarations).initContextComponent(context);
	}

	initContextComponent{arg context_;
		context = context_;
	}

	free{
		super.free;
		context = nil;
	}

	path{
		if(context.notNil, {
			if(context.isKindOf(VTMContext), {
				^context.fullPath;
			}, {
				"/%".format(context).asSymbol;
			});
		});
		^'/';
	}

	//This method overrides the superclass
	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			var itemAction;
			//The action is a part of the ValueElements declaration
			//but only relevant to the Element it is declared in.
			//Therefor we extract the action here and wrap it into a function
			//that also includes the context as the second argument.
			itemAction = itemDeclaration.removeAt(\action);
			newItem = this.class.dataClass.new(itemName, itemDeclaration, this);
			if(itemAction.notNil, {
				//If this object is in a context we bind the item action
				//to the context environment so that environment variables
				//can be used inside the defined action.
				if(context.notNil, {
					itemAction = context.prContextualizeFunction(itemAction);
				});
				newItem.action_({arg item;
					itemAction.value(newItem, context);
				});
			});
			this.addItem(newItem);
		});
	}

	addItem{arg newItem;
		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			var newItemName = newItem.name;
			//If the manager has already registered a context of this name then
			//we free the old context.
			//TODO: See if this need to be scheduled/synced in some way.
			if(this.hasItemNamed(newItemName), {
				"Freeing item: % from %".format(newItemName, this.fullPath).postln;
				this.freeItem(newItemName);
			});
			super.addItem(newItem);
		});
	}

}

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

	//This method overrides the superclass
	addItemsFromItemDeclarations{arg itemDecls;
		itemDecls.keysValuesDo({arg itemName, itemDeclaration;
			var newItem;
			var itemAction;
			//The action is a part of the Controls declaration
			//but only relevant to the Element it is declared in.
			//Therefor we extract the action here and wrap it into a function
			//that also includes the context as the second argument.
			itemAction = itemDeclaration.removeAt(\action);
			newItem = this.class.dataClass.new(itemName, itemDeclaration, this);
			if(itemAction.notNil, {
				//If this object is in a context we bind the item action
				//to the context environment so that environment variables
				//can be used inside the defined action.
				if(this.context.notNil, {
					itemAction = this.context.prContextualizeFunction(itemAction);
				});
				newItem.action_({arg item;
					itemAction.value(newItem, this.context);
				});
			});
			this.addItem(newItem);
		});
	}

	addItem{arg newItem;
		"% added item: %".format(this.fullPath, newItem).vtmdebug(2, thisMethod);
		if(newItem.isKindOf(this.class.dataClass), {//check arg type
			var newItemName = newItem.name;
			//If the manager has already registered a context of this name then
			//we free the old context.
			//TODO: See if this need to be scheduled/synced in some way.
			if(this.hasItemNamed(newItemName), {
				this.freeItem(newItemName);
			});
			super.addItem(newItem);
		});
	}

}

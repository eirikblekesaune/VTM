VTMContextManagerView : VTMContextView {
	var treeView;

	*new{arg parent, bounds, context, definition, settings;
		^super.new(parent, bounds, context, definition, settings).initContextManagerView;
	}

	initContextManagerView{
		treeView = TreeView()
		.columns_([context.name.asString.toUpper])
		.minHeight_(150)
		.fixedWidth_(150);

		this.layout_(
			HLayout( treeView ).spacing_(3).margins_(3)
		);
	}
/*
	prUpdateChildren{
		{
			treeView.clear;
			context.children.do({arg child;
				treeView.addItem([child.name]);
				//"updateing children: %".format(child.name).debug;
			});
		}.defer;
	}

	update{arg theChanged, whatChanged, toValue ...args;
		// "[%] Update: %".format(this.name, [theChanged, whatChanged, theChanger, args]).debug;
		if(theChanged === context, {
			if(this.children.includes(theChanged), {
				switch(whatChanged,
					\addedChild, {
						this.prUpdateChildren;
					},
					\removedChild, {
						this.prUpdateChildren;
					}
				);
			});
		});
	}*/
}

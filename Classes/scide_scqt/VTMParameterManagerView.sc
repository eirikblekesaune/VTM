VTMParameterManagerView : VTMView {
	var contextView;
	var parameterViews;

	*new{arg parent, bounds, definition, settings;
		if(parent.isKindOf(VTMContextView), {
			^super.new(parent, bounds, definition, settings).initParameterManagerView;
		}, {
			"VTMParameterManagerView - parent View must be a kind of VTMContextView".warn;
			^nil;
		});
	}

	initParameterManagerView{
		parameterViews = [];
		//"Context parameters are : %".format(this.context.parameters).debug;
		if(this.context.parameters.notEmpty, {
			parameterViews = this.context.parameterOrder.collect({arg item;
				//"making para view: %".format(item).debug;
				this.context.parameters[item].makeView(this);
			});
		});

		// ([11,22,33,44].flop ++ [\align, \topLeft]).flop

		this.layout_(
			VLayout(*(parameterViews.flop ++ [\align, \topLeft]).flop)
		);
		this.layout.spacing_(0).margins_(0);
	}

	context{ ^this.parent.context; }
}

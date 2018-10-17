VTMDataParametersView : VTMView {
	var contextView;
	var parameterViews;

	*new{| parent, bounds, definition, settings |
		if(parent.isKindOf(VTMContextView), {
			^super.new(parent, bounds, definition, settings).initParameterManagerView;
		}, {
			"VTMDataParametersView - parent View must be a kind of VTMContextView".warn;
			^nil;
		});
	}

	initParameterManagerView{
		parameterViews = [];
		"Context parameters are : %".format(this.context.parameters).vtmdebug(4, thisMethod);
		if(this.context.parameters.notEmpty, {
			parameterViews = this.context.parameterOrder.collect({| item |
				"making para view: %".format(item).vtmdebug(4, thisMethod);
				this.context.parameters[item].makeView(this);
			});
		});

		this.layout_(
			VLayout(*(parameterViews.flop ++ [\align, \topLeft]).flop)
		);
		this.layout.spacing_(0).margins_(0);
	}

	context{ ^this.parent.context; }
}

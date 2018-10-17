VTMContextProxyImplementation {
	var context;
	var declaration;
	var definition;

	*new{| context, definition, declaration |
		^super.new.initContextProxyImplementation(context, definition, declaration);
	}

	initContextProxyImplementation{| context_, definition_, declaration_ |
		context = context_;
		declaration = declaration_;
		definition = definition_;
	}

	sendMsg{| subpath ...msg |
		this.subclassResponsibility(thisMethod);
	}
}

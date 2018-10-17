VTMContextProxy {
	var implementation;

	*new{| name, definition, declaration, manager |
		^super.new().initContextProxy(name, definition, declaration, manager);
	}

	initContextProxy{| name, definition, declaration, manager |
		//determine which implementation to use
	}

	sendMsg{| ...msg |
		implementation.sendMsg(*msg);
	}
}

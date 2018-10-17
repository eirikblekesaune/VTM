VTMRemoteContextProxyImplementation : VTMContextProxyImplementation {
	var <targetAddr;
	var <targetPath;

	*new{| context, definition, declaration |
		^super.new(context, definition, declaration).initRemoteContextProxyImplementation;
	}

	initRemoteContextProxyImplementation{
		if(declaration.notNil, {
			if(declaration.includesKey(\targetAddr), {
				targetAddr = declaration[\targetAddr];
			});
			if(declaration.includesKey(\targetPath), {
				targetPath = declaration[\targetPath];
			});
		});
		if(targetPath.isNil, {
			targetPath = "/%".format(context.name).asSymbol;
		});
	}

	sendMsg{| subpath ...msg |
		targetAddr.sendMsg("%%".format(this.targetPath, subpath), *msg);
	}

}

VTM{
	classvar <systemConfiguration;

	*initClass{
		var configFilePath = "~/.vtm.conf.yaml".standardizePath;
		if(File.exists(configFilePath), {
			try{
				systemConfiguration = configFilePath.parseYAMLFile.changeScalarValuesToDataTypes;
			} {
				"Error reading VTM config file".warn;
			}
		}, {
			systemConfiguration = IdentityDictionary.new;
		});
	}

	*local{
		^VTMLocalNetworkNode.singleton;
	}

	*nodes{
		^List[this.local].addAll(this.local.networkNodeManager.items);
	}

	*sendMsg{arg hostname, port, path ...data;
		this.local.sendMsg(hostname, port, path, *data);
	}

	*sendLocalMsg{arg path ...data;
		this.sendMsg(
			NetAddr.localAddr.hostname, NetAddr.localAddr.port,
			path, *data
		);
	}

	*activate{arg discovery = false, remoteNetworkNodesToActivate;
		this.local.activate(discovery, remoteNetworkNodesToActivate);
	}

	*deactivate{
		this.local.deactivate;
	}

	*discover{
		this.local.discover;
	}

	*vtmPath{ ^PathName(PathName(this.filenameSymbol.asString).parentPath).parentPath; }

	*loadLibrary{arg folderPath;

	}

	*makeView{arg parent, bounds, viewDef, settings;
		var result;
		result = Window("VTM", bounds).layout_(
			HLayout(
				this.local.makeView(parent, bounds, viewDef, settings)
				// [
				// 	this.local.networkNodeManager.makeView(
				// 		bounds: Rect(0,0, 200, 300)
				// 	).fixedSize_(Size(200, 300)),
				// 	\align: \topLeft
				// ]
			)
		);
		^result;
	}
}

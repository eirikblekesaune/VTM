VTM{
	classvar <systemConfiguration;
	classvar <>debugLevel = 0;
	classvar <>debugFilterFunction;
	classvar <>throwpoint = false;

	*initClass{
		var configFilePath = "~/.vtm.conf.yaml".standardizePath;
		if(File.exists(configFilePath), {
			try{
				systemConfiguration = configFilePath.parseYAMLFile.changeScalarValuesToDataTypes;
			} {
				"Failed to read VTM config file: %".format(configFilePath).warn;
			}
		}, {
			systemConfiguration = IdentityDictionary.new;
		});
	}

	// *new{arg key;
	// 	var str = key.asString;
	// 	var result;
	// 	//check if it a path for the local network node
	// 	if(str.first == $/, {
	// 		result = this.local.find(str.drop(1));
	// 		}, {
	// 	});
	// 	^result;
	// }

	*local{
		^VTMLocalNetworkNode.singleton;
	}

	*nodes{
		^List[this.local].addAll(this.local.networkNodeManager.items);
	}

	*sendMsg{| hostname, port, path ...data |
		this.local.sendMsg(hostname, port, path, *data);
	}

	*sendLocalMsg{| path ...data |
		this.sendMsg(
			NetAddr.localAddr.hostname, NetAddr.localAddr.port,
			path, *data
		);
	}

	*activate{| discovery = false, remoteNetworkNodesToActivate |
		this.local.activate(discovery, remoteNetworkNodesToActivate);
	}

	*deactivate{
		this.local.deactivate;
	}

	*discover{
		this.local.discover;
	}

	*vtmPath{ ^PathName(PathName(this.filenameSymbol.asString).parentPath).parentPath; }

	*loadLibrary{| folderPath |

	}

	*makeView{| parent, bounds, viewDef, settings |
		var result;
		result = Window("VTM", bounds).layout_(
			HLayout(
				this.local.makeView(parent, bounds, viewDef, settings)
			)
		);
		^result;
	}

	*find{arg vtmPath;
		^VTM.local.find(vtmPath);
	}
}

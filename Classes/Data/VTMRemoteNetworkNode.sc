VTMRemoteNetworkNode : VTMElement {
	var <addr;
	var <localNetworks;

	*managerClass{ ^VTMNetworkNodeManager; }

	*new{arg name, declaration, localNetwork;
		^super.new(name, declaration).initRemoteNetworkNode(
			localNetwork
		);
	}

	initRemoteNetworkNode{arg localNetwork;
		localNetworks = [];
		if(localNetwork.notNil, {
			localNetworks = localNetworks.add(localNetwork);
		});
		addr = NetAddr.newFromIPString(this.get(\ipString));
	}


	//when a computer is available both on WIFI and Cable LAN
	//we add the second one with this method.
	addLocalNetwork{arg localNetwork;
		localNetworks = localNetworks.add(localNetwork);
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(VTMOrderedIdentityDictionary[
			\ipString -> (type: \string, optional: false),
			\mac -> (type: \string, optional: false)
		]);
	}

	sendMsg{arg path ...args;
		VTM.sendMsg(addr.hostname, addr.port, path, *args);
	}

	discover{
		VTM.local.discover(addr.hostname);
	}

	debugString{
		var result = super.debugString;
		result = result ++ "\t'localNetworks':\n";
		if(localNetworks.notNil and: {localNetworks.notEmpty}, {
			result = result ++ "\t\t[\n";
			localNetworks.do({arg item, i;
				result = result ++ item.getDiscoveryData.makeTreeString(5);
				result = result ++ "\t\t\t,\n";
			});
			result = result ++ "\t\t]\n";
		});
		^result;
	}

	hasLocalNetwork{arg lan;
		var result;
		result = localNetworks.includes(lan);
		^result;
	}
}

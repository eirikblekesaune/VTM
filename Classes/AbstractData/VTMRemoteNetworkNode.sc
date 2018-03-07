VTMRemoteNetworkNode : VTMElement {
	var <addr;
	var <localNetworks;

	*managerClass{ ^VTMNetworkNodeManager; }

	*new{arg name, declaration, manager, localNetwork;
		^super.new(name, declaration, manager).initRemoteNetworkNode(localNetwork);
	}

	initRemoteNetworkNode{arg localNetwork;
		localNetworks = [localNetwork];
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
}

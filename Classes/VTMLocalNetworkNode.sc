//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode {
	classvar <singleton;
	classvar <discoveryBroadcastPort = 57500;
	classvar <childKeys;
	var <hostname;
	var <localNetworks;
	var discoveryResponder;
	var discoveryReplyResponder;
	var remoteActivateResponder;
	var shutdownResponder;

	var <library;

	//global data managers for unnamed contexts
	var <applicationManager;
	var <networkNodeManager;
	var <hardwareSetup;
	var <moduleHost;
	var <sceneOwner;
	var <controls;

	var <active = false;

	*initClass{
		Class.initClassTree(VTMData);
		Class.initClassTree(VTMDataManager);
		Class.initClassTree(VTMDefinitionLibrary);
		Class.initClassTree(IdentitySet);
		childKeys = [
			':controls',
			':applications',
			':networkNodes',
			':hardwareDevices',
			':modules',
			':scenes'
		];
		singleton = super.new.initLocalNetworkNode;
	}

	*new{
		^singleton;
	}

	initLocalNetworkNode{
		applicationManager = VTMApplicationManager.new(this);
		networkNodeManager = VTMNetworkNodeManager.new(this);
		hardwareSetup = VTMHardwareSetup.new(this);
		moduleHost = VTMModuleHost.new(this);
		sceneOwner = VTMSceneOwner.new(this);
		controls = VTMControlManager(this);

		hostname = Pipe("hostname", "r").getLine();
		if(".local$".matchRegexp(hostname), {
			hostname = hostname.drop(-6);
		});
		hostname = hostname.asSymbol;
		this.findLocalNetworks;
		NetAddr.broadcastFlag = true;
		StartUp.add({
			//Make remote activate responder
			remoteActivateResponder = OSCFunc({| msg, time, addr, port |
				var hostnames = VTMJSON.parse(msg[1]);
				if(hostnames.detect({| item |
					item == this.name;
				}).notNil, {
					"Remote VTM activation from: %".format(addr).vtmdebug(2, thisMethod);
					this.activate(doDiscovery: true);
				})
			}, '/activate', recvPort: this.class.discoveryBroadcastPort);
		});

	}

	activate{| doDiscovery = false, remoteNetworkNodesToActivate |

		if(discoveryResponder.isNil, {
			discoveryResponder = OSCFunc({| msg, time, resp, addr |
				var jsonData = VTMJSON.parse(msg[1]).changeScalarValuesToDataTypes;
				var senderHostname, senderAddr, registered = false;
				var localNetwork;
				senderHostname = jsonData['hostname'].asSymbol;
				senderAddr = NetAddr.newFromIPString(jsonData['ip'].asString);

				//find which network the node is sending on
				localNetwork = localNetworks.detect({| net |
					net.isIPPartOfSubnet(senderAddr.ip);
				});

				if(localNetwork.isNil, {
					"Discovery was sent from a network where this network node is not connected:" ++
					"\thostname: %".format(senderHostname) ++
					"\taddress: %".format(senderAddr).vtmdebug(1, thisMethod);
				}, {
					//Check if it the local computer that sent it.
					if(senderAddr.isLocal.not, {
						//a remote network node sent discovery
						var isAlreadyRegistered;
						isAlreadyRegistered = networkNodeManager.hasItemNamed(senderHostname);
						if(isAlreadyRegistered.not, {
							var newNetworkNode;
							"Registering new network node:" ++
							"\tname: '%'".format(senderHostname) ++
							"\taddr: '%'".format(senderAddr).vtmdebug(1, thisMethod);
							newNetworkNode = VTMRemoteNetworkNode(
								senderHostname,
								(
									ip: jsonData['ip'].asString,
									mac: jsonData['mac'].asString
								),
								networkNodeManager,
								localNetwork
							);
						}, {
							var networkNode = networkNodeManager[senderHostname];
							//Check if it sent on a different local network
							if(networkNode.hasLocalNetwork(localNetwork).not, {
								//add the new local network to the remote network node
								networkNode.addLocalNetwork(localNetwork);
							});
						});
						this.sendMsg(
							senderAddr.hostname,
							this.class.discoveryBroadcastPort,
							'/discovery/reply',
							localNetwork.getDiscoveryData
						);
					});
				});
			}, '/discovery', recvPort: this.class.discoveryBroadcastPort);
		});

		if(discoveryReplyResponder.isNil, {
			discoveryReplyResponder = OSCFunc({| msg, time, addr, port |
				var jsonData = VTMJSON.parse(msg[1]).changeScalarValuesToDataTypes;
				var senderHostname, senderAddr, registered = false;
				var localNetwork;
				senderHostname = jsonData['hostname'].asSymbol;
				senderAddr = NetAddr.newFromIPString(jsonData['ip'].asString);
				//find which network the node is sending on

				localNetwork = localNetworks.detect({| net |
					net.isIPPartOfSubnet(senderAddr.ip);
				});

				//Check if it the local computer that sent it.
				if(senderAddr.isLocal.not, {
					//a remote network node sent discovery
					var isAlreadyRegistered;
					isAlreadyRegistered = networkNodeManager.hasItemNamed(senderHostname);
					if(isAlreadyRegistered.not, {
						var newNetworkNode;
						"Registering new network node:" ++
						"\tname: '%'".format(senderHostname) ++
						"\taddr: '%'".format(senderAddr).vtmdebug(2, thisMethod);
						newNetworkNode = VTMRemoteNetworkNode(
							senderHostname,
							(
								ip: jsonData['ip'].asString,
								mac: jsonData['mac'].asString
							),
							networkNodeManager,
							localNetwork
						);
						newNetworkNode.discover;
					}, {
						var networkNode = networkNodeManager[senderHostname];
						//Check if it sent on a different local network
						if(networkNode.hasLocalNetwork(localNetwork).not, {
							//add the new local network to the remote network node
							networkNode.addLocalNetwork(localNetwork);
						});
					});
				});
			}, '/discovery/reply', recvPort: this.class.discoveryBroadcastPort);
		});

		if(shutdownResponder.isNil, {
			shutdownResponder = OSCFunc({| msg, time, addr, port |
				var senderHostname;
				senderHostname = VTMJSON.parseYAMLValue(msg[1].asString);
				//Check if it the local computer that sent it.
				if(addr.isLocal.not, {
					//a remote network node notifued shutdown
					if(networkNodeManager.hasItemNamed(senderHostname), {
						var networkNode = networkNodeManager[senderHostname];
						networkNode.free;
						"Remote network node: '%' sent '/shutdown'".format(senderHostname).vtmdebug(1, thisMethod);
					});
				});
			}, '/shutdown', recvPort: this.class.discoveryBroadcastPort);
		});

		//Notify shutdown to other nodes
		ShutDown.add({
			"Shutting down VTM".vtmdebug(1, thisMethod);
			[
				shutdownResponder,
				discoveryReplyResponder,
				discoveryResponder,
				remoteActivateResponder
			].do({| resp | resp.clear; resp.free;});

			networkNodeManager.items.do({| remoteNetworkNode |
				this.sendMsg(
					remoteNetworkNode.addr.hostname.asString,
					this.class.discoveryBroadcastPort,
					'/shutdown',
					hostname
				);
			});
		});

		active = true;
		if(remoteNetworkNodesToActivate.notNil, {
			this.activateRemoteNetworkNodes(remoteNetworkNodesToActivate);
		});

		if(doDiscovery) { this.discover(); }

	}

	activateRemoteNetworkNodes{| remoteHostnames |
		this.broadcastMsg('/activate', remoteHostnames);
	}

	deactivate{
		discoveryResponder !? {discoveryResponder.free;};
		discoveryReplyResponder !? {discoveryReplyResponder.free;};
		shutdownResponder !? {shutdownResponder.free;};
		remoteActivateResponder !? {remoteActivateResponder.free;};
		active = false;
	}

	applications{
		^applicationManager.applications;
	}

	modules{
		^moduleHost.items;
	}

	findLocalNetworks{
		var lines;
		var parseOSXIfconfig = {| lns |
			var result, entries;

			lns.collect({| line |
				if(line.first != Char.tab, {
					entries = entries.add([line]);
				}, {
					entries[entries.size - 1] = entries[entries.size - 1].add(line);
				});
			});

			//remove the entries that don't have any extra information
			entries = entries.reject({| item | item.size == 1});

			//remove the LOOPBACK entry(ies)
			entries = entries.reject({| item |
				"[,<]?LOOPBACK[,>]?".matchRegexp(item.first);
			});

			//get only the active entries
			entries = entries.reject({| item |
				item.any({| jtem |
					"status: inactive".matchRegexp(jtem);
				})
			});
			//get only the lines with IPV4 addresses and
			entries = entries.collect({| item |
				var inetLine, hwLine;
				inetLine = item.detect({| jtem |
					"\\<inet\\>".matchRegexp(jtem);
				});
				if(inetLine.notNil, {
					hwLine = item.detect({| jtem |
						"\\<ether\\>".matchRegexp(jtem);
					})
				});
				[inetLine, hwLine];
			});
			//remove all that are nil
			entries = entries.reject({| jtem | jtem.first.isNil; });
			//separate the addresses
			entries.collect({| item |
				var ip, bcast, mac, netmask;
				var inetLine, hwLine;
				#inetLine, hwLine = item;

				ip = inetLine.copy.split(Char.space)[1];
				bcast = inetLine.findRegexp("broadcast (.+)");
				bcast = bcast !? {bcast[1][1];};
				mac = hwLine.findRegexp("ether (.+)");
				mac = mac !? {mac[1][1]};
				netmask = inetLine.findRegexp("netmask (.+?)\\s");
				netmask = netmask !? {netmask[1][1].interpret.asIPString;};
				(
					ip: ip.stripWhiteSpace,
					broadcast: bcast.stripWhiteSpace,
					mac: mac.stripWhiteSpace,
					hostname: this.hostname,
					netmask: netmask
				)
			}).collect({| item |
				result = result.add(VTMLocalNetwork.performWithEnvir(\new, item));
			});
			result;
		};
		var parseLinuxIfconfig = {| lns |
			var result, entries;

			lns.collect({| line |
				if(line.first != Char.space, {
					entries = entries.add([line]);
				}, {
					entries[entries.size - 1] = entries[entries.size - 1].add(line);
				});
			});

			//remove the entries that don't have any extra information
			entries = entries.reject({| item | item.size == 1});

			//remove the LOOPBACK entry(ies)
			entries = entries.reject({| item |
				"[,<]?LOOPBACK[,>]?".matchRegexp(item.first);
			});

			//get only the active entries
			entries = entries.reject({| item |
				item.any({| jtem |
					"status: inactive".matchRegexp(jtem);
				})
			});

			//get only the lines with IPV4 addresses and MAC address
			entries = entries.collect({| item |
				var inetLine, hwLine;
				inetLine = item.detect({| jtem |
					"\\<inet\\>".matchRegexp(jtem);
				});
				if(inetLine.notNil, {
					hwLine = item.detect({| jtem |
						"\\<ether\\>".matchRegexp(jtem);
					})
				});
				[inetLine, hwLine];
			});

			//remove all that are nil
			entries = entries.reject({| jtem | jtem.first.isNil; });

			//separate the addresses
			entries.collect({| item |
				var ip, bcast, mac, netmask;
				var inetLine, hwLine;
				#inetLine, hwLine = item;

				ip = inetLine.findRegexp("inet ([^\\s]+)");
				ip = ip !? {ip[1][1];};
				bcast = inetLine.findRegexp("broadcast ([^\\s]+)");
				bcast = bcast !? {bcast[1][1];};
				mac = hwLine.findRegexp("ether ([^\\s]+)");
				mac = mac !? {mac[1][1]};
				netmask = inetLine.findRegexp("netmask ([^\\s]+)");
				netmask = netmask !? {netmask[1][1];};
				(
					ip: ip.stripWhiteSpace,
					broadcast: bcast.stripWhiteSpace,
					mac: mac.stripWhiteSpace,
					hostname: this.hostname,
					netmask: netmask
				);
			}).collect({| item |
				result = result.add(VTMLocalNetwork.performWithEnvir(\new, item));
				nil;
			});
			result;
		};

		//delete previous local networks
		localNetworks = [];
		lines = "ifconfig".unixCmdGetStdOutLines;
		//clump into separate network interface entries

		Platform.case(
			\osx, {
				localNetworks = parseOSXIfconfig.value(lines);
			},
			\linux, {
				localNetworks = parseLinuxIfconfig.value(lines);

			},
			\windows, {
				"No find local network method for Windows yet!".warn;
			}
		);
	}

	name{
		^this.hostname;
	}

	fullPath{
		^'/';
	}

	*leadingSeparator { ^$/; }

	discover {| targetHostname |
		//Broadcast discover to all network connections
		if(localNetworks.isNil, { ^this; });
		localNetworks.do({| network |
			var data, targetAddr;

			data = network.getDiscoveryData;

			// if the method argument is nil, the message is broadcasted

			if(targetHostname.isNil, {
				targetAddr = NetAddr(
					network.broadcast,
					this.class.discoveryBroadcastPort
				);
			}, {
				targetAddr = NetAddr(targetHostname, this.class.discoveryBroadcastPort);
			});

			this.sendMsg(
				targetAddr.hostname, targetAddr.port, '/discovery', data
			);
		});
	}

	sendMsg{| targetHostname, port, path ...data |
		//sending eeeeverything as typed YAML for now.
		NetAddr(targetHostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
	}

	broadcastMsg{| path ...data |
		if(localNetworks.notNil, {
			localNetworks.do({| item |
				item.broadcastAddr.sendMsg(path, VTMJSON.stringify(data.unbubble));
			})
		});
	}

	findManagerForContextClass{| class |
		var managerObj;
		case
		{class.isKindOf(VTMModule.class) } {managerObj =  moduleHost; }
		{class.isKindOf(VTMHardwareDevice.class) } {managerObj =  hardwareSetup; }
		{class.isKindOf(VTMScene.class) } {managerObj =  sceneOwner; }
		{class.isKindOf(VTMApplication.class) } {managerObj =  applicationManager; }
		{class.isKindOf(VTMControl.class) } {managerObj = controls; }
		{class.isKindOf(VTMRemoteNetworkNode.class) } {managerObj =  networkNodeManager; };
		^managerObj;
	}

	makeView{| parent, bounds, viewDef, settings |
		var viewClass = 'VTMLocalNetworkNodeView'.asClass;
		//override class if defined in settings.
		^viewClass.new(parent, bounds, viewDef, settings, this);
	}

	// find{arg key;
	// 	var str = key.asString;
	// 	var result, numToDrop = 0;
	// 	//check if it is one of the managers keys
	// 	if(str.first == $:, {
	// 		var managerKey, aChar, manager;
	// 		str = str.iter;
	// 		aChar = str.next; //away with the colon
	// 		aChar = str.next;
	// 		numToDrop = 2;
	// 		while({aChar != $/}, {
	// 			managerKey = managerKey.add;
	// 			aChar = str.next;
	// 			numToDrop = numToDrop + 1;
	// 		});
	// 		switch(managerKey.asSymbol,
	// 			\controls, { manager = controls; },
	// 			\modules, { manager = moduleHost; },
	// 			\applications, { manager = applicationManager; },
	// 			\scenes, { manager = sceneOwner; },
	// 			\networkNodes, { manager = networkNodeManager; },
	// 			\devices, { manager = hardwareSetup; }
	// 		);
	// 		result = manager.find(key.asString.drop(numToDrop));
	// 		}, {
	//
	// 	});
	// 	^result;
	// }'/ :applications / PinneInstallasjon / :modules / pinne.2'

	find{arg vtmPath;
		if(vtmPath.isKindOf(VTMPath), {
			var i = 0, result;
			var child;
			if(vtmPath.isGlobal, {
				i = 1;
				//special case if it is the global network node path
				if(i == vtmPath.length, {
					^this;
				});
			});
			child = this;
			while({i < vtmPath.length}, {
				var childKey = vtmPath.at(i);
				"Child key: %".format(childKey).vtmdebug(0, thisMethod);
				"\t has child key: %".format(child.hasChildKey(childKey)).vtmdebug(0, thisMethod);
				if(child.hasChildKey(childKey), {
					child = child.getChild(childKey);
					"Next Child key: %".format(child).vtmdebug(0, thisMethod);
					i = i + 1;
					if(i == vtmPath.length, {
						^child;
					});
				}, {
					i = vtmPath.length; //this stops the while loop
				});
			});
			^nil; //return nil here if not found
		}, {
			"Not a VTMPath: %[%]".format(vtmPath, vtmPath.class).vtmwarn(0, thisMethod);
			^nil;
		});
	}

	hasChildKey{arg key;
		^this.childKeys.includes(key.asSymbol);
	}

	childKeys{
		^this.class.childKeys;
	}

	parentKey{
		^'/'
	}

	getChild{arg childKey;
		^this.children[childKey.asSymbol];
	}

	hasChildren{
		^true;
	}

	children{
		^VTMOrderedIdentityDictionary[
			':controls' -> controls,
			':applications' -> applicationManager,
			':networkNodes' -> networkNodeManager,
			':hardwareDevices' -> hardwareSetup,
			':modules' -> moduleHost,
			':scenes' -> sceneOwner
		];
	}
}


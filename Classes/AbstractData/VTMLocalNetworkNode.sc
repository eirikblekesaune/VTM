//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode {
	classvar <singleton;
	classvar <discoveryBroadcastPort = 57500;
	var <hostname;
	var <localNetworks;
	var discoveryResponder;
	var discoveryReplyResponder;
	var remoteActivateResponder;
	var shutdownResponder;

	var <networkNodeManager;
	var <library;

	//global data managers for unnamed contexts
	var <applicationManager;
	var <hardwareSetup;
	var <moduleHost;
	var <sceneOwner;
	var <scoreManager;

	var <active = false;


	*initClass{
		Class.initClassTree(VTMAbstractData);
		Class.initClassTree(VTMNetworkNodeManager);
		Class.initClassTree(VTMDefinitionLibrary);
		singleton = super.new.initLocalNetworkNode;
	}

	*new{
		^singleton;
	}

	initLocalNetworkNode{
		applicationManager = VTMApplicationManager.new(nil, this);
		networkNodeManager = VTMNetworkNodeManager.new(nil, this);
		hardwareSetup = VTMHardwareSetup.new(nil, this);
		moduleHost = VTMModuleHost.new(nil, this);
		sceneOwner = VTMSceneOwner.new(nil, this);
		scoreManager = VTMScoreManager.new(nil, this);
		hostname = Pipe("hostname", "r").getLine();
		if(".local$".matchRegexp(hostname), {
			hostname = hostname.drop(-6);
		});
		hostname = hostname.asSymbol;
		this.findLocalNetworks;
		NetAddr.broadcastFlag = true;
		StartUp.add({
			//Make remote activate responder
			remoteActivateResponder = OSCFunc({arg msg, time, addr, port;
				var hostnames = VTMJSON.parse(msg[1]);
				if(hostnames.detect({arg item;
					item == this.name;
				}).notNil, {
					//"Remote VTM activation from: %".format(addr).debug;
					this.activate(doDiscovery: true);
				})
			}, '/activate', recvPort: this.class.discoveryBroadcastPort);
		});

	}

	activate{arg doDiscovery = false, remoteNetworkNodesToActivate;

		if(discoveryResponder.isNil, {
			discoveryResponder = OSCFunc({arg msg, time, resp, addr;
				var jsonData = VTMJSON.parse(msg[1]).changeScalarValuesToDataTypes;
				var senderHostname, senderAddr, registered = false;
				var localNetwork;
				senderHostname = jsonData['hostname'].asSymbol;
				senderAddr = NetAddr.newFromIPString(jsonData['ipString'].asString);

				//find which network the node is sending on
				localNetwork = localNetworks.detect({arg net;
					net.isIPPartOfSubnet(senderAddr.ip);
				});

				//Check if it the local computer that sent it.
				if(senderAddr.isLocal.not, {
					//a remote network node sent discovery
					var isAlreadyRegistered;
					isAlreadyRegistered = networkNodeManager.hasItemNamed(senderHostname);
					if(isAlreadyRegistered.not, {
						var newNetworkNode;
						"Registering new network node: %".format([senderHostname, senderAddr]).debug;
						newNetworkNode = VTMRemoteNetworkNode(
							senderHostname,
							(
								ipString: jsonData['ipString'].asString,
								mac: jsonData['mac'].asString
							),
							networkNodeManager,
							localNetwork
						);
					}, {
						var networkNode = networkNodeManager[senderHostname];
						//Check if it sent on a different local network
						"Already registered: %".format(senderHostname).postln;
						"\tSending on local network: %\n\t".format(localNetwork.getDiscoveryData).post;
						networkNode.localNetworks.collect(_.getDiscoveryData).postln;
						if(networkNode.hasLocalNetwork(localNetwork).not, {
							//add the new local network to the remote network node
							"Will add new local network".postln;
							networkNode.addLocalNetwork(localNetwork);
						});
					});
					this.sendMsg(
						senderAddr.hostname,
						this.class.discoveryBroadcastPort,
						'/discovery/reply',
						localNetwork.getDiscoveryData
					);
				}, {
					"IT WAS LOCALHOST, ignoring it!".debug;
				});
			}, '/discovery', recvPort: this.class.discoveryBroadcastPort);
		});

		if(discoveryReplyResponder.isNil, {
			discoveryReplyResponder = OSCFunc({arg msg, time, addr, port;
				var jsonData = VTMJSON.parse(msg[1]).changeScalarValuesToDataTypes;
				var senderHostname, senderAddr, registered = false;
				var localNetwork;
				senderHostname = jsonData['hostname'].asSymbol;
				senderAddr = NetAddr.newFromIPString(jsonData['ipString'].asString);
				//find which network the node is sending on

				localNetwork = localNetworks.detect({arg net;
					net.isIPPartOfSubnet(senderAddr.ip);
				});

				//Check if it the local computer that sent it.
				if(senderAddr.isLocal.not, {
					//a remote network node sent discovery
					var isAlreadyRegistered;
					isAlreadyRegistered = networkNodeManager.hasItemNamed(senderHostname);
					if(isAlreadyRegistered.not, {
						var newNetworkNode;
						"Registering new network node: %".format([senderHostname, senderAddr]).debug;
						newNetworkNode = VTMRemoteNetworkNode(
							senderHostname,
							(
								ipString: jsonData['ipString'].asString,
								mac: jsonData['mac'].asString
							),
							networkNodeManager,
							localNetwork
						);
						newNetworkNode.discover;
					}, {
						var networkNode = networkNodeManager[senderHostname];
						//Check if it sent on a different local network
						"Already registered: %".format(senderHostname).postln;
						"\tSending on local network: %\n\t".format(localNetwork.getDiscoveryData).post;
						networkNode.localNetworks.collect(_.getDiscoveryData).postln;
						if(networkNode.hasLocalNetwork(localNetwork).not, {
							//add the new local network to the remote network node
							"Will add new local network".postln;
							networkNode.addLocalNetwork(localNetwork);
						});
					});
				}, {
					"IT WAS LOCALHOST, ignoring it!".debug;
				});
			}, '/discovery/reply', recvPort: this.class.discoveryBroadcastPort);
		});

		if(shutdownResponder.isNil, {
			shutdownResponder = OSCFunc({arg msg, time, addr, port;
				var senderHostname;
				senderHostname = VTMJSON.parseYAMLValue(msg[1].asString);
				//Check if it the local computer that sent it.
				if(addr.isLocal.not, {
					//a remote network node notifued shutdown
					if(networkNodeManager.hasItemNamed(senderHostname), {
						var networkNode = networkNodeManager[senderHostname];
						networkNode.free;
						"Freed remote network node: %".format(senderHostname).postln;
					});
				}, {
					"IT WAS LOCALHOST, ignoring it!".debug;
				});

			}, '/shutdown', recvPort: this.class.discoveryBroadcastPort);
		});

		//Notify shutdown to other nodes
		ShutDown.add({
			"Shutting down VTM".postln;
			[
				shutdownResponder,
				discoveryReplyResponder,
				discoveryResponder,
				remoteActivateResponder
			].do({arg resp; resp.clear; resp.free;});


			networkNodeManager.items.do({arg remoteNetworkNode;
				this.sendMsg(
					remoteNetworkNode.addr.hostname,
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

	activateRemoteNetworkNodes{arg remoteHostnames;
		this.broadcastMsg('/activate', remoteHostnames);
	}

	deactivate{
		discoveryResponder !? {discoveryResponder.free;};
		active = false;
	}

	applications{
		^applicationManager.applications;
	}

	modules{
		^moduleHost.items;
	}

	findLocalNetworks{
		var lines, entries;

		//delete previous local networks
		localNetworks = [];
		lines = "ifconfig".unixCmdGetStdOutLines;
		//clump into separate network interface entries

		Platform.case(
			\osx, {
				lines.collect({arg line;
					if(line.first != Char.tab, {
						entries = entries.add([line]);
					}, {
						entries[entries.size - 1] = entries[entries.size - 1].add(line);
					});
				});
				//remove the entries that don't have any extra information
				entries = entries.reject({arg item; item.size == 1});
				//remove the LOOPBACK entry(ies)
				entries = entries.reject({arg item;
					"[,<]?LOOPBACK[,>]?".matchRegexp(item.first);
				});
				//get only the active entries
				entries = entries.reject({arg item;
					item.any({arg jtem;
						"status: inactive".matchRegexp(jtem);
					})
				});
				//get only the lines with IPV4 addresses and
				entries = entries.collect({arg item;
					var inetLine, hwLine;
					inetLine = item.detect({arg jtem;
						"\\<inet\\>".matchRegexp(jtem);
					});
					if(inetLine.notNil, {
						hwLine = item.detect({arg jtem;
							"\\<ether\\>".matchRegexp(jtem);
						})
					});
					[inetLine, hwLine];
				});
				//remove all that are nil
				entries = entries.reject({arg jtem; jtem.first.isNil; });

				//separate the addresses
				entries.collect({arg item;
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
				}).collect({arg item;
					localNetworks = localNetworks.add(VTMLocalNetwork.performWithEnvir(\new, item));
				});
			},
			\linux, {
				var result;
				//"No find local network method ifr Linux yet!".warn;
				//clump into separate network interface entries
				lines.collect({arg line;
					if(line.first != Char.space, {
						entries = entries.add([line]);
					}, {
						entries[entries.size - 1] = entries[entries.size - 1].add(line);
					});
				});
				//remove empty lines
				entries = entries.reject{arg entry;
					var lineIsEmpty = false;
					if(entry.size == 1, {
						lineIsEmpty = entry.first.isString and: {entry.first.isEmpty};
					});
					lineIsEmpty;
				};
				//remove loopback device
				entries = entries.reject{arg entry;
					"Loopback".matchRegexp(entry.first);
				};
				//select only entries with IPV4 addresses
				entries = entries.select{arg entry;
					entry.any{arg line;
						"\\<inet\\> .+".matchRegexp(line);
					};
				};
				//Get the MAC addresses
				entries.do{arg entry;
					var mac, ip, broadcast;
					var inetLine, entryData;
					entryData = ();
					mac = entry.first.findRegexp("HWaddr (.+)");
					if(mac.notNil, {
						entryData.put(\mac, mac[1][1].stripWhiteSpace);
					}, {
						"Did not find MAC for entry: %".format(entry).warn;
					});
					inetLine = entry.detect{arg line;
						"\\<inet\\> .+".matchRegexp(line);
					};
					if(inetLine.notNil, {
						var regx;
						regx = inetLine.findRegexp("addr:(.+) Bcast:(.+) Mask:(.+)");
						if(regx.notEmpty, {
							entryData.put(\ip, regx[1][1].stripWhiteSpace);
							entryData.put(\broadcast, regx[2][1].stripWhiteSpace);
							entryData.put(\netmask, regx[3][1].stripWhiteSpace);
						}, {
							"Could not parse inet line for %\n\t%".format(
								entry.first, inetLine
							).warn;
						});
					}, {
						"Did not find IP and broadcast for %".format(
							String.newFrom(entry.flat)).warn;
					});
					result.put(\hostname, this.hostname);
					result = result.add(entryData);
				};
				if(result.notNil, {
					result.do({arg item;
						localNetworks = localNetworks.add(
							VTMLocalNetwork.performWithEnvir(\new, item)
						);
					});
				});
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

	discover {arg targetHostname;
		//Broadcast discover to all network connections
		if(localNetworks.isNil, { ^this; });
		localNetworks.do({arg network;
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

			"Sending discovery on %".format(data).postln;
			this.sendMsg(
				targetAddr.hostname, targetAddr.port, '/discovery', data
			);
		});
	}

	*leadingSeparator { ^$/; }

	sendMsg{arg targetHostname, port, path ...data;
		//sending eeeeverything as typed YAML for now.
		NetAddr(targetHostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
	}

	broadcastMsg{arg path ...data;
		if(localNetworks.notNil, {
			localNetworks.do({arg item;
				item.broadcastAddr.sendMsg(path, VTMJSON.stringify(data.unbubble));
			})
		});
	}

	findManagerForContextClass{arg class;
		var managerObj;
		case
		{class.isKindOf(VTMModule.class) } {managerObj =  moduleHost; }
		{class.isKindOf(VTMHardwareDevice.class) } {managerObj =  hardwareSetup; }
		{class.isKindOf(VTMScene.class) } {managerObj =  sceneOwner; }
		{class.isKindOf(VTMScore.class) } {managerObj =  scoreManager; }
		{class.isKindOf(VTMApplication.class) } {managerObj =  applicationManager; };
		^managerObj;
	}
}


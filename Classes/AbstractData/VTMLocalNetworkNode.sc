//a Singleton class that communicates with the network and manages Applications
VTMLocalNetworkNode : VTMAbstractDataManager {
	classvar <singleton;

	*dataClass{ ^VTMApplication; }
	name{ ^\undefined; }

	*initClass{
		Class.initClassTree(VTMAbstractData);
		NetAddr.broadcastFlag = true;
		singleton = super.new.initLocalNetworkNode;
	}

	*new{
		^singleton;
	}

	getBroadcastIp {
		^Pipe("ifconfig | grep broadcast | awk '{print $NF}'", "r").getLine();
	}

	discover {

		// check BSD, may vary ...
		var line, lnet = false, lnet_ip;
		var addr_list = Pipe("ifconfig | grep \"inet \" | awk '{print $2}'","r");
		line = addr_list.getLine();

		while({line.notNil()})
		{
			lnet = "[0-9]{3}\.[0-9]{3}\.[0-9]{1,}\.[1-9]{1,}"
			.matchRegexp(line);

			if(lnet)
			{
				lnet_ip = line;
				lnet_ip.postln();
			};

			line = addr_list.getLine();
		};

		if(lnet.not) { Error("VTM Error, could not find localnetwork..").throw(); };

		this.sendMsg(this.getBroadcastIp(), 57120, '/discovery',
			format("%:%", lnet_ip, 57120));
	}

	initLocalNetworkNode{}

	leadingSeparator { ^$/; }

	sendMsg{arg hostname, port, path ...data;
		//sending eeeeverything as typed YAML for now.
		NetAddr(hostname, port).sendMsg(path, VTMJSON.stringify(data.unbubble));
	}
}


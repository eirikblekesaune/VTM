//Represents a local area network

VTMLocalNetwork{
	var <ip; //the IP for this computer
	var <broadcast;
	var <mac; //this computers network mac addr
	var <netmask;
	var <hostname;

	var <addr; //this computer netaddr instance
	var <netmaskAddr;
	var <broadcastAddr;

	*new{arg ip, broadcast, mac, netmask, hostname;
		^super.newCopyArgs(ip, broadcast, mac, netmask, hostname).init;
	}

	init{
		addr = NetAddr(ip, this.port);
		netmaskAddr = NetAddr(netmask, this.port);
		broadcastAddr = NetAddr(broadcast, VTMLocalNetworkNode.discoveryBroadcastPort);
	}

	getDiscoveryData{
		var result;
		result = (
			hostname: hostname,
			ipString: addr.makeIPString,
			mac: mac,
			port: this.port
		);
		^result;
	}

	port{
		^NetAddr.localAddr.port;
	}

	//check if another ip is a part of this subnet
	isIPPartOfSubnet{arg otherIP;
		var result;
		var lanmask = netmaskAddr.addr.bitNot.bitOr(addr.addr);
		result = lanmask.bitOr(NetAddr(otherIP).addr);
		result = result == lanmask;
		^result;
	}

	=={arg obj;
		^this.hash == obj.hash;
	}

	hash{
		^this.instVarHash(#[
			\ip, \broadcast, \mac, \netmask, \hostname
		])
	}
}
TestVTMRemoteNetworkNode : TestVTMElement {
	*makeRandomParameter{arg key, params;
		var result;
		result = super.makeRandomParameter(key, params);
		//
		result = switch(key,
			\ipString, {
				NetAddr(
					VTM.local.localNetworks[0].ip,
					NetAddr.localAddr.port
				).makeIPString
			},
			\mac, { VTM.local.localNetworks[0].mac; }
		);
		^result;
	}
}

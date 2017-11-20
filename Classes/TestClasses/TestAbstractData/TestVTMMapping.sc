TestVTMMapping : TestVTMElement {

	*makeRandomParameter{arg key, params;
		var result;
		result = super.makeRandomParameter(key, params);
		result = switch(key,
			\source, { this.makeRandomPath; },
			\destination, { this.makeRandomPath; },
			\when, {this.makeRandomString},//TODO: Change 'when'
			//	to whatever we'll use to express conditionals
			\type, { [\forwarding, \subscription, \bind, \exclusiveBind].choose; },
			result
		);
		^result;
	}
}

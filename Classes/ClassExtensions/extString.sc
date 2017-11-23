+ NetAddr {
	*newFromIPString{arg str;
		var hostname, port;
		#hostname, port = str.split($:);
		port = port.asInteger;
		^this.new(hostname, port);
	}

	makeIPString{
		^"%:%".format(this.hostname, this.port);
	}
}

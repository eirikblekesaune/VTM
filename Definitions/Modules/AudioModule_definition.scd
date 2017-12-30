~type = 'Module';
~prototypes = ['Audible'];
~server = Server.default;
~prepare = {arg self, cond;
	if(~server.notRunning, {
		~server.waitForBoot{cond.unhang;};
		"Booting server".postln;
		cond.hang;
	});
	~source = NodeProxy.audio(~server ? Server.default,2);
	"Audio module preprared".postln;
	~output = NodeProxy.audio(~server, ~numOutputs ? 1);
	~output.source = ~source;
};
~play = {
	~output.playN(~outbuses ? [0]);
};
~stop = {
	~output.stop;
};
~free = {
	~output.free;
	~source.free;
};

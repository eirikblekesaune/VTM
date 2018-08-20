VTMAudioEngine {
// 	var <server;
// 	var <setup;
// 	var <groups;
// 	var <application;
// 	var <audioFileLibrary;
// 	var <audioFileBufferManager;
//
// 	*new{arg application;
// 		^super.new.init(application);
// 	}
//
// 	init{arg application_;
// 		application = application_;
// 		groups = IdentityDictionary[
// 			\sources -> Group.new(server),
// 			\filters -> Group.new(server),
// 			\destinations -> Group.new(server)
// 		];
// 		audioFileLibrary = VTMAudioFileLibrary.new(this);
// 		audioFileBufferManager = VTMAudioFileBufferManager.new(this);
// 	}
//
// 	start{arg condition;
// 		var serverOptions;
// 		forkIfNeeded{
// 			// if(declaration.includesKey(\serverOptions), {
// 			// 	serverOptions = ServerOptions.new;
// 			// 	declaration[\serverOptions].keysValuesDo({arg opt, val;
// 			// 		serverOptions.perform(opt.asSetter, val);
// 			// 	});
// 			// });
// 			condition.test = false;
// 			// server = Server(
// 			// 	this.name,
// 			// 	NetAddr(this.addr.hostname, this.addr.port + 10),
// 			// 	serverOptions
// 			// );
// 			//change to this.bootServer later in order to use multiple apps on one computer
// 			server = Server.default;
// 			if(serverOptions.notNil, {
// 				server.options = serverOptions;
// 			});
// 			// server.doWhenBooted(
// 			// 	onComplete: {
// 			// 		condition.test = true;
// 			// 		condition.signal;
// 			// 	}
// 			// );
// 			server.waitForBoot(
// 				onFailure: {
// 					VTMError("ScSynth server failed to boot").throw;
// 				}
// 			);
// 		};
// 	}
//
// 	stop{
// 		server.quit;
// 	}
//
// 	prLoadAudioSetup{
// 		//what is the hardware we are using?
// 		//load input description
// 		//load output description
// 		//Do we have outboard effects?
// 	}
}

//All classes that uses objects of this class must define a .makeOSCAPI
//classmethod returning getter and setter methods and functions for those.
//In order to define the OSC path the user class needs to define:
//- .path method.
//- .name method
//- .leadingSeparator
//- *makeOSCAPI(obj)
VTMOSCInterface {

	var parent;
	var <enabled = false;
	var responder, compliant_responder;

	*new {| parent |

		if(parent.respondsTo(\fullPath).not, {
			NotYetImplementedError(
				"% has not implemented 'fullPath' method yet!"
				.format(parent.class)).throw;
		});

		"OSC Interface created for: %".format(parent.fullPath).vtmdebug(4, thisMethod);
		^super.newCopyArgs(parent);
	}

	*makeOSCPathCompliant {| path |
		var res = path.asString().replace("/:", "/");
		if(res.contains(":")) { res = res.replace(":", "/") };
		^res
	}

	makeResponderFromParent {
		var compliantPath;

		responder = OSCFunc({| msg, time, addr, recvport |
			var path = msg[0];
			msg = msg.drop(1);
			"OSC Message received at %, on port %, addressed to: %, with value: %".format(
				time, recvport, path, msg
			).vtmdebug(5, thisMethod);

			//this is a temporary hack that will be removed.
			parent.valueAction_(*msg);
			/////////
		}, parent.fullPath, recvPort: NetAddr.localAddr.port());

		//make compliant responder if path is not compliant with OSC standard
		compliantPath = this.class.makeOSCPathCompliant(parent.fullPath.asString());
		compliantPath = compliantPath.asSymbol;
		if(compliantPath != parent.fullPath, {
			compliant_responder = OSCFunc({| msg, time, addr, recvport |
				var path = msg[0];
				msg = msg.drop(1);
				"OSC Message received at %, on port %, addressed to: %, with value: %".format(
					time, recvport, path, msg).vtmdebug(5, thisMethod);
			}, VTMOSCInterface.makeOSCPathCompliant(parent.fullPath.asString()),
			recvPort: NetAddr.localAddr.port());
		});
	}

	enable {
		enabled = true;
		this.makeResponderFromParent();
	}

	disable {
		enabled = false;
	}

	free {
		this.disable;
		parent = nil;
	}
}

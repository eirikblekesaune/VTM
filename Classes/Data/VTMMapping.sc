VTMMapping : VTMControl {
	classvar <isAbstractClass=false;
	var source;
	var destination;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager );
	}

	enable{
		var src, dest;
		src = this.prFindSource(VTMPath(this.get(\source)));
		dest = this.prFindDestination(VTMPath(this.get(\destination)));
		if(src.notNil and: {dest.notNil}, {
			switch(this.get(\type),
				\forwarding, {
					//send the data from the source to the destination
					source.forwardTo(destination);
				}/*,
				\subscription, {
					//subscribe to the data from the destination
					source.addSubscription(destination);
				},
				\bind, {
					//both source and destination subscribes to eachother
					source.addForwarding(destination);
					source.addSubscriptions(destination);
				},
				\exclusiveBind, {
					//both source and destination subscribes to eachother exclusively
					source.addForwarding(destination, exclusive: true);
					source.addSubscriptions(destination, exclusive: true);
				}*/
			);
		}, {
			"Could not enable mapping for source: % and destination: %".format(
				this.get(\source), this.get(\destination)
			).vtmwarn(0, thisMethod);
		});
		super.enable;
	}

	prFindSource{arg vtmPath;
		var result, sourceObj;
		sourceObj = this.find(this.get(\source).value);
		if(sourceObj.isNil, {
			result = VTMMappingSource(sourceObj);
		});
		^result;
	}

	prFindDestinationObj{arg vtmPath;
		var result, destinationObj;
		destinationObj = this.find(this.get(\destination).value);
		if(destinationObj.notNil, {
			result = VTMMappingDestination.from(destinationObj);
		});
		^result;
	}

	*parameterDescriptions{
		^super.parameterDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\source -> (type: \string, optional: false),
				\destination -> (type: \string, optional: false),
				\type -> (type: \string, optional: true,
					default: \forwarding,
					enum: [\forwarding, \subscription, \bind, \exclusiveBind])
			]
		);
	}

	*attributeDescriptions{
		^super.attributeDescriptions.putAll(
			VTMOrderedIdentityDictionary[
				\enabled -> (
					type: \boolean,
					defaultValue: true,
					action: { | attr, mapping |
						if(attr.value, {
							mapping.enable;
						}, {
							mapping.disable;
						});
					}
				)
			]
		);
	}

	buildDependsOn{
		^[
			this.get(\source), this.get(\destination)
		];
	}
}

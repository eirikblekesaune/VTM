VTMMapping : VTMControl {
	classvar <isAbstractClass=false;
	var source;
	var destination;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager );
	}

	enable{
		var src, dest;
		src = this.prFindSourceObj(VTMPath(this.get(\source).value));
		dest = this.prFindDestinationObj(VTMPath(this.get(\destination).value));
		if(src.notNil and: {dest.notNil}, {
			source = src;
			destination = dest;
			switch(this.get(\type).value.asSymbol,
				\forwarding, {
					//send the data from the source to the destination
					"AAA".vtmdebug(0, thisMethod);
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
			"Could not enable mapping for \n\tsource: % [found: %] \n\tdestination: % [found: %]".format(
				this.get(\source).value, src.notNil,
				this.get(\destination).value, dest.notNil
			).vtmwarn(0, thisMethod);
		});
		super.enable;
	}

	prFindSourceObj{arg vtmPath;
		var result, sourceObj;
		sourceObj = this.find(vtmPath);
		if(sourceObj.notNil, {
			result = VTMMappingSource(sourceObj);
		});
		^result;
	}

	prFindDestinationObj{arg vtmPath;
		var result, destinationObj;
		destinationObj = this.find(vtmPath);
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

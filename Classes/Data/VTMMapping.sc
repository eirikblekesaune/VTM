VTMMapping : VTMControl {
	classvar <isAbstractClass=false;
	var source;
	var destination;

	*new{| name, declaration, manager |
		^super.new(name, declaration, manager ).initMapping;
	}

	initMapping{
		var sourceObj, destinationObj;
		sourceObj = this.find(this.get(\source));
		source = VTMMappingSource.from(sourceObj);
		destination = VTMMappingDestination(this.get(\destination));
		source.map(destination);
	}

	enable{
		switch(this.get(\type),
			\forwarding, {
				//send the data from the source to the destination
				source.addForwarding(destination);
			},
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
			}
		);
		super.enable;
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
}

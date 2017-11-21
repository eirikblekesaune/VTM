/*
An Element is an object that has components.
*/
VTMElement : VTMAbstractData {
	var attributes;
	var commands;
	var returns;
	var signals;
	var mappings;

	*new{arg name, declaration, manager;
		^super.new(name, declaration, manager).initElement;
	}

	initElement{
		this.prInitAttributes;
		this.prInitSignals;
		this.prInitReturns;
		this.prInitCommands;
		this.prInitMappings;

		//TODO: register with LocalNetworkNode singleton.
	}

	prInitAttributes{
		var itemDeclarations = VTMOrderedIdentityDictionary.new;
		this.class.attributeDescriptions.keysValuesDo({arg attrKey, attrDesc;
			itemDeclarations.put(attrKey, attrDesc.deepCopy);
			if(declaration.includesKey(attrKey), {
				itemDeclarations.at(attrKey).put(\value, declaration[attrKey]);
			});
		});
		attributes = VTMAttributeManager(itemDeclarations);
	}

	prInitSignals{
		var itemDeclarations = this.class.signalDescriptions.deepCopy;
		signals = VTMSignalManager(itemDeclarations);
	}

	prInitReturns{
		var itemDeclarations = this.class.returnDescriptions.deepCopy;
		returns  = VTMReturnManager(itemDeclarations);
	}

	prInitCommands{
		var itemDeclarations = this.class.commandDescriptions.deepCopy;
		commands = VTMCommandManager(itemDeclarations);
	}

	prInitMappings{
		var itemDeclarations = this.class.mappingDescriptions.deepCopy;
		commands = VTMMappingManager(itemDeclarations);
	}

	components{
		^[attributes, returns, signals, commands, mappings];
	}

	free{
		this.components.select(_.notNil).do(_.free);
		super.free;
	}

	*attributeDescriptions{  ^VTMOrderedIdentityDictionary[]; }
	*commandDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*returnDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*signalDescriptions{ ^VTMOrderedIdentityDictionary[]; }
	*mappingDescriptions{ ^VTMOrderedIdentityDictionary[]; }

	description{
		var result = super.description;
		result.putAll(VTMOrderedIdentityDictionary[
			\attributes -> this.class.attributeDescriptions,
			\commands -> this.class.commandDescriptions,
			\signals -> this.class.signalDescriptions,
			\returns -> this.class.returnDescriptions,
			\mappings -> this.class.mappingDescriptions
		]);
		^result;
	}

	//set attribute values.
	set{arg key...args;
		attributes.set(key, *args);
	}

	//get attribute(init or run-time) or parameter(init-time) values.
	get{arg key;
		var result = attributes.get(key);
		if(result.notNil, {
			^result;
		});
		//if no attribute found try getting a parameter
		^super.get(key);
	}

	//do command with possible value args. Only run-time.
	doCommand{arg key ...args;
		commands.doCommand(key, *args);
	}

	//get return results. Only run-time
	query{arg key;
		^returns.query(key);
	}

	//emits a signal
	//should not be used outside the class.
	//TODO: How to make this method esily avilable from within a
	//context definition, and still protected from the outside?
	emit{arg key...args;
		signals.emit(key, *args);
	}

	return{arg key ...args;
		returns.return(key, *args);
	}

	onSignal{arg key, func;
		//TODO: Warn or throw if signal not found
		if(signals.hasItemNamed(key), {
			signals[key].action_(func);
		});
	}

	attributes {
		^attributes.names;
	}

	commands{
		^commands.names;
	}

	returns{
		^returns.names;
	}

	signals{
		^signals.names;
	}

	mappings {
		^mappings.names;
	}

	addForwarding{arg key, compName, itemName,  addr, path, vtmJson = false, mapFunc;
		var comp = switch(compName, 
			\attributes, {attributes},
			\returns, {returns}
		);
		comp.addForwarding(key, itemName, addr, path, vtmJson, mapFunc);
	}

	removeForwarding{arg key, compName, itemName;
		var comp = switch(compName, 
			\attributes, {attributes},
			\returns, {returns}
		);
		comp.removeForwarding(key, itemName);
	}

	removeAllForwardings{
		this.components.select(_.notNil).do({arg comp;
			comp.removeAllForwarding;
		});
	}

	enableForwarding{
		this.components.select(_.notNil).do({arg comp;
			comp.enableForwarding;
		});
	}

	disableForwarding{
		this.components.select(_.notNil).do({arg comp;
			comp.disableForwarding;
		});
	}
}


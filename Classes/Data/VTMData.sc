VTMData {
	var <name;
	var <manager;
	var parameters;
	var oscInterface;
	var declaration;

	classvar <isAbstractClass=true;

	*viewClass{
		^\VTMDataView.asClass;
	}

	*managerClass{
		^this.subclassResponsibility(thisMethod);
	}

	//name is mandatory, must be defined in arg or declaration
	*new{arg name, declaration;
        var decl;
		if(name.isNil, {
			VTMError(
				"% - 'name' not defined".format(this)
			).throw;
		});
        ^super.new.initData(name.asSymbol, declaration);
	}

	initData{arg name_, declaration_;
		name = name_;
		declaration = VTMDeclaration.newFrom(declaration_ ? []);
		this.prInitParameters;
	}

	//get the parameter values from the declaration
	//Check for missing mandatory parameter values
	prInitParameters{
		parameters = VTMOrderedIdentityDictionary.new;

		this.class.parameterDescriptions.keysValuesDo({
			| key, props |
			var tempVal;
			//check if parameter is defined in parameter values
			if(declaration.includesKey(key), {
				var checkType;
				var checkValue;
				tempVal = VTMValue.makeFromProperties(props);
				//is type strict? true bk default
				checkType = props[\strictType] ? true;
				if(checkType, {
					if(tempVal.isValidType(declaration[key]).not, {
						VTMError(
							"Parameter value '%' must be of type '%'"
                            "value: %".format(
								key,
								tempVal.type,
								tempVal.value.asCompileString
							)
						).throw;
					});
				});
				//check if value is e.g. within described range.
				checkValue = props[\strictValid] ? false;
				if(checkValue, {
					if(tempVal.isValidValue(declaration[key]).not, {
						VTMError("Parameter value '%' is invalid"
							.format(key)).throw;
					});
				});
				tempVal.value = declaration[key];
			}, {
				var optional;
				//if not check if it is optional, true by default
				optional = props[\optional] ? true;
				if(optional.not, {
					VTMError(
                      "Parameters is missing non-optional value '%'"
						.format(key)).throw;
				}, {
					tempVal = VTMValue.makeFromProperties(props)
				});
			});

			if(tempVal.isNil, {
				VTMError("Building Parameter '%' failed!".format(
					key
				)).throw
			});
			parameters.put(key, tempVal.deepCopy);
		});
	}

	disable{
		this.disableForwarding;
		this.disableOSC;
	}

	enable{
		this.enableForwarding;
		this.enableOSC;
	}

	free{
		this.disableOSC;
		this.changed(\freed);
		this.releaseDependants;
		this.release;
		manager = nil;
	}

	*parameterDescriptions{
		^VTMOrderedIdentityDictionary.new;
	}

	*mandatoryParameters{
		var result = [];
		this.parameterDescriptions.keysValuesDo({arg key, desc;
			if(desc.includesKey(\optional) and: {
				desc[\optional].not
			}, {
				result = result.add(key);
			});
		});
		^result;
	}

	*description{
		var result = VTMOrderedIdentityDictionary[
			\parameters -> this.parameterDescriptions,
		];
		^result;
	}

	description{arg includeDeclaration = false;
		var result = this.class.description;
		if(includeDeclaration, {
			result.put(\declaration, this.declaration);
		});
		^result;
	}

	parameters{
		^parameters.copy;
	}

	declaration{
		this.subclassResponsibility(thisMethod);
	}

	fullPath{
		^(this.path ++ this.leadingSeparator ++ this.name).asSymbol;
	}

	isUnmanaged{
		^manager.isNil;
	}

	path{
		if(manager.isNil, {
			^'';
		}, {
			^manager.fullPath;
		});
	}

	hasDerivedPath{
		^manager.notNil;
	}

	get{arg key;
		^parameters.at(key);
	}

	leadingSeparator{ ^'/'; }

	enableOSC {
		oscInterface !? { oscInterface.enable(); };
		oscInterface ?? {
          oscInterface = VTMOSCInterface(this).enable()
        };
	}


	disableOSC {
		oscInterface !? { oscInterface.free() };
		oscInterface = nil;
	}

	oscEnabled {
		^oscInterface.notNil();
	}

	makeView{arg parent, bounds, viewDef, settings;
		var viewClass = this.class.viewClass;
		//override class if defined in settings.
		^viewClass.new(parent, bounds, viewDef, settings, this);
	}

	debugString{
		var result;
		result = "\n'%' [%]\n".format(this.name, this.class);
		result = result ++ "\t'fullPath': %\n".format(this.fullPath);
		result = result ++ "\t'description':\n %".format(
          this.description.makeTreeString(3));
		^result;
	}

	update{arg theChanged, whatChanged ...args;
		(
			"'%'\n\ttheChanged: %".format(this.name, theChanged) ++
			"\n\twhatChanged: %".format(whatChanged) ++
			"\n\targs: %".format(args)
		).vtmdebug( 2, thisMethod);

		switch(whatChanged,
			\items, {
				var cmd, obj;
				#cmd, obj = args;
				switch(cmd,
					\added, {
						if(theChanged.isKindOf(this.class.managerClass)
						and: {obj === this}, {
							manager = theChanged;
							this.addDependant(manager);
							this.changed(\addedToManager, theChanged);
						});
					},
					\removed, {
						if(theChanged.isKindOf(this.class.managerClass)
						and: {obj === this}, {
							this.removeDependant(manager);
							manager = nil;
							this.changed(\removedFromManager, theChanged);
						});
					}
				);
			}
		)
	}
}

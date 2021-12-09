VTMData {
	var <name;
	var <manager;
	var parameters;
	var oscInterface;
	var declaration;
	var children;

	classvar <isAbstractClass=true;

	*viewClass{
		^\VTMDataView.asClass;
	}

	*managerClass{
		^this.subclassResponsibility(thisMethod);
	}

	//name is mandatory, must be defined in arg or declaration
	*new{| name, declaration, manager |
		var decl;
		if(name.isNil, {
			VTMError(
				"% - 'name' not defined".format(this)
			).throw;
		});
		^super.new.initData(name.asSymbol, declaration, manager);
	}

	initData{| name_, declaration_, manager_ |
		name = name_;
		manager = manager_;
		children = IdentityDictionary.new;

		//manager must not rely on the item to be fully initalized
		if(manager.notNil, {
			manager.addItem(this);
		}, {
			"No manager found: % - %".format(name_, declaration_).vtmwarn(0, thisMethod);
		});

		declaration = VTMDeclaration.newFrom(declaration_ ? []);
		this.prInitParameters;
	}

	//get the parameter values from the class declaration
	//Check for missing mandatory parameter values
	prInitParameters{
		parameters = VTMOrderedIdentityDictionary.new;

		this.class.parameterDescriptions.keysValuesDo({| key, props |
			var tempVal;
			tempVal = this.class.validateParameterValue(props, key, declaration);
			if(tempVal.isKindOf(Error).not, {
				parameters.put(key, tempVal.deepCopy);
				}, {
					tempVal.throw;
			});
		});
	}

	registerChild{|child|
		children.put(child.name, child);
	}

	unregisterChild{|child|
		children.removeAt(child.name);
	}

	*validateParameterValue{arg props, key, declaration;
		var tempVal, result;
		"props: %, key: %, declaration: %".format(props, key, declaration).vtmdebug(2, thisMethod);
		try{
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
			result = tempVal;
		} {|err|
			result = err;
		}
		^result;
	}

	disable{
		this.disableOSC;
	}

	enable{
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
		this.parameterDescriptions.keysValuesDo({| key, desc |
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

	description{| includeDeclaration = false |
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
		^this.parent.fullPath;
	}

	hasDerivedPath{
		^manager.notNil;
	}

	get{| key |
		^parameters.at(key);
	}

	parent{
		if(manager.isNil, {
			^VTM.local;
		}, {
			^manager.parent;
		});
	}

	leadingSeparator{ 
		if(this.parent === VTM.local, {
			^'';
		}, {
			^'/';
		})
	}

	enableOSC {
		if( oscInterface.notNil, {
			oscInterface.enable();
		}, {
			oscInterface = VTMOSCInterface(this).enable()
		});
	}


	disableOSC {
		oscInterface !? { oscInterface.free() };
		oscInterface = nil;
	}

	oscEnabled {
		^oscInterface.notNil();
	}

	makeView{| parent, bounds, viewDef, settings |
		var viewClass = this.class.viewClass;
		//override class if defined in settings.
		^viewClass.new(parent, bounds, viewDef, settings, this);
	}

	debugString{|includeDeclaration = false|
		var result;
		result = "\n'%' [%]\n".format(this.name, this.class);
		result = result ++ "\t'fullPath': %\n".format(this.fullPath);
		result = result ++ "\t'description':\n %".format(
			this.description(includeDeclaration).makeTreeString(3));
		^result;
	}

	update{| theChanged, whatChanged ...args |
		(
			"'%'\n\ttheChanged: %".format(this.name, theChanged) ++
			"\n\twhatChanged: %".format(whatChanged) ++
			"\n\targs: %".format(args)
		).vtmdebug( 3, thisMethod);

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

	childKeys{
		^children.keys;
	}

	getChild{arg key;
		^children.at(key);
	}

	hasChildKey{|key|
		^children.includesKey(key);
	}

	find{arg vtmPath;
		^this.subclassResponsibility(thisMethod);
	}
}

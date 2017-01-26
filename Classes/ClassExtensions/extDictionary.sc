+ Dictionary {
	//converts all string values from string to either Integer, Float or Boolean
	//this is practical when importing YAML and JSON files with parseYAML
	//uses its own type angle bracket notation , since the YAML-cpp
	//lib swallows the type tags before it enters sclang.
	changeScalarValuesToDataTypes {
		var parseYAMLValue, changeValue, tempdict;
		parseYAMLValue= {arg str;
			var result = str;
			case
			{"^<int> -?[0-9]+$".matchRegexp(str)}
			{
				result = str.drop(6).interpret;
			}
			{"^<float> [0-9a-fA-F]{16}$".matchRegexp(str)}
			{
				result = str.drop(8).clump(8).collect({arg it; "0x%".format(it).interpret});
				result = Float.from64Bits(*result);
			}
			{"^<float> [0-9a-fA-F]{8}$".matchRegexp(str)}
			{
				result = "0x%".format(str.drop(8)).interpret;
				result = Float.from32Bits(result);
			}
			{"^<symbol> .+$".matchRegexp(str)} {result = str.drop(9).asSymbol; }
			{"^<string> .+$".matchRegexp(str)} {result = str.drop(9).drop(1).drop(-1); }
			{"^<bool> true$".matchRegexp(str)} {result = true; }
			{"^<bool> false$".matchRegexp(str)} {result = false; }
			{"^-?[0-9]+(?:\.[0-9]+)?$".matchRegexp(str)}//if number
			{
				if(str.asFloat == str.asInteger,
					{
						result = str.asInteger;
					}, {
						result = str.asFloat;
					}
				);
			}
			{"^0[xX][0-9a-fA-F]+$".matchRegexp(str)} {result = str.interpret; } //hex notation
			{"^true$".matchRegexp(str)} { result = true; }// yaml1.2 /json compatible booleans
			{"^false$".matchRegexp(str)} { result = false; }
			{ result = str.asString; };//convert to symbol by default
			result;
		};
		changeValue = {|key,val|
			var result = val;
			if(val.isString, {
				result = parseYAMLValue.value(val);
			}, {
				if(val.isCollection, {
					if(val.isKindOf(Dictionary), {
						result = val.keysValuesChange(changeValue);
					}, {
						result = val.collect({|item|
							changeValue.value(nil, item);
						});
					});
				});
			});
			result;
		};
		^this.deepCopy.keysValuesChange(changeValue).asIdentityDictionaryWithSymbolKeys;
	}
	asIdentityDictionaryWithSymbolKeys {
		var d = IdentityDictionary();
		var maybeRecurseValue = {|val|
			if(val.isCollection) {
				if(val.isKindOf(Dictionary)) {
					val.asIdentityDictionaryWithSymbolKeys;
				} {
					val.collect(maybeRecurseValue);
				};
			} {
				val;
			};
		};
		this.pairsDo {|key,val|
			d[key.asSymbol] = maybeRecurseValue.value(val);
		};
		^d;
	}
}
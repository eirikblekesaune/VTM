TestVTMStringValue : TestVTMValue {

	*makeRandomValue{arg params;
		^this.makeRandomString(params);
	}

	*makeRandomProperty{arg key, params;
		var result;
		result = super.makeRandomProperty(key, params);
		if(result.isNil, {
			switch(key,
				\pattern, { result = this.makeRandomString(params); },
				\matchPattern, { result = this.makeRandomBoolean(params); }
			);
		});
		^result;
	}

	test_DefaultProperties{
		var valueObj = VTMStringValue.new;
		this.assert(
			valueObj.defaultValue.class == String and: {valueObj.defaultValue.isEmpty},
			"StringValue defaultValue defaults to empty string:\n\tIS: %\n\tSHOULD BE: %".format(valueObj.defaultValue, "")
		);
		//value should default to empty string if value not defined
		this.assert(
			valueObj.value.class == String and: { valueObj.value == valueObj.defaultValue },
			"StringValue value defaults to empty string.\n\tIS: %\n\tSHOULD BE: %".format(valueObj.value.asCompileString, "".asCompileString)
		);
		//pattern should be empty string by default
		this.assertEquals(
			valueObj.pattern, "",
			"StringValue pattern is empty symbol by default"
		);

		//matchPattern should be false by default
		this.assertEquals(
			valueObj.matchPattern, false,
			"StringValue matchPattern is true by default"
		);
	}

	test_SettingPropertiesWithProperties{
		var desc, valueObj;
		desc = (
			value: "heisann.3",
			defaultValue: "heisann.5",
			pattern: "^heisann\\.\\d+$"
		);
		valueObj = VTMStringValue.new(desc);
		this.assertEquals(
			valueObj.value, desc[\value],
			"StringValue set value through properties"
		);
		this.assertEquals(
			valueObj.defaultValue, desc[\defaultValue],
			"StringValue set defaultValue through properties"
		);
		this.assertEquals(
			valueObj.pattern, desc[\pattern],
			"StringValue set value through properties"
		);
	}


	test_ConvertingSymbolArgsToStrings{
		var desc, valueObj;
		desc = (
			value: 'heisann.3',
			defaultValue: 'heisann.5',
			pattern: '^heisann\\.\\d+$'
		);
		valueObj = VTMStringValue.new(desc);
		this.assertEquals(
			valueObj.defaultValue.class, String,
			"StringValue converted defaultValue symbol arg to String"
		);
		this.assertEquals(
			valueObj.value.class, String,
			"StringValue converted value symbol arg to String"
		);
		this.assertEquals(
			valueObj.pattern.class, String,
			"StringValue converted pattern symbol arg to String"
		);
	}

	test_RegexMatchingWhenSettingValue{
		var testValue;
		var desc = (
			defaultValue: "bingo",
			value: "bongo",
			pattern: "^b(a|i|o|e|y)ngo$",
			matchPattern: true
		);
		var valueObj = VTMStringValue.new(desc);

		//Should ignore values that doesn't match the pattern
		testValue = "bikke";
		valueObj.value_(testValue);
		this.assertEquals(
			valueObj.value, desc[\value],
			"StringValue ignored setting unmatched value string"
		);

		//Should set values that match the pattern
		testValue = "bango";
		valueObj.value_(testValue);
		this.assertEquals(
			valueObj.value, testValue,
			"StringValue set setting matched value string"
		);

		//Should not check pattern is pattern is empty
		valueObj.pattern = "";
		testValue = "baila";
		valueObj.value_(testValue);
		this.assertEquals(
			valueObj.value, testValue,
			"StringValue did not check pattern when pattern was empty string"
		);

		//Should not check pattern if matchPattern is false
		valueObj.pattern = "^b(a|i|o|e|y)ngo$";
		valueObj.matchPattern = false;
		testValue = "bokfngo";
		valueObj.value = testValue;
		this.assertEquals(
			valueObj.value, testValue,
			"StringValue did not check pattern when matchPatter is false"
		);

		//Should set pattern to default when matchPattern is turned on
		//and current value is non-matching
		valueObj.matchPattern = false;
		valueObj.pattern = "^b(a|i|o|e|y)ngo$";
		testValue = "jogge";
		valueObj.value = testValue;
		valueObj.matchPattern = true;
		this.assertEquals(
			valueObj.value, valueObj.defaultValue,
			"StringValue set value to default when matchPattern is turned on and current value don't match"
		);

	}

	test_ClearingValue{
		var testValue, wasRun = false;
		var desc = (
			defaultValue: "bingo",
			value: "bongo",
			pattern: "^b(a|i|o|e|y)ngo$",
			patternMatching: false
		);
		var valueObj = VTMStringValue.new(desc);
		valueObj.action = {|p| wasRun = true;};

		//temporary diabled as it is unclear what this behaviour should be yet.
		// valueObj.clear;
		// this.assertEquals(
		// 	valueObj.value, valueObj.defaultValue,
		// 	"StringValue set value to defaultValue upon clearing with pattern matching off"
		// );

		//Should set to empty when pattern matching is not active
		valueObj.matchPattern = false;
		valueObj.value = "bingo";
		valueObj.clear;
		this.assertEquals(
			valueObj.value, "",
			"StringValue set value to empty string upon clear when pattern matchng is disabled"
		);

		//Should run action if doActionUponClear is true
		valueObj.value = "bungo";
		wasRun = false;
		valueObj.matchPattern = true;
		valueObj.clear(doActionUponClear: true);
		this.assertEquals(
			wasRun, true,
			"StringValue did do Action when clearing with doActionUponClear is true"
		);

		///Should do action when pattern match is disabled and doActionUponClear is true
		wasRun = false;
		valueObj.value = "bango";
		valueObj.matchPattern = false;
		valueObj.clear(doActionUponClear: true);
		this.assertEquals(
			wasRun, true,
			"StringValue did do Action when clearing with doActionUponClear true, and pattern matching active"
		);
	}

}

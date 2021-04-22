VTMBooleanValue : VTMValue {
	var <doActionOn = \change; //\change | \rising | \falling

	*prDefaultValueForType{ ^false; }

	*type{ ^\boolean; }

	isValidType{| val |
		^val.isKindOf(Boolean);
	}

	*new{| properties |
		^super.new(properties).initBooleanValue;
	}

	initBooleanValue{
		if(properties.notEmpty, {
			if(properties.includesKey(\doActionOn), {
				size = properties[\doActionOn];
			});
		});
	}

	*propertyKeys{
		^super.propertyKeys.addAll([
			\doActionOn
		]);
	}

	toggle{
		this.valueAction_(this.value.not);
	}

	doActionOn_{| when |
		if([\rising, \falling, \change].includes(when), {
			doActionOn = when;
		}, {
			"%:% - Uknown option %.\n\tAlternatives are 'rising', 'falling', and 'change'".format(
				this.class.name,
				thisMethod.name,
				when
			).warn;
		});
	}

	parseStringValue{|str|
		^switch(str,
			"true", true,
			"false", false,
			nil //return if neither option matches
		);
	}

	*defaultViewType{ ^\toggle; }
}

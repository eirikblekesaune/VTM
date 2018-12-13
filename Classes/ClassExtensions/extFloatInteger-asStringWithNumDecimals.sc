+ Float {
	asStringWithNumDecimals{|fracSize = 3|
		var val, isNeg = this.isNegative;
		val = this.round( 10 ** (fracSize.neg) );
		val = (val.asInt.abs.asString ++ "." ++
			( val.abs.frac* (10**fracSize) ).round(1).asInt.asStringToBase(10,fracSize));
		if(isNeg, {
			val = "-" ++ val;
		});
		^val;
	}
}

+ Integer {
	asStringWithNumDecimals{|fracSize = 3|
		var val, isNeg = this.isNegative;
		val = this.round( 10 ** (fracSize.neg) );
		val = (val.asInt.abs.asString ++ "." ++
			( val.abs.frac* (10**fracSize) ).round(1).asInt.asStringToBase(10,fracSize));
		if(isNeg, {
			val = "-" ++ val;
		});
		^val.asInteger;
	}
}
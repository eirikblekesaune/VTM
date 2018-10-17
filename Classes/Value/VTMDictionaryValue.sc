VTMDictionaryValue : VTMCollectionValue {
	*prDefaultValueForType {^Dictionary.new}
	isValidType{| val |
		^val.isKindOf(Dictionary);
	}
	value{^super.value.copy}
	*type{ ^\dictionary; }

}

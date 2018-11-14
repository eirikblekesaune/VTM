VTMOrderedIdentityDictionary : IdentityDictionary {
	var <order;

	put{| key, value |
		if(this.includesKey(key).not, {
			order = order.add(key);
		});
		^super.put(key, value);
	}

	keysValuesArrayDo { | argArray, function |
		var arr;
		if(this.isEmpty.not, {
			arr = [
				order,
				order.collect({| item | this.at(item); })
			].lace;
			super.keysValuesArrayDo(arr, function);
		});
	}

	keys { | species(Array) |
		^super.keys(species);
	}

	values {
		var list = List.new(size);
		this.do({ | value | list.add(value) });
		^list
	}

	sorted{
		var result = this.class.new(size);
		order.sort.do({| item |
			result.put(item, this.at(item));
		});
		^result;
	}

	//adding additional check for equal order
	== {| what |
		var result = super == what;
		if(result.not, { ^false; });
		if(order != what.order, {^false;});
		^true;
	}

	removeAt{| key |
		if(order.includes(key), {
			order.remove(key);
		});
		^super.removeAt(key);
	}

	first{
		^this.at(this.order.first);
	}
}

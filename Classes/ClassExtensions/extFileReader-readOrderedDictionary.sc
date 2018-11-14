+ FileReader{
	*readDictionaries{ | pathOrFile, skipEmptyLines=false, skipBlanks=false,  delimiter |
		var array;
		var result;
		var keys, values;
		array = this.read( pathOrFile, skipEmptyLines, skipBlanks,  delimiter);
		result = [];
		keys = array.first;
		values = array[1..];
		values.do({arg item, i;
			var dict = Dictionary.new;
			keys.collect({arg key, j;
				dict.put(key, item[j]);
			});
			result = result.add(dict);
		});
		^result;
	}
}
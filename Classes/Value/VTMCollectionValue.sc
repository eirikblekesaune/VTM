VTMCollectionValue : VTMValue {
	var <items;
	var <itemDescription;
	var <maxLength;
	var <minLength;

	*new{| properties |
		^super.new(properties).initCollectionValue;
	}

	initCollectionValue{
	}

	addItem{| val |
		items.add(val);
	}

	removeItem{| val |
		items.remove(val);
	}

	minLength_{| val |
		minLength = val;
	}

	maxLength_{| val |
		maxLength = val;
	}
}

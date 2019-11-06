VTMPath{
	var <breadcrumbs;
	var pathStr;

	*new{arg path;
		^super.new.init(path);
	}

	*newFromList{arg list;
		var ll = list.copy;
		var str;
		var isGlobal = false;
		if(ll.first == "/", {
			isGlobal = true;
			ll = ll[1..];
			if(ll.isEmpty, {
				^super.new.init("/");
			});
		});
		ll = [ll, "/"].lace((ll.size * 2) - 1);
		if(isGlobal, {
			ll = ["/"] ++ ll;
		});
		str = String.newFrom(ll.flat);
		^super.new.init(str);
	}

	init{arg path;
		var str = path.asString;
		pathStr = str.copy;
		if(str.first == $/, {
			breadcrumbs = breadcrumbs.add("/");
			str = str[1..];
			if(str.isEmpty.not, {
				breadcrumbs = breadcrumbs.addAll(str.split);
			});
		}, {
			breadcrumbs = breadcrumbs.addAll(str.split);
		});

	}

	isGlobal{
		^breadcrumbs.first == "/";
	}

	isLocal{
		^this.isGlobal().not;
	}

	length{
		^breadcrumbs.size;
	}

	parent{
		if(this.hasParent, {
			^VTMPath.newFromList(breadcrumbs.drop(-1));
		}, {
			//Returns itself if it has no parent;
			^this;
		});
	}

	asSymbol{
		this.asString.asSymbol;
	}

	asString{
		^pathStr;
	}

	hasParent{
		^this.length > 1;
	}
}
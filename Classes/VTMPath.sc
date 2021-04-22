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
		^(this.isGlobal() or: {this.isApplicationLocal}).not;
	}

	isApplicationLocal {
		^breadcrumbs.first == "~"
	}

	length{
		^breadcrumbs.size;
	}

	parentPath{
		if(this.hasParentPath, {
			^VTMPath.newFromList(breadcrumbs.drop(-1));
		}, {
			//Returns itself if it has no parent;
			^this;
		});
	}

	asSymbol{
		^this.pathStr;
	}

	hasParentPath{
		^this.length > 1;
	}

	printOn{|stream|
		stream << this.class.asString << "('" << pathStr << "')";
	}

	at{arg index;
		^breadcrumbs[index];
	}

	first{
		^breadcrumbs.first;
	}

	last{
		^breadcrumbs.last;
	}

	*isManagerCrumb{arg crumb;
		^crumb.first == $:;
	}

	resolve{|context|
		var result;
		if(context.isNil, {
			if(this.isGlobal, {
				result = VTM.find(this);
			});
		}, {
			result = context.find(this)
		});
		^result;
	}
}

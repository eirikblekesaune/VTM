VTMDeclaration : VTMOrderedIdentityDictionary {

	*newFrom{arg what;
		if(what.notNil, {
			if(what.isEmpty, {
				^super.newFrom(what);
			}, {
				if(what.every(_.isKindOf(Association)), {
					var d;
					what.do({arg item;
						d = d.addAll([item.key, item.value]);
					});
					^super.newFrom(d);
				}, {
					^super.newFrom(what);
				});
			});
		}, {
			^this.new;
		});
	}

	*readFromFile{arg pathName;
		^this.readArchive(pathName);
	}

	writeToFile{arg pathName, overwrite = false;
		if(File.exists(pathName), {
			if(overwrite == true, {
				this.prWriteFile(pathName);
			});
		}, {
			this.prWriteFile(pathName);
		});
	}

	prWriteFile{arg pathName;
		this.writeArchive(pathName);
	}
}

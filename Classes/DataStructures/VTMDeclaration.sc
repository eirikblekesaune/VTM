VTMDeclaration : VTMOrderedIdentityDictionary {

	*newFrom{| what |
		if(what.notNil, {
			if(what.isEmpty, {
				^super.newFrom(what);
			}, {
				if(what.every(_.isKindOf(Association)), {
					var d;
					what.do({| item |
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

	*readFromFile{| pathName |
		^this.readArchive(pathName);
	}

	writeToFile{| pathName, overwrite = false |
		if(File.exists(pathName), {
			if(overwrite == true, {
				this.prWriteFile(pathName);
			});
		}, {
			this.prWriteFile(pathName);
		});
	}

	prWriteFile{| pathName |
		this.writeArchive(pathName);
	}
}

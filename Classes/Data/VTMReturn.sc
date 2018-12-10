VTMReturn : VTMValueControl {
	classvar <isAbstractClass=false;

	query{
		var val = valueObj.action.value;
		valueObj.value = val;
		^valueObj.value;
	}

	value{
		^this.query;
	}
}

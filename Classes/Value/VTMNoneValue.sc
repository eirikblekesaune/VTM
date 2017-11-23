VTMNoneValue : VTMValue{
	*type{ ^\none; }

	*prDefaultValueForType{ 
		^VTMValue.allSubclasses.choose.prDefaultValueForType;
	}
}

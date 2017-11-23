TestVTMNoneValue : TestVTMValue{
	*makeRandomValue{
		VTMValue.typeToClass(TestVTMValue.testTypes.choose).prDefaultValueForType.class.generateRandom;
	}
}

TestVTMLocalNetworkNode : TestVTMAbstractDataManager {

	test_singleton{
		var aa, bb;
		aa = VTMLocalNetworkNode.new;
		bb = VTMLocalNetworkNode.new;
		this.assert(aa === bb,
			"Constructor returned singleton object"
		);
	}

	test_autoAddUnmanagedContexts{
	}
}

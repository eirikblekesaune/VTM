~prototypes = ['AudioFilter'];
~prepare = {arg module, condition;
	var irFilePath;
	var fftSize = 2048;
	var irSpectrumBufferSize, irBuffer, bufferLoaded = false;
	var declaration = module.declaration;

	if(declaration.includesKey(\irFilePath), {
		var irFile;
		if(File.exists(declaration.at(\irFilePath)), {
			irFilePath = declaration.at(\irFilePath);
		}, {
			VTMError("irReverb module failed to build: ir file not found '%'".format(declaration.at(\irFilePath))).throw;
		})
	}, {
		VTMError("irReverb module failed to build: Needs irFilePath").throw;
	});
	if(declaration.includesKey(\fftSize), {
		if(declaration[\fftSize].isNumber.not, {
			VTMError().throw;
		})
	});


	irBuffer = Buffer.read(
		~server,
		irFilePath,
		action: {
			condition.test = true;
			condition.signal;
		}
	);
	condition.wait;
	condition.test = false;
	//Calculate the size of the ir spectrum buffer
	irSpectrumBufferSize = PartConv.calcBufSize(fftSize, irBuffer);
	//Allocate buffer for irSpectrum data
	~irSpectrumBuffer = Buffer.alloc(
		~server,
		irSpectrumBufferSize,
		1,
		completionMessage: {
			condition.test = true;
			condition.unhang;
			bufferLoaded = true;
		}
	);
	condition.wait;
	condition.test = false;
	~irSpectrumBuffer = ~irSpectrumBuffer.preparePartConv(irBuffer, fftSize);
	~server.sync;
	irBuffer.free;
	if(bufferLoaded.not, {
		VTMError("KLANG: Error while loading ir response").throw;
	});

	// ~filterKlang[1] = \filter -> {|in|
	// 	PartConv.ar(
	// 		in,
	// 		2048,
	// 		~irSpectrumBufferKlang.bufnum;
	// 	);
	//
	// };

};
~run = {};
~free = {arg module, condition;
	~irSpectrumBuffer.free({arg buf;
		condition.unhang;
	});
	condition.hang;
};

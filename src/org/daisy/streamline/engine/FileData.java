package org.daisy.streamline.engine;

import org.daisy.streamline.api.media.AnnotatedFile;

class FileData {
	private final TempFileHandler fj;
	private AnnotatedFile current;
	
	FileData(TempFileHandler fj) {
		this.fj = fj;
		this.current = null;
	}
	
	void setCurrent(AnnotatedFile current) {
		this.current = current;
	}
	
	AnnotatedFile getCurrent() {
		return current;
	}
	
	TempFileHandler getTempFileHandler() {
		return fj;
	}

}

package org.daisy.streamline.engine;

import java.util.Objects;

import org.daisy.streamline.api.media.FileSet;

class FolderData {
	private TempFolderHandler tfh;
	private FileSet current;
	
	FolderData() {
		this.tfh = null;
		this.current = null;
	}
	
	void setCurrent(FileSet current) {
		this.current = Objects.requireNonNull(current);
	}
	
	FileSet getCurrent() {
		return current;
	}
	
	void setTempFolderHandler(TempFolderHandler tfh) {
		this.tfh = Objects.requireNonNull(tfh);
	}
	
	TempFolderHandler getTempFolderHandler() {
		return tfh;
	}
}

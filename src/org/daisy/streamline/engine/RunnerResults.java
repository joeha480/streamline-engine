package org.daisy.streamline.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.daisy.streamline.api.media.FileSet;

public final class RunnerResults {
	private final Optional<FileSet> fileSet;
	private final List<RunnerResult> results;
	private final List<Exception> exceptions;
	
	public static class Builder {
		private FileSet fileSet = null;
		private List<RunnerResult> results = new ArrayList<>();
		private List<Exception> exceptions = new ArrayList<>();
		
		public Builder fileSet(FileSet value) {
			this.fileSet = value;
			return this;
		}
		
		public Builder addResult(RunnerResult value) {
			this.results.add(value);
			return this;
		}
		
		public Builder addResults(List<RunnerResult> value) {
			this.results.addAll(value);
			return this;
		}
		
		public Builder addException(Exception e) {
			this.exceptions.add(e);
			return this;
		}
		
		public RunnerResults build() {
			return new RunnerResults(this);
		}
	}
	
	private RunnerResults(Builder builder) {
		this.fileSet = Optional.ofNullable(builder.fileSet);
		this.results = Collections.unmodifiableList(new ArrayList<>(builder.results));
		this.exceptions = Collections.unmodifiableList(new ArrayList<>(builder.exceptions));
	}
	
	public Optional<FileSet> getFileSet() {
		return fileSet;
	}

	public List<RunnerResult> getResults() {
		return results;
	}
	
	public List<Exception> getExceptions() {
		return exceptions;
	}

}

package org.daisy.streamline.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.daisy.streamline.api.media.FileSet;

/**
 * Provides the results from running a list of tasks.
 * @author Joel Håkansson
 */
public final class RunnerResults {
	private final Optional<FileSet> fileSet;
	private final List<RunnerResult> results;
	
	/**
	 * Provides a builder for runner results.
	 */
	public static class Builder {
		private FileSet fileSet = null;
		private List<RunnerResult> results = new ArrayList<>();
		
		/**
		 * Sets the resulting file set in this result.
		 * @param value the file set
		 * @return this builder
		 */
		public Builder fileSet(FileSet value) {
			this.fileSet = value;
			return this;
		}
		
		/**
		 * Adds a task result to this builder.
		 * @param value the result to add
		 * @return this builder
		 */
		public Builder addResult(RunnerResult value) {
			this.results.add(value);
			return this;
		}
		
		/**
		 * Adds a list of task results to this builder.
		 * @param value the results to add
		 * @return this builder
		 */
		public Builder addResults(List<RunnerResult> value) {
			this.results.addAll(value);
			return this;
		}
		
		/**
		 * Builds the result using the current state of the builder.
		 * @return a new {@link RunnerResults} instance
		 */
		public RunnerResults build() {
			return new RunnerResults(this);
		}
	}
	
	private RunnerResults(Builder builder) {
		this.fileSet = Optional.ofNullable(builder.fileSet);
		this.results = Collections.unmodifiableList(new ArrayList<>(builder.results));
	}
	
	/**
	 * Gets the resulting file set.
	 * @return the resulting file set
	 */
	public Optional<FileSet> getFileSet() {
		return fileSet;
	}

	/**
	 * Gets the task results.
	 * @return the task results
	 */
	public List<RunnerResult> getResults() {
		return results;
	}
	
}

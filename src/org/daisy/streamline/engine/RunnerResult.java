package org.daisy.streamline.engine;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTask;

/**
 * Provides details about a task runner execution.
 * @author Joel Håkansson
 */
public class RunnerResult {
	private final AnnotatedFile input;
	private final InternalTask task;
	private final boolean success;
	
	/**
	 * Creates a new runner result builder.
	 * @author Joel Håkansson
	 */
	public static class Builder {
		private final AnnotatedFile input;
		private final InternalTask task;
		private boolean success = false;

		/**
		 * Creates a new builder with the specified details.
		 * @param input the input file
		 * @param task the task
		 */
		public Builder(AnnotatedFile input, InternalTask task) {
			this.input = input;
			this.task = task;
		}
		
		/**
		 * Sets the success.
		 * @param value the success
		 * @return returns this builder
		 */
		public Builder success(boolean value) {
			this.success = value;
			return this;
		}
		
		/**
		 * Creates a new runner result based on the current state of the builder.
		 * @return returns a new runner result
		 */
		public RunnerResult build() {
			return new RunnerResult(this);
		}
	}

	private RunnerResult(Builder builder) {
		this.input = builder.input;
		this.task = builder.task;
		this.success = builder.success;
	}

	/**
	 * Gets the input file for this result.
	 * @return returns the input file
	 */
	public AnnotatedFile getInput() {
		return input;
	}

	/**
	 * Gets the task for this result.
	 * @return returns the task
	 */
	public InternalTask getTask() {
		return task;
	}

	/**
	 * Returns true if the task was successful, false otherwise.
	 * @return returns true if the task was successful, false otherwise
	 */
	public boolean isSuccess() {
		return success;
	}

}
package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskSystemException;

/**
 * Utility class to run a list of tasks.
 * @author Joel Håkansson
 *
 */
public class TaskRunner {
	private final Logger logger;
	private final String name;
	private final boolean writeTempFiles;
	private final boolean keepTempFilesOnSuccess;
	private final TempFileWriter tempFileWriter;
	private final Set<Consumer<ProgressEvent>> progressListeners;
	
	/**
	 * Provides a builder for TaskRunner
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final String name;
		private boolean writeTempFiles = false;
		private boolean keepTempFilesOnSuccess = false;
		private TempFileWriter tempFileWriter = null;
		private Set<Consumer<ProgressEvent>> progressListeners = new HashSet<>();

		/**
		 * Creates a new builder with the default values
		 * @param name the name of the task runner
		 */
		public Builder(String name) {
			this.name = name;
		}
		
		/**
		 * Creates a new builder with the default name.
		 */
		public Builder() {
			this("Task Runner");
		}

		/**
		 * If true, temporary files are written to the temporary folder
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder writeTempFiles(boolean value) {
			this.writeTempFiles = value;
			return this;
		}
		/**
		 * If true, keeps temporary files created by the task runner
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder keepTempFiles(boolean value) {
			this.keepTempFilesOnSuccess = value;
			return this;
		}
		/**
		 * Sets the temporary file writer to use
		 * @param value the writer
		 * @return returns this builder
		 */
		public Builder tempFileWriter(TempFileWriter value) {
			this.tempFileWriter = value;
			return this;
		}
		/**
		 * Adds a progress listener.
		 * @param value the listener
		 * @return returns this builder
		 */
		public Builder addProgressListener(Consumer<ProgressEvent> value) {
			progressListeners.add(value);
			return this;
		}
		/**
		 * Creates a new TaskRunner with the current status of the builder
		 * @return a new TaskRunner
		 */
		public TaskRunner build() {
			return new TaskRunner(this);
		}
	}
	
	private TaskRunner(Builder builder) {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.name = builder.name;
		this.writeTempFiles = builder.writeTempFiles;
		this.keepTempFilesOnSuccess = builder.keepTempFilesOnSuccess;
		this.tempFileWriter = builder.tempFileWriter;
		this.progressListeners = builder.progressListeners;
	}
	
	/**
	 * Creates a new TaskRunner builder with the specified name
	 * @param name the name of the task runner
	 * @return returns a new builder
	 */
	public static TaskRunner.Builder withName(String name) {
		return new Builder(name);
	}

	/**
	 * Runs a list of tasks starting from the input file as input to the first task, the following tasks use the preceding result
	 * as input. The final result is written to the output.
	 * @param input the input file
	 * @param output the output file
	 * @param tasks the list of tasks
	 * @return returns a list of runner results
	 * @throws IOException if there is an I/O error
	 * @throws TaskSystemException if there is a problem with the task system
	 */
	public List<RunnerResult> runTasks(File input, File output, List<InternalTask> tasks) throws IOException, TaskSystemException {
		return runTasks(DefaultAnnotatedFile.with(input).extension(input).build(), output, tasks);
	}

	/**
	 * Runs a list of tasks starting from the input file as input to the first task, the following tasks use the preceding result
	 * as input. The final result is written to the output.
	 * @param input the input file
	 * @param output the output file
	 * @param tasks the list of tasks
	 * @return returns a list of runner results
	 * @throws IOException if there is an I/O error
	 * @throws TaskSystemException if there is a problem with the task system
	 */
	public List<RunnerResult> runTasks(AnnotatedFile input, File output, List<InternalTask> tasks) throws IOException, TaskSystemException {
		Progress progress = new Progress();
		logger.info(name + " started on " + progress.getStart());
		int i = 0;
		NumberFormat nf = NumberFormat.getPercentInstance();
		//FIXME: implement temp file handling as per issue #47
		TempFileWriter tempWriter =
				writeTempFiles?
							tempFileWriter!=null?tempFileWriter:new DefaultTempFileWriter.Builder().build()
						:
							null;
		List<RunnerResult> ret = new ArrayList<>();
		try (TaskRunnerCore itr = new TaskRunnerCore(input, output, tempWriter)) {
			for (InternalTask task : tasks) {
				ret.addAll(itr.runTask(task));
				i++;
				ProgressEvent event = progress.updateProgress(i/(double)tasks.size());
				logger.info(nf.format(event.getProgress()) + " done. ETC " + event.getETC());
				progressListeners.forEach(v->v.accept(event));
			}
		} catch (IOException | TaskSystemException | RuntimeException e) {
			//This is called after the resource (fj) is closed.
			//Since the temp file handler is closed the current state will be written to output. However, we do not want it.
			if (!output.delete()) {
				output.deleteOnExit();
			}
			throw e;
		}
		if (!keepTempFilesOnSuccess && tempWriter!=null) {
			// Process were successful, delete temp files
			tempWriter.deleteTempFiles();
		}
		logger.info(name + " finished in " + Math.round(progress.timeSinceStart()/100d)/10d + " s");
		return ret;
	}

}

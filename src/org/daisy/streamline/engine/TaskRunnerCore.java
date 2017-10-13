package org.daisy.streamline.engine;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.ExpandingTask;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadOnlyTask;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.io.TempFileHandler;

/**
 * Provides a utility for running a single task at a time.
 * 
 * @author Joel Håkansson
 */
public class TaskRunnerCore implements Closeable {
	private final Logger logger;
	private final TempFileHandler fj;
	private final TempFileWriter tfw;
	private AnnotatedFile current;
	
	/**
	 * Creates a new task runner core with the specified options. Consider using
	 * {@link #TaskRunnerCore(AnnotatedFile, File)} instead, as it provides
	 * the task with more details.
	 * 
	 * @param input the input file
	 * @param output the final output file
	 * @throws IOException if an I/O error occurs
	 */
	public TaskRunnerCore(File input, File output) throws IOException {
		this(input, output, null);
	}
	
	/**
	 * Creates a new task runner core with the specified options.
	 * @param input the input file
	 * @param output the final output file
	 * @throws IOException if an I/O error occurs
	 */
	public TaskRunnerCore(AnnotatedFile input, File output) throws IOException {
		this(input, output, null);
	}
	
	/**
	 * Creates a new task runner core with the specified options. Consider using
	 * {@link #TaskRunnerCore(AnnotatedFile, File, TempFileWriter)} instead, as it provides
	 * the task with more details.
	 * @param input the input file
	 * @param output the final output file
	 * @param tfw a temporary file writer for writing debug copies of intermediary files
	 * @throws IOException if an I/O error occurs
	 */
	public TaskRunnerCore(File input, File output, TempFileWriter tfw) throws IOException {
		this.fj = new TempFileHandler(input, output);
		this.tfw = tfw;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.current = DefaultAnnotatedFile.with(fj.getInput()).extension(input).build();
	}
	
	/**
	 * Creates a new task runner core with the specified options.
	 * @param input the input file
	 * @param output the final output file
	 * @param tfw a temporary file writer for writing debug copies of intermediary files
	 * @throws IOException if an I/O error occurs
	 */
	public TaskRunnerCore(AnnotatedFile input, File output, TempFileWriter tfw) throws IOException {
		this.fj = new TempFileHandler(input.getFile(), output);
		this.tfw = tfw;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.current = DefaultAnnotatedFile.with(input).file(fj.getInput()).build();
	}

	/**
	 * Runs a single tasks or task bundle (if the task is expanding) and returns the
	 * results. If a temporary file handler has been assigned, copies of the intermediary
	 * results are also created.
	 *  
	 * @param task the task to run
	 * @return returns a list of runner results
	 * @throws InternalTaskException if there is a problem with a task
	 * @throws IOException if an I/O error occurs
	 */
	public List<RunnerResult> runTask(InternalTask task) throws InternalTaskException, IOException {
		List<RunnerResult> ret = new ArrayList<>();
		RunnerResult.Builder r = new RunnerResult.Builder(current, task);
		if (task instanceof ExpandingTask) {
			logger.info("Expanding " + task.getName());
			List<InternalTask> exp = ((ExpandingTask)task).resolve(current);
			ret.add(r.success(true).build());
			for (InternalTask t : exp) {
				ret.addAll(runTask(t));
			}
		} else if (task instanceof ReadWriteTask) {
			logger.info("Running (r/w) " + task.getName());
			current = ((ReadWriteTask)task).execute(current, fj.getOutput());
			ret.add(r.success(true).build());
			if (tfw!=null) {
				tfw.writeTempFile(fj.getOutput(), task.getName());
			}
			fj.reset();
		} else if (task instanceof ReadOnlyTask) {
			logger.info("Running (r) " + task.getName());
			((ReadOnlyTask)task).execute(current);
			ret.add(r.success(true).build());
		} else {
			logger.warning("Unknown task type, skipping.");
			ret.add(r.success(false).build());
		}
		return ret;
	}

	@Override
	public void close() throws IOException {
		fj.close();
	}

}
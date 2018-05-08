package org.daisy.streamline.engine;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.tasks.ExpandingTask;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;
import org.daisy.streamline.api.tasks.ReadWriteTask;

abstract class AbstractTaskRunnerCore implements Closeable {
	private static final Logger logger = Logger.getLogger(AbstractTaskRunnerCore.class.getCanonicalName());
	protected final TempFileWriter tfw;
	
	protected AbstractTaskRunnerCore(TempFileWriter tfw) {
		this.tfw = tfw;
	}
	
	protected abstract void writeTempFile(InternalTask task, TempFileWriter tfw) throws IOException;
	
	protected abstract void reset() throws IOException;
	
	//TODO: this is temporary
	protected abstract AnnotatedFile getManifest();
	
	protected abstract void execute(ReadOnlyTask task) throws InternalTaskException;
	
	protected abstract List<InternalTask> execute(ExpandingTask task) throws InternalTaskException;
	
	protected abstract void execute(ReadWriteTask task) throws InternalTaskException;
	
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
		//TODO: what is the input file used for in the runner result? It can't be used for much, because the file name is reused...
		RunnerResult.Builder r = new RunnerResult.Builder(getManifest(), task);
		switch (task.getType()) {
			case EXPANDING:
			{
				logger.info("Expanding " + task.getName());
				List<InternalTask> exp = execute(task.asExpandingTask());
				ret.add(r.success(true).build());
				for (InternalTask t : exp) {
					ret.addAll(runTask(t));
				}
				break;
			}
			case READ_WRITE:
			{
				logger.info("Running (r/w) " + task.getName());
				execute(task.asReadWriteTask());
				ret.add(r.success(true).build());
				if (tfw!=null) {
					writeTempFile(task, tfw);
				}
				reset();
				break;
			}
			case READ_ONLY:
			{
				logger.info("Running (r) " + task.getName());
				execute(task.asReadOnlyTask());
				ret.add(r.success(true).build());
				break;
			}
			default:
			{
				logger.warning("Unknown task type, skipping.");
				ret.add(r.success(false).build());
			}
		}
		return ret;
	}
}

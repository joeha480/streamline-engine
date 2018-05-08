package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.ExpandingTask;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;
import org.daisy.streamline.api.tasks.ReadWriteTask;

/**
 * Provides a utility for running a single task at a time.
 * 
 * @author Joel Håkansson
 */
public class TaskRunnerCore extends AbstractTaskRunnerCore {
	private final FileData fd;
	
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
	 * @deprecated use {@link #TaskRunnerCore(AnnotatedFile, File)}
	 */
	@Deprecated
	public TaskRunnerCore(File input, File output, TempFileWriter tfw) throws IOException {
		this(DefaultAnnotatedFile.with(input).extension(input).build(), output, tfw);
	}
	
	/**
	 * Creates a new task runner core with the specified options.
	 * @param input the input file
	 * @param output the final output file
	 * @param tfw a temporary file writer for writing debug copies of intermediary files
	 * @throws IOException if an I/O error occurs
	 */
	public TaskRunnerCore(AnnotatedFile input, File output, TempFileWriter tfw) throws IOException {
		super(tfw);
		this.fd = new FileData(new TempFileHandler(input.getPath().toFile(), output));
		fd.setCurrent(DefaultAnnotatedFile.with(input).file(fd.getTempFileHandler().getInput().toPath()).build());
	}

	@Override
	public void close() throws IOException {
		fd.getTempFileHandler().close();
	}

	@Override
	protected void writeTempFile(InternalTask task, TempFileWriter tfw) throws IOException {
		tfw.writeTempFile(fd.getTempFileHandler().getOutput(), task.getName());
	}

	@Override
	protected void reset() throws IOException {
		fd.getTempFileHandler().reset();
	}

	@Override
	protected AnnotatedFile getManifest() {
		return fd.getCurrent();
	}

	@Override
	protected void execute(ReadWriteTask task) throws InternalTaskException {
		fd.setCurrent(task.asReadWriteTask().execute(fd.getCurrent(), fd.getTempFileHandler().getOutput()));
	}

	@Override
	protected List<InternalTask> execute(ExpandingTask task) throws InternalTaskException {
		return task.asExpandingTask().resolve(fd.getCurrent());
	}

	@Override
	protected void execute(ReadOnlyTask task) throws InternalTaskException {
		task.execute(fd.getCurrent());
	}

}
package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.BaseFolder;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.media.DefaultFileSet;
import org.daisy.streamline.api.media.FileSet;
import org.daisy.streamline.api.media.ModifiableFileSet;
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
class TaskRunnerCore2 extends AbstractTaskRunnerCore {
	private final FolderData fd;
	
	/**
	 * Creates a new task runner core with the specified options.
	 * @param input the input file
	 * @param output the final output file
	 * @param tfw a temporary file writer for writing debug copies of intermediary files
	 * @throws IOException if an I/O error occurs
	 */
	TaskRunnerCore2(AnnotatedFile input, File output, TempFileWriter tfw) throws IOException {
		super(tfw);
		fd = new FolderData();
		fd.setTempFolderHandler(new TempFolderHandler(f->{
			try {
				Files.copy(fd.getCurrent().getManifest().getPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return Optional.empty();
			} catch (IOException e) {
				return Optional.of(e);
			}
		})); 
		Path manifest = Files.createTempFile(fd.getTempFolderHandler().getInput(), "file", ".tmp");
		Files.copy(input.getPath(), manifest, StandardCopyOption.REPLACE_EXISTING);
		fd.setCurrent(DefaultFileSet.with(BaseFolder.with(fd.getTempFolderHandler().getInput()), DefaultAnnotatedFile.with(input).file(manifest).build()).build());
	}
	
	/**
	 * Creates a new task runner core with the specified options.
	 * @param input the input file set
	 * @param output an action to perform on the resulting file set
	 * @param tfw a temporary file writer for writing debug copies of intermediary files
	 * @throws IOException if an I/O error occurs
	 */
	TaskRunnerCore2(FileSet input, Consumer<FileSet> output, TempFileWriter tfw) throws IOException {
		super(tfw);
		fd = new FolderData();
		fd.setTempFolderHandler(new TempFolderHandler(f->{
			FileSet c = fd.getCurrent();
			if (c.getBaseFolder().getPath().equals(f)) {
				output.accept(c);
				return Optional.empty();
			} else {
				return Optional.of(new IOException("Error in code."));
			}
		}));
		fd.setCurrent(DefaultFileSet.copy(input, BaseFolder.with(fd.getTempFolderHandler().getInput())));
	}

	@Override
	public void close() throws IOException {
		fd.getTempFolderHandler().close();
	}

	@Override
	protected void writeTempFile(InternalTask task, TempFileWriter tfw) throws IOException {
		tfw.writeTempFolder(fd.getCurrent().getBaseFolder().getPath(), task.getName());
	}

	@Override
	protected void reset() throws IOException {
		fd.getTempFolderHandler().reset();
	}

	@Override
	protected AnnotatedFile getManifest() {
		return fd.getCurrent().getManifest();
	}

	@Override
	protected void execute(ReadWriteTask task) throws InternalTaskException {
		ModifiableFileSet mfs = task.execute(fd.getCurrent(), BaseFolder.with(fd.getTempFolderHandler().getOutput()));
		mfs.internalizeBelow(fd.getTempFolderHandler().getInput());
		fd.setCurrent(mfs);
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
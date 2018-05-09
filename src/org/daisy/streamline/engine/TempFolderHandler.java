package org.daisy.streamline.engine;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

/**
 * This class can be used to limit the code needed to handle temporary
 * files in a folder to folder chain. After each step (folder written to)
 * the temporary folders are reset by calling reset() and you are ready
 * to use the folders again.
 * 
 * Note: TempFolderHandler does not work on empty folders. The output folder
 * must contain at least one file when a call to {@link #reset()} is made.
 * 
 * @author Joel Håkansson
 */
public class TempFolderHandler implements Closeable {
	private Path t1;
	private Path t2;
	private final Function<Path, Optional<? extends IOException>> output;
	private boolean toggle;
	
	/**
	 * Constructs a new TempFolderHandler object
	 * 
	 * @param output an action to perform on the output
	 * @throws IOException
	 *             An IOException is thrown if the input does not exist
	 *             or if the input or output is a directory or if the temporary
	 *             folders could not be created.
	 */
	public TempFolderHandler(Function<Path, Optional<? extends IOException>> output) throws IOException {
		this.toggle = true;
		this.output = output;
		this.t1 = PathTools.createTempFolder();
		this.t2 = PathTools.createTempFolder();
	}

	/**
	 * Get the current input folder
	 * 
	 * @return Returns the current input folder or null if TempFolderHandler has been closed
	 */
	public Path getInput() {
		return toggle ? t1 : t2;
	}
	
	/**
	 * Get the current output folder
	 * 
	 * @return Returns the current output folder or null if TempFolderHandler has been closed
	 */
	public Path getOutput() {
		return toggle ? t2 : t1;
	}
	
	/**
	 * Resets the input and output folder before writing to the output again
	 * 
	 * @throws IOException
	 *             An IOException is thrown if TempFolderHandler has been
	 *             closed or if the output folder is empty.
	 */
	public void reset() throws IOException {
		if (t1==null || t2==null) {
			throw new IllegalStateException("Cannot reset after close.");
		}
		if (!isEmpty(getOutput())) {
			toggle = !toggle;
			// reset the new output
			PathTools.deleteRecursive(getOutput(), false);
		} else {
			throw new IOException("Cannot swap to an empty folder.");
		}
	}
	
	private static boolean isEmpty(Path folder) throws IOException {
		try (DirectoryStream<Path> s = Files.newDirectoryStream(folder)) {
			return !s.iterator().hasNext();
		}
	}

	/**
	 * Process the result with the output function and then closes the temporary folders.
	 * Closing the TempFolderHandler is a mandatory last step after which no other
	 * calls to the object should be made.
	 * 
	 * @throws IOException
	 *             An IOException is thrown if the temporary folders have been
	 *             deleted, or are empty.
	 */
	public void close() throws IOException {
		if (t1==null || t2==null) {
			return;
		}
		try {
			if (!isEmpty(getOutput())) {
				Optional<? extends IOException> ex = output.apply(getOutput());
				if (ex.isPresent()) {
					throw ex.get();
				}
			} else if (!isEmpty(getInput())) {
				Optional<? extends IOException> ex = output.apply(getInput());
				if (ex.isPresent()) {
					throw ex.get();
				}
			} else {
				throw new IOException("Corrupted state.");
			}
		} finally {
			PathTools.deleteRecursive(t1);
			PathTools.deleteRecursive(t2);
			t1 = null;
			t2 = null;
		}
	}

}

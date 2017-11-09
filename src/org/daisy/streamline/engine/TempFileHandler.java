package org.daisy.streamline.engine;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Given an initial input file and a final output file, this class can be
 * used to limit the code needed to handle temporary files in a file to file
 * chain. After each step (file written) the temporary files are
 * reset by calling reset() and you are ready to use the files again.
 * Very convenient together with optional steps.
 * 
 * Note: TempFileHandler does not work on zero byte files. The output file
 * must contain data when a call to swap() is made.
 * 
 * @author Joel Håkansson
 */
public class TempFileHandler implements Closeable {
	private File t1;
	private File t2;
	private final File output;
	private boolean toggle;
	
	/**
	 * Constructs a new TempFileHandler object
	 * 
	 * @param input
	 *            An existing input file
	 * @param output
	 *            An output file
	 * @throws IOException
	 *             An IOException is thrown if the input does not exist
	 *             or if the input or output is a directory or if the temporary
	 *             files could not be created.
	 */
	public TempFileHandler(File input, File output) throws IOException {
		if (!input.exists()) {
			throw new FileNotFoundException(input.getAbsolutePath());
		}
		if (!input.isFile() || (output.exists() && !output.isFile())) {
			throw new IOException("Cannot perform this operation on directories.");
		}
		this.toggle = true;
		this.output = output;
		this.t1 = createTempFile();
		this.t2 = createTempFile();
		Files.copy(input.toPath(), this.t1.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	private static File createTempFile() throws IOException {
		File ret = File.createTempFile("temp", null, null);
		ret.deleteOnExit();
		return ret;
	}

	/**
	 * Get the current input file
	 * 
	 * @return Returns the current input file or null if TempFileHandler has
	 *         been closed
	 */
	public File getInput() {
		return toggle ? t1 : t2;
	}
	
	/**
	 * Get the current output file
	 * 
	 * @return Returns the current output file or null if TempFileHandler has
	 *         been closed
	 */
	public File getOutput() {
		return toggle ? t2 : t1;
	}
	
	/**
	 * Resets the input and output file before writing to the output again
	 * 
	 * @throws IOException
	 *             An IOException is thrown if TempFileHandler has been
	 *             closed or if the output file is open or empty.
	 */
	public void reset() throws IOException {
		if (t1==null || t2==null) {
			throw new IllegalStateException("Cannot swap after close.");
		}
		if (getOutput().length()>0) {
			toggle = !toggle;
			// reset the new output to length()=0
			try (OutputStream unused = new FileOutputStream(getOutput())) {
				//this is empty because we only need to close it
			}
		} else {
			throw new IOException("Cannot swap to an empty file.");
		}
	}
	
	/**
	 * Closes the temporary files and copies the result to the output file.
	 * Closing the TempFileHandler is a mandatory last step after which no other
	 * calls to the object should be made.
	 * 
	 * @throws IOException
	 *             An IOException is thrown if the temporary files have been
	 *             deleted, or are empty.
	 */
	public void close() throws IOException {
		if (t1==null || t2==null) {
			return;
		}
		try {
			if (getOutput().length() > 0) {
				Files.copy(getOutput().toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			else if (getInput().length() > 0) {
				Files.copy(getInput().toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			else {
				throw new IOException("Temporary files corrupted.");
			}
		} finally {
			t1.delete();
			t2.delete();
			t1 = null;
			t2 = null;
		}
	}

}

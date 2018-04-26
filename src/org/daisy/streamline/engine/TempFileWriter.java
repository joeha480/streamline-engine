package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides an interface for copying files to a separate folder 
 * for debugging purposes.
 * 
 * @author Joel Håkansson
 */
public interface TempFileWriter {

	/**
	 * Writes a copy of the source file to a temporary folder. The identifier is
	 * intended as a means to identify the file at a later time. Therefore,
	 * an implementation should include the identifier as a part of the copy's file
	 * name (to the extent possible with respect to file system limitations).
	 * The implementation should also attempt to ensure that each created copy is
	 * preserved even when the same information is supplied multiple times, for 
	 * example by appending a time stamp to each file name. 
	 * 
	 * @param source the source file to copy
	 * @param identifier a string that can help identify the file in the temporary folder
	 * @throws IOException if something goes wrong
	 */
	public void writeTempFile(File source, String identifier) throws IOException;
	
	/**
	 * Writes a copy of the source tree to a temporary folder. The identifier is
	 * intended as a means to identify the folder at a later time. Therefore,
	 * an implementation should include the identifier as a part of the copy's folder
	 * name (to the extent possible with respect to file system limitations).
	 * The implementation should also attempt to ensure that each created copy is
	 * preserved even when the same information is supplied multiple times, for 
	 * example by appending a time stamp to each file name. 
	 * 
	 * @param source the source folder to copy
	 * @param identifier a string that can help identify the folder in the temporary folder
	 * @throws IOException if something goes wrong
	 * @throws IllegalArgumentException if source is not an existing folder
	 */
	public void writeTempFolder(Path source, String identifier) throws IOException;

	/**
	 * Deletes all temporary files written by this writer
	 */
	public void deleteTempFiles();
}
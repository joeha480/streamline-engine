package org.daisy.streamline.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a default implementation of {@link TempFileWriter}.
 * @author Joel Håkansson
 */
public class DefaultTempFileWriter implements TempFileWriter {
	/**
	 * Defines a path to a temporary folder. 
	 */
	public static final String TEMP_DIR;
	static {
		String path = System.getProperty("java.io.tmpdir");
		if (path!=null && !"".equals(path) && new File(path).isDirectory()) {
			TEMP_DIR = path;
		} else {
			// user.home is guaranteed to be defined
			TEMP_DIR = System.getProperty("user.home");
		}
	}
	private final Logger logger;
	private final File tempFilesFolder;
	private final String prefix;
	private final List<File> tempFiles;
	private final List<Path> tempFolders;
	private int currentIndex;
	
	/**
	 * Creates a default temp file writer builder.
	 */
	public static class Builder {
		private File tempFilesFolder = new File(TEMP_DIR);
		private String prefix = "";
		/**
		 * Creates a new empty builder.
		 */
		public Builder() {
			super();
		}
		/**
		 * Sets the prefix to use when writing temp files
		 * @param value the value
		 * @return returns this builder
		 */
		public Builder prefix(String value) {
			this.prefix = value;
			return this;
		}
		/**
		 * Sets the folder where to write files. Note that
		 * writing of temporary files is disabled by default.
		 * @param value the folder
		 * @return returns this builder
		 * @throws IllegalArgumentException if <tt>value</tt> is not an existing directory
		 */
		public Builder tempFilesFolder(File value) {
			if (!value.isDirectory()) {
				throw new IllegalArgumentException(value + " is not an existing directory.");
			}
			this.tempFilesFolder = value;
			return this;
		}
		/**
		 * Sets the folder where to write files. Note that
		 * writing of temporary files is disabled by default. If the string
		 * is empty or null, the previously set value is used.
		 * @param value a string representing a folder
		 * @return returns this builder
		 * @throws IllegalArgumentException if <tt>value</tt> is not an existing directory
		 */
		public Builder tempFilesFolder(String value) {
			if (value!=null && !"".equals(value)) {
				tempFilesFolder(new File(value));
			}
			return this;
		}
		/**
		 * Creates a new default temp file writer.
		 * @return returns a new default temp file writer
		 */
		public DefaultTempFileWriter build() {
			return new DefaultTempFileWriter(this);
		}
	}
	
	private DefaultTempFileWriter(Builder builder) {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.tempFilesFolder = builder.tempFilesFolder;
		this.prefix = builder.prefix + "@" + Integer.toHexString((int)(System.currentTimeMillis()-1261440000000l));
		this.tempFiles = new ArrayList<>();
		this.tempFolders = new ArrayList<>();
		this.currentIndex = 0;
	}
	
	private synchronized String makeName(String identifier) {
		String fileNumber = ""+(currentIndex+1);
		currentIndex++;
		while (fileNumber.length()<3) {
			fileNumber = "0" + fileNumber;
		}
		String fileName = (prefix + "-" 
						+ fileNumber + "-" 
						+ truncate(identifier, 20)
					).replaceAll("[^a-zA-Z0-9@\\-]+", "_");
		return fileName;
	}
	
	@Override
	public void writeTempFile(File source, String identifier) throws IOException {
		String fileName = makeName(identifier);
		File f = new File(tempFilesFolder, fileName + ".tmp");
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Writing debug file: " + f);
		}
		Files.copy(source.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
		tempFiles.add(f);
	}
	
	@Override	
	public void writeTempFolder(Path source, String identifier) throws IOException {
		if (!Files.isDirectory(source)) {
			throw new IllegalArgumentException();
		}
		String folderName = makeName(identifier);
		Path target = tempFilesFolder.toPath().resolve(folderName);
		Files.createDirectories(target);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Writing debug file: " + target.toAbsolutePath().toString());
		}
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
				Path targetdir = target.resolve(source.relativize(dir));
				try {
					Files.copy(dir, targetdir);
				} catch (FileAlreadyExistsException e) {
					if (!Files.isDirectory(targetdir)) {
						throw e;
					}
				}
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException
			{
				Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
		tempFolders.add(target);
	}
	
	private String truncate(String str, int pos) {
		if (str.length()>pos) {
			return str.substring(0, pos);
		} else {
			return str;
		}
	}
	
	@Override
	public void deleteTempFiles() {
		for (File f : tempFiles) {
			if (!f.delete()) {
				f.deleteOnExit();
			}
		}
		tempFiles.clear();
		for (Path start : tempFolders) {
			try {
				deleteRecursive(start, true);
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to delete folder: " + start.toAbsolutePath().toString(), e);
			}
		}
		tempFolders.clear();
	}
	
	static void deleteRecursive(Path start) throws IOException {
		deleteRecursive(start, true);
	}
	
	static void deleteRecursive(Path start, boolean deleteStart) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				if (e == null) {
					if (deleteStart || !Files.isSameFile(dir, start)) {
						Files.delete(dir);
					}
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed
					throw e;
				}
			}
		});
	}
}
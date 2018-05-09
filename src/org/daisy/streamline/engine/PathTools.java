package org.daisy.streamline.engine;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Random;

/**
 * Provides path related tools.
 * @author Joel Håkansson
 */
public final class PathTools {
	private static final Random RND = new Random();
	
	private PathTools() {}
	
	/**
	 * Creates a temporary folder.
	 * @return the temporary folder
	 * @throws IOException if a folder could not be created
	 */
	public static Path createTempFolder() throws IOException {
		return createTempFolder("");
	}

	/**
	 * Creates a temporary folder.
	 * @param prefix a folder prefix
	 * @return the temporary folder
	 * @throws IOException if a folder could not be created
	 * @throws NullPointerException if {@code prefix} is null
	 */
	public static Path createTempFolder(String prefix) throws IOException {
		Path parent = Paths.get(System.getProperty("java.io.tmpdir"));
		if (!Files.isDirectory(parent)) {
			throw new IOException("java.io.tmpdir points to a non-existing folder: " + parent);
		}
		Path ret = null;
		int i = 0;
		do {
			ret = parent.resolve(Objects.requireNonNull(prefix)+Long.toHexString(System.currentTimeMillis())+"-"+Integer.toHexString(RND.nextInt()));
			i++;
			if (i>=100) {
				throw new IOException("Failed to create temporary folder.");
			}
		} while (!ret.toFile().mkdirs());
		return ret;
	}

	/**
	 * Deletes files and folders at the specified path, including the start path.
	 * @param start the path
	 * @throws IOException if an I/O occurs 
	 */
	public static void deleteRecursive(Path start) throws IOException {
		deleteRecursive(start, true);
	}
	
	/**
	 * Deletes files and folders at the specified path.
	 * @param start the path
	 * @param deleteStart true if the start path should also be deleted, false if only the contents should be deleted
	 * @throws IOException if an I/O error occurs
	 */
	public static void deleteRecursive(Path start, boolean deleteStart) throws IOException {
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

package org.daisy.streamline.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TempFolderHandlerTest {

	@Test
	public void testFolderHandler() throws IOException {
		Path out = Files.createTempFile("temp", ".tmp");
		out.toFile().deleteOnExit();
		List<String> lines = new ArrayList<>();
		lines.add("test");
		TempFolderHandler tf = new TempFolderHandler(f->{
			try {
				Files.copy(f.resolve("x"), out, StandardCopyOption.REPLACE_EXISTING);
				return Optional.empty();
			} catch (IOException e) {
				return Optional.of(e);
			}
		}); 
		Files.write(tf.getOutput().resolve("x"), lines);
		tf.reset();
		//test
		assertEquals(Files.list(tf.getInput()).count(), 1);
		assertEquals(Files.list(tf.getOutput()).count(), 0);
		tf.close();
		assertNull(tf.getInput());
		assertNull(tf.getOutput());

		assertTrue(Files.size(out)>0);
	}
}

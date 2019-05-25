package net.cadrian.macchiato.ruleset.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

public class TestParser {

	private String read(String path) throws IOException {
		final File file = new File(path);
		if (!file.exists()) {
			return "";
		}

		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			final byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer)) >= 0) {
				result.write(buffer, 0, n);
			}
		}
		return new String(result.toByteArray());
	}

	private void run(final File file, final int expectedStatus) throws IOException {
		final PrintStream oldOut = System.out;
		final PrintStream oldErr = System.err;

		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outStream);
		final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
		final PrintStream err = new PrintStream(errStream);
		System.setOut(out);
		System.setErr(err);

		final int actualStatus;
		try {
			actualStatus = Parser.run(new String[] { file.getAbsolutePath() });
		} finally {
			System.setOut(oldOut);
			System.setErr(oldErr);
		}

		Assert.assertEquals(expectedStatus, actualStatus);
		Assert.assertEquals(read(file + ".out"), new String(outStream.toByteArray()));
		Assert.assertEquals(read(file + ".err"), new String(errStream.toByteArray()));
	}

	private void run(File dir, String suffix, int expectedStatus) throws IOException {
		Assert.assertTrue(dir.isDirectory());
		for (final File file : dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(suffix);
			}
		})) {
			run(file, expectedStatus);
		}
	}

	@Test
	public void testSyntaxOK() throws IOException {
		run(new File("src/test/resources/syntax"), ".ok", 0);
	}

	@Test
	public void testSyntaxKO() throws IOException {
		run(new File("src/test/resources/syntax"), ".ko", 1);
	}

}

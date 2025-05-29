package net.cadrian.macchiato;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRun {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestRun.class);

	private static byte[] read(final String path) throws IOException {
		final File file = new File(path);
		if (!file.exists()) {
			return new byte[0];
		}

		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			final byte[] buffer = new byte[4096];
			int n;
			while ((n = in.read(buffer)) >= 0) {
				result.write(buffer, 0, n);
			}
		}
		return result.toByteArray();
	}

	private static void write(final String path, final byte[] data) throws IOException {
		final File file = new File(path);

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			out.write(data);
			out.flush();
		}
	}

	private void run(final File file, final int expectedStatus) throws IOException {
		LOGGER.debug("******** TESTING [{}] {}", expectedStatus, file);

		final String path = file.getPath();

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
			String midPath = path + ".mid";
			if (!new File(midPath).exists()) {
				midPath = new File(file.getParentFile(), "default.mid").getPath();
			}
			actualStatus = Run.run(new String[] { path, midPath, path + ".out.mid" });
		} catch (RuntimeException | AssertionError e) {
			System.setOut(oldOut);
			System.setErr(oldErr);
			e.printStackTrace();
			Assert.fail(e.getMessage());
			throw e;
		} finally {
			out.flush();
			System.setOut(oldOut);
			err.flush();
			System.setErr(oldErr);
		}

		final byte[] actualOut = outStream.toByteArray();
		LOGGER.debug("out: {}", new String(actualOut));
		write(path + ".out.new", actualOut);
		Assert.assertEquals("Differing stdout: " + path, new String(read(path + ".out")), new String(actualOut));
		final byte[] actualErr = errStream.toByteArray();
		LOGGER.debug("err: {}", new String(actualErr));
		write(path + ".err.new", actualErr);
		Assert.assertEquals("Differing stderr: " + path, new String(read(path + ".err")), new String(actualErr));
		Assert.assertEquals(expectedStatus, actualStatus);
		Assert.assertArrayEquals("Differing MIDI:" + path, read(path + ".ref.mid"), read(path + ".out.mid"));

		// If OK (otherwise keep for forensics)
		new File(path + ".out.mid").delete();
	}

	private void run(final File dir, final String suffix, final int expectedStatus) throws IOException {
		Assert.assertTrue(dir.isDirectory());
		for (final File file : dir.listFiles((FileFilter) file1 -> file1.getName().endsWith(suffix))) {
			run(file, expectedStatus);
		}
	}

	@Test
	public void testRunOK() throws IOException {
		run(new File("src/test/resources/run"), ".ok", 0);
	}

	@Test
	public void testRunKO() throws IOException {
		run(new File("src/test/resources/run"), ".ko", 1);
	}

}

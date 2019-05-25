package net.cadrian.macchiato;

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

public class TestRun {

	private byte[] read(String path) throws IOException {
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

	private void run(final File file, final int expectedStatus) throws IOException {
		String path = file.getPath();

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
			System.setOut(oldOut);
			System.setErr(oldErr);
		}

		Assert.assertEquals("Differing stdout: " + path, new String(read(path + ".out")),
				new String(outStream.toByteArray()));
		Assert.assertEquals("Differing stderr: " + path, new String(read(path + ".err")),
				new String(errStream.toByteArray()));
		Assert.assertEquals(expectedStatus, actualStatus);
		Assert.assertArrayEquals("Differing MIDI:" + path, read(path + ".ref.mid"), read(path + ".out.mid"));

		// If OK (otherwise keep for forensics)
		new File(path + ".out.mid").delete();
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
	public void testRunOK() throws IOException {
		run(new File("src/test/resources/run"), ".ok", 0);
	}

	@Test
	public void testRunKO() throws IOException {
		run(new File("src/test/resources/run"), ".ko", 1);
	}

}

package gov.usgs.ngwmn.dm.io.transform;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import org.junit.Test;

public class DirectWaterlevelCSVOutputStreamTest {

	@Test
	public void testSample() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectWaterlevelCSVOutputStream victim = new DirectWaterlevelCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("20515416303801");
		victim.setElevation(99.999);
		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_20515416303801_WATERLEVEL_ABBREV.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("Depth to Water Below Land Surface in ft"));
		assertTrue("has timestamp", result.contains("1983-08-25"));
		assertTrue("has value", result.contains("up,ft,14.04"));
	}

	@Test
	public void testSkipHeaders() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectWaterlevelCSVOutputStream victim = new DirectWaterlevelCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("20515416303801");
		victim.setElevation(99.999);
		victim.setWrittenHeaders(true);
		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_20515416303801_WATERLEVEL_ABBREV.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertFalse("has header", result.contains("Direction"));
		assertTrue("has timestamp", result.contains("1983-08-25"));
		assertTrue("has value", result.contains("up,ft,14.04"));
	}

	@Test
	public void testEmpty() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectWaterlevelCSVOutputStream victim = new DirectWaterlevelCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("20515416303801");
		victim.setElevation(99.999);
		victim.setWrittenHeaders(false);

		byte[] emptyBuf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<nothing/>\n".getBytes();
		InputStream tis = new ByteArrayInputStream(emptyBuf);

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("Original Direction"));
		assertFalse("has timestamp", result.contains("1983-08-25"));
		assertFalse("has value", result.contains("up,ft,14.04"));

	}

	private static class ClosingByteOutputStream extends ByteArrayOutputStream {
		private boolean closed;

		public ClosingByteOutputStream() {
			super();
			closed = false;
		}

		@Override
		public void close() throws IOException {
			super.close();
			closed = true;
		}

		//		public synchronized void unclose() {
		//			closed = false;
		//		}

		public boolean isClosed() {
			return closed;
		}
	}

	/**
	 * Verify that initial output stream is not closed when the input is closed.
	 * @throws Exception
	 */
	@Test
	public void testJoin() throws Exception {
		ClosingByteOutputStream bos = new ClosingByteOutputStream();
		DirectWaterlevelCSVOutputStream victim1 = new DirectWaterlevelCSVOutputStream(bos);

		victim1.setExecutor(Executors.newSingleThreadExecutor());
		victim1.setAgency("USGS");
		victim1.setSite("SAMPLE");
		victim1.setElevation(99.999);
		victim1.setWrittenHeaders(false);

		InputStream tis1 = getClass().getResourceAsStream("/sample-data/USGS_SAMPLE_WATERLEVEL_COMMAS.xml");

		copy(tis1,victim1);

		victim1.close();

		assertFalse("output closed", bos.isClosed());

		DirectWaterlevelCSVOutputStream victim2 = new DirectWaterlevelCSVOutputStream(bos);
		victim2.setExecutor(Executors.newSingleThreadExecutor());
		victim2.setAgency("USGS");
		victim2.setSite("20515416303801");
		victim2.setElevation(99.999);
		victim2.setWrittenHeaders(true);
		InputStream tis2 = getClass().getResourceAsStream("/sample-data/USGS_20515416303801_WATERLEVEL_ABBREV.xml");

		copy(tis2,victim2);

		victim2.close();

		assertFalse("output closed", bos.isClosed());

		String result = bos.toString();

		assertTrue("has first file", result.contains("SAMPLE"));
		assertTrue("has second file", result.contains("20515416303801"));

	}

	@Test
	public void testComma() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectWaterlevelCSVOutputStream victim = new DirectWaterlevelCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("SAMPLE");
		victim.setElevation(99.999);
		victim.setWrittenHeaders(false);

		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_SAMPLE_WATERLEVEL_COMMAS.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		// System.out.println(result);

		assertTrue("has header", result.contains("Original Unit"));
		assertFalse("has raw input", result.contains("Comment, with embedded commas, and some \"quoted text\" as well"));
		assertTrue("has escaped commas", result.contains("\"Comment, with embedded commas, and some \"\"quoted text\"\" as well\""));

	}

	private void copy(InputStream is, OutputStream os) throws IOException {
		while (true) {
			int c = is.read();
			if (c < 0) {
				break;
			}
			os.write(c);
		}
	}
}

package gov.usgs.ngwmn.dm.io.transform;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.ibm.icu.text.SimpleDateFormat;

public class DirectQualityCSVOutputStreamTest {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	public void testSample() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectQualityCSVOutputStream victim = new DirectQualityCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("402734087033401");
		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_402734087033401_QUALITY.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("Sample Fraction"));
		assertTrue("has timestamp", result.contains("1957-08-27"));
		assertTrue("has timestamp", result.contains("1962-09-12"));
		assertTrue("has timestamp", result.contains("1965-04-14"));
		assertTrue("has timestamp", result.contains("2001-03-29"));
		assertTrue("has value", result.contains("32.0,mg/l"));
	}

	@Test
	public void testSkipHeaders() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectQualityCSVOutputStream victim = new DirectQualityCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("402734087033401");
		victim.setWrittenHeaders(true);
		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_402734087033401_QUALITY.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertFalse("has header", result.contains("Sample Fraction"));
		assertTrue("has timestamp", result.contains("2001-03-29,10:57:00,EST"));
		assertTrue("has value", result.contains("Silica,29.9,mg/l"));
	}

	@Test
	public void testEmpty() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectQualityCSVOutputStream victim = new DirectQualityCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("20515416303801");
		victim.setWrittenHeaders(false);

		byte[] emptyBuf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<nothing/>\n".getBytes();
		InputStream tis = new ByteArrayInputStream(emptyBuf);

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("USGS PCode"));
		assertFalse("has timestamp", result.contains("2001-03-29,10:57:00,EST"));
		assertFalse("has value", result.contains("Total dissolved solids,255,mg/l"));

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
		DirectQualityCSVOutputStream victim1 = new DirectQualityCSVOutputStream(bos);

		victim1.setExecutor(Executors.newSingleThreadExecutor());
		victim1.setAgency("USGS");
		victim1.setSite("402734087033401");
		victim1.setWrittenHeaders(false);

		InputStream tis1 = getClass().getResourceAsStream("/sample-data/USGS_402734087033401_QUALITY.xml");

		copy(tis1,victim1);

		victim1.close();

		assertFalse("output closed", bos.isClosed());

		DirectQualityCSVOutputStream victim2 = new DirectQualityCSVOutputStream(bos);
		victim2.setExecutor(Executors.newSingleThreadExecutor());
		victim2.setAgency("USGS");
		victim2.setSite("385650074531101");
		victim2.setWrittenHeaders(true);
		InputStream tis2 = getClass().getResourceAsStream("/sample-data/USGS_385650074531101_QUALITY.xml");

		copy(tis2,victim2);

		victim2.close();

		assertFalse("output closed", bos.isClosed());

		String result = bos.toString();

		assertTrue("has first file", result.contains("402734087033401"));
		assertTrue("has first file data", result.contains("Magnesium,11.6,mg/l"));
		assertTrue("has second file", result.contains("385650074531101"));
		assertTrue("has second file data", result.contains("USGS TWRI 5-A1/1989"));

	}

	@Test
	public void testDateLimits() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectQualityCSVOutputStream victim = new DirectQualityCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("USGS");
		victim.setSite("402734087033401");
		victim.setBeginDate(sdf.parse("1960-01-01"));
		victim.setEndDate(sdf.parse("1970-01-01"));

		InputStream tis = getClass().getResourceAsStream("/sample-data/USGS_402734087033401_QUALITY.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("Sample Fraction"));
		assertFalse("has timestamp", result.contains("1957-08-27"));
		assertTrue("has timestamp", result.contains("1962-09-12"));
		assertTrue("has timestamp", result.contains("1965-04-14"));
		assertFalse("has timestamp", result.contains("2001-03-29"));
		assertTrue("has value", result.contains("32.0,mg/l"));
	}

	@Test
	public void test_IL_EPA() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DirectQualityCSVOutputStream victim = new DirectQualityCSVOutputStream(bos);

		victim.setExecutor(Executors.newSingleThreadExecutor());
		victim.setAgency("IL_EPA");
		victim.setSite("P406197");
		victim.setBeginDate(sdf.parse("2000-01-01"));
		victim.setEndDate(sdf.parse("2008-01-01"));

		InputStream tis = getClass().getResourceAsStream("/sample-data/IL_EPA_P406197_QUALITY.xml");

		copy(tis,victim);

		victim.close();

		String result = bos.toString();

		assertTrue("has header", result.contains("Sample Fraction"));
		assertTrue("has timestamp", result.contains("2004-01-06"));
		assertTrue(result.contains("RUN TIME PRIOR TO SAMPLING (MIN)"));
		assertTrue(result.contains("COLIFORM (TCR)"));
		assertTrue(result.contains("1,1,1-TRICHLOROETHANE"));
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

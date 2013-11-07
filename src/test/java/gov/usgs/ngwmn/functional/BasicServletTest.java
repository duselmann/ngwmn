package gov.usgs.ngwmn.functional;

import static org.junit.Assert.*;
import static gov.usgs.ngwmn.dm.DataManagerServlet.*;
import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.io.IoUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class BasicServletTest extends ContextualTest {

	private static final String WELL_LIST_EMPTY_FIRST = "http://localhost:8080/ngwmn/data?"+PARAM_AGENCY+"=USGS&"+PARAM_FEATURE+"=403948085414601&"+PARAM_FEATURE+"=403836085374401&"+PARAM_FEATURE+"=402734087033401&"+PARAM_FEATURE+"=402431075020801&"+PARAM_TYPE+"="+WellDataType.CONSTRUCTION;
	private static final String WELL_LIST_AGENCY_DATA = "http://localhost:8080/ngwmn/data?"+PARAM_AGENCY+"=USGS&"+PARAM_FEATURE+"=402734087033401&"+PARAM_FEATURE+"=402431075020801&"+PARAM_TYPE+"="+WellDataType.WATERLEVEL;
	private static final String WELL_LIST_CSV_LOG     = "http://localhost:8080/ngwmn/data?"+PARAM_ENCODING+"=CSV&"+PARAM_AGENCY+"=USGS&"+PARAM_FEATURE+"=402734087033401&"+PARAM_FEATURE+"=402431075020801&"+PARAM_TYPE+"="+WellDataType.LOG;
	private static final String WELL_LIST_CSV         = "http://localhost:8080/ngwmn/data?"+PARAM_ENCODING+"=CSV&"+PARAM_AGENCY+"=USGS&"+PARAM_FEATURE+"=402734087033401&"+PARAM_FEATURE+"=402431075020801&"+PARAM_TYPE+"="+WellDataType.WATERLEVEL+"&"+PARAM_TYPE+"="+WellDataType.LITHOLOGY+"&"+PARAM_TYPE+"="+WellDataType.CONSTRUCTION+"&"+PARAM_TYPE+"="+WellDataType.QUALITY;
	//	private static final String WELL_LIST_CSV         = "http://localhost:8080/ngwmn/data?"+PARAM_ENCODING+"=CSV&"+PARAM_FEATURE+"=USGS:402734087033401&"+PARAM_FEATURE+"=NJGS:2288614&"+PARAM_TYPE+"="+WellDataType.WATERLEVEL;
	private static final String WELL_LIST_DATA        = "http://localhost:8080/ngwmn/data?"+PARAM_FEATURE+"=USGS:402734087033401&"+PARAM_FEATURE+"=NJGS:2288614&"+PARAM_TYPE+"="+WellDataType.WATERLEVEL;
	/* Not meaningful
	private static final String WELL_WITH_DATA        = "http://localhost:8080/ngwmn/data?"+PARAM_AGENCY+"=USGS&"+PARAM_FEATURE+"=402734087033401&"+PARAM_TYPE+"="+WellDataType.ALL; // TODO ALL asdf
	private static final String WELL_NO_DATA          = "http://localhost:8080/ngwmn/data?"+PARAM_AGENCY+"=NJGS&"+PARAM_FEATURE+"=2288614&"+PARAM_TYPE+"="+WellDataType.ALL; // TODO ALL asdf
	 */
	@BeforeClass
	public static void clearCache() {
		// TODO on windows this is not the final destination for the cache. it is relative to the working dir
		File c = new File( getBaseDir() );

		if (c.exists() && c.isDirectory()) {
			for (File f : c.listFiles()) {
				if ( f.canWrite() ) {
					boolean did = f.delete();
					if (did) {
						System.out.printf("Deleted cache file %s\n", f);
					} else {
						System.out.printf("Could not delete cache file %s\n", f);
						f.deleteOnExit(); // this way subsequent runs might work
					}
				} else {
					System.out.printf("Preserving cache file %s\n", f);
				}
			}
		}
	}

	@Override
	public void preTest() throws Exception {
		System.out.println("beforeOnce - checking sites used in these tests.");

		checkSiteIsVisible("NJGS","2288614");
		checkSiteIsVisible("USGS","400204074145401");
		checkSiteIsVisible("USGS","402734087033401");
	}

	@Test
	public void test_listOfCsvWithFirstTwoMissingData_testingHeadersWrittenOnSubsequentSites() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		System.out.printf("test_listOfCsvWithFirstTwoMissingData_testingHeadersWrittenOnSubsequentSites URL: %s\n", WELL_LIST_EMPTY_FIRST);

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_LIST_EMPTY_FIRST);
		System.out.printf("test_listOfCsvWithFirstTwoMissingData_testingHeadersWrittenOnSubsequentSites URL: %s\n", req.getURL());

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		//		assertTrue("response size too big", body.length() < 850);
		//		assertTrue("response size too small", body.length() > 100);

		File file = new File("/tmp","dataHeaderTest.zip");
		FileOutputStream fos = new FileOutputStream(file);
		ByteStreams.copy(resp.getInputStream(), fos);
		fos.flush();
		fos.close();
		FileInputStream fis = new FileInputStream(file);
		int available = fis.available();
		IoUtil.quiteClose(fis);
		int bodyLenth = body.length();
		assertEquals("response size", available, bodyLenth);
	}

	@Test
	public void test_listOfCsvLogEncodedData() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		System.out.printf("test_listOfCsvLogEncodedData URL: %s\n", WELL_LIST_CSV_LOG);

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_LIST_CSV_LOG);
		System.out.printf("test_listOfCsvLogEncodedData URL: %s\n", req.getURL());

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		assertTrue("response size too big", body.length() < 70000);
		assertTrue("response size too small", body.length() > 64000);

		File file = new File("/tmp","dataCsvLOG.zip");
		FileOutputStream fos = new FileOutputStream(file);
		ByteStreams.copy(resp.getInputStream(), fos);
		fos.flush();
		fos.close();
		FileInputStream fis = new FileInputStream(file);
		int available = fis.available();
		IoUtil.quiteClose(fis);
		int bodyLenth = body.length();
		assertEquals("response size", available, bodyLenth);
	}

	@Test
	public void test_listOfCsvEncodedData() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		System.out.printf("test_listOfCsvEncodedData URL: %s\n", WELL_LIST_CSV);

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_LIST_CSV);
		System.out.printf("test_listOfCsvEncodedData URL: %s\n", req.getURL());

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		assertTrue("response size", body.length() > 6000);

		File file = new File("/tmp","dataCsv.zip");
		FileOutputStream fos = new FileOutputStream(file);
		ByteStreams.copy(resp.getInputStream(), fos);
		fos.flush();
		fos.close();
		FileInputStream fis = new FileInputStream(file);
		int available = fis.available();
		IoUtil.quiteClose(fis);
		int bodyLenth = body.length();
		assertEquals("response size", available, bodyLenth);
	}

	@Test
	public void test_listOfWells() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_LIST_DATA);
		System.out.printf("test_listOfWells URL: %s\n", req.getURL());

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		// not much data there, and it compresses down pretty good
		assertTrue("response size", body.length() > 100);

		File file = new File("/tmp","data.zip");
		FileOutputStream fos = new FileOutputStream(file);
		ByteStreams.copy(resp.getInputStream(), fos);
		fos.flush();
		fos.close();
		FileInputStream fis = new FileInputStream(file);
		int available = fis.available();
		IoUtil.quiteClose(fis);
		int bodyLenth = body.length();
		assertEquals("response size", available, bodyLenth);
	}

	@Test
	public void test_listOfWells_forSingleAgency() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_LIST_AGENCY_DATA);
		System.out.printf("test_listOfWells_forSingleAgency URL: %s\n", req.getURL());

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);


		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		// Not much data, and it compressed good.
		assertTrue("response size", body.length() > 100);

		File file = new File("/tmp","data2.zip");
		FileOutputStream fos = new FileOutputStream(file);
		ByteStreams.copy(resp.getInputStream(), fos);
		fos.flush();
		fos.close();
		FileInputStream fis = new FileInputStream(file);
		int available = fis.available();
		IoUtil.quiteClose(fis);
		int bodyLenth = body.length();
		assertEquals("response size", available, bodyLenth);
	}


	/*	@Test
	public void testWithData() throws Exception {
		ServletRunner sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest(WELL_WITH_DATA);
		System.out.printf("testWithData URL: %s\n", req.getURL());

		WebResponse resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("testWithData contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		assertTrue("response size", body.length() > 1000);
	}
	 */

	/*	@Test
	public void testWithNoData() throws Exception {
		// this site exists, but has no data (on 2012/03/23)
		ServletRunner sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest(WELL_NO_DATA);
		System.out.printf("testWithNoData URL: %s\n", req.getURL());

		WebResponse resp = sc.getResponse(req);
		assertNotNull("response", resp);

		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());

		// TODO We would prefer to get an HTTP error code here.
		assertTrue("response size small 100 > " +body.length(), body.length() > 100); // was 1000 but csv format is much smaller than the original test result
	}
	 */

	@Test(expected=HttpNotFoundException.class)
	public void testNonSite() throws Exception {
		// this site does not exist, so we expect an Exception when fetching
		ServletRunner sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest("http://localhost:8080/ngwmn/data?"+PARAM_FEATURE+"=NOSUCHSITE&"+PARAM_AGENCY+"=USGS");
		System.out.printf("testNonSite URL: %s\n", req.getURL());

		sc.getResponse(req);
		assertFalse("expected exception", true);
	}

	/*
    @Test(expected=HttpInternalErrorException.class)
	public void testIOError() throws Exception {
		// special test specifier, causes IO exception
		ServletRunner sr = new ServletRunner(this.getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest("http://localhost:8080/ngwmn/data?"+PARAM_FEATURE+"=NOSUCHSITE&"+PARAM_AGENCY+"=TEST_INPUT_ERROR");
		sc.getResponse(req);
		assertFalse("expected exception", true);
	}
	 */

	/*	// Now repeat the tests; we expect to get cached results
	@Test(timeout=5000) // TODO why is this sometime this slow on Windows
	public void testWithData_fromCacheShouldBeFast() throws Exception {
		testWithData();
	}
	 */

	/*	@Test(timeout=5000) // TODO why is this sometime this slow on Windows
	public void testWithNoData_fromCacheShouldBeFast() throws Exception {
		testWithNoData();
	}
	 */

	@Test(expected=HttpNotFoundException.class,timeout=5000)
	public void testNonSite_fromCacheShouldBeFast() throws Exception {
		testNonSite();
	}


}

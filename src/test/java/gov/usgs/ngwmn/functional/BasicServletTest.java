package gov.usgs.ngwmn.functional;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class BasicServletTest {

	private static final String WELL_WITH_DATA = "http://localhost:8080/ngwmn/data?agency_cd=USGS&featureID=402734087033401";
	private static final String WELL_NO_DATA = "http://localhost:8080/ngwmn/data?agency_cd=NJGS&featureID=2288614";

	@BeforeClass
	public static void clearCache() {
		File c = new File("/tmp/gwdp-cache");
		if (c.exists() && c.isDirectory()) {
			for (File f : c.listFiles()) {
				if (f.canWrite()) {
					boolean did = f.delete();
					if (did) {
						System.out.printf("Deleted cache file %s\n", f);
					} else {
						System.out.printf("Could not delete cache file %s\n", f);
					}
				} else {
					System.out.printf("Preserving cache file %s\n", f);
				}
			}
		}
	}
	
	@BeforeClass
	public static void setupNaming() throws Exception {
		final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
		String dataSource = "java:comp/env/jdbc/GW_DATA_PORTAL";
		// TODO Set up data source for servlet.
		
		try {
			builder.activate();
		} catch (IllegalStateException ise) {
			// already had a naming provider; ignore
		}
	}

	
	@Test
	public void testWithData() throws Exception {
		ServletRunner sr = new ServletRunner(this.getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");
		
		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest(WELL_WITH_DATA);
		WebResponse resp = sc.getResponse(req);
		assertNotNull("response", resp);
		
		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		assertTrue("response size", body.length() > 10000);
	}

	@Test
	public void testWithNoData() throws Exception {
		// this site exists, but has no data (on 2012/03/23)
		ServletRunner sr = new ServletRunner(this.getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");
		
		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest(WELL_NO_DATA);
		WebResponse resp = sc.getResponse(req);
		assertNotNull("response", resp);
		
		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		
		// TODO We would prefer to get an HTTP error code here.
		assertTrue("response size", body.length() > 1000);
	}

	@Test(expected=HttpNotFoundException.class)
	public void testNonSite() throws Exception {
		// this site does not exist, so we expect an Exception when fetching
		ServletRunner sr = new ServletRunner(this.getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");
		
		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest("http://localhost:8080/ngwmn/data?featureID=NOSUCHSITE&agency_cd=USGS");
		WebResponse resp = sc.getResponse(req);
		assertFalse("expected exception", true);
	}
	
/*	
    @Test(expected=HttpInternalErrorException.class)
	public void testIOError() throws Exception {
		// special test specifier, causes IO exception
		ServletRunner sr = new ServletRunner(this.getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");
		
		ServletUnitClient sc = sr.newClient();
		WebRequest req = new GetMethodWebRequest("http://localhost:8080/ngwmn/data?featureID=NOSUCHSITE&agency_cd=TEST_INPUT_ERROR");
		WebResponse resp = sc.getResponse(req);
		assertFalse("expected exception", true);
	}
*/

	// Now repeat the tests; we expect to get cached results
	@Test(timeout=1000)
	public void testWithData_2() throws Exception {
		testWithData();
	}
	
	@Test(timeout=1000)
	public void testWithNoData_2() throws Exception {
		testWithNoData();
	}

	@Test(expected=HttpNotFoundException.class,timeout=1000)
	public void testNonSite_2() throws Exception {
		testNonSite();
	}

}

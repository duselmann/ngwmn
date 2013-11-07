package gov.usgs.ngwmn.functional;

import static org.junit.Assert.*;
import gov.usgs.ngwmn.dm.dao.ContextualTest;

import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

public class RESTfulDataServletIntegrationTest extends ContextualTest {

	private static final String WELL_WITH_DATA        = "http://localhost:8080/ngwmn/data/USGS/402734087033401/WATERLEVEL";
	private static final String IL_EPA_WELL        = "http://localhost:8080/ngwmn/data/IL_EPA/P405805/WATERLEVEL";
	private static final String IL_EPA_WELL_20        = "http://localhost:8080/ngwmn/data/IL%20EPA/P405805/WATERLEVEL";
	private static final String IL_EPA_WELL_SP        = "http://localhost:8080/ngwmn/data/IL EPA/P405805/WATERLEVEL";


	@Override
	public void preTest() throws Exception {
		System.out.println("beforeOnce - checking sites used in these tests.");

		checkSiteIsVisible("USGS","402734087033401");

		HttpUnitOptions.setScriptingEnabled(false);
	}

	@Test
	public void test_waterlevel_usgs() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();
		WebRequest       req = new GetMethodWebRequest(WELL_WITH_DATA);

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		// TODO check response code
		for (String hn : resp.getHeaderFieldNames()) {
			System.out.printf("Header %s:%s\n", hn, resp.getHeaderField(hn));
		}
		String body = resp.getText();
		System.out.printf("contentLength=%d,size=%d\n", resp.getContentLength(), body.length());
		//		assertTrue("response size too big", body.length() < 850);
		//		assertTrue("response size too small", body.length() > 100);

		int bodyLength = body.length();
		assertTrue("body has some body", bodyLength > 1);

		// TODO check it's well-formed XML
	}

	private String body(ServletUnitClient sc, String url) throws Exception {
		WebRequest       req = new GetMethodWebRequest(url);

		WebResponse     resp = sc.getResponse(req);
		assertNotNull("response", resp);

		String body = resp.getText();

		return body;
	}

	@Test
	public void test_waterlevel_il_epa() throws Exception {
		ServletRunner     sr = new ServletRunner( getClass().getResourceAsStream("/servlet-test-web.xml"), "/ngwmn");

		ServletUnitClient sc = sr.newClient();

		String b = body(sc,IL_EPA_WELL);
		assertNotNull(b);

		b = body(sc, IL_EPA_WELL_20);
		assertNotNull(b);

		b = body(sc, IL_EPA_WELL_SP);
		assertNotNull(b);
	}

}

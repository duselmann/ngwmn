package gov.usgs.ngwmn.admin;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import gov.usgs.ngwmn.admin.CSVController.WLSample;
import gov.usgs.ngwmn.dm.dao.ContextualTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class CSVControllerIntegrationTest extends ContextualTest {

	private CSVController victim;

	@Before
	public void setup() {
		victim = ctx.getBean(CSVController.class);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testTable() throws Exception {
		Writer w = new StringWriter();
		victim.generateTable("USGS", "402734087033401", w);

		assertFalse("empty", w.toString().isEmpty());
	}

	@Test
	public void testJSON() throws Exception {
		@SuppressWarnings("deprecation")
		List<WLSample> json = victim.generateJSON("USGS", "402734087033401");
		assertNotNull(json);
		assertFalse("empty", json.isEmpty());
		for (WLSample sample : json) {
			assertTrue("non-negative", sample.value.signum() >= 0);
		}
	}

	@Test
	public void testJSONpcode() throws Exception {
		@SuppressWarnings("deprecation")
		List<WLSample> json = victim.generateJSON("USGS", "392754074270101");
		assertNotNull(json);
		assertFalse("empty", json.isEmpty());
		for (WLSample sample : json) {
			assertTrue("non-negative", sample.value.signum() >= 0);
			assertNotNull("pcode", sample.pcode);
		}
	}

	@Test
	public void testFlatXML() throws Exception {
		MockHttpServletResponse resp = new MockHttpServletResponse();

		victim.flatXML("USGS", "392754074270101",resp);

		String contentType = resp.getContentType();
		assertEquals("text/xml", contentType);

		String content = resp.getContentAsString();

		assertFalse("empty", content.isEmpty());
		assertTrue("direction", content.contains("direction"));
		assertTrue("mediated-value", content.contains("mediated-value"));
		assertTrue("agency", content.contains("USGS"));
		assertTrue("site", content.contains("392754074270101"));

	}
}

package gov.usgs.ngwmn.dm.harvest;

import static org.junit.Assert.*;
import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.spec.Specifier;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:urlFactoryContext.xml"})
public class UrlFactoryTest {

	private static final String BASE_URL = "http://cida-eros-ngwmndev.er.usgs.gov:8080/cocoon";

	@Autowired
	protected ApplicationContext ctx;

	private SpringUrlFactory victim;

	@BeforeClass
	public static void setSystemProperty() {
		System.setProperty("ngwmn_cocoon", BASE_URL);
	}

	@Before
	public void setUp() throws Exception {
		victim = ctx.getBean(SpringUrlFactory.class);
	}

	@Test
	public void testALL() {
		WellDataType t = WellDataType.ALL;

		String u = victim.resolve(t, "MBMG", "1388");

		assertEquals(BASE_URL + "/gin/gwdp/cache/download/xls/MBMG?featureId=1388", u);
	}

	@Test
	public void testWQ() {
		WellDataType t = WellDataType.QUALITY;

		String u = victim.resolve(t, "MBMG", "1388");

		assertEquals(BASE_URL + "/gin/gwdp/agency/MBMG/qw?mimeType=xml&siteid=1388", u);
	}

	@Test
	public void testWQ_for_MN_DNR() {
		WellDataType t = WellDataType.QUALITY;

		String u = victim.resolve(t, "MN DNR", "210308");

		assertEquals(BASE_URL + "/gin/gwdp/agency/MN_DNR/qw?mimeType=xml&siteid=210308", u);
	}

	@Test
	public void test_makeUrl_forWaterQualityData() {
		Specifier  spec = new Specifier("USGS","WELL0",WellDataType.QUALITY);
		String url = victim.makeUrl(spec);
		System.out.println(url);
		assertTrue(url.contains("qw?"));
	}

	@Test
	public void test_makeUrl_forWaterLevelData() {
		Specifier  spec = new Specifier("USGS","WELL0",WellDataType.WATERLEVEL);
		String url = victim.makeUrl(spec);
		System.out.println(url);
		assertTrue(url.contains("sos?"));
	}

	@Test
	public void test_makeUrl_forLogData() {
		Specifier  spec = new Specifier("USGS","WELL0",WellDataType.LOG);
		String url = victim.makeUrl(spec);
		System.out.println(url);
		assertTrue(url.contains("wfs?"));
	}

	@Test
	public void test_makeUrl_containsAgencyAndFeature() {
		Specifier  spec = new Specifier("USGS","WELL0",WellDataType.LOG);
		String url = victim.makeUrl(spec);
		assertTrue(url.contains(spec.getAgencyID()));
		assertTrue(url.contains(spec.getFeatureID()));
	}

	@Test
	public void test_makeUrl_startsWithBaseUrl() {
		Specifier  spec = new Specifier("USGS","WELL0",WellDataType.LOG);
		String url = victim.makeUrl(spec);
		assertTrue("generated url is " + url, url.startsWith(BASE_URL));
	}

}

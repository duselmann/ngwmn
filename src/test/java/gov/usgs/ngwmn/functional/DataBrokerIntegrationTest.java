package gov.usgs.ngwmn.functional;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.DataBroker;
import gov.usgs.ngwmn.dm.SiteNotFoundException;
import gov.usgs.ngwmn.dm.cache.Cache;
import gov.usgs.ngwmn.dm.cache.CacheInfo;
import gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache;
import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.dao.FetchLog;
import gov.usgs.ngwmn.dm.dao.FetchLogDAO;
import gov.usgs.ngwmn.dm.io.Pipeline;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.spec.Specifier;

import org.junit.Before;
import org.junit.Test;

public class DataBrokerIntegrationTest extends ContextualTest {

	private static final String AGENCY_CD = "USGS";
	private static final String SITE_NO = "402734087033401";
	private static final String SILLY_SITE_NO = "007";

	private static final long TIMESLOP = 1000;
	private DataBroker dataBroker;
	private Cache qualityCache;
	//	private Cache fileCache;
	private FetchLogDAO fetchLogDAO;
	private DatabaseXMLCache logCache;

	@Before
	public void setUp() throws Exception {
		//FileCache c = ctx.getBean("FileCache",  FileCache.class);
		dataBroker   = ctx.getBean("DataBroker",   DataBroker.class);
		qualityCache = ctx.getBean("QualityCache", Cache.class);
		//		fileCache    = ctx.getBean("FileCache",    FileCache.class);
		fetchLogDAO  = ctx.getBean("FetchLogDAO",  FetchLogDAO.class);

		logCache     = ctx.getBean("LogCache",     DatabaseXMLCache.class);
	}

	private Specifier makeSpec(String agency, String site, WellDataType dt) {
		Specifier spec = new Specifier(agency,site,dt);
		return spec;
	}

	private Specifier makeSpec(String agency, String site) {
		return makeSpec(agency, site, WellDataType.WATERLEVEL);
	}

	@Test
	public void testSiteNotFound() throws Exception {

		Specifier spec = makeSpec(AGENCY_CD,"no-such-site");

		try {
			dataBroker.checkSiteExists(spec);
		} catch (SiteNotFoundException ok) {
			assertTrue("Expected exception", true);
		}
	}

	@Test
	public void testSiteFound() throws Exception {

		Specifier spec = makeSpec(AGENCY_CD,SITE_NO);

		try {
			dataBroker.checkSiteExists(spec);
			assertTrue(true);
		} catch (SiteNotFoundException ok) {
			assertFalse(true);
		}
	}

	@Test
	public void testPrefetch_QUALITY() throws Exception {
		// allow for some slop in the clock
		Date bot = new Date(System.currentTimeMillis() - TIMESLOP);
		Specifier spec = makeSpec(AGENCY_CD,SITE_NO, WellDataType.QUALITY);

		long ct = dataBroker.prefetchWellData(spec);

		assertTrue("got bytes", ct > 100);

		CacheInfo info = qualityCache.getInfo(spec);
		assertTrue("cache exists", info.isExists());
		System.out.printf("now %s, modified %s\n", bot, info.getModified());
		assertTrue("is recent",  ! info.getModified().before(bot));
		// assertEquals("cached size", ct, info.getLength());
		assertTrue("cached size (may have expanded)", info.getLength() >= ct);
	}

	// @Test
	public void testPrefetch_fail() throws Exception {
		// need a valid well that will return an HTTP error when we try to prefetch.
		Specifier spec = makeSpec(AGENCY_CD,SILLY_SITE_NO, WellDataType.QUALITY);

		dataBroker.prefetchWellData(spec);

		// fetch log should show error

		FetchLog mr = fetchLogDAO.mostRecent(spec.getWellRegistryKey());

		assertNotNull("reported problem", mr.getProblem());
	}

	@Test
	public void testFetchWellData() throws Exception {
		Specifier spec = new Specifier(AGENCY_CD,SITE_NO,WellDataType.LOG);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		Supplier<OutputStream> out = new SimpleSupplier<OutputStream>(bos);
		dataBroker.fetchWellData(spec, out);

		assertTrue("expect well data is cached", logCache.contains(spec));
	}

	@Test
	public void testEmpty_real_site() throws Exception {
		Specifier spec = new Specifier(AGENCY_CD,SITE_NO,WellDataType.LITHOLOGY);

		Pipeline p = new Pipeline(spec);

		boolean v = dataBroker.checkForEmpty(spec, p);
		System.out.printf("empty check of %s returns %s", spec, v);

		assertTrue("survived", true);

	}

	@Test
	public void testEmpty_silly_site() throws Exception {
		Specifier spec = new Specifier(AGENCY_CD,SILLY_SITE_NO,WellDataType.LITHOLOGY);

		Pipeline p = new Pipeline(spec);

		boolean v = dataBroker.checkForEmpty(spec, p);
		System.out.printf("empty check of %s returns %s", spec, v);

		assertTrue("survived", true);

	}
}

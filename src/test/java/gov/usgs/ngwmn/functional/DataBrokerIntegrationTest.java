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
import gov.usgs.ngwmn.dm.cache.Loader;
import gov.usgs.ngwmn.dm.cache.Retriever;
import gov.usgs.ngwmn.dm.cache.fs.FileCache;
import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.harvest.WebRetriever;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.spec.Specifier;

import org.junit.Before;
import org.junit.Test;

public class DataBrokerIntegrationTest extends ContextualTest {

	private static final String AGENCY_CD = "USGS";
	private static final String SITE_NO = "402734087033401";
	private DataBroker dataBroker;
	private Cache qualityCache;
	private Cache fileCache;
	
	@Before
	public void setUp() throws Exception {
		FileCache c = ctx.getBean("FileCache",  FileCache.class);
		dataBroker  = ctx.getBean("DataBroker", DataBroker.class);		
		qualityCache = ctx.getBean("QualityCache", Cache.class);
		fileCache = ctx.getBean("FileCache", Cache.class);
	}

	private Specifier makeSpec(String agency, String site, WellDataType dt) {
		Specifier spec = new Specifier(agency,site,dt);
		return spec;
	}
	
	private Specifier makeSpec(String agency, String site) {
		return makeSpec(agency, site, WellDataType.ALL);
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
	public void testPrefetch_ALL() throws Exception {
		Date bot = new Date();
		
		Specifier spec = makeSpec(AGENCY_CD,SITE_NO);

		long ct = dataBroker.prefetchWellData(spec);
		
		assertTrue("got bytes", ct > 100);
		
		CacheInfo info = fileCache.getInfo(spec);
		assertTrue("cache exists", info.isExists());
		assertTrue("is recent",  ! info.getModified().before(bot));
		assertEquals("cached size", ct, info.getLength());
	}
	
	@Test
	public void testPrefetch_QUALITY() throws Exception {
		Date bot = new Date();
		Specifier spec = makeSpec(AGENCY_CD,SITE_NO, WellDataType.QUALITY);

		long ct = dataBroker.prefetchWellData(spec);
		
		assertTrue("got bytes", ct > 100);
		
		CacheInfo info = qualityCache.getInfo(spec);
		assertTrue("cache exists", info.isExists());
		System.out.printf("now %s, modified %s\n", bot, info.getModified());
		assertTrue("is recent",  ! info.getModified().before(bot));
		assertEquals("cached size", ct, info.getLength());
	}

	@Test
	public void testFetchWellData() throws Exception {
		Specifier spec = new Specifier(AGENCY_CD,SITE_NO,WellDataType.ALL);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		Supplier<OutputStream> out = new SimpleSupplier<OutputStream>(bos);
		dataBroker.fetchWellData(spec, out);
		
		Cache cache = ctx.getBean("FileCache", Cache.class);
		
		assertTrue("expect well data is cached", cache.contains(spec));
	}
	

}

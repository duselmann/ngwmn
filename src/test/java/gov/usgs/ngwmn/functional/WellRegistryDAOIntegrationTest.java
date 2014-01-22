package gov.usgs.ngwmn.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.dao.WellRegistry;
import gov.usgs.ngwmn.dm.dao.WellRegistryDAO;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WellRegistryDAOIntegrationTest extends ContextualTest {
	// test well: MN DNR:200661 
	private static final String AGENCY_CD = "MN DNR";
	private static final String SITE_NO = "200661";
	private static final String MT = "30";
	private static final String MN = "27";

	private WellRegistryDAO dao;

	@Before
	public void setUp() throws Exception {
		dao = ctx.getBean("WellRegistryDAO", WellRegistryDAO.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSelectAll() {
		List<WellRegistry> all = dao.selectAll();
		assertNotNull("all", all);
		assertFalse("empty", all.isEmpty());
		
		for (WellRegistry w : all) {
			assertEquals("display flag", "1", w.getDisplayFlag());
		}
	}
	
	@Test
	public void testFindOne() {
		WellRegistry w = dao.findByKey(AGENCY_CD, SITE_NO);
		assertNotNull("well",w);
		assertEquals("agency", AGENCY_CD, w.getAgencyCd());
		assertEquals("site", SITE_NO, w.getSiteNo());
	}

	@Test
	public void testFindNone() {
		WellRegistry w;
		w = dao.findByKey("no-such-agency", SITE_NO);
		assertNull("well",w);
		w = dao.findByKey(AGENCY_CD, "no-such-well");
		assertNull("well",w);
	}
	
	@Test
	public void testSelectByAgency() {
		List<WellRegistry> ww = dao.selectByAgency(AGENCY_CD);
		assertNotNull("all", ww);

		System.out.printf("agency well count: %d for %s\n", ww.size(), AGENCY_CD);
		
		assertFalse("empty", ww.isEmpty());
		assertFalse("too many", ww.size() > 100);
		
		for (WellRegistry w : ww) {
			assertEquals("agency cd", AGENCY_CD, w.getAgencyCd());
			assertEquals("display flag", "1", w.getDisplayFlag());
		}
	}
	
	@Test
	public void testSelectByState_Montana() {
		List<WellRegistry> ww = dao.selectByState(MT);
		assertNotNull("by state", ww);
		assertFalse("empty", ww.isEmpty());
		assertFalse("too many", ww.size() > 1000);
		
		System.out.printf("MT well count: %d\n", ww.size());
		
		for (WellRegistry w : ww) {
			assertEquals("State cd", MT, w.getStateCd());
			assertTrue("agency name contains state name", w.getAgencyNm().contains("Montana"));
			assertEquals("display flag", "1", w.getDisplayFlag());
		}
	}
	
	@Test
	public void testSelectByState_Minnesota() {
		List<WellRegistry> ww = dao.selectByState(MN);
		assertNotNull("by state", ww);
		assertFalse("empty", ww.isEmpty());
		assertFalse("too many", ww.size() > 1000);
		
		System.out.printf("MN well count: %d\n", ww.size());
		
		for (WellRegistry w : ww) {
			assertEquals("State cd", MN, w.getStateCd());
			assertTrue("agency name contains state name", w.getAgencyNm().contains("Minnesota"));
			assertEquals("display flag", "1", w.getDisplayFlag());
		}
	}
	
	@Test
	public void testSelectNoneAgency() {
		List<WellRegistry> ww = dao.selectByAgency("no-such-agency");
		assertNotNull("all", ww);
		assertTrue("empty", ww.isEmpty());
	}

	@Test
	public void testAgencies() {
		List<String> aa = dao.agencies();
		assertTrue("has USGS", aa.contains("USGS"));
		assertFalse("contains Silly", aa.contains("Silly"));
	}
	
}

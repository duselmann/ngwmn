package gov.usgs.ngwmn.dm.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FetchLogDAOIntegrationTest extends ContextualTest {

	private static final String AGENCY_CD = "USGS";
	private static final String SITE_NO = "007";
	private FetchLogDAO dao;
	
	@Before
	public void setUp() throws Exception {
		dao = ctx.getBean("FetchLogDAO", FetchLogDAO.class);
	}

	@Test
	public void testInsertId() {
		FetchLog entry = insertWithCt(System.currentTimeMillis());
		assertNotNull("id after insert", entry.getFetchlogId());
		
		System.out.printf("id after insert: %d\n", entry.getFetchlogId());
	}

	protected FetchLog insertWithCt(long ct) {
		FetchLog entry = new FetchLog();
		// NJGS:2288614
		entry.setAgencyCd(AGENCY_CD);
		entry.setSiteNo(SITE_NO);
		entry.setCt(ct);
		entry.setFetcher(getClass().getSimpleName());
		dao.insertId(entry);
		return entry;
	}
	
	@Test
	public void testBigProblem() {
		StringBuilder pb = new StringBuilder();
		while (pb.length() < 500) {
			pb.append("Too long a problem\n");
		}
		String problem = pb.toString();
		
		FetchLog entry = new FetchLog();
		// NJGS:2288614
		entry.setAgencyCd(AGENCY_CD);
		entry.setSiteNo(SITE_NO);
		entry.setCt(null);
		entry.setFetcher(getClass().getSimpleName());
		entry.setProblem(problem);
		dao.insertId(entry);
		assertNotNull(entry.getFetchlogId());
		assertTrue("has a problem", entry.getProblem().startsWith("Too long a problem"));
	}
	
	@Test
	public void testSelectByWell() {
		long ct = System.currentTimeMillis();
		insertWithCt(ct);
		
		WellRegistryKey key = new WellRegistryKey(AGENCY_CD, SITE_NO);
		
		List<FetchLog> ff = dao.byWell(key);
		assertFalse("empty", ff.isEmpty());
		boolean gotCt = false;
		for (FetchLog f : ff) {
			assertEquals(AGENCY_CD,f.getAgencyCd());
			assertEquals(SITE_NO, f.getSiteNo());
			if (f.getCt() != null && f.getCt().longValue() == ct) {
				gotCt = true;
			}
		}
		assertTrue("expected to get an entry with my count", gotCt);
	}

}

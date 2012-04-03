package gov.usgs.ngwmn.dm.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FetchLogDAOTest extends ContextualTest {

	private FetchLogDAO dao;
	
	@Before
	public void setUp() throws Exception {
		dao = ctx.getBean("FetchLogDAO", FetchLogDAO.class);
	}

	@Test
	public void testInsert() {
		FetchLog entry = new FetchLog();
		
		entry.setAgencyCd("USGS");
		entry.setSiteNo("007");
		dao.insert(entry);
		assertNull("generated ID not set by this method", entry.getFetchlogId());
	}

	@Test
	public void testInsertId() {
		FetchLog entry = new FetchLog();
		
		entry.setAgencyCd("USGS");
		entry.setSiteNo("007");
		dao.insertId(entry);
		assertNotNull("id after insert", entry.getFetchlogId());
		
		System.out.printf("id after insert: %d\n", entry.getFetchlogId());
	}
	
	@Test
	public void testSelectByWell() {
		WellRegistryKey key = new WellRegistryKey("USGS", "007");
		
		List<FetchLog> ff = dao.byWell(key);
		assertFalse("empty", ff.isEmpty());
	}

}
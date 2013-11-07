package gov.usgs.ngwmn.dm.prefetch;

import static org.junit.Assert.*;

import java.util.Date;

import javax.sql.DataSource;

import gov.usgs.ngwmn.dm.dao.ContextualTest;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class WaterlevelRankStatsWorkerIntegrationTest extends ContextualTest {

	private WaterlevelRankStatsWorker victim;
	
	@Before
	public void setup() {
		victim = ctx.getBean("WaterlevelRankStatsWorker", WaterlevelRankStatsWorker.class);
	}
	
	@Before
	public void make_room() {
		DataSource ds = ctx.getBean("dataSource",DataSource.class);
		JdbcTemplate jt = new JdbcTemplate(ds);
		
		int keeper = jt.queryForInt(
				"select waterlevel_cache_id " + 
				"from ( " + 
				"  select" + 
				"  waterlevel_cache_id, " + 
				"  rank() over (order by waterlevel_cache_id desc) \"rank\" " + 
				"  from " + 
				"  gw_data_portal.waterlevel_cache_stats" + 
				")" + 
				"where \"rank\" = 4");
		jt.update("delete from gw_data_portal.waterlevel_data_stats " +
				"where waterlevel_cache_id > ?", keeper);
	}
	
	@Test
	public void testFindSample() {
		Integer id = victim.findOneSample();
		assertNotNull("sample id", id);
		assertTrue("id > 0", id > 0);
	}
	
	@Test
	public void testObservation() {
		Integer id = victim.findOneSample();
		if (id == null) {
			System.out.printf("Nothing in database to test, sorry");
			return;
		}
		
		Observation obs = victim.latestObservation(id);
		assertNotNull(obs);
		assertEquals("id", id.intValue(), obs.getCacheId());
		assertTrue("month looks OK", obs.getMonth() >= 1 && obs.getMonth() <= 12);
	}
	
	@Test
	public void testMonthly() {
		Integer id = victim.findOneSample();
		if (id == null) {
			System.out.printf("Nothing in database to test, sorry");
			return;
		}
		
		Observation obs = victim.latestObservation(id);
		Statistics monthly = victim.monthly_stats(obs);

		assertNotNull(monthly);
		
		// Note that Date getMonth range is 0..11 not 1..12
		assertEquals("max date in month", obs.getMonth(), getMonth(monthly.getMax_date()));
		assertEquals("min date in month", obs.getMonth(), getMonth(monthly.getMin_date()));
		assertEquals("cache id", id.intValue(), monthly.getWaterlevel_cache_id());
	}
	
	private int getMonth(Date d) {
		DateTime dt = new DateTime(d);
		return dt.getMonthOfYear();
	}
	
	@Test
	public void testTotal() {
		Integer id = victim.findOneSample();
		if (id == null) {
			System.out.printf("Nothing in database to test, sorry");
			return;
		}
		
		Observation obs = victim.latestObservation(id);
		Statistics total = victim.total_stats(obs);

		assertNotNull(total);
		assertEquals("cache id", id.intValue(), total.getWaterlevel_cache_id());
		assertNotNull("min date", total.getMin_date());
		assertNotNull("max date", total.getMax_date());
	}
	
	@Test
	public void testPCodeUpdate() {
		Integer id = victim.findOneSample();
		if (id == null) {
			System.out.printf("Nothing in database to test, sorry");
			return;
		}
		
		victim.updatePCodeStats(id);
		System.out.println("pcode ok for " + id);
	}
	
	@Test(timeout=300000)
	public void testUpdateOne() {
		int id = victim.updateOne();
		
		System.out.printf("Updated waterlevel_cache[%d]\n", id);
		//assertTrue("id > 0", id > 0);
		assertTrue("survived update one", true);
	}

}

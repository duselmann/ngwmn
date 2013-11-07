package gov.usgs.ngwmn.dm.prefetch;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class PrefetchConfigurationTest {

	@Before
	public void setCocoon() {
		System.setProperty("ngwmn_cocoon", "http://localhost:8080/cocoon");

	}

	@Test
	public void test_custom_values() {
		System.setProperty("ngwmn_prefetch_count_limit", "401");
		System.setProperty("ngwmn_prefetch_ms_limit", "19181921");
		System.setProperty("ngwmn_prefetch_start_hour", "03");

		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextTest.xml");

		Prefetcher pef = ctx.getBean("Prefetcher", Prefetcher.class);
		assertEquals("count", 401, pef.getFetchLimit());
		assertEquals("time", 19181921L, pef.getTimeLimit().longValue());

		Object sked = ctx.getBean("scheduler");
		assertNotNull("scheduler", sked);
		assertTrue("instanceof", sked instanceof ThreadPoolTaskScheduler);
	}

	@Test
	public void test_default_values() {
		System.clearProperty("ngwmn_prefetch_count_limit");
		System.clearProperty("ngwmn_prefetch_ms_limit");
		System.clearProperty("ngwmn_prefetch_start_hour");
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextTest.xml");

		Prefetcher pef = ctx.getBean("Prefetcher", Prefetcher.class);
		assertEquals("count", 10, pef.getFetchLimit());
		assertEquals("time", 600000L, pef.getTimeLimit().longValue());

		Object sked = ctx.getBean("scheduler");
		assertNotNull("scheduler", sked);
	}

	// @Test(expected=BeanCreationException.class)
	public void test_bad_hour() {
		System.setProperty("ngwmn_prefetch_start_hour", "bad");

		/*ApplicationContext ctx = */new ClassPathXmlApplicationContext("applicationContextTest.xml");

		fail("should have thrown exception in context load");
	}


}

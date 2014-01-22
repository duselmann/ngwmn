package gov.usgs.ngwmn.dm.dao;


import gov.usgs.ngwmn.IntegrationTest;
import gov.usgs.ngwmn.dm.cache.fs.FileCache;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.*;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContextTest.xml"})
// Cannot use this, because it overrides system property spring.profiles.active; use that instead.
// Or set environment SPRING_PROFILES_ACTIVE or SPRING_PROFILES_DEFAULT
// @ActiveProfiles("local")
public abstract class ContextualTest extends IntegrationTest {

	private static String  basedir   = "/tmp/gwdp-cache";

	private DataSource ds;

	@Autowired
	protected ApplicationContext ctx;


	public static void setBasedir(String basedir) {
		ContextualTest.basedir = basedir;
	}
	public static String getBaseDir() {
		return basedir;
	}

	@BeforeClass
	public static void setupNaming() throws Exception {
		final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
		builder.bind(FileCache.BASEDIR_JNDI_NAME, basedir);
		// builder.bind("java:comp/env/ngwmn_cocoon", "http://cida-eros-ngwmndev.er.usgs.gov:8080/cocoon");
		// In the web context, a jndi resolver is configured automatically
		// set it up as a system property for now
		System.setProperty("ngwmn_cocoon", "http://cida-eros-ngwmndev.er.usgs.gov:8080/cocoon");

		try {
			builder.activate();
		} catch (IllegalStateException ise) {
			// Set the required value into the existing context instead
			InitialContext ctx = new InitialContext();
			ctx.bind(FileCache.BASEDIR_JNDI_NAME, basedir);
			ctx.bind("java:comp/env/ngwmn_cocoon", "http://cida-eros-ngwmndev.er.usgs.gov:8080/cocoon");
		}
	}

	@Override
	public DataSource getDataSource() {
		if (ds == null) {
			ds = ctx.getBean("dataSource", DataSource.class);
		}
		return ds;
	}
}

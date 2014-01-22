package gov.usgs.ngwmn.dm.dao;

import static org.junit.Assert.*;

import gov.usgs.ngwmn.dm.ErrorReportBuilder;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FetchLogDaoMonitorIntegrationTests extends ContextualTest {

	private FetchLogDAO dao;
	private ErrorReportBuilder rpt;

	@Before
	public void setUp() throws Exception {
		dao = ctx.getBean("FetchLogDAO", FetchLogDAO.class);
		rpt = ctx.getBean("errorReportBuilder", ErrorReportBuilder.class);
	}

	//@Test
	public void fetchSiteProblems() {
		List<FetchLog> logs = dao.fetchSiteProblems();


		assertNotNull("logs list not null", logs);
		assertFalse("expect some errors",logs.isEmpty());

		for (FetchLog log : logs) {
			String problem = log.getProblem()==null ?log.getStatus() :log.getProblem();
			System.out.println(log.getAgencyCd() +"\t"+ log.getSiteNo()  +"\t"+ problem);
		}
	}

	//@Test
	public void fetchAgencyProblemCounts() {
		List<FetchLog> logs = dao.fetchAgencyProblemCount();
		//List<FetchLog> logs = dao.fetchSiteProblems();


		assertNotNull("logs list not null", logs);
		assertFalse("expect some errors",logs.isEmpty());

		for (FetchLog log : logs) {
			System.out.println(log.getAgencyCd() +"\t"+ log.getCt());
		}
	}

	@Test
	public void buildErrorReport() {
		String msg = rpt.build(5, 2, 2);
		System.out.println(msg);

		assertNotNull("logs list not null", msg);
		assertFalse("expect some errors", msg.length()==0);
	}
}

package gov.usgs.ngwmn.dm.visualization;

import static org.junit.Assert.*;
import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.dao.FetchStatsDAO;

import org.junit.Before;
import org.junit.Test;

import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;

public class FetchStatsGeneratorIntegrationTest extends ContextualTest {

	private FetchStatsAgencyGenerator victim;

	@Before
	public void init() {
		FetchStatsDAO dao = ctx.getBean("WaterlevelStatsDAO", FetchStatsDAO.class);
		victim = new FetchStatsAgencyGenerator(dao);
		victim.setAgency(null);
	}

	@Test
	public void testGenerateDataTable() throws Exception {
		DataTable dt = victim.generateDataTable(null, null);

		assertTrue(dt.containsColumn("FETCH_DATE"));
		assertTrue(dt.containsColumn("DONE"));
		assertTrue(dt.containsColumn("EMPTY"));
		assertTrue(dt.containsColumn("FAIL"));
		assertTrue(dt.containsColumn("SKIP"));

		int nr = dt.getNumberOfRows();
		assertTrue("some rows", nr > 0);

		for (int r = 0; r < nr; r++) {
			for (int c = 0; c < 3; c++) {
				TableCell cell = dt.getCell(r, c);
				assertNotNull("cell",cell);
				assertNotNull("value",cell.getValue());
			}
		}
	}

}

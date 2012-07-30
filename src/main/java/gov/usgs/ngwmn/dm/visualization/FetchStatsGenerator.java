package gov.usgs.ngwmn.dm.visualization;

import java.sql.SQLException;

import gov.usgs.ngwmn.dm.dao.FetchStatsDAO;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.ResultSetExtractor;

import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataTableGenerator;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;

public class FetchStatsGenerator implements DataTableGenerator {

	private FetchStatsDAO dao;
	
	public FetchStatsGenerator(FetchStatsDAO dao) {
		super();
		this.dao = dao;		
	}

	@Override
	public DataTable generateDataTable(Query query, HttpServletRequest request)
			throws DataSourceException {
		
		try {
			return queryDataTable();
		} 
		catch (RuntimeException rte) {
			if (rte.getCause() instanceof DataSourceException) {
				throw (DataSourceException)rte.getCause();
			}
			else throw rte;
		}
		catch (SQLException e) {
			DataSourceException dse = new DataSourceException(ReasonType.OTHER, "Hmmm");
			dse.initCause(e);
			throw dse;
		}
	}

	private ResultSetExtractor<DataTable> rs2dt = new DataTableExtractor();

	public DataTable queryDataTable() throws SQLException {
		DataTable dt = dao.timeSeriesData(rs2dt);
		
		return dt;
	}
	
	@Override
	public Capabilities getCapabilities() {
		return Capabilities.NONE;
	}

}

package gov.usgs.ngwmn.dm.visualization;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import com.google.visualization.datasource.datatable.DataTable;

public class QueryDataTableGenerator extends SQLDataTableGenerator {

	private String query;
	private DataSource ds;
	
	public QueryDataTableGenerator(String query, DataSource dataSource) {
		this.query = query;
		this.ds = dataSource;
	}

	@Override
	protected DataTable generateDT() throws SQLException, RuntimeException {
		JdbcTemplate t = new JdbcTemplate(ds);
		
		DataTable value = t.query(query, rs2dt);
		
		return value;
	}

}

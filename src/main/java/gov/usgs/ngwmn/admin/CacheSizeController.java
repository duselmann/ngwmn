package gov.usgs.ngwmn.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CacheSizeController {

	private DataSource datasource;

	public CacheSizeController(DataSource datasource) {
		this.datasource = datasource;
	}

	private String ifNull(String v, String dflt) {
		if (v == null) {
			return dflt;
		}
		return v;
	}
	
	/** Produce a CSV for the cache size record.
	 * Lots of empty items.
	 * @throws IOException
	 */
	@RequestMapping(value="cache_size",produces="text/plain")
	public void generateTable(
			Writer writer) 
	throws IOException {
		
		
		// extract the data as a CSV
		String query = 
				"select * from  " + 
				"( select ts, tablename, bytes/(1024*1024) mbytes" + 
				"  from GW_DATA_PORTAL.xml_size_history" +
				") " + 
				"pivot ( " + 
				"  avg(mbytes) " + 
				"  for tablename in ('QUALITY_CACHE' as \"Quality\",'LOG_CACHE' as \"Log\",'WATERLEVEL_CACHE' as \"Waterlevel\") " + 
				")" +
				"order by ts asc";
		
		JdbcTemplate t = new JdbcTemplate(datasource);
		
		final PrintWriter pw = new PrintWriter(writer);
		pw.println("time,quality,log,waterlevel");

		t.query(query, new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				// Process the current row in the result set
				
				Timestamp time = rs.getTimestamp(1);
				String quality = ifNull(rs.getString("Quality"),"");
				String log = ifNull(rs.getString("Log"),"");
				String waterlevel = ifNull(rs.getString("Waterlevel"),"");
				
				pw.append(time.toString()).append(",")
					.append(quality).append(",")
					.append(log).append(",")
					.append(waterlevel);
				pw.println();
				
			}
		});
	}
}

package gov.usgs.ngwmn.dm.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientPropertiesIntegrationTest extends ContextualTest {

	@Test
	public void testShowClientProperties() throws Exception {
		DataSource ds = getDataSource();
		Connection conn = ds.getConnection();
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet clientProps = md.getClientInfoProperties();
			display(clientProps);
			
			Properties cp = conn.getClientInfo();
			display(cp);
		} finally {
			conn.close();
		}
		assertTrue("Made it", true);
	}

	private void display(Properties cp) {
		System.out.println("Client Properties");
		cp.list(System.out);
	}

	private void display(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		
		String fmt[] = new String[rsmd.getColumnCount()+1];
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			int displaySize = rsmd.getColumnDisplaySize(i);
			String label = rsmd.getColumnLabel(i);
			if (displaySize < label.length()) {
				displaySize = label.length();
			}
			String f = "|%" + displaySize + "s";
			System.out.printf(f, rsmd.getColumnLabel(i));
			fmt[i] = f;
		}
		System.out.println();
		
		while (rs.next()) {
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String v = rs.getString(i);
				System.out.printf(fmt[i], v);
			}
			System.out.println();
		}
		
		
	}
	
}

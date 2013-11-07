package gov.usgs.ngwmn.dm.harvest;

import static org.junit.Assert.*;

import gov.usgs.ngwmn.dm.dao.ContextualTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import  org.w3c.dom.Document;

public class GWDP_362_IntegrationTest
extends ContextualTest
{

	private static final String RESOURCE_PATH = "/sample-data/MBMG_257423_LOG.xml";
	private Harvester victim;

	@Before
	public void setUp() throws Exception {
		victim = new Harvester();
	}

	@After
	public void tearDown() throws Exception {
	}

	private byte[] readFully(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while (true) {
				int b = is.read();
				if (b < 0) {
					break;
				}
				bos.write(b);
			}
		} finally {
			bos.close();
		}

		return bos.toByteArray();
	}

	// @Test
	public void testWget_GWDP_362() throws Exception {
		String url = "http://cida-wiwsc-javaprodp.er.usgs.gov:8080/cocoon/gin/gwdp/agency/MBMG/wfs?request=GetFeature&featureId=257423";
		int code = victim.wget(url);
		System.out.printf("Got code %d\n", code);

		byte[] contents = null;
		InputStream is = victim.getInputStream();
		try {
			contents = readFully(is);
		} finally {
			is.close();
		}

		int nonAsciiCt = 0;
		for (int p = 0; p < contents.length; p++) {
			if ( contents[p] > 127) {
				nonAsciiCt++;
				System.out.printf("non-ascii char %d at %d\n", contents[p], p);
			}
		}
		System.out.printf("found %d non-ascii\n", nonAsciiCt);


		String c = new String(contents,"ISO-8859-1");
		System.out.printf("Decode length = %d\n", c.length());
		assertTrue("string decode ok", true);

		// look for encoded char
		Pattern enpat = Pattern.compile("&#(\\d+);");
		Matcher enmat = enpat.matcher(c);
		int enct = 0;
		while (enmat.find()) {
			if (enct == 0) {
				System.out.println("Found encoded character " + enmat.group());
			}
			enct++;
		}
		System.out.printf("Found %d matches\n", enct);

		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(new ByteArrayInputStream(contents));
		assertNotNull("well-formed document", document.getDocumentElement());
		System.out.printf("Top-level element: %s\n", document.getDocumentElement().getNodeName());
	}

	private void copy(InputStream is, OutputStream os) throws IOException {
		while (true) {
			int c = is.read();
			if (c < 0) {
				break;
			}
			os.write(c);
		}
	}

	private Blob makeBlob(Connection conn, InputStream is) throws Exception {
		if (conn instanceof DelegatingConnection) {
			conn = ((DelegatingConnection) conn).getInnermostDelegate();
		}

		final Blob blob = conn.createBlob();
		OutputStream os = blob.setBinaryStream(0);

		try {
			copy(is,os);
		} finally {
			os.close();
		}

		return blob;
	}


	private int insert(Connection conn, String agncy, String site, Blob blob)
			throws SQLException
			{
		String SQLTEXT = "INSERT INTO GW_DATA_PORTAL."+"LOG_CACHE"+"(agency_cd,site_no,fetch_date,xml,md5) VALUES (" +
				"?, ?, ?, XMLType(?,nls_charset_id('UTF8')), ?)";

		int[] pkColumns = {1};
		PreparedStatement s = conn.prepareStatement(SQLTEXT, pkColumns);

		s.setString(1, agncy);
		s.setString(2, site);
		s.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
		s.setBlob(4, blob);
		s.setString(5, "fake");

		s.executeUpdate();

		ResultSet gkrs = s.getGeneratedKeys();
		BigDecimal newkey = null;
		while (gkrs.next()) {
			newkey = gkrs.getBigDecimal(1);
			// logger.info("Generated key {}", newkey);
		}
		return newkey.intValueExact();
			}

	// TODO UNUSED CODE
	protected int insert(Connection conn, String agncy, String site, Clob clob)
			throws SQLException
			{
		String SQLTEXT = "INSERT INTO GW_DATA_PORTAL."+"LOG_CACHE"+"(agency_cd,site_no,fetch_date,xml,md5) VALUES (" +
				"?, ?, ?, XMLType(?), ?)";

		int[] pkColumns = {1};
		PreparedStatement s = conn.prepareStatement(SQLTEXT, pkColumns);

		s.setString(1, agncy);
		s.setString(2, site);
		s.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
		s.setClob(4, clob);
		s.setString(5, "fake");

		s.executeUpdate();

		ResultSet gkrs = s.getGeneratedKeys();
		BigDecimal newkey = null;
		while (gkrs.next()) {
			newkey = gkrs.getBigDecimal(1);
			// logger.info("Generated key {}", newkey);
		}
		return newkey.intValueExact();
			}

	@Test
	public void checkData() throws Exception {
		InputStream is = getClass().getResourceAsStream(RESOURCE_PATH);

		byte[] contents = null;
		try {
			contents = readFully(is);
		} finally {
			is.close();
		}

		String c = new String(contents,"ISO-8859-1");
		System.out.printf("Decode length = %d\n", c.length());
		assertTrue("string decode ok", true);

		// look for encoded char
		Pattern enpat = Pattern.compile("&#(\\d+);");
		Matcher enmat = enpat.matcher(c);
		int enct = 0;
		while (enmat.find()) {
			if (enct == 0) {
				System.out.println("Found encoded character " + enmat.group());
			}
			enct++;
		}

		System.out.printf("Found %d matches\n", enct);
		assertTrue("sample data has encoded chars", enct>0);
	}

	/** Verify that clob has the same contents as the resource at path.
	 *  TODO UNUSED CODE
	 * @param clob
	 * @param path
	 */
	protected void checkClob(Clob clob, String path) throws Exception {
		InputStream cis = clob.getAsciiStream();
		InputStream ris = getClass().getResourceAsStream(path);

		try {
			// compare stream contents
			long pos = 0;
			while (true) {
				int c_in = cis.read();
				int r_in = ris.read();

				pos++;
				if (c_in < 0 && r_in < 0) {
					return;
				}
				assertEquals("at pos " + pos, c_in, r_in);
			}
		} finally {
			cis.close();
			ris.close();
		}
	}

	/** Verify that blob has the same contents as the resource at path.
	 * 
	 * @param blob
	 * @param path
	 */
	private void checkBlob(Blob blob, String path) throws Exception {
		InputStream cis = blob.getBinaryStream();
		InputStream ris = getClass().getResourceAsStream(path);

		try {
			// compare stream contents
			long pos = 0;
			while (true) {
				int c_in = cis.read();
				int r_in = ris.read();

				pos++;
				if (c_in < 0 && r_in < 0) {
					return;
				}
				assertEquals("at pos " + pos, c_in, r_in);
			}
		} finally {
			cis.close();
			ris.close();
		}
	}

	@Test
	public void testToOracleXML() throws Exception {
		DataSource ds = getDataSource();
		Connection conn = ds.getConnection();

		try {
			InputStream is = getClass().getResourceAsStream(RESOURCE_PATH);
			try {
				Blob blob = makeBlob(conn, is);

				checkBlob(blob,RESOURCE_PATH);
				int pk = insert(conn, "MBMG", "257423", blob);
				System.out.printf("cache row %d\n", pk);
			} finally {
				is.close();
			}
		} finally {
			conn.close();
		}

		assertTrue("inserted blob", true);
	}

}

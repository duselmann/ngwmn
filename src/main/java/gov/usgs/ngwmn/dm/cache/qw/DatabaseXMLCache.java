package gov.usgs.ngwmn.dm.cache.qw;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.cache.Cache;
import gov.usgs.ngwmn.dm.cache.CacheInfo;
import gov.usgs.ngwmn.dm.dao.WellRegistryKey;
import gov.usgs.ngwmn.dm.io.Pipeline;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;


public class DatabaseXMLCache implements Cache {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final String tablename;
	private final WellDataType wdt;
	private Inspector inspector;
	private ExecutorService xService = Executors.newSingleThreadExecutor();

	public DatabaseXMLCache(DataSource ds, WellDataType datatype, LobHandler h) {
		this.ds = ds;
		wdt = datatype;
		tablename = getDatatype().aliasFor.name() + "_CACHE";
		handler = h;
	}

	@Override
	protected void finalize() throws Throwable {
		// this shutdown should allow queued and running jobs to finish
		xService.shutdown();
	}

	private DataSource ds;
	// private PreparedStatement insert;
	private LobHandler handler;

	public Inspector getInspector() {
		return inspector;
	}
	public void setInspector(Inspector inspector) {
		WellDataType iwdt = inspector.forDataType();
		if (iwdt != null && ! iwdt.equals(getDatatype().aliasFor)) {
			throw new RuntimeException("Inspector data type does not agree  with mine, " + iwdt);
		}
		this.inspector = inspector;
	}

	public void setExecutorService(ExecutorService x) {
		xService = x;
	}

	@Override
	public WellDataType getDatatype() {
		return wdt;
	}

	private Future<Specifier> invokeInspect(final int key, final Specifier spec) {
		logger.debug("call invokeInspect for {} key {}", spec, key);
		final Map<?, ?> mdc = MDC.getCopyOfContextMap();
		Future<Specifier> result =
				xService.submit(
						new Runnable() {

							@Override
							public void run() {
								try {
									MDC.setContextMap((mdc == null) ? Collections.emptyMap() : mdc);
									inspectAndRelease(key, spec);
									logger.trace("finished out-line invokeInspect of {}", spec);
								} finally {
									MDC.clear();
								}
							}
						},spec);

		logger.debug("finished invokeInspect of {}", spec);
		return result;
	}

	@Override
	public int cleanCache(int daysToRetain,int countToRetain) {
		String typename = getDatatype().aliasFor.name();

		String sql =
				"UPDATE gw_data_portal." + tablename + " c " +
						"SET xml         = NULL " +
						"WHERE (published = 'R' or published = 'N') " +
						"AND xml        IS NOT NULL " +
						"AND NOT EXISTS " +
						"  (SELECT * " +
						"  FROM GW_DATA_PORTAL.cache_retain_sites retain " +
						"  WHERE retain.agency_cd = c.agency_cd " +
						"  AND retain.site_no     = c.site_no " +
						"  AND retain." + typename + "  = 'Y' " +
						"  ) " +
						"AND EXISTS " +
						"  (SELECT * " +
						"  FROM gw_data_portal." + tablename + " c2 " +
						"  WHERE c2.agency_cd = c.agency_cd " +
						"  AND c2.site_no     = c.site_no " +
						"  AND c2.published   = 'Y' " +
						"  ) " +
						"AND c.fetch_date < (sysdate - ?) " +
						"AND c." + tablename + "_id IN " +
						"(SELECT " + tablename + "_id " +
						" FROM " +
						"  (SELECT  " +
						"    " + tablename + "_id, " +
						"    dense_rank() over (partition BY agency_cd, site_no order by fetch_date DESC) dr " +
						"    FROM GW_DATA_PORTAL." + tablename + " " +
						"    WHERE published = 'R' " +
						"  ) " +
						" WHERE dr > ? " +
						") "
						;

		recordSpaceUsed();

		JdbcTemplate template = new JdbcTemplate(ds);

		int ct = template.update(sql,daysToRetain, countToRetain);
		logger.info("Cleaned {} from {}", ct, tablename);

		recordSpaceUsed();

		return ct;
	}

	private void recordSpaceUsed() {
		JdbcTemplate template = new JdbcTemplate(ds);
		int ct = template.update("{call gw_data_portal.GATHER_XML_SIZE_STATS(?)}", tablename);
		if (ct == 1) {
			logger.info("recorded current XML space for {}", tablename);
		} else {
			logger.warn("did not record current XML space for table {}, ct = {}", tablename, ct);
		}
	}

	public void linkFetchLog(int fetchLogID , int cacheKey) {
		logger.info("link cache key {} type {} to fetch log id {}", new Object[] {cacheKey, wdt, fetchLogID});

		JdbcTemplate template = new JdbcTemplate(ds);
		int ct = template.update(
				"UPDATE GW_DATA_PORTAL." + tablename + " " +
						"SET fetchlog_ref = ? " +
						"WHERE " + tablename+"_id = ?",
						fetchLogID, cacheKey);
		if (ct != 1) {
			logger.warn("Failed to set fetchlog_ref to {} for cache row {},ct is {}", new Object[] {fetchLogID, cacheKey,ct});
		}

	}

	public void inspectAndRelease(int key, Specifier spec) {
		logger.debug("Inspecting key {} for spec {}", key, spec);
		try {
			boolean acceptable = false;
			try {
				acceptable = inspector.acceptable(key);
				logger.trace("done checking contents of {}", key);
			} catch (Exception e) {
				logger.error("Problem in inspectAndRelease.inspect " + key + " for spec " + spec, e);
			}
			if (acceptable) {
				publish(key,spec);
			} else {
				withdraw(key, spec);
			}
		} catch (Exception e) {
			logger.error("Problem in inspectAndRelease " + key + " for spec " + spec, e);
		}
	}

	@Override
	public OutputStream destination(final Specifier well) throws IOException {
		try {
			// TODO This is a rather ugly.

			final WellRegistryKey key = new WellRegistryKey(well.getAgencyID(), well.getFeatureID());

			// Ugly code to work around Tomcat 6 pooled connection, which does not have createClob method.
			// Could avoid this by using handler.getLobCreator, but that has only set from InputStream or from
			// Reader, so we'd have to copy the bytes; the created clob provides an output stream so we can
			// stream straight to the destination clob, rather than copying the bytes.
			final Connection pooledConn = ds.getConnection();
			Connection dconn = pooledConn;
			if (pooledConn instanceof DelegatingConnection) {
				dconn = ((DelegatingConnection) pooledConn).getInnermostDelegate();
			}
			final Connection conn = dconn;

			final Blob blob = conn.createBlob();
			OutputStream bos = blob.setBinaryStream(0);

			// TODO Defer MD5 calculation until quality inspection step
			MessageDigest md5 = null;
			try {
				md5 = MessageDigest.getInstance("MD5");
				OutputStream digested = new DigestOutputStream(bos, md5);
				bos = digested;
			} catch (NoSuchAlgorithmException e) {
				logger.warn("Problem getting MD5 digest, will not record hash for " + well, e);
			}
			final MessageDigest md5final = md5; // Wow, now I feel so threadsafe. Not.

			OutputStream fos = new FilterOutputStream(bos) {

				@Override
				public void close() throws IOException {
					super.close();
					try {
						long length = blob.length();
						logger.debug("About to insert, blob.length={}", length);
						String hash = null;
						if (md5final != null) {
							byte[] raw = md5final.digest();
							char[] hex = Hex.encodeHex(raw);
							hash = new String(hex);
						}
						int newkey = insert(conn, key, blob, hash);
						conn.commit();
						// TODO Clean up clob?
						blob.free();
						pooledConn.close();
						logger.info("saved data for {}, sz {} as {}[{}] with md5 {}", new Object[] {well, length,tablename,newkey,hash});

						Future<Specifier> future = invokeInspect(newkey, well);

						Specifier done = future.get(5, TimeUnit.MINUTES);
						logger.info("Got finish for inspect of {}", done);
					} catch (SQLException sqle) {
						logger.warn("Problem (sql) in inspect of {}", well);
						throw new IOException(sqle);
					} catch (InterruptedException e) {
						logger.warn("Problem (interrupt) in inspect of {}", well);
						throw new IOException(e);
					} catch (ExecutionException e) {
						logger.warn("Problem (execution) in inspect of {}", well);
						throw new IOException(e);
					} catch (TimeoutException e) {
						logger.warn("Problem (timeout) in inspect of {}", well);
						throw new IOException(e);
					}

				}
			};

			return fos;
		} catch (SQLException sqle) {
			throw new IOException(sqle);
		} finally {
			// is there anything we can do here?
		}
	}

	public void publish(int id,Specifier spec) throws Exception {
		logger.info("publishing {}[{}] for spec {}", new Object[] {tablename, id,spec});
		replacePublished(spec.getAgencyID(), spec.getFeatureID());
		setPublished(id, "Y");
	}

	public void replacePublished(String agency, String site) throws SQLException  {
		// TODO use Spring jdbc template
		final Connection conn = ds.getConnection();
		try {
			PreparedStatement s = conn.prepareStatement(
					"UPDATE GW_DATA_PORTAL." + tablename + " " +
							"SET published = 'R' " +
							"WHERE agency_cd = ? " +
							"AND site_no = ? " +
					"AND published = 'Y' ");

			s.setString(1,agency);
			s.setString(2, site);

			int ct = s.executeUpdate();
			logger.info("withdrew {} for {}", ct, agency+":"+site);

		} finally {
			conn.close();
		}

	}

	public void withdraw(int id, Specifier spec) throws Exception {
		logger.warn("fetched data was found unacceptable, {}[{}] for spec{}", new Object[] {tablename, id, spec});
		setPublished(id, "N");
	}

	private void setPublished(int id, String flag) throws SQLException, Exception {
		final Connection conn = ds.getConnection();
		try {
			PreparedStatement s = conn.prepareStatement("UPDATE GW_DATA_PORTAL." + tablename + " " +
					"SET published = ? " +
					"WHERE " + tablename+"_id = ? ");

			s.setString(1,flag);
			s.setInt(2, id);

			int ct = s.executeUpdate();
			if (ct != 1) {
				throw new Exception("Unexpected update count");
			}

		} finally {
			conn.close();
		}
	}

	@Override
	public boolean fetchWellData(final Specifier spec, Pipeline pipe)
			throws IOException {

		try {
			final Connection conn = ds.getConnection();
			// To use the getCLOBVal function, the table alias must be explicit; use of the implicit alias fails with error ORA-00904
			String query = "SELECT cachetable.fetch_date, cachetable.xml.getCLOBVal() " +
					"FROM GW_DATA_PORTAL."+tablename+" cachetable " +
					"WHERE cachetable.agency_cd = ? and cachetable.site_no = ? " +
					"AND cachetable.published = 'Y' " +
					"AND cachetable.xml IS NOT NULL " +
					"ORDER BY cachetable.fetch_date DESC ";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setMaxRows(1);
			ps.setString(1, spec.getAgencyID());
			ps.setString(2, spec.getFeatureID());

			return fetchDataAndClose(conn, ps, pipe);
		} catch (SQLException e1) {
			throw new IOException(e1);
		}
	}

	private boolean fetchDataAndClose(final Connection conn, PreparedStatement ps,
			Pipeline pipe) throws SQLException, IOException {
		InputStream stream = getConnectionClosingInputStream(conn, ps);

		Supplier<InputStream> supply = new SimpleSupplier<InputStream>(stream);

		pipe.setInputSupplier(supply);

		return true;
	}

	private InputStream getConnectionClosingInputStream(final Connection conn,
			PreparedStatement ps) throws SQLException, IOException {
		Timestamp fetch_date = null;
		InputStream stream = null;

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			fetch_date = rs.getTimestamp(1);
			stream = handler.getClobAsAsciiStream(rs, 2);
		}

		logger.debug("got stream, fetch_date={}, length={}", new Object[]{fetch_date, (stream==null)?-1:stream.available()});

		if (stream == null) {
			conn.close();
			return null;
		}

		stream = new ConnectionClosingInputStream(conn, stream);
		return stream;
	}

	// only called by the CacheSnoopServlet
	@Override
	public InputStream retrieve(String id) throws IOException {

		try {
			final Connection conn = ds.getConnection();
			// To use the getCLOBVal function, the table alias must be explicit; use of the implicit alias fails with error ORA-00904
			String query = "SELECT cachetable.fetch_date, cachetable.xml.getCLOBVal() " +
					"FROM GW_DATA_PORTAL."+tablename+" cachetable " +
					"WHERE cachetable." + tablename + "_ID = ? ";

			PreparedStatement ps = conn.prepareStatement(query);
			ps.setMaxRows(1);
			ps.setString(1, id);

			return getConnectionClosingInputStream(conn, ps);
		} catch (SQLException e1) {
			throw new IOException(e1);
		}
	}


	private static class ConnectionClosingInputStream extends FilterInputStream {

		private Connection conn;

		public ConnectionClosingInputStream(Connection conn, InputStream in) {
			super(in);
			this.conn = conn;
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
			} finally {
				try {
					conn.close();
				} catch (SQLException ioe) {
					throw new IOException(ioe);
				}
			}
		}
	};

	@Override
	public boolean contains(Specifier spec) {
		Connection conn;
		try {
			conn = ds.getConnection();
			try {
				String query = "SELECT count(*) FROM GW_DATA_PORTAL."+tablename+" WHERE agency_cd = ? and site_no = ? " +
						"AND published = 'Y' AND xml IS NOT NULL ";
				PreparedStatement ps = conn.prepareStatement(query);
				try {
					ps.setString(1, spec.getAgencyID());
					ps.setString(2, spec.getFeatureID());

					ResultSet rs = ps.executeQuery();

					int ct = -1;
					while (rs.next()) {
						ct = rs.getInt(1);
					}

					return (ct > 0);
				} finally {
					ps.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException sqle) {
			return false;
		}
	}

	@Override
	public CacheInfo getInfo(Specifier spec) {
		Timestamp created = null;
		Timestamp modified = null;
		boolean exists = false;
		long length = 0;
		String md5 = null;
		String published = null;

		try {
			Connection conn = ds.getConnection();
			try {
				// TODO Do not really need to fetch all rows.
				PreparedStatement ps = conn.prepareStatement("SELECT " +
						" fetch_date " +
						",dbms_lob.getlength(xmltype.getclobval(xml)) sz " +
						",md5 " +
						",published " +
						"from GW_DATA_PORTAL."+tablename+" " +
						"where agency_cd = ? and site_no = ? " +
						"order by fetch_date ASC ");
				ps.setString(1, spec.getAgencyID());
				ps.setString(2, spec.getFeatureID());

				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					exists = true;
					if (created == null) {
						created = rs.getTimestamp(1);
					}
					modified = rs.getTimestamp(1);
					length = rs.getLong(2);
					md5 = rs.getString(3);
					published = rs.getString(4);
					logger.trace("Row fetch_date {}, length {}", modified, length);
				}
			} finally {
				conn.close();
			}
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}

		CacheInfo val = new CacheInfo(created, exists, modified, length, md5, published);
		return val;
	}

	// Cannot do the insert until the Clob has been filled up
	private int insert(Connection conn, WellRegistryKey key, Blob blob, String hash)
			throws SQLException
			{
		String SQLTEXT = "INSERT INTO GW_DATA_PORTAL."+tablename+"(agency_cd,site_no,fetch_date,xml,md5) VALUES (" +
				"?, ?, ?, XMLType(?,nls_charset_id('UTF8')), ?)";

		int[] pkColumns = {1};
		PreparedStatement s = conn.prepareStatement(SQLTEXT, pkColumns);

		s.setString(1, key.getAgencyCd());
		s.setString(2, key.getSiteNo());
		s.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
		s.setBlob(4, blob);
		s.setString(5, hash);

		s.executeUpdate();

		ResultSet gkrs = s.getGeneratedKeys();
		BigDecimal newkey = null;
		while (gkrs.next()) {
			newkey = gkrs.getBigDecimal(1);
			logger.info("Generated key {}", newkey);
		}
		return newkey.intValueExact();
			}

	public int cleanCache(WellRegistryKey key) throws Exception {

		String cleanable =
				"select t1."+tablename+"_id from GW_DATA_PORTAL."+tablename+" t1 "+
						" where t1.fetch_date < (select max(t2.fetch_date) from GW_DATA_PORTAL."+tablename+" t2"+
						"                     where t2.md5 = t1.md5"+
						"                     and t2.agency_cd = t1.agency_cd"+
						"                     and t2.site_no = t1.site_no)"+
						" AND t1.xml is not null " +
						" AND t1.agency_cd = ? " +
						" AND t1.site_no = ? ";

		String update =
				"update GW_DATA_PORTAL."+tablename+" " +
						"set xml = null " +
						"where "+tablename+"_id = ? ";

		logger.debug("timeSeriesQuery is {}", cleanable);
		int ct = 0;
		Connection conn = ds.getConnection();
		try {
			PreparedStatement pstmt = conn.prepareStatement(cleanable);

			pstmt.setString(1, key.getAgencyCd());
			pstmt.setString(2, key.getSiteNo());

			List<Integer> killist = new ArrayList<Integer>();

			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				killist.add(rs.getInt(1));
			}
			pstmt.close();

			PreparedStatement killer = conn.prepareStatement(update);
			for (Integer i : killist) {
				killer.setInt(1, i);
				int uct = killer.executeUpdate();
				logger.info("cleaned {} result {}", i, uct);
				ct += uct;
			}
		} finally {
			conn.close();
		}

		return ct;
	}

	public int fixMD5() throws Exception {
		String select = "SELECT cachetable."+tablename+"_id id, cachetable.xml.getCLOBVal() "+
				"FROM GW_DATA_PORTAL."+tablename+" cachetable " +
				"WHERE cachetable.md5 IS NULL " +
				"AND cachetable.xml IS NOT NULL ";
		String update = "UPDATE GW_DATA_PORTAL."+tablename+" " +
				"SET md5 = ? " +
				"WHERE "+tablename+"_id = ? " +
				"AND md5 IS NULL ";

		int ct = 0;
		Connection conn = ds.getConnection();
		try {
			PreparedStatement query = conn.prepareStatement(select);
			PreparedStatement setter = conn.prepareStatement(update);

			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				int idx = rs.getInt(1);
				InputStream xis = rs.getAsciiStream(2);

				logger.info("Working on {}", idx);

				String md5 = null;
				try {
					md5 = md5digest(xis);
				} finally {
					xis.close();
				}
				setter.setString(1, md5);
				setter.setInt(2, idx);

				logger.info("Updating md5 of {} to {}", idx, md5);
				int uc = setter.executeUpdate();
				logger.debug("update count is {}", uc);
				ct += uc;
			}
		} finally {
			conn.close();
		}
		return ct;
	}

	protected String md5digest(InputStream xis)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] buf = new byte[1024];
		while (true) {
			int bct = xis.read(buf);
			if (bct <= 0) {
				break;
			}
			digest.update(buf, 0, bct);
		}

		byte[] raw = digest.digest();
		char[] hex = Hex.encodeHex(raw);
		String md5 = new String(hex);
		return md5;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatabaseXMLCache [")
		.append("tablename=").append(tablename)
		// 		.append(", ds=").append(ds)
		.append("]");
		return builder.toString();
	}



}

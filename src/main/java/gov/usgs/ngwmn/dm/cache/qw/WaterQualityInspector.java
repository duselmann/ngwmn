package gov.usgs.ngwmn.dm.cache.qw;

import gov.usgs.ngwmn.WellDataType;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterQualityInspector implements Inspector {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private DataSource ds;
	
	
	@Override
	public WellDataType forDataType() {
		return WellDataType.QUALITY;
	}

	@Override
	public boolean acceptable(int cachekey) throws Exception {
		Connection conn = ds.getConnection();
		try {
			logger.debug("inspecting data for {}", cachekey);
			CallableStatement stat = conn.prepareCall("{call GW_DATA_PORTAL.INSPECT_QUALITY_DATA(?)}");
			stat.setInt(1, cachekey);
			
			boolean did = false;
			try {
				did = stat.execute();
			} catch (SQLIntegrityConstraintViolationException ix) {
				logger.warn("integrity constraint violated, assuming no quality data for quality cache key {}", cachekey);
				logger.warn("Problem", ix);
				return false;
			} catch (java.sql.SQLDataException sqde) {
				logger.warn("data exception, assuming no quality data for quality cache key {}", cachekey);
				logger.warn("Problem", sqde);
				return false;
			}
			logger.debug("finished update for {}, got {}", cachekey, did);
			
			// TODO would be convenient if stored proc contained a select to supply this result set
			PreparedStatement ps = conn.prepareStatement(
					"SELECT qdq.md5,qdq.consituent,qdq.ct,qdq.firstDate,qdq.lastDate " +
					"FROM GW_DATA_PORTAL.QUALITY_DATA_QUALITY qdq, GW_DATA_PORTAL.quality_cache qc " +
					"WHERE qdq.md5 = qc.md5 AND qc.quality_cache_id = ?");
			ps.setInt(1, cachekey);
			
			int totct = 0;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String md5= rs.getString(1);
				String name = rs.getString(2);
				int ct = rs.getInt(3);
				Date frst = rs.getDate(4);
				Date lst = rs.getDate(5);
				
				logger.debug("Stats for quality, id={} md5={}: nm {} ct {} min {} max {}",
						new Object[] {cachekey, md5, name, ct, frst, lst});
				
				totct += ct;
			}
			return totct > 0;
		} finally {
			conn.close();
		}
	}

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	
}

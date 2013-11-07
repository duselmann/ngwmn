package gov.usgs.ngwmn.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.admin.stats.JitTree;
import gov.usgs.ngwmn.dm.dao.FetchStatsDAO;
import gov.usgs.ngwmn.dm.dao.WellRegistryDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/jit")
public class JITDataController {

	private static final WellDataType[] WDTT = { WellDataType.LOG, WellDataType.QUALITY, WellDataType.WATERLEVEL };

	private WellRegistryDAO wellDao;
	private FetchStatsDAO waterlevelStatsDAO;
	private FetchStatsDAO qualityStatsDAO;
	private FetchStatsDAO wellLogStatsDAO;

	private FetchStatsDAO forWDT(WellDataType wdt) {
		switch (wdt) {
		case LOG: return wellLogStatsDAO;

		case QUALITY: return qualityStatsDAO;

		case WATERLEVEL: return waterlevelStatsDAO;
		default: // TODO UNHANDLED ENUM TYPES
		}

		return null;
	}

	private Logger logger = LoggerFactory.getLogger(getClass());

	@RequestMapping("tree/{day}")
	@ResponseBody
	public JitTree getTree(@PathVariable @DateTimeFormat(iso=ISO.DATE) Date day) {

		// top level is a container
		JitTree base = new JitTree("CacheStatistics");

		// next is agencies
		List<String> agencies = wellDao.agencies();
		for (String a : agencies) {
			JitTree agency = agencyResults(a, day);
			base.addChild(agency);
		}

		return base;
	}

	private JitTree agencyResults(String a, Date when) {
		JitTree agency = new JitTree(a);

		for (WellDataType t : WDTT) {
			// TODO check for network membership
			JitTree ttree = results(a, t, when);
			agency.addChild(ttree);
		}

		return agency;
	}

	private static class ResultCounts {
		public int fail;
		public int success;
		public int empty;
		public int attempt;
	};

	private JitTree results(String agency, WellDataType wdt, final Date when) {
		ResultSetExtractor<ResultCounts> rse = new ResultSetExtractor<JITDataController.ResultCounts>() {

			private int safeGet(ResultSet rs, String col) {
				try {
					return rs.getInt(col);
				} catch (SQLException sql) {
					logger.warn("Problem getting int column named {}, will return 0", col);
					return 0;
				}
			}

			@Override
			public ResultCounts extractData(ResultSet rs) throws SQLException,
			DataAccessException {

				ResultCounts val = null;

				while (rs.next()) {
					if (val != null) {
						logger.warn("Multiple results?");
					}
					val = new ResultCounts();
					val.success = safeGet(rs,"DONE");
					val.empty = safeGet(rs,"EMPTY");
					val.fail = safeGet(rs, "FAIL");
					val.attempt = val.success + val.empty + val.fail;
				}
				return val;
			}
		};

		ResultCounts rc = null;
		FetchStatsDAO dao = forWDT(wdt);
		if (dao != null) {
			try {
				rc = dao.dateAgencyData(agency, when, rse);
			}
			catch (SQLException sqe) {
				logger.warn("Problem getting fetch counts", sqe);
			}
		}
		if (rc == null) {
			rc = new ResultCounts(); // all zero
		}

		JitTree value = new JitTree(wdt.name());

		value.addChild(new JitTree("success", rc.success));
		value.addChild(new JitTree("empty", rc.empty));
		value.addChild(new JitTree("fail", rc.fail));

		// TODO add count of unfetched wells

		logger.debug("got counts {} for attempts {}", value.data.area, rc.attempt);
		// value.data.area = rc.attempt;

		return value;
	}

	public void setWellRegistryDAO(WellRegistryDAO wellDao) {
		this.wellDao = wellDao;
	}

	public void setWaterlevelStatsDAO(FetchStatsDAO waterlevelStatsDAO) {
		this.waterlevelStatsDAO = waterlevelStatsDAO;
	}

	public void setQualityStatsDAO(FetchStatsDAO qualityStatsDAO) {
		this.qualityStatsDAO = qualityStatsDAO;
	}

	public void setWellLogStatsDAO(FetchStatsDAO wellLogStatsDAO) {
		this.wellLogStatsDAO = wellLogStatsDAO;
	}


}

package gov.usgs.ngwmn.admin;

import gov.usgs.ngwmn.WaterlevelMediator;
import gov.usgs.ngwmn.dm.dao.WellRegistry;
import gov.usgs.ngwmn.dm.dao.WellRegistryDAO;
import gov.usgs.ngwmn.dm.io.transform.XSLHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CSVController {

	public static class WLSample {

		public String time;
		public BigDecimal value;
		public BigDecimal original_value;
		public String units;
		public String comment;
		public Boolean up;
		public String pcode;
		
		public WLSample(String time, BigDecimal value, String units,
				BigDecimal orig_value, String comment, Boolean up, String pcode) {
			this.time = time;
			this.value = value;
			this.original_value = orig_value;
			this.units = units;
			this.comment = comment;
			this.up = up;
			this.pcode = pcode;
		}
		
	};
	
	@Autowired
	private DataSource datasource;

	@Autowired
	private WellRegistryDAO registry;
	
	@Autowired
	private LobHandler lobHandler;

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	
	public CSVController() {
	}

	private long getCacheID(JdbcTemplate t, String agency, String site) {
		String query = "SELECT cachetable.WATERLEVEL_CACHE_ID " +
				"FROM GW_DATA_PORTAL.WATERLEVEL_CACHE cachetable " +
				"WHERE cachetable.agency_cd = ? and cachetable.site_no = ? " +
				"AND cachetable.published = 'Y' " +
				"AND cachetable.xml IS NOT NULL " +
				"ORDER BY cachetable.fetch_date DESC ";
		
		long val;
		
		t.setMaxRows(1);
		try {
			val = t.queryForLong(query, agency, site);
		}
		catch (EmptyResultDataAccessException ee) {
			val = -1;
		} finally {
			t.setMaxRows(0);
		}
		return val;
	}
	
	@RequestMapping(value="/flatXML/waterlevel/{agency}/{site}",
			produces="text/xml")
	public void flatXML(		
			@PathVariable String agency,
			@PathVariable String site,			
			HttpServletResponse response) 
		throws Exception
	{
		JdbcTemplate t = new JdbcTemplate(datasource);
		String query = "SELECT cachetable.xml.getCLOBVal() " +
				"FROM GW_DATA_PORTAL.waterlevel_cache cachetable " +
				"WHERE cachetable.agency_cd = ? " +
				"and cachetable.site_no = ? " +
				"AND cachetable.published = 'Y' " +
				"AND cachetable.xml IS NOT NULL " +
				"ORDER BY cachetable.fetch_date DESC ";
		
		logger.debug("Extracting waterlevel flat xml for site {}", agency+":"+site);
		long commence = System.nanoTime();

		final XSLHelper xslHelper = new XSLHelper();
		xslHelper.setTransform("/gov/usgs/ngwmn/wl2flat.xsl"); // so much for final...
		
		final Double elevation = getElevation(agency,site);
		final Transformer xform = xslHelper.getTemplates().newTransformer();
		if (elevation != null) {
			xform.setParameter("elevation", String.valueOf(elevation));
		}
		xform.setParameter("agency", agency);
		xform.setParameter("site", site);
		
		logger.debug("Getting writer");
		response.setContentType("text/xml");
		final Writer writer = response.getWriter();
		
		ResultSetExtractor<Void> rse = new ResultSetExtractor<Void>() {

			@Override
			public Void extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				try {

					while (rs.next()) {
						InputStream stream = lobHandler.getClobAsAsciiStream(rs, 1);
							
						StreamResult result = new StreamResult(writer);
						StreamSource source = new StreamSource(stream);
							
						xform.transform(source, result);
	
					}
					return null;
				} catch (TransformerConfigurationException e) {
					throw new RuntimeException(e);
				} catch (TransformerException e) {
					throw new RuntimeException(e);
				}
			}
			
		};
		t.query(query, rse, agency, site);
		
		long finish = System.nanoTime();
		logger.debug("flat xml done, elapsed={}", (finish-commence)/1.0e9);

	}

	@Deprecated
	/** Produce waterlevels for the given site, in JSON format

	 * @param agency
	 * @param site
	 * @throws IOException
	 * 
	 * @deprecated due to terrible performance.
	 */
	@RequestMapping("/json/waterlevel/{agency}/{site}") 
	@ResponseBody
	public List<WLSample>  generateJSON(
			@PathVariable String agency,
			@PathVariable String site) 
	throws IOException {
		
		// XPaths from wl2csv-dates.xsl
		
		String query = 
				"	select" +
				" 	  xq.*	from " +
				"		gw_data_portal.waterlevel_cache qc," +
				"		XMLTable(" +
				"		XMLNAMESPACES(" +
				"		  'http://www.wron.net.au/waterml2' AS \"wml2\"," +
				"		  'http://www.opengis.net/om/2.0' AS \"om\"," +
				"		  'http://www.opengis.net/swe/2.0' AS \"swe\"," +
				"		  'https://github.com/USGS-CIDA/ngwmn/sos' as \"gwdp\"" +
				"		)," +
				"		'$wmo//wml2:WaterMonitoringObservation/om:result/wml2:TimeSeries/wml2:element/wml2:TimeValuePair'" +
				"		passing qc.xml as \"wmo\" " +
				"		columns " +
				"    	\"FULLDATE\" varchar2(40) path 'wml2:time'," +
				"		\"VAL\" number path 'wml2:value/swe:Quantity/swe:value'," +
				"		\"UNITS\" varchar(40) path 'wml2:value/swe:Quantity/swe:uom/@code'," +
				"    	\"COMMENT\" varchar(80) path 'wml2:comment'," +
				"		\"DIRECTION\" varchar(12) path 'wml2:value/swe:Quantity/gwdp:nwis/@direction'," +
				"       \"PCODE\" varchar(5) path 'wml2:value/swe:Quantity/gwdp:nwis/@pcode' " +
				"		) xq" +
				"	where qc.waterlevel_cache_id = ? " +
				"	order by xq.FULLDATE asc " +
				"";
		
		JdbcTemplate t = new JdbcTemplate(datasource);
		
		Double altitude = getElevation(agency, site);
		BigDecimal offset = null;
		if (altitude != null) {
			offset = BigDecimal.valueOf(altitude);
		}
		
		final BigDecimal foffset = offset;
		
		final WaterlevelMediator mediator = new WaterlevelMediator();
		
		long id = getCacheID(t, agency, site);
		
		logger.debug("Extracting waterlevel json object for cache id {} (site {})", id, agency+":"+site);
		long commence = System.nanoTime();
		
		List<WLSample> value = t.query(query, new RowMapper<WLSample>() {
			@Override
			public WLSample mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				String time = rs.getString("FULLDATE");
				BigDecimal value = rs.getBigDecimal("VAL");
				String units = rs.getString("UNITS");
				String comment = rs.getString("COMMENT");
				String direction = rs.getString("DIRECTION");
				String pcode = rs.getString("PCODE");
				
				Boolean up = ("up".equals(direction));
				
				BigDecimal mediated_value = null;
				if (  value != null) {
					mediated_value = mediator.mediate(value, foffset, direction);
 				}
				
				return new WLSample(time,mediated_value,units,value,comment,up, pcode);
			}
		}, id);
		
		long finish = System.nanoTime();
		
		logger.debug("Query done, count={} elapsed={}", value.size(), (finish-commence)/1.0e9);
		return value;
			
	}

	private Double getElevation(String agency, String site) {
		WellRegistry well = registry.findByKey(agency, site);
		Double altitude = (well != null) ? well.getAltVa() : null;
		return altitude;
	}

	@Deprecated
	/** Produce waterlevels for the given site, in simple csv as expected by dygraphs

	 * @param agency
	 * @param site
	 * @throws IOException
	 * 
	 * @deprecated in favor of flatXML which is a lot faster
	 */
	@RequestMapping("/csv/waterlevel/{agency}/{site}")
	public void generateTable(
			@PathVariable String agency,
			@PathVariable String site,
			Writer writer) 
	throws IOException {
		
		// extract the data as a CSV
		String query = 
				"	select" +
				" 	  xq.*	from " +
				"		gw_data_portal.waterlevel_cache qc," +
				"		XMLTable(" +
				"		XMLNAMESPACES(" +
				"		  'http://www.wron.net.au/waterml2' AS \"wml2\"," +
				"		  'http://www.opengis.net/om/2.0' AS \"om\"," +
				"		  'http://www.opengis.net/swe/2.0' AS \"swe\"" +
				"		)," +
				"		'for $r in //wml2:WaterMonitoringObservation/om:result/wml2:TimeSeries/wml2:element" +
				"		let " +
				"		$p := $r/wml2:TimeValuePair" +
				"		return $p" +
				"		'" +
				"		passing qc.xml" +
				"		columns " +
				"		\"DT\" varchar2(12) path 'ora:replace(substring(wml2:time,1,10),\"-00\",\"-01\")'," +
				"    	\"FULLDATE\" varchar2(40) path 'wml2:time'," +
				"		\"VAL\" number path 'wml2:value/swe:Quantity/swe:value'," +
				"		\"UNITS\" varchar(40) path 'wml2:value/swe:Quantity/swe:uom/@code'," +
				"    	\"COMMENT\" varchar(80) path 'wml2:comment'" +
				"		) xq" +
				"	where qc.waterlevel_cache_id = ? " +
				"	order by xq.FULLDATE asc ";
		
		JdbcTemplate t = new JdbcTemplate(datasource);
		
		long id = getCacheID(t, agency, site);
		
		logger.debug("Extracting waterlevel data for cache id {} (site {})", id, agency+":"+site);
		
		final PrintWriter pw = new PrintWriter(writer);
		pw.println("time,value");

		t.query(query, new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				// Process the current row in the result set
				
				// TODO clean up date (using Joda time?) or use truncated date column?
				String time = rs.getString("FULLDATE");
				String value = rs.getString("VAL");
				if (! value.isEmpty() && ! value.startsWith("-")) {
					value = "-" + value;
				}
				
				pw.append(time).append(",").append(value);
				pw.println();
				
			}
		}, id);
	}

}

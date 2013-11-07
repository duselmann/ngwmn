package gov.usgs.ngwmn.admin;

import gov.usgs.ngwmn.dm.dao.FetchStatsDAO;
import gov.usgs.ngwmn.dm.dao.WellRegistryDAO;
import gov.usgs.ngwmn.dm.visualization.FetchStatsAgencyGenerator;
import gov.usgs.ngwmn.dm.visualization.FetchDataAgeGenerator;
import gov.usgs.ngwmn.dm.visualization.StatsTableGenerator;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.visualization.datasource.DataSourceHelper;

@Controller
@RequestMapping("/quality")
public class QualityStatsController {

	private FetchStatsDAO dao;
	private WellRegistryDAO wellDao;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static class Charter {
		private String agency;

		public String getAgency() {
			return agency;
		}

		public void setAgency(String agency) {
			this.agency = agency;
		}

	}

	// TODO Eliminate this (here only to make form:form tag work)
	@ModelAttribute("charter")
	public Charter getModel() {
		return new Charter();
	}

	@RequestMapping("chart")
	public String showChart(
			@ModelAttribute("agency") String agency
			) {
		return "quality/chart";
	}

	@RequestMapping("timechart")
	public String showTimeChart(
			) {
		return "quality/timechart";
	}

	@ModelAttribute("agencyCodes")
	public List<String> getAgencyCodes() {
		logger.info("getting agency codes");
		return wellDao.agencies();
	}

	@RequestMapping("table")
	public void generateTable(
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException {
		FetchStatsAgencyGenerator gen = new FetchStatsAgencyGenerator(dao);
		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
	}

	// TODO Handle table/all, which should produce a separate data series (i.e. column) for each agency
	// (or use "pivot" operation in data query, https://developers.google.com/chart/interactive/docs/querylanguage#Pivot)

	@RequestMapping("table/{agency}")
	public void generateTable(
			@PathVariable String agency,
			HttpServletRequest request,
			HttpServletResponse response)
					throws IOException {
		FetchStatsAgencyGenerator gen = new FetchStatsAgencyGenerator(dao);
		gen.setAgency(agency);
		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
	}

	@RequestMapping("stats")
	public void statsTable(
			HttpServletRequest request,
			HttpServletResponse response
			)
					throws IOException
					{
		StatsTableGenerator gen = new StatsTableGenerator(dao);
		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
					}

	@RequestMapping("fetchdates")
	public String showFetchDates(
			) {
		return "quality/fetchdates";
	}

	@RequestMapping("age")
	public void ageTable(
			HttpServletRequest request,
			HttpServletResponse response
			)
					throws IOException
					{
		FetchDataAgeGenerator gen = new FetchDataAgeGenerator(dao);
		DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
					}

	@RequestMapping(value="data/{agency}", produces="text/csv")
	public void exportData(
			@PathVariable String agency,
			final Writer writer
			)
					throws SQLException, IOException
					{
		ResultSetExtractor<Void> rse = new ResultSetExtractor<Void>() {

			@Override
			public Void extractData(ResultSet rs) throws SQLException,
			DataAccessException {
				@SuppressWarnings("resource") // managed by pipeline
				CSVWriter cw = new CSVWriter(writer);
				try {
					cw.writeAll(rs, true);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				return null;
			}
		};

		dao.viewData(agency, rse);
					}

	@RequestMapping(value="data", produces="text/csv")
	public void exportAllData(
			Writer 		writer
			)
					throws SQLException, IOException
					{
		exportData(null, writer);
					}

	public void setDao(FetchStatsDAO dao) {
		this.dao = dao;
	}

	public void setWellDao(WellRegistryDAO wellDao) {
		this.wellDao = wellDao;
	}

}

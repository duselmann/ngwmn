package gov.usgs.ngwmn.dm.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FetchLogDAO {
	private FetchLogMapper mapper;

	public FetchLogDAO(FetchLogMapper mapper) {
		super();
		this.mapper = mapper;
	}

	public void insertId(FetchLog item) {
		mapper.insertId(item);
	}

	public FetchLog mostRecent(WellRegistryKey well) {
		return mapper.selectLatestByWell(well.getAgencyCd(), well.getSiteNo());
	}

	public List<FetchLog> byWell(WellRegistryKey well) {
		FetchLogExample selector = new FetchLogExample();
		selector.createCriteria()
		.andAgencyCdEqualTo(well.getAgencyCd())
		.andSiteNoEqualTo(well.getSiteNo());
		// newest first
		selector.setOrderByClause("started_at DESC");

		return mapper.selectByExample(selector);
	}

	public FetchLog select(Integer fetchlogId) {
		return mapper.selectByPrimaryKey(fetchlogId);
	}

	public int update(FetchLog record) {
		return mapper.updateByPrimaryKey(record);
	}

	public List<Map<String, Object>> statisticsByDay(Date day) {
		List<Map<String,Object>> v = mapper.statisticsByDay(day);
		return v;
	}

	public List<FetchLog> fetchHistory(String agencyCd, String siteNo) {
		List<FetchLog> v = mapper.fetchHistory(agencyCd, siteNo);
		return v;
	}


	/**
	 * 
	 * @return a list of recent problems/empty results for sites
	 */
	public List<FetchLog> fetchSiteProblems() {
		List<FetchLog> v = mapper.fetchSiteProblems(1); // look back one day for issues
		return v;
	}

	/**
	 * this CT for each fetch log is the problem/empty count rather than the data row count
	 * 
	 * @return a list of agencies "fetchlogs" with the count of sites that have had errors
	 */
	public List<FetchLog> fetchAgencyProblemCount() {
		List<FetchLog> v = mapper.fetchAgencyProblemCount(1); // look back one day for issues
		return v;
	}

	/**
	 * 
	 * @param agencyCd
	 * @param siteNo
	 * @param amount
	 * @return a given site's recent success count for the given amount of fetch logs
	 */
	public int fetchSuccessCount(String agencyCd, String siteNo, String data_stream, int amount) {
		int v = mapper.fetchRecentSuccessCount(agencyCd, siteNo, data_stream, amount);
		return v;
	}

	/**
	 * 
	 * @param agencyCd
	 * @param siteNo
	 * @param amount
	 * @return a given site's recent amount of fetch logs
	 */
	public List<FetchLog> fetchProblems(String agencyCd, String siteNo, String data_stream, int amount) {
		List<FetchLog> v = mapper.fetchRecentProblems(agencyCd, siteNo, data_stream, amount);
		return v;
	}
}

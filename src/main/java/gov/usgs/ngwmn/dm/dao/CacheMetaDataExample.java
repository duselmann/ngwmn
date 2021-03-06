package gov.usgs.ngwmn.dm.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CacheMetaDataExample {
    /**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	protected String orderByClause;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	protected boolean distinct;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	protected List<Criteria> oredCriteria;

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public CacheMetaDataExample() {
		oredCriteria = new ArrayList<Criteria>();
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public String getOrderByClause() {
		return orderByClause;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public List<Criteria> getOredCriteria() {
		return oredCriteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public void or(Criteria criteria) {
		oredCriteria.add(criteria);
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public Criteria or() {
		Criteria criteria = createCriteriaInternal();
		oredCriteria.add(criteria);
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public Criteria createCriteria() {
		Criteria criteria = createCriteriaInternal();
		if (oredCriteria.size() == 0) {
			oredCriteria.add(criteria);
		}
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	protected Criteria createCriteriaInternal() {
		Criteria criteria = new Criteria();
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public void clear() {
		oredCriteria.clear();
		orderByClause = null;
		distinct = false;
	}

	/**
	 * This class was generated by MyBatis Generator. This class corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	protected abstract static class GeneratedCriteria {
		protected List<Criterion> criteria;

		protected GeneratedCriteria() {
			super();
			criteria = new ArrayList<Criterion>();
		}

		public boolean isValid() {
			return criteria.size() > 0;
		}

		public List<Criterion> getAllCriteria() {
			return criteria;
		}

		public List<Criterion> getCriteria() {
			return criteria;
		}

		protected void addCriterion(String condition) {
			if (condition == null) {
				throw new RuntimeException("Value for condition cannot be null");
			}
			criteria.add(new Criterion(condition));
		}

		protected void addCriterion(String condition, Object value,
				String property) {
			if (value == null) {
				throw new RuntimeException("Value for " + property
						+ " cannot be null");
			}
			criteria.add(new Criterion(condition, value));
		}

		protected void addCriterion(String condition, Object value1,
				Object value2, String property) {
			if (value1 == null || value2 == null) {
				throw new RuntimeException("Between values for " + property
						+ " cannot be null");
			}
			criteria.add(new Criterion(condition, value1, value2));
		}

		public Criteria andAgencyCdIsNull() {
			addCriterion("AGENCY_CD is null");
			return (Criteria) this;
		}

		public Criteria andAgencyCdIsNotNull() {
			addCriterion("AGENCY_CD is not null");
			return (Criteria) this;
		}

		public Criteria andAgencyCdEqualTo(String value) {
			addCriterion("AGENCY_CD =", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdNotEqualTo(String value) {
			addCriterion("AGENCY_CD <>", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdGreaterThan(String value) {
			addCriterion("AGENCY_CD >", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdGreaterThanOrEqualTo(String value) {
			addCriterion("AGENCY_CD >=", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdLessThan(String value) {
			addCriterion("AGENCY_CD <", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdLessThanOrEqualTo(String value) {
			addCriterion("AGENCY_CD <=", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdLike(String value) {
			addCriterion("AGENCY_CD like", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdNotLike(String value) {
			addCriterion("AGENCY_CD not like", value, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdIn(List<String> values) {
			addCriterion("AGENCY_CD in", values, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdNotIn(List<String> values) {
			addCriterion("AGENCY_CD not in", values, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdBetween(String value1, String value2) {
			addCriterion("AGENCY_CD between", value1, value2, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andAgencyCdNotBetween(String value1, String value2) {
			addCriterion("AGENCY_CD not between", value1, value2, "agencyCd");
			return (Criteria) this;
		}

		public Criteria andDataTypeIsNull() {
			addCriterion("DATA_TYPE is null");
			return (Criteria) this;
		}

		public Criteria andDataTypeIsNotNull() {
			addCriterion("DATA_TYPE is not null");
			return (Criteria) this;
		}

		public Criteria andDataTypeEqualTo(String value) {
			addCriterion("DATA_TYPE =", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeNotEqualTo(String value) {
			addCriterion("DATA_TYPE <>", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeGreaterThan(String value) {
			addCriterion("DATA_TYPE >", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeGreaterThanOrEqualTo(String value) {
			addCriterion("DATA_TYPE >=", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeLessThan(String value) {
			addCriterion("DATA_TYPE <", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeLessThanOrEqualTo(String value) {
			addCriterion("DATA_TYPE <=", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeLike(String value) {
			addCriterion("DATA_TYPE like", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeNotLike(String value) {
			addCriterion("DATA_TYPE not like", value, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeIn(List<String> values) {
			addCriterion("DATA_TYPE in", values, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeNotIn(List<String> values) {
			addCriterion("DATA_TYPE not in", values, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeBetween(String value1, String value2) {
			addCriterion("DATA_TYPE between", value1, value2, "dataType");
			return (Criteria) this;
		}

		public Criteria andDataTypeNotBetween(String value1, String value2) {
			addCriterion("DATA_TYPE not between", value1, value2, "dataType");
			return (Criteria) this;
		}

		public Criteria andSiteNoIsNull() {
			addCriterion("SITE_NO is null");
			return (Criteria) this;
		}

		public Criteria andSiteNoIsNotNull() {
			addCriterion("SITE_NO is not null");
			return (Criteria) this;
		}

		public Criteria andSiteNoEqualTo(String value) {
			addCriterion("SITE_NO =", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoNotEqualTo(String value) {
			addCriterion("SITE_NO <>", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoGreaterThan(String value) {
			addCriterion("SITE_NO >", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoGreaterThanOrEqualTo(String value) {
			addCriterion("SITE_NO >=", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoLessThan(String value) {
			addCriterion("SITE_NO <", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoLessThanOrEqualTo(String value) {
			addCriterion("SITE_NO <=", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoLike(String value) {
			addCriterion("SITE_NO like", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoNotLike(String value) {
			addCriterion("SITE_NO not like", value, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoIn(List<String> values) {
			addCriterion("SITE_NO in", values, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoNotIn(List<String> values) {
			addCriterion("SITE_NO not in", values, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoBetween(String value1, String value2) {
			addCriterion("SITE_NO between", value1, value2, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSiteNoNotBetween(String value1, String value2) {
			addCriterion("SITE_NO not between", value1, value2, "siteNo");
			return (Criteria) this;
		}

		public Criteria andSuccessCtIsNull() {
			addCriterion("SUCCESS_CT is null");
			return (Criteria) this;
		}

		public Criteria andSuccessCtIsNotNull() {
			addCriterion("SUCCESS_CT is not null");
			return (Criteria) this;
		}

		public Criteria andSuccessCtEqualTo(Integer value) {
			addCriterion("SUCCESS_CT =", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtNotEqualTo(Integer value) {
			addCriterion("SUCCESS_CT <>", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtGreaterThan(Integer value) {
			addCriterion("SUCCESS_CT >", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtGreaterThanOrEqualTo(Integer value) {
			addCriterion("SUCCESS_CT >=", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtLessThan(Integer value) {
			addCriterion("SUCCESS_CT <", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtLessThanOrEqualTo(Integer value) {
			addCriterion("SUCCESS_CT <=", value, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtIn(List<Integer> values) {
			addCriterion("SUCCESS_CT in", values, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtNotIn(List<Integer> values) {
			addCriterion("SUCCESS_CT not in", values, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtBetween(Integer value1, Integer value2) {
			addCriterion("SUCCESS_CT between", value1, value2, "successCt");
			return (Criteria) this;
		}

		public Criteria andSuccessCtNotBetween(Integer value1, Integer value2) {
			addCriterion("SUCCESS_CT not between", value1, value2, "successCt");
			return (Criteria) this;
		}

		public Criteria andFailCtIsNull() {
			addCriterion("FAIL_CT is null");
			return (Criteria) this;
		}

		public Criteria andFailCtIsNotNull() {
			addCriterion("FAIL_CT is not null");
			return (Criteria) this;
		}

		public Criteria andFailCtEqualTo(Integer value) {
			addCriterion("FAIL_CT =", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtNotEqualTo(Integer value) {
			addCriterion("FAIL_CT <>", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtGreaterThan(Integer value) {
			addCriterion("FAIL_CT >", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtGreaterThanOrEqualTo(Integer value) {
			addCriterion("FAIL_CT >=", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtLessThan(Integer value) {
			addCriterion("FAIL_CT <", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtLessThanOrEqualTo(Integer value) {
			addCriterion("FAIL_CT <=", value, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtIn(List<Integer> values) {
			addCriterion("FAIL_CT in", values, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtNotIn(List<Integer> values) {
			addCriterion("FAIL_CT not in", values, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtBetween(Integer value1, Integer value2) {
			addCriterion("FAIL_CT between", value1, value2, "failCt");
			return (Criteria) this;
		}

		public Criteria andFailCtNotBetween(Integer value1, Integer value2) {
			addCriterion("FAIL_CT not between", value1, value2, "failCt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtIsNull() {
			addCriterion("FIRST_DATA_DT is null");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtIsNotNull() {
			addCriterion("FIRST_DATA_DT is not null");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtEqualTo(Date value) {
			addCriterion("FIRST_DATA_DT =", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtNotEqualTo(Date value) {
			addCriterion("FIRST_DATA_DT <>", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtGreaterThan(Date value) {
			addCriterion("FIRST_DATA_DT >", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtGreaterThanOrEqualTo(Date value) {
			addCriterion("FIRST_DATA_DT >=", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtLessThan(Date value) {
			addCriterion("FIRST_DATA_DT <", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtLessThanOrEqualTo(Date value) {
			addCriterion("FIRST_DATA_DT <=", value, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtIn(List<Date> values) {
			addCriterion("FIRST_DATA_DT in", values, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtNotIn(List<Date> values) {
			addCriterion("FIRST_DATA_DT not in", values, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtBetween(Date value1, Date value2) {
			addCriterion("FIRST_DATA_DT between", value1, value2, "firstDataDt");
			return (Criteria) this;
		}

		public Criteria andFirstDataDtNotBetween(Date value1, Date value2) {
			addCriterion("FIRST_DATA_DT not between", value1, value2,
					"firstDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtIsNull() {
			addCriterion("LAST_DATA_DT is null");
			return (Criteria) this;
		}

		public Criteria andLastDataDtIsNotNull() {
			addCriterion("LAST_DATA_DT is not null");
			return (Criteria) this;
		}

		public Criteria andLastDataDtEqualTo(Date value) {
			addCriterion("LAST_DATA_DT =", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtNotEqualTo(Date value) {
			addCriterion("LAST_DATA_DT <>", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtGreaterThan(Date value) {
			addCriterion("LAST_DATA_DT >", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtGreaterThanOrEqualTo(Date value) {
			addCriterion("LAST_DATA_DT >=", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtLessThan(Date value) {
			addCriterion("LAST_DATA_DT <", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtLessThanOrEqualTo(Date value) {
			addCriterion("LAST_DATA_DT <=", value, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtIn(List<Date> values) {
			addCriterion("LAST_DATA_DT in", values, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtNotIn(List<Date> values) {
			addCriterion("LAST_DATA_DT not in", values, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtBetween(Date value1, Date value2) {
			addCriterion("LAST_DATA_DT between", value1, value2, "lastDataDt");
			return (Criteria) this;
		}

		public Criteria andLastDataDtNotBetween(Date value1, Date value2) {
			addCriterion("LAST_DATA_DT not between", value1, value2,
					"lastDataDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtIsNull() {
			addCriterion("MOST_RECENT_SUCCESS_DT is null");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtIsNotNull() {
			addCriterion("MOST_RECENT_SUCCESS_DT is not null");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtEqualTo(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT =", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtNotEqualTo(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT <>", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtGreaterThan(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT >", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtGreaterThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT >=", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtLessThan(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT <", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtLessThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_SUCCESS_DT <=", value,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtIn(List<Date> values) {
			addCriterion("MOST_RECENT_SUCCESS_DT in", values,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtNotIn(List<Date> values) {
			addCriterion("MOST_RECENT_SUCCESS_DT not in", values,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_SUCCESS_DT between", value1, value2,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentSuccessDtNotBetween(Date value1,
				Date value2) {
			addCriterion("MOST_RECENT_SUCCESS_DT not between", value1, value2,
					"mostRecentSuccessDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtIsNull() {
			addCriterion("MOST_RECENT_ATTEMPT_DT is null");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtIsNotNull() {
			addCriterion("MOST_RECENT_ATTEMPT_DT is not null");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtEqualTo(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT =", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtNotEqualTo(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT <>", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtGreaterThan(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT >", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtGreaterThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT >=", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtLessThan(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT <", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtLessThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_ATTEMPT_DT <=", value,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtIn(List<Date> values) {
			addCriterion("MOST_RECENT_ATTEMPT_DT in", values,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtNotIn(List<Date> values) {
			addCriterion("MOST_RECENT_ATTEMPT_DT not in", values,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_ATTEMPT_DT between", value1, value2,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentAttemptDtNotBetween(Date value1,
				Date value2) {
			addCriterion("MOST_RECENT_ATTEMPT_DT not between", value1, value2,
					"mostRecentAttemptDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtIsNull() {
			addCriterion("MOST_RECENT_FAIL_DT is null");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtIsNotNull() {
			addCriterion("MOST_RECENT_FAIL_DT is not null");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtEqualTo(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT =", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtNotEqualTo(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT <>", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtGreaterThan(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT >", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtGreaterThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT >=", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtLessThan(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT <", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtLessThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_FAIL_DT <=", value, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtIn(List<Date> values) {
			addCriterion("MOST_RECENT_FAIL_DT in", values, "mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtNotIn(List<Date> values) {
			addCriterion("MOST_RECENT_FAIL_DT not in", values,
					"mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_FAIL_DT between", value1, value2,
					"mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentFailDtNotBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_FAIL_DT not between", value1, value2,
					"mostRecentFailDt");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityIsNull() {
			addCriterion("FETCH_PRIORITY is null");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityIsNotNull() {
			addCriterion("FETCH_PRIORITY is not null");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityEqualTo(Integer value) {
			addCriterion("FETCH_PRIORITY =", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityNotEqualTo(Integer value) {
			addCriterion("FETCH_PRIORITY <>", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityGreaterThan(Integer value) {
			addCriterion("FETCH_PRIORITY >", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityGreaterThanOrEqualTo(Integer value) {
			addCriterion("FETCH_PRIORITY >=", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityLessThan(Integer value) {
			addCriterion("FETCH_PRIORITY <", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityLessThanOrEqualTo(Integer value) {
			addCriterion("FETCH_PRIORITY <=", value, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityIn(List<Integer> values) {
			addCriterion("FETCH_PRIORITY in", values, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityNotIn(List<Integer> values) {
			addCriterion("FETCH_PRIORITY not in", values, "fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityBetween(Integer value1, Integer value2) {
			addCriterion("FETCH_PRIORITY between", value1, value2,
					"fetchPriority");
			return (Criteria) this;
		}

		public Criteria andFetchPriorityNotBetween(Integer value1,
				Integer value2) {
			addCriterion("FETCH_PRIORITY not between", value1, value2,
					"fetchPriority");
			return (Criteria) this;
		}

		public Criteria andEmptyCtIsNull() {
			addCriterion("EMPTY_CT is null");
			return (Criteria) this;
		}

		public Criteria andEmptyCtIsNotNull() {
			addCriterion("EMPTY_CT is not null");
			return (Criteria) this;
		}

		public Criteria andEmptyCtEqualTo(Integer value) {
			addCriterion("EMPTY_CT =", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtNotEqualTo(Integer value) {
			addCriterion("EMPTY_CT <>", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtGreaterThan(Integer value) {
			addCriterion("EMPTY_CT >", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtGreaterThanOrEqualTo(Integer value) {
			addCriterion("EMPTY_CT >=", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtLessThan(Integer value) {
			addCriterion("EMPTY_CT <", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtLessThanOrEqualTo(Integer value) {
			addCriterion("EMPTY_CT <=", value, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtIn(List<Integer> values) {
			addCriterion("EMPTY_CT in", values, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtNotIn(List<Integer> values) {
			addCriterion("EMPTY_CT not in", values, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtBetween(Integer value1, Integer value2) {
			addCriterion("EMPTY_CT between", value1, value2, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andEmptyCtNotBetween(Integer value1, Integer value2) {
			addCriterion("EMPTY_CT not between", value1, value2, "emptyCt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtIsNull() {
			addCriterion("MOST_RECENT_EMPTY_DT is null");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtIsNotNull() {
			addCriterion("MOST_RECENT_EMPTY_DT is not null");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtEqualTo(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT =", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtNotEqualTo(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT <>", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtGreaterThan(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT >", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtGreaterThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT >=", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtLessThan(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT <", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtLessThanOrEqualTo(Date value) {
			addCriterion("MOST_RECENT_EMPTY_DT <=", value, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtIn(List<Date> values) {
			addCriterion("MOST_RECENT_EMPTY_DT in", values, "mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtNotIn(List<Date> values) {
			addCriterion("MOST_RECENT_EMPTY_DT not in", values,
					"mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_EMPTY_DT between", value1, value2,
					"mostRecentEmptyDt");
			return (Criteria) this;
		}

		public Criteria andMostRecentEmptyDtNotBetween(Date value1, Date value2) {
			addCriterion("MOST_RECENT_EMPTY_DT not between", value1, value2,
					"mostRecentEmptyDt");
			return (Criteria) this;
		}
	}

	/**
	 * This class was generated by MyBatis Generator. This class corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
	 * @mbggenerated  Tue Jun 19 12:15:56 CDT 2012
	 */
	public static class Criterion {
		private String condition;
		private Object value;
		private Object secondValue;
		private boolean noValue;
		private boolean singleValue;
		private boolean betweenValue;
		private boolean listValue;
		private String typeHandler;

		public String getCondition() {
			return condition;
		}

		public Object getValue() {
			return value;
		}

		public Object getSecondValue() {
			return secondValue;
		}

		public boolean isNoValue() {
			return noValue;
		}

		public boolean isSingleValue() {
			return singleValue;
		}

		public boolean isBetweenValue() {
			return betweenValue;
		}

		public boolean isListValue() {
			return listValue;
		}

		public String getTypeHandler() {
			return typeHandler;
		}

		protected Criterion(String condition) {
			super();
			this.condition = condition;
			this.typeHandler = null;
			this.noValue = true;
		}

		protected Criterion(String condition, Object value, String typeHandler) {
			super();
			this.condition = condition;
			this.value = value;
			this.typeHandler = typeHandler;
			if (value instanceof List<?>) {
				this.listValue = true;
			} else {
				this.singleValue = true;
			}
		}

		protected Criterion(String condition, Object value) {
			this(condition, value, null);
		}

		protected Criterion(String condition, Object value, Object secondValue,
				String typeHandler) {
			super();
			this.condition = condition;
			this.value = value;
			this.secondValue = secondValue;
			this.typeHandler = typeHandler;
			this.betweenValue = true;
		}

		protected Criterion(String condition, Object value, Object secondValue) {
			this(condition, value, secondValue, null);
		}
	}

	/**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table GW_DATA_PORTAL.CACHE_META_DATA
     *
     * @mbggenerated do_not_delete_during_merge Thu Apr 05 11:31:26 CDT 2012
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }
}
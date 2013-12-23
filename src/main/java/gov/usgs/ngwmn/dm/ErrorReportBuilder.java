package gov.usgs.ngwmn.dm;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.ngwmn.dm.dao.FetchLog;
import gov.usgs.ngwmn.dm.dao.FetchLogDAO;

public class ErrorReportBuilder {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private FetchLogDAO dao;


	public ErrorReportBuilder() {
	}


	public String build(int agencyMax, int emptyMax, int problemMax) {
		StringBuilder msg = new StringBuilder();

		List<String> agencies = buildAgencyErrorCount(agencyMax, msg);
		logger.debug("\n message  {}\n",msg);
		logger.debug("\n agencies {}\n",agencies.toString());

		buildSiteErrorMsg(agencies, msg, emptyMax, problemMax);
		return msg.toString();
	}

	protected void buildSiteErrorMsg(List<String> agencies, StringBuilder msg, int emptyMax, int problemMax) {
		// now get the sites' issues for agencies with fewer than agencyMax
		List<FetchLog> siteProblems = dao.fetchSiteProblems();

		StringBuilder submsg = new StringBuilder();
		for (FetchLog fetchLog : siteProblems) {

			// if the agency has too many sites with issues then do not report specific issues
			if ( agencies.contains( fetchLog.getAgencyCd() ) ) continue;

			int max = StringUtils.isEmpty( fetchLog.getProblem() ) ?emptyMax :problemMax;

			int count  = dao.fetchSuccessCount( fetchLog.getAgencyCd(), fetchLog.getSiteNo(), fetchLog.getDataStream(), max);

			// if not all failed then the threshold has not been hit yet
			if (count != 0) {
				logger.debug("Skipping errors for site {}:{} because there were not enough errors recently.", fetchLog.getAgencyCd(), fetchLog.getSiteNo());
				continue;
			}

			append(submsg,fetchLog.getAgencyCd(),":",fetchLog.getSiteNo(),
					" has had issues for ", max, " days. The most recent issue is ",
					fetchLog.getDataStream(), " - ");
			if ( ! StringUtils.isEmpty( fetchLog.getProblem() ) ) {
				submsg.append( fetchLog.getProblem() );
			} else {
				submsg.append( fetchLog.getStatus() );
			}

			submsg.append("\n");

			// TODO possible enh : could also fetch all recent issues
			//List<FetchLog> issues = dao.fetchProblems(fetchLog.getAgencyCd(), fetchLog.getSiteNo(), problemMax);
		}

		// only apply the sub-message if something was reported
		if (submsg.length() > 0) {
			append(msg, "\n\nThe following sites have had problem/empty issues. (agency:site) (type - issue)\n", submsg, "\n");
		}

	}

	protected List<String> buildAgencyErrorCount(int agencyMax, StringBuilder msg) {
		List<String>   agencies = new LinkedList<String>();
		List<FetchLog> agencyCounts = dao.fetchAgencyProblemCount();

		for (FetchLog fetchLog : agencyCounts) {
			if (fetchLog.getCt() < agencyMax) continue;

			agencies.add( fetchLog.getAgencyCd() ); // keep track of the agencies with too many errors
		}
		if (agencies.size() > 0) {
			append(msg,"\n","The following agencies have ",agencyMax," or more sites with problem/empty issues. (agency - site count)");
			for (FetchLog fetchLog : agencyCounts) {
				if (agencies.contains(fetchLog.getAgencyCd())) {
					// the ct or count in this case is the count of sites with empty or problem
					append(msg,"\n",fetchLog.getAgencyCd()," -\t",fetchLog.getCt());
				}
			}
			msg.append("\n");
		}

		return agencies;
	}


	public void append(StringBuilder msg, Object ... parts) {
		for (Object part : parts) {
			if (part==null) continue;
			msg.append(part.toString());
		}
	}


	public void setDao(FetchLogDAO dao) {
		this.dao = dao;
	}


}

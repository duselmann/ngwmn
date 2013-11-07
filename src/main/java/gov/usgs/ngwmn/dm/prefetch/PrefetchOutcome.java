package gov.usgs.ngwmn.dm.prefetch;

/**
 * States for Prefetch processes
 * 
 * @author rhayes
 */

public enum PrefetchOutcome {
	UNSTARTED,
	RUNNING,
	FINISHED,
	LIMIT_TIME,
	LIMIT_COUNT,
	INTERRUPTED,
	PROBLEM
}

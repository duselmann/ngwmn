package gov.usgs.ngwmn.dm.aspect;

import gov.usgs.ngwmn.dm.DataFetcher;
import gov.usgs.ngwmn.dm.cache.PipeStatistics;
import gov.usgs.ngwmn.dm.cache.PipeStatisticsWithProblem;
import gov.usgs.ngwmn.dm.cache.PipeStatistics.Status;
import gov.usgs.ngwmn.dm.dao.FetchLog;
import gov.usgs.ngwmn.dm.io.Pipeline;
import gov.usgs.ngwmn.dm.spec.Specifier;
import gov.usgs.ngwmn.dm.harvest.WebRetriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public aspect PipeStatisticsAspect {
	private PipeStatistics Pipeline.stats = new PipeStatistics();
	private FetchLog Specifier.fetchLog = null;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
		
	// we expect this to be overridden by Spring configuration
	private EventBus fetchEventBus = new EventBus();
	
	public void setEventBus(EventBus eventBus) {
		this.fetchEventBus = eventBus;
	}

	// Monitor pipeline setup
	pointcut setInput(DataFetcher df, Specifier spec, Pipeline p) : 
		call(* DataFetcher.configureInput(Specifier, Pipeline))
		&& target(df)
		&& args(spec, p);
	
	before(DataFetcher df, Specifier spec, Pipeline p) : setInput(df, spec, p) {
		p.stats.setSpecifier(spec);
		p.stats.setCalledBy(df.getClass());
	}
	
	after(DataFetcher df, Specifier spec, Pipeline p) throwing (Exception oops): setInput(df, spec, p) {
		p.stats.markEnd(Status.FAIL);
		logger.debug("stopped in setInput {}", p);
		PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(p.stats, oops);
		fetchEventBus.post(pswp);
	}
		
	// monitor pipeline execution
	// TODO Find a better way to handle aggregate pipelines
	pointcut invoke(Pipeline p):
		call(* Pipeline.invoke())
		&& target(p);
	
	before(Pipeline p): invoke(p){
		p.stats.markStart();
		logger.debug("started {} in invoke", p);
	}
	
	after(Pipeline p) returning (long ct): invoke(p) {
		p.stats.incrementCount(ct);
		logger.debug("stopped in invoke {} returning {}", p, ct);
		// System.out.println("returning tjp=" + thisJointPoint);
		p.stats.markEnd(Status.DONE);
		if (null == p.stats.getSpecifier()) {
			// presume it was an aggregate, have to use some generalized recording mechanism
			logger.info("after invoke of aggregate {}", p);
		} else {
			fetchEventBus.post(p.stats);
		}
	}
	
	pointcut inspect(Pipeline p, int cacheKey, Specifier spec):
		cflow(invoke(p)) &&
		call(* gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache.invokeInspect(int,Specifier))
		&& args(cacheKey, spec);
	
	before(Pipeline p, int cacheKey, Specifier spec):
		inspect(p,cacheKey,spec) {
		// pass fetch log (if it exists) through to inspector
		spec.fetchLog = p.stats.getFetchLog();
		// launching inspection of cache entry
		logger.debug("inspecting {} for spec {}", cacheKey, p.getSpecifier());
	}
	
	// use recorded fetch log, as inspection may be asynchronous
	pointcut withdraw(int ck, Specifier spec):
		call(* gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache.withdraw(int,Specifier)) &&
		args(ck,spec);
	
	after(int ck, Specifier spec):withdraw(ck,spec) {
		// record that this fetch was inspected and found empty
		logger.debug("record withdraw for {} with fetchlog={}", spec, spec.fetchLog);
		
		
	}
	
	// use recorded fetch log, as inspection may be asynchronous
	pointcut publish(int ck, Specifier spec):
		call(* gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache.publish(int,Specifier)) &&
		args(ck,spec);
	
	after(int ck, Specifier spec):publish(ck,spec) {
		// record that this fetch was inspected and found OK
		logger.debug("record publish for {} with fetchlog={}", spec, spec.fetchLog);
		
	}

	after(Pipeline p) throwing (Exception e) : invoke(p) {
		logger.debug("stopped in invoke {} throwing {}", p, e);
		// System.out.println("throwing tjp=" + thisJointPoint);
		p.stats.markEnd(Status.FAIL);
		if (null == p.stats.getSpecifier()) {
			// presume it was an aggregate, have to use somne generalized recording mechanism
			logger.info("after invoke of aggregate {}", p);
		} else {
			PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(p.stats, e);
			fetchEventBus.post(pswp);
		}
	}
	
	// special monitoring for web fetcher
	// gov.usgs.ngwmn.dm.harvest.WebRetriever.WebInputSupplier.makeSupply(Specifier)
	pointcut webfetch(WebRetriever.WebInputSupplier supplier):
		call(* *.initialize())
		&& target(supplier);
	
	before(WebRetriever.WebInputSupplier s):webfetch(s) {
		Pipeline pipe = s.getPipeline();
		pipe.stats.setSource(s.getUrl());
		pipe.stats.markStart(); 
		logger.debug("started in webfetch {}", pipe);
	}
	
	after(WebRetriever.WebInputSupplier s) throwing(Exception e): 
		webfetch(s) && ! cflow(call(* Pipeline.invoke())) 
	{
		Pipeline pipe = s.getPipeline();
		logger.debug("stopped in webfetch {} throw", pipe);
		pipe.stats.markEnd(Status.FAIL);
		
		PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(pipe.stats, e);
		fetchEventBus.post(pswp);
	}
	
	// Note that webfetch does not fetch the bytes, so cannot mark end in this pointcut.
}

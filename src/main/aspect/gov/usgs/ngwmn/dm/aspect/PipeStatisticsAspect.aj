package gov.usgs.ngwmn.dm.aspect;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import gov.usgs.ngwmn.dm.DataFetcher;
import gov.usgs.ngwmn.dm.cache.PipeStatistics;
import gov.usgs.ngwmn.dm.cache.PipeStatisticsWithProblem;
import gov.usgs.ngwmn.dm.cache.PipeStatistics.Status;
import gov.usgs.ngwmn.dm.dao.FetchLog;
import gov.usgs.ngwmn.dm.io.Pipeline;
import gov.usgs.ngwmn.dm.prefetch.Prefetcher;
import gov.usgs.ngwmn.dm.spec.Specifier;
import gov.usgs.ngwmn.dm.harvest.WebRetriever;
import gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache;
import gov.usgs.ngwmn.dm.io.CopyInvoker;
import gov.usgs.ngwmn.dm.io.Invoker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

public aspect PipeStatisticsAspect {
	private PipeStatistics Pipeline.stats = new PipeStatistics();
	private PipeStatistics Specifier.stats = null;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
		
	// we expect this to be overridden by Spring configuration
	private EventBus fetchEventBus = new EventBus();
	
	public void setEventBus(EventBus eventBus) {
		this.fetchEventBus = eventBus;
	}
	
	private void recordFetch(Pipeline p, long ct) {
		p.stats.incrementCount(ct);
		logger.debug("record fetch for {} returning {}", p, ct);
		logger.debug("stats in invoke is {}", p.stats);

		p.stats.markEnd(Status.DONE);

		if (null == p.stats.getSpecifier()) {
			// presume it was an aggregate, have to use some generalized recording mechanism
			logger.info("after fetch of aggregate {}", p);
		} else {
			fetchEventBus.post(p.stats);
		}
	}


	// Monitor pipeline setup
	pointcut setInput(DataFetcher df, Specifier spec, Pipeline p) : 
		call(* DataFetcher.configureInput(Specifier, Pipeline))
		&& target(df)
		&& args(spec, p);
	
	pointcut prefetch() :
		cflow(call( * gov.usgs.ngwmn.dm.PrefetchI.prefetchWellData(Specifier)));
	
	before(DataFetcher df, Specifier spec, Pipeline p) : 
		setInput(df, spec, p) 
		&& ! prefetch()
	{
		logger.trace("ASPECT: Enter before setInput not prefetch");
		p.stats.setSpecifier(spec);
		p.stats.setCalledBy(df.getClass());
		logger.trace("ASPECT: Exit  before setInput");
	}
	
	before(DataFetcher df, Specifier spec, Pipeline p) : 
		setInput(df, spec, p) 
		&& prefetch()
	{
		logger.trace("ASPECT: Enter before setInput prefetch");
		p.stats.setSpecifier(spec);
		p.stats.setCalledBy(gov.usgs.ngwmn.dm.PrefetchI.class);
		logger.trace("ASPECT: Exit  before setInput");
	}

	after(DataFetcher df, Specifier spec, Pipeline p) throwing (Exception oops): setInput(df, spec, p) {
		logger.trace("ASPECT: Enter after setInput");
		p.stats.markEnd(Status.FAIL);
		logger.debug("stopped in setInput {}", p);
		PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(p.stats, oops);
		fetchEventBus.post(pswp);
		logger.trace("ASPECT: Exit  after setInput");
	}
		
	// monitor pipeline execution
	// TODO Find a better way to handle aggregate pipelines
	pointcut invoke(Pipeline p):
		call(* Pipeline.invoke())
		&& target(p);
	
	before(Pipeline p): invoke(p){
		logger.trace("ASPECT: Enter before invoke");
		p.stats.markStart();
		logger.debug("started {} in invoke", p);
		logger.trace("ASPECT: Exit  before invoke");
	}
	
	after(Pipeline p) returning (long ct): invoke(p) {
		logger.trace("ASPECT: Enter after invoke");
		// recordFetch(p,ct);
		logger.trace("ASPECT: Exit  after invoke");
	}
	
	after(Pipeline p) throwing (Exception e) : invoke(p) {
		logger.trace("ASPECT: Enter after publish:exception");
		recordFail(p, e);
		logger.trace("ASPECT: Exit  after publish:exception");
	}

	private void recordFail(Pipeline p, Exception e) {
		logger.debug("stopped in invoke {} throwing {}", p, e);
		// System.out.println("throwing tjp=" + thisJointPoint);
		p.stats.markEndForce(Status.FAIL);
		if (null == p.stats.getSpecifier()) {
			// presume it was an aggregate, have to use somne generalized recording mechanism
			logger.info("after invoke of aggregate {}", p);
		} else {
			PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(p.stats, e);
			fetchEventBus.post(pswp);
		}
	}
	
	pointcut copyInvoke(Pipeline p):
		call(* Invoker.invoke(InputStream, OutputStream)) &&
		// TODO add target qualifier for CopyInvoke?
		cflow(invoke(p));
		
	after(Pipeline p) returning (long ct): copyInvoke(p) {
		logger.trace("ASPECT: Enter after copy invoke normal");
		recordFetch(p,ct);
		logger.trace("ASPECT: Exit  after copy invoke normal");		
	}
		
	after(Pipeline p) throwing (Exception e) : copyInvoke(p) {
		logger.trace("ASPECT: Enter after copy invoke exception");
		// TODO Why does this not get called?
		// Replaced by invocation in after pipleline invoke.
		// recordFail(p, e);
		logger.trace("ASPECT: Exit  after copy invoke exception");
	}
	
	pointcut inspect(Pipeline p, int cacheKey, Specifier spec):
		cflow(invoke(p)) &&
		call(* gov.usgs.ngwmn.dm.cache.qw.DatabaseXMLCache.invokeInspect(int,Specifier))
		&& args(cacheKey, spec);
	
	before(Pipeline p, int cacheKey, Specifier spec):
		inspect(p,cacheKey,spec) {
		logger.trace("ASPECT: Enter before inspect");
		// pass pipe stats through to inspector
		spec.stats = p.stats;
		// launching inspection of cache entry
		logger.debug("inspecting {} for spec {}", cacheKey, p.getSpecifier());
		logger.debug("stats in inspect is {}", p.stats);
		logger.trace("ASPECT: Exit  before inspect");
	}
	
	// use recorded pipe stats, as inspection may be asynchronous
	pointcut withdraw(int ck, Specifier spec, DatabaseXMLCache cache):
		call(* DatabaseXMLCache.withdraw(int,Specifier)) &&
		target(cache) &&
		args(ck,spec);
	
	after(int ck, Specifier spec, DatabaseXMLCache cache):withdraw(ck,spec,cache) {
		logger.trace("ASPECT: Enter after withdraw");
		// record that this fetch was inspected and found empty
		if (spec.stats == null) {
			logger.warn("After withdraw, no stats in spec {}", spec);
			return;
		}
		
		logger.debug("note withdraw for {} with pipe stats={}", spec, spec.stats);		
		try {
			// this waits until the FetchRecorder sets the fetch log on the PipeStatistics
			FetchLog fl = spec.stats.getFetchLog(15, TimeUnit.SECONDS);
			if (fl != null) {
				// TODO is withdraw really the same as EMPY?
				fl.setStatus(PipeStatistics.Status.EMPY.as4Char());
				
				// let the event bus update the fetch log
				fetchEventBus.post(fl);
				
				// remember the linkage
				cache.linkFetchLog(fl.getFetchlogId(), ck);
			} else {
				logger.warn("Null fetch log entry for {} in withdraw", spec);
			}
		} catch (InterruptedException ie) {
			// oh well, the fetch did not get recorded in time, give up
			logger.warn("Abandoned wait for fetchlog in withdraw for {}", spec);
		}
		logger.trace("ASPECT: Exit  after withdraw");
	}
	
	// use recorded fetch log, as inspection may be asynchronous
	pointcut publish(int ck, Specifier spec, DatabaseXMLCache cache):
		call(* DatabaseXMLCache.publish(int,Specifier)) &&
		target(cache) &&
		args(ck,spec);
	
	after(int ck, Specifier spec, DatabaseXMLCache cache):publish(ck,spec,cache) {
		logger.trace("ASPECT: Enter after publish");
		if (spec.stats == null) {
			logger.warn("After publish, No stats in specifier {}", spec);
			return;
		}

		// record that this fetch was inspected and found OK
		logger.debug("record publish for {} with pipe stats={}", spec, spec.stats);
		
		// update cache to remember fetch log
		try {
			// this waits until the FetchRecorder sets the fetch log on the PipeStatistics
			FetchLog fl = spec.stats.getFetchLog(15, TimeUnit.SECONDS);
			if (fl != null) {
				// remember the linkage
				cache.linkFetchLog(fl.getFetchlogId(), ck);
			} else {
				logger.warn("Null fetch log entry for {} in publish", spec);
			}
		} catch (InterruptedException ie) {
			// oh well, the fetch did not get recorded in time, give up
			logger.warn("Abandoned wait for fetchlog in publish for {}", spec);
		}		
		
		logger.trace("ASPECT: Exit  after publish");
	}

	// special monitoring for web fetcher
	// gov.usgs.ngwmn.dm.harvest.WebRetriever.WebInputSupplier.makeSupply(Specifier)
	pointcut webfetch(WebRetriever.WebInputSupplier supplier):
		call(* *.initialize())
		&& target(supplier);
	
	before(WebRetriever.WebInputSupplier s):webfetch(s) {
		logger.trace("ASPECT: Enter before webfetch");
		Pipeline pipe = s.getPipeline();
		pipe.stats.setSource(s.getUrl());
		pipe.stats.markStart(); 
		logger.debug("started in webfetch {}", pipe);
		logger.trace("ASPECT: Exit  before webfetch");
	}
	
	after(WebRetriever.WebInputSupplier s) throwing(Exception e): 
		webfetch(s) && ! cflow(call(* Pipeline.invoke())) 
	{
		logger.trace("ASPECT: Enter after webfetch");
		Pipeline pipe = s.getPipeline();
		logger.debug("stopped in webfetch {} throw", pipe);
		pipe.stats.markEnd(Status.FAIL);
		
		PipeStatisticsWithProblem pswp = new PipeStatisticsWithProblem(pipe.stats, e);
		fetchEventBus.post(pswp);
		logger.trace("ASPECT: Exit  after webfetch");
	}
	
	// Note that webfetch does not fetch the bytes, so cannot mark end in this pointcut.
}

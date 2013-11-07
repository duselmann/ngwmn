package gov.usgs.ngwmn.dm.io.aggregate;

import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelFlowAggregator implements Flow {
	FlowFactory 		   factory;
	Iterable<Specifier>    specifiers;
	Supplier<OutputStream> output;

	int parallex = 1;

	private static final Logger logger = LoggerFactory.getLogger(ParallelFlowAggregator.class);

	public ParallelFlowAggregator(FlowFactory fac, Iterable<Specifier> specs, OutputStream out) {
		factory    = fac;
		specifiers = specs;
		output     =  new SimpleSupplier<OutputStream>(out);
	}
	public ParallelFlowAggregator(FlowFactory fac, Iterable<Specifier> specs, OutputStream out, int parallelism) {
		this(fac, specs, out);
		parallex = parallelism;
	}

	@Override
	public Void call() throws Exception {
		List<Flow> execs = new LinkedList<Flow>();
		for (Specifier spec : specifiers) {
			execs.add( factory.makeFlow(spec, output) );
		}

		ExecutorService exec = Executors.newFixedThreadPool(parallex);
		try {
			List<Future<Void>> futures = exec.invokeAll(execs);

			for (Future<Void> future : futures) {
				try {
					future.get(); // Since our current impl is Callable<Void> this just waits for finish
				} catch (Exception e) {
					logger.warn("Problem getting future", e);
				}
			}
		} finally {
			exec.shutdown();
		}
		return null;
	}

}

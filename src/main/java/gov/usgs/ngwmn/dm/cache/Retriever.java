package gov.usgs.ngwmn.dm.cache;

import java.io.IOException;

import gov.usgs.ngwmn.dm.DataFetcher;
import gov.usgs.ngwmn.dm.io.Pipeline;

public class Retriever implements DataFetcher {
	private Cache cache;
	
	public Retriever(Cache c) {
		this.cache = c;
	}

	@Override
	public boolean fetchWellData(Specifier spec, Pipeline pipe) throws Exception {
		return cache.fetchWellData(spec, pipe) != null; // TODO place holder for now
	}
	
	public boolean contains(Specifier spec) {
		return cache.contains(spec);
	}
}

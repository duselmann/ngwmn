package gov.usgs.ngwmn.ogc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Waterlevel19DataSource implements Closeable {

	private String baseURL; // example "http://localhost:8080/ngwmn_cache";

	static private Logger logger = LoggerFactory.getLogger(Waterlevel19DataSource.class);

	private HttpClient client;
	private GetMethod method;
	
	private String agency;
	private String site;
	
	public Waterlevel19DataSource(String base, String a, String s) {
		baseURL = base;
		agency = a;
		site = s;
	}
	
	public synchronized InputStream getStream() throws Exception {
		// data URL will be like /ngwmn_cache/data/$AGENCY/$SITE/WATERLEVEL
		// example http://cida-wiwsc-ngwmndev.er.usgs.gov:8080/ngwmn_cache/data/TWDB/6550504/WATERLEVEL
		client = new HttpClient();
		String url = MessageFormat.format("{0}/data/{1}/{2}/WATERLEVEL", baseURL, agency, site);
		method = new GetMethod(url);

		logger.info("trying to fetch some data from {} ", method.getURI());
		
		int statusCode = client.executeMethod(method);
		
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException("status " + statusCode);
		}
		
		InputStream is = method.getResponseBodyAsStream();
		
		return is;

	}

	@Override
	public synchronized void close() throws IOException {
		if (method != null) {
			method.releaseConnection();
			method = null;
		}
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	
	
}

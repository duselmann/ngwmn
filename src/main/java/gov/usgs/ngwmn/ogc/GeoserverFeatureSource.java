package gov.usgs.ngwmn.ogc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoserverFeatureSource implements Closeable {

	private String baseURL; // example "http://cida-wiwsc-ngwmndev.er.usgs.gov:8080/ngwmn/geoserver";
	private List<NameValuePair> extraParams = new ArrayList<NameValuePair>();

	static private Logger logger = LoggerFactory.getLogger(GeoserverFeatureSource.class);

	private HttpClient client;
	private PostMethod method;
		
	public GeoserverFeatureSource(String base) {
		baseURL = base;
	}
	
	public GeoserverFeatureSource addParameter(String name, String value) {
		NameValuePair nvp = new NameValuePair(name, value);
		extraParams.add(nvp);
		return this;
	}
	
	public InputStream getStream() throws Exception {
		// generate params for GeoServer WFS request, example:
		/*		SERVICE:WFS
		 		request=GetFeature
				VERSION:1.0.0
				srsName:EPSG:4326
				outputFormat:GML2
				typeName:ngwmn:VW_GWDP_GEOSERVER
				CQL_FILTER:((QW_SN_FLAG = '1') OR (WL_SN_FLAG = '1')) AND (BBOX(GEOM,-101.333008,34.269568,-99.838867,35.459421))
				*/
		client = new HttpClient();
		method = new PostMethod(baseURL + "/wfs");
		method.addParameter("request","GetFeature");
		method.addParameter("SERVICE", "WFS");
		method.addParameter("VERSION", "1");
		method.addParameter("outputFormat","GML2");
		method.addParameter("typeName","ngwmn:VW_GWDP_GEOSERVER");
		
		for (NameValuePair nvp : extraParams) {
			method.addParameter(nvp);
		}
		
		logger.info("trying to fetch some data from {} with params {}", method.getURI(), method.getParameters());
		
		int statusCode = client.executeMethod(method);
		
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException("status " + statusCode);
		}
		
		InputStream is = method.getResponseBodyAsStream();
		
		return is;

	}

	@Override
	public void close() throws IOException {
		method.releaseConnection();
		method = null;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	
	
}

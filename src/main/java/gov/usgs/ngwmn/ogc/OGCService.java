package gov.usgs.ngwmn.ogc;

import gov.usgs.ngwmn.dm.io.TeeInputStream;
import gov.usgs.ngwmn.dm.io.transform.XSLHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CountingInputStream;
import com.google.common.io.CountingOutputStream;

public abstract class OGCService {

	private String geoserverURL;
	
	static private Logger logger = LoggerFactory.getLogger(OGCService.class);

	public static void copyThroughTransform(InputStream is, OutputStream os,
			String xformName) throws IOException, TransformerException {
		XSLHelper xslHelper = new XSLHelper();
		xslHelper.setTransform(xformName);

		CountingInputStream countingIs = null;
		TeeInputStream teeIs = null;
		CountingOutputStream countingOs = null;

		if (logger.isDebugEnabled()) {
			countingIs = new CountingInputStream(is);
			is = countingIs;

			countingOs = new CountingOutputStream(os);
			os = countingOs;
		}

		if (logger.isTraceEnabled()) {
			File tOut = File.createTempFile("geoserver",".xml");
			logger.info("Saving a copy of geo-output to {}", tOut);
			FileOutputStream fos = new FileOutputStream(tOut);
			teeIs = new TeeInputStream(is, fos, true);
			is = teeIs;
		}

		Transformer t = xslHelper.getTemplates().newTransformer();
		StreamResult result = new StreamResult(os);
		StreamSource source = new StreamSource(is);	

		t.transform(source, result);

		if (countingIs != null) {
			logger.debug("Processed {} bytes of input", countingIs.getCount());
		}
		if (countingOs != null) {
			logger.debug("Got {} bytes of output", countingOs.getCount());
		}
	}

	public OGCService() {
		super();
	}

	public String getGeoserverURL() {
		return geoserverURL;
	}

	public void setGeoserverURL(String gsURL) {
		this.geoserverURL = gsURL;
		logger.info("Will use geoserver URL {}", this.geoserverURL);
	}

	public abstract String getTransformLocation();

}
package gov.usgs.ngwmn.ogc;

import gov.usgs.ngwmn.NotImplementedException;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class WFSService extends OGCService {

	public static final String FEATURE_PREFIX = "VW_GWDP_GEOSERVER";
	
	static private Logger logger = LoggerFactory.getLogger(WFSService.class);

	@RequestMapping(params={"REQUEST=GetCapabilities"})
	public void getCapabilities(
			OutputStream out
	) {
		// TODO deliver static file
		throw new NotImplementedException();
	}
	
	// GetFeatureOfInterest
	// implement on geoserver, with output transformed to gwml
	@RequestMapping(params={"REQUEST=GetFeature"})
	public void getFeature(
			@RequestParam("FID") String featureOfInterest,
			HttpServletResponse response
			)
		throws Exception
	{
		GeoserverFeatureSource featureSource = new GeoserverFeatureSource(getGeoserverURL());
		
		logger.info("GetFeature");
		
		SiteID site = SiteID.fromFid(featureOfInterest);
		logger.debug("Filter for site {}", site);
		
		featureSource.addParameter("featureID", featureOfInterest);
		logger.debug("Added filter featureID={}", featureOfInterest);
		
		try {
			InputStream is = featureSource.getStream();
			
			response.setContentType("text/xml");
			OutputStream os = response.getOutputStream();

			// copy from stream to response, filtering through xsl transform
			copyThroughTransform(is,os, getTransformLocation());
			logger.debug("done");
		}
		catch (Exception e) {
			logger.warn("Problem", e);
			throw e;
		}
		finally {
			featureSource.close();
		}
	}

	public String getTransformLocation() {
		return "/gov/usgs/ngwmn/geoserver-2-gwml.xsl";
	}
	
}

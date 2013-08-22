package gov.usgs.ngwmn.ogc;

import gov.usgs.ngwmn.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Controller
public class SOSService extends OGCService {

	public static final String FEATURE_PREFIX = "VW_GWDP_GEOSERVER";
	public static final String BOUNDING_BOX_PREFIX = "om:featureOfInterest/*/sams:shape";
	
	static private Logger logger = LoggerFactory.getLogger(SOSService.class);

	@RequestMapping(params={"REQUEST=GetCapabilities"})
	public void getCapabilities(
			OutputStream out
	) {
		// TODO deliver static file
		throw new NotImplementedException();
	}
	
	@RequestMapping(params={"!REQUEST"},method={RequestMethod.POST})
	public void processXmlParams(
			HttpServletRequest request,
			@RequestBody DOMSource dom,
			HttpServletResponse response
	) throws IOException
	{
		Document doc = (Document) dom.getNode();

		XMLParameterExtractor xtractor = new XMLParameterExtractor(doc);
		
		xtractor.callService(request, response);
	}
	
	
	/**
	 * Prepares and sends a string argument to the equivalent GeoServer method
	 * (GeoServer implements the OGC Web Feature Service.)
	 * @param featureOfInterest a comma-separated list of SOS Feature IDs
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	@RequestMapping(params={"REQUEST=GetObservation"})
	public void getObservation(
			@RequestParam String featureOfInterest,
			// @RequestParam(required=false) @DateTimeFormat(iso=ISO.DATE) Date startDate,
			// @RequestParam(required=false) @DateTimeFormat(iso=ISO.DATE) Date endDate,
			HttpServletRequest request,
			HttpServletResponse response
			) throws IOException
	{
		// Implement by fetching from self-URL for raw data, passing thru wml1.9 to wml2 transform
		
		String thisServletURL = "http" + "://" + request.getLocalName() + ":" + request.getLocalPort() + "/" + request.getContextPath();
		
		SiteID site = SiteID.fromFid(featureOfInterest);
		
		Waterlevel19DataSource source = new Waterlevel19DataSource(thisServletURL, site.agency, site.site);
		
		try {
			InputStream is = source.getStream();
			
			response.setContentType("text/xml");
			OutputStream os = response.getOutputStream();

			// copy from stream to response, filtering through xsl transform
			copyThroughTransform(is,os, getTransformLocation());
			logger.debug("done");
		}
		catch (IOException ioe) {
			logger.warn("Problem", ioe);
			throw ioe;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			source.close();
		}
	}

	@Override
	public String getTransformLocation() {
		return "/gov/usgs/ngwmn/wl2waterml2.xslt";
	}
		
	// TODO Make param names case-insensitive (might require use of filter)
	// See http://stackoverflow.com/questions/12684183/case-insensitive-mapping-for-spring-mvc-requestmapping-annotations
	
	// GetFeatureOfInterest
	// implement on the back of geoserver
	// two forms of filter: featureId or bounding box
	// example URL $BASE?REQUEST=GetFeatureOfInterest&VERSION=2.0.0&SERVICE=SOS&featureOfInterest=ab.mon.45
	@RequestMapping(params={"REQUEST=GetFeatureOfInterest"})
	public void getFOI(
			@RequestParam(required=false) String featureOfInterest,
			@RequestParam(required=false) String spatialFilter,
			@RequestParam(required=false, defaultValue="EPSG:4326") String srsName,
			HttpServletResponse response
			)
		throws Exception
	{
		GeoserverFeatureSource featureSource = new GeoserverFeatureSource(getGeoserverURL());
		
		logger.info("GetFeatureOfInterest");
		
		// optional filters
		int filterCt = 0;
		
		if (featureOfInterest != null) {
			SiteID site = SiteID.fromFid(featureOfInterest);
			logger.debug("Filter for site {}", site);
			
			featureSource.addParameter("featureID", featureOfInterest);
			logger.debug("Added filter featureID={}", featureOfInterest);
			filterCt++;
		}
		
		if (spatialFilter != null) {
			String[] part = spatialFilter.split(",");
			if ( ! SOSService.BOUNDING_BOX_PREFIX.equals(part[0]) ) {
				throw new RuntimeException("bad filter");
			}
			for (int i = 1; i <= 4; i++) {
				// just for validation
				Double.parseDouble(part[i]);
			}
		
			// extra params for GeoServer WFS request, example:
			// use the original input strings for fidelity
			String cql_filter = MessageFormat.format("(BBOX(GEOM,{0},{1},{2},{3}))",
					part[1],part[2], part[3], part[4]);
				
			featureSource.addParameter("srsName", srsName);
			featureSource.addParameter("CQL_FILTER", cql_filter);
			
			logger.debug("added spatial filter {} in srs {}", cql_filter, srsName);
			filterCt++;
		}
		
		if (filterCt == 0) {
			logger.warn("No filters for WFS request, may get lots of data");
		}
		
		// TODO It seems that geoserver may not accept the two filters in conjunction
		if (spatialFilter != null && featureOfInterest != null) {
			logger.warn("Sending geoserver a WFS request with both spatial and FOI filters");
		}
		
		try {
			InputStream is = featureSource.getStream();
			
			response.setContentType("text/xml");
			OutputStream os = response.getOutputStream();

			// copy from stream to response, filtering through xsl transform
			copyThroughTransform(is,os, "/gov/usgs/ngwmn/geoserver-2-sos.xsl");
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

	// GetDataAvailability
	// later
	
	/**
	 * NOT threadsafe, but multithreaded access is not anticipated.
	 */
	public class XMLParameterExtractor {
		
		private XPath xPath;
		private Element rootnode;
		
		public XMLParameterExtractor(Document docParams) {
			if (docParams == null) {
				throw new IllegalArgumentException (
						"Parameter 'docParams' not permitted to be null.");
			}
			xPath = XPathFactory.newInstance().newXPath();
			rootnode = docParams.getDocumentElement();
			
			// register namespaces with the XPath processor
			SimpleNamespaceContext nc = new SimpleNamespaceContext();
			nc.bindNamespaceUri("sos", "http://www.opengis.net/sos/2.0");
			nc.bindNamespaceUri("fes", "http://www.opengis.net/fes/2.0");
			nc.bindNamespaceUri("gml", "http://www.opengis.net/gml/3.2");
			nc.bindNamespaceUri("swe", "http://www.opengis.net/swe/2.0");
			nc.bindNamespaceUri("swes", "http://www.opengis.net/swes/2.0");
			xPath.setNamespaceContext(nc);
			
		}

		
		void callService(
				HttpServletRequest request, 
				HttpServletResponse response) 
				throws IOException {
			
			String opname = getSOSRequest();
			
			// switch (opname) {
				if ( "GetFeatureOfInterest".equals(opname)) {
					
					BoundingBox bbox = getSOSBoundingBox();
					String srsName = null;
					String spatialParam = null;
					if (bbox != null) {
						srsName = bbox.getSrsName();
						
						String[] corners = bbox.getCoordinates();
						spatialParam = joinvar(",",
								SOSService.BOUNDING_BOX_PREFIX, 
								corners[0], 
								corners[1], 
								corners[2], 
								corners[3]);
					}
					
					List<String> features = getSOSFeatures();
					String featureParam = null;
					if ( ! features.isEmpty()) {
						featureParam = join(",", features);
					}
					try {
						getFOI(featureParam, spatialParam, srsName, response);			
					}
					catch (Exception e) {
						throw new RuntimeException (e);
					}
					// break;
				}
				else if ("GetObservation".equals(opname)) {
				// case "GetObservation": {
					List<String> features = getSOSFeatures();
					if (features.isEmpty()) {
						response.sendError(400, 
								"GetObservation requires Feature ID input.");
					}
					else {
						String featuresParam = join(",", features);
						getObservation(featuresParam, request, response);
					}
					// break;
				}
				else if ("GetCapabilities".equals(opname)) {
				// case "GetCapabilities": {
					response.setContentType("text/xml");
					getCapabilities(response.getOutputStream());
					// break;
				}
				else {
				// default: {
					response.sendError(400, "Root node '" + opname 
							+ "' is not a recognized Request");
				}
			}
		
		
		/**
		 * TODO clarify returns
		 * @return 
		 */
		public BoundingBox getSOSBoundingBox() {
			
			// extract featureOfInterest parameters
			String corners;
			try {
					Boolean hasSpatialFilter = (Boolean)xPath.evaluate(
							"boolean(//sos:spatialFilter)",
							rootnode, 
							XPathConstants.BOOLEAN);

					if ( ! hasSpatialFilter) {
						return null;
					}	
							
					String lowerCorner = (String)xPath.evaluate(
						"//sos:spatialFilter/fes:Intersects/gml:Envelope/gml:lowerCorner/text()",
						rootnode, 
						XPathConstants.STRING);
				
					String upperCorner = (String)xPath.evaluate(
						"//sos:spatialFilter/fes:Intersects/gml:Envelope/gml:upperCorner/text()",
						rootnode, 
						XPathConstants.STRING);
					
					String srsName = (String)xPath.evaluate(
						"//sos:spatialFilter//*/@srsName",
						rootnode, 
						XPathConstants.STRING);
					
				
					corners = lowerCorner + " " + upperCorner;
					
					BoundingBox bbox = new BoundingBox(srsName, corners.split(" "));
					return bbox;
			}
			catch (XPathExpressionException xee) {
				throw new RuntimeException("Faulty xpath expression.", xee);
			}

			
		}
	
		public List<String> getSOSFeatures() {
			
			// extract featureOfInterest parameters
			try {
				NodeList featuresOfInterest = (NodeList)xPath.evaluate(
					"//sos:featureOfInterest",
					rootnode, 
					XPathConstants.NODESET);
				List<String> features = new ArrayList<String>(featuresOfInterest.getLength());
				for (int indx = 0; indx < featuresOfInterest.getLength(); indx++) {
					Node curNode = featuresOfInterest.item(indx);
					if (!curNode.getTextContent().trim().isEmpty()) {
						features.add(curNode.getTextContent().trim());
					}
				}

				return features;
			}
			catch (XPathExpressionException xee) {
				throw new RuntimeException("Faulty xpath expression.", xee);
			}
		}
		
		public String join(String separator, Collection<String> items) {
			StringBuilder b = new StringBuilder();
			String sep = "";
			for (String item : items) {
				b.append(sep).append(item);
				sep = separator;
			}
			return b.toString();
		}
		
		public String joinvar(final String sep, String... items) {
			return join(sep, Arrays.asList(items));
		}

		public String getSOSRequest() {
			return rootnode.getLocalName();
		}
	}
}

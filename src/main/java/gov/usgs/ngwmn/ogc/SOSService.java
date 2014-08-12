package gov.usgs.ngwmn.ogc;


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
    
    public static final String CAPABILITIES_XML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
"<sos:Capabilities xmlns:ogc='http://www.opengis.net/ogc' xmlns:fes='http://www.opengis.net/fes/2.0' xmlns:swes='http://www.opengis.net/swes/2.0' xmlns:ows='http://www.opengis.net/ows/1.1' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gml='http://www.opengis.net/gml/3.2' xmlns:swe='http://www.opengis.net/swe/2.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:sos='http://www.opengis.net/sos/2.0' version='2.0.0' xsi:schemaLocation='http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd'>\n" +
"	<!-- ServiceIdentification contains general information about the service like title, type and version as well as information about supported profiles of the service-->\n" +
"	<ows:ServiceIdentification>\n" +
"		<ows:Title>NGWMN SOS</ows:Title>\n" +
"		<ows:Abstract>This service provides groundwater level data from the U.S. National Ground-Water Monitoring Network. This SOS web service delivers the data using OGC's WaterML 2.0. The SOS service federates data from various state and local data providers and publishes them as a single service. Many monitoring stations also have water well characteristics available through the NGWMN WFS web service.</ows:Abstract>\n" +
"		<ows:Keywords>\n" +
"			<ows:Keyword>groundwater level</ows:Keyword>\n" +
"			<ows:Keyword>monitoring</ows:Keyword>\n" +
"			<ows:Keyword>timeseries</ows:Keyword>\n" +
"			<ows:Keyword>groundwater</ows:Keyword>\n" +
"			<ows:Keyword>USGS</ows:Keyword>\n" +
"			<ows:Keyword>NGWMN</ows:Keyword>\n" +
"			<ows:Keyword>ACWI</ows:Keyword>\n" +
"			<ows:Keyword>SOGW</ows:Keyword>\n" +
"			<ows:Keyword>CIDA</ows:Keyword>\n" +
"			<ows:Keyword>NWIS</ows:Keyword>\n" +
"			<ows:Keyword>water</ows:Keyword>\n" +
"			<ows:Keyword>well</ows:Keyword>\n" +
"			<ows:Keyword>United States</ows:Keyword>\n" +
"			<ows:Keyword>water level</ows:Keyword>\n" +
"		</ows:Keywords>\n" +
"		<ows:ServiceType codeSpace='http://opengeospatial.net'>OGC:SOS</ows:ServiceType>\n" +
"		<ows:ServiceTypeVersion>2.0.0</ows:ServiceTypeVersion>\n" +
"		<!--  XML binding -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/SOS/2.0/req/xml</ows:Profile>\n" +
"		<!--  KVP binding -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/SOS/2.0/req/kvp-core</ows:Profile>\n" +
"		<!-- supported profiles as -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/SOS/2.0/conf/gfoi</ows:Profile>\n" +
"		<!-- Observations can be queried using spatial geometry expressed in param -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/SOS/2.0/conf/spatialFilteringProfile</ows:Profile>\n" +
"		<!-- sampling feature must have a point geometry -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint</ows:Profile>\n" +
"		<!-- Observations are encoded with GML 3.2 XML-->\n" +
"		<ows:Profile>http://www.opengis.net/spec/OMXML/2.0/conf/observation</ows:Profile>\n" +
"		<!-- this service implements WaterML 2.0 -->\n" +
"		<ows:Profile>http://www.opengis.net/spec/waterml/2.0/conf/xsd-observation-process</ows:Profile>\n" +
"		<ows:Fees>NONE</ows:Fees>\n" +
"		<ows:AccessConstraints>NONE</ows:AccessConstraints>\n" +
"	</ows:ServiceIdentification>\n" +
"	<!-- ServiceProvider section contains information about service provider like contact, adress, etc. -->\n" +
"	<ows:ServiceProvider>\n" +
"		<ows:ProviderName>U.S. Geological Survey, Office of Water Information, Center for Integrated Data Analytics, United States Government</ows:ProviderName>\n" +
"		<ows:ProviderSite xlink:href='http://cida.usgs.gov'/>\n" +
"		<ows:ServiceContact>\n" +
"			<ows:IndividualName>Jessica Lucido</ows:IndividualName>\n" +
"			<ows:PositionName>IT Specialist</ows:PositionName>\n" +
"			<ows:ContactInfo>\n" +
"				<ows:Phone>\n" +
"					<ows:Voice>+1-608-821-3841</ows:Voice>\n" +
"				</ows:Phone>\n" +
"				<ows:Address>\n" +
"					<ows:DeliveryPoint>8505 Research Way</ows:DeliveryPoint>\n" +
"					<ows:City>Middleton</ows:City>\n" +
"					<ows:PostalCode>WI</ows:PostalCode>\n" +
"					<ows:Country>USA</ows:Country>\n" +
"					<ows:ElectronicMailAddress>jlucido@usgs.gov</ows:ElectronicMailAddress>\n" +
"				</ows:Address>\n" +
"			</ows:ContactInfo>\n" +
"			<ows:Role/>\n" +
"		</ows:ServiceContact>\n" +
"	</ows:ServiceProvider>\n" +
"	<!-- extension is used for providing profile specific sections; in this case, the InsertionCapabilities section is contained, because the SOS supports the  obs- and resultInsertion profiles-->\n" +
"	<!-- the filterCapabilities section lists the filters and operands which are supported in the observation, result and feature retrieval operations -->\n" +
"	<ows:OperationsMetadata>\n" +
"		<!-- TODO <ows:Operation name='DescribeSensor'> -->\n" +
"		<ows:Operation name='GetCapabilities'>\n" +
"			<ows:DCP>\n" +
"				<ows:HTTP>\n" +
"					<ows:Get xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/?'/>\n" +
"					<ows:Post xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/'/>\n" +
"				</ows:HTTP>\n" +
"			</ows:DCP>\n" +
"			<ows:Parameter name='updateSequence'>\n" +
"				<ows:AnyValue/>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='AcceptVersions'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>2.0.0</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='Sections'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>ServiceIdentification</ows:Value>\n" +
"					<ows:Value>ServiceProvider</ows:Value>\n" +
"					<ows:Value>OperationsMetadata</ows:Value>\n" +
"					<ows:Value>FilterCapabilities</ows:Value>\n" +
"					<ows:Value>Contents</ows:Value>\n" +
"					<ows:Value>All</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='AcceptFormats'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>text/xml</ows:Value>\n" +
"					<ows:Value>application/zip</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"		</ows:Operation>\n" +
"		<ows:Operation name='GetObservation'>\n" +
"			<ows:DCP>\n" +
"				<ows:HTTP>\n" +
"					<ows:Get xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/?'/>\n" +
"					<ows:Post xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/'/>\n" +
"				</ows:HTTP>\n" +
"			</ows:DCP>\n" +
"			<ows:Parameter name='srsName'>\n" +
"				<ows:NoValues/>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='offering'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>GW_LEVEL</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<!-- TODO <ows:Parameter name='temporalFilter'> -->\n" +
"			<ows:Parameter name='procedure'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>urn:ogc:object:Sensor:usgs-gw</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='observedProperty'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>urn:ogc:def:phenomenon:OGC:1.0.30:groundwaterlevel</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='result'>\n" +
"				<ows:AnyValue/>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='responseFormat'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>http://www.opengis.net/waterml/2.0</ows:Value>\n" +
"					<ows:Value>application/zip</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"		</ows:Operation>\n" +
"		<ows:Operation name='GetFeatureOfInterest'>\n" +
"			<ows:DCP>\n" +
"				<ows:HTTP>\n" +
"					<ows:Get xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/?'/>\n" +
"					<ows:Post xlink:href='http://cida.usgs.gov/ngwmn_cache/sos/'/>\n" +
"				</ows:HTTP>\n" +
"			</ows:DCP>\n" +
"			<ows:Parameter name='featureOfInterest'>\n" +
"				<ows:NoValues/>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='observableProperty'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>urn:ogc:def:phenomenon:OGC:1.0.30:groundwaterlevel</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='procedure'>\n" +
"				<ows:AllowedValues>\n" +
"					<ows:Value>urn:ogc:object:Sensor:usgs-gw</ows:Value>\n" +
"				</ows:AllowedValues>\n" +
"			</ows:Parameter>\n" +
"			<ows:Parameter name='spatialFilter'>\n" +
"				<ows:AnyValue/>\n" +
"			</ows:Parameter>\n" +
"		</ows:Operation>\n" +
"		<ows:Parameter name='service'>\n" +
"			<ows:AllowedValues>\n" +
"				<ows:Value>SOS</ows:Value>\n" +
"			</ows:AllowedValues>\n" +
"		</ows:Parameter>\n" +
"		<ows:Parameter name='version'>\n" +
"			<ows:AllowedValues>\n" +
"				<ows:Value>2.0.0</ows:Value>\n" +
"			</ows:AllowedValues>\n" +
"		</ows:Parameter>\n" +
"	</ows:OperationsMetadata>\n" +
"	<sos:filterCapabilities>\n" +
"		<fes:Filter_Capabilities>\n" +
"			<fes:Conformance>\n" +
"				<fes:Constraint name='ImplementsQuery'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsAdHocQuery'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsFunctions'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsMinStandardFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsStandardFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsMinSpatialFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>true</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsSpatialFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>true</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsMinTemporalFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsTemporalFilter'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsVersionNav'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsSorting'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"				<fes:Constraint name='ImplementsExtendedOperators'>\n" +
"					<ows:NoValues/>\n" +
"					<ows:DefaultValue>false</ows:DefaultValue>\n" +
"				</fes:Constraint>\n" +
"			</fes:Conformance>\n" +
"			<fes:Spatial_Capabilities>\n" +
"				<fes:GeometryOperands>\n" +
"					<fes:GeometryOperand name='gml:Point'/>\n" +
"					<fes:GeometryOperand name='gml:Polygon'/>\n" +
"				</fes:GeometryOperands>\n" +
"				<fes:SpatialOperators>\n" +
"					<fes:SpatialOperator name='BBOX'/>\n" +
"					<fes:SpatialOperator name='Intersects'/>\n" +
"					<fes:SpatialOperator name='Within'/>\n" +
"				</fes:SpatialOperators>\n" +
"			</fes:Spatial_Capabilities>\n" +
"		</fes:Filter_Capabilities>\n" +
"	</sos:filterCapabilities>\n" +
"	<!-- The contents section contains information about the observations offered by the service. The observations are group per sensor(-system) into observation offerings.-->\n" +
"	<sos:contents>\n" +
"		<sos:Contents>\n" +
"			<swes:offering>\n" +
"				<sos:ObservationOffering xmlns:ogc='http://www.opengis.net/ogc' xmlns:fes='http://www.opengis.net/fes/2.0' xmlns:swes='http://www.opengis.net/swes/2.0' xmlns:ows='http://www.opengis.net/ows/1.1' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gml='http://www.opengis.net/gml/3.2' xmlns:swe='http://www.opengis.net/swe/2.0' xmlns:sos='http://www.opengis.net/sos/2.0'>\n" +
"					<swes:identifier>GW_LEVEL</swes:identifier>\n" +
"					<swes:procedure>urn:ogc:object:Sensor:usgs-gw</swes:procedure>\n" +
"					<swes:procedureDescriptionFormat>http://www.opengis.net/sensorML/1.0.1\n" +
"	</swes:procedureDescriptionFormat>\n" +
"					<swes:observableProperty>urn:ogc:def:phenomenon:OGC:1.0.30:groundwaterlevel</swes:observableProperty>\n" +
"					<sos:observedArea>\n" +
"						<gml:Envelope srsName='http://www.opengis.net/def/crs/EPSG/0/4326'>\n" +
"							<gml:lowerCorner>24 -125</gml:lowerCorner>\n" +
"							<gml:upperCorner>123 -66</gml:upperCorner>\n" +
"						</gml:Envelope>\n" +
"					</sos:observedArea>\n" +
"					<sos:phenomenonTime>\n" +
"						<gml:TimePeriod gml:id='phenomenonTime'>\n" +
"							<gml:beginPosition>1900-01-01T12:00:00Z</gml:beginPosition>\n" +
"							<!-- TODO make the end time dynamic -->\n" +
"							<gml:endPosition>2014-06-17T12:00:00Z</gml:endPosition>\n" +
"						</gml:TimePeriod>\n" +
"					</sos:phenomenonTime>\n" +
"				</sos:ObservationOffering>\n" +
"			</swes:offering>\n" +
"			<sos:responseFormat>http://www.opengis.net/om/2.0</sos:responseFormat>\n" +
"			<sos:observationType>http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation</sos:observationType>\n" +
"		</sos:Contents>\n" +
"	</sos:contents>\n" +
"</sos:Capabilities>";
    
    private static final byte[] CAPABILITIES_BYTES = CAPABILITIES_XML.getBytes();

	public static final String FEATURE_PREFIX = "VW_GWDP_GEOSERVER";
	public static final String BOUNDING_BOX_PREFIX = "om:featureOfInterest/*/sams:shape";

	static private Logger logger = LoggerFactory.getLogger(SOSService.class);
	public final String sosFeatureXformName = "/gov/usgs/ngwmn/geoserver-2-sos.xsl";
    
    /**
     * Writes an image of the GetCapabilties XML to the <tt>out</tt> parameter.
     * Makes no attempt to reference any other service or resource.
     * @param out the outbound response stream
     * @throws java.io.IOException if the content cannot be read
     */
	@RequestMapping(params={"request=GetCapabilities"})
	public void getCapabilities(
			OutputStream out
			) throws IOException
    {
        out.write(SOSService.CAPABILITIES_BYTES);
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

		try {
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

			InputStream is = featureSource.getStream();

			response.setContentType("text/xml");
			OutputStream os = response.getOutputStream();

			copyThroughTransform(is,os, sosFeatureXformName);
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
                try {
                    getCapabilities(response.getOutputStream());
                }
                catch (IOException iox) {
                    response.sendError(500, "Unable to obtain capabilities.");
                }
                    
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

package gov.usgs.ngwmn.ogc;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

public class SOSServiceTest {

	@Test
	public void testTransform_1() throws Exception {
		SOSService victim = new SOSService();
		
		String xform = victim.getTransformLocation();
		
		InputStream is = new FileInputStream("src/test/resources/sample-data/USGS_20515416303801_WATERLEVEL_ABBREV.xml");
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			SOSService.copyThroughTransform(is, os, xform);
			
			String output = os.toString();
			assertTrue(output.contains("wml2:TVPMeasurementMetadata"));
			assertTrue(output.contains("http://cida.usgs.gov/cocoon/gin/gwdp/agency/NWIS/wfs?request=GetFeature&amp;featureId=205154156303801"));
		} finally {
			is.close();
		}
	}
	
	@Test
	public void testTransform_2() throws Exception {
		SOSService victim = new SOSService();
		
		String xform = victim.getTransformLocation();
		
		InputStream is = new FileInputStream("src/test/resources/sample-data/sample_WATERLEVEL.xml");
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			SOSService.copyThroughTransform(is, os, xform);
			
			String output = os.toString();
			assertTrue(output.contains("urn:ogc:object:Sensor:usgs-gw:NWIS-205154156303801"));
		} finally {
			is.close();
		}
	}

}

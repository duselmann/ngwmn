package gov.usgs.ngwmn.ogc;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

public class WFSServiceTest {

	@Test
	public void testTransform_1() throws Exception {
		WFSService victim = new WFSService();
		
		String xform = victim.getTransformLocation();
		
		InputStream is = new FileInputStream("src/test/resources/sample-data/geoserver-wfs-output.xml");
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			SOSService.copyThroughTransform(is, os, xform);
			
			String output = os.toString();
			assertTrue(output.contains("Texas Water Development Board" ));
			assertTrue(output.contains("http://wiid.twdb.state.tx.us/wwm/wwm_welldata.asp?state_well=1206707"));
		} finally {
			is.close();
		}
	}
	

}

package gov.usgs.ngwmn.admin.stats;

import static org.junit.Assert.*;

import java.io.StringWriter;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class JacksonTreeTest {

	@Test
	public void test() throws Exception {
		JitTree base = new JitTree("Name 1", 11);
		
		JitTree k1 = new JitTree("Kid 1", 3);
		
		base.addChild(k1);
		
		StringWriter sw = new StringWriter();

		ObjectMapper mapper = new ObjectMapper();

		mapper.writeValue(sw, base);
		
		System.out.println(sw.toString());
		
		assertTrue(sw.toString().contains("Kid 1"));
		assertFalse(sw.toString().contains("null"));
	}

}

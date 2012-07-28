package gov.usgs.ngwmn.admin;


import gov.usgs.ngwmn.WellDataType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trial")
public class TrialMVCController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping("agency/{agency}")
	public void agencyData(
			@PathVariable String agency,
			Writer writer)
	throws IOException
	{
		PrintWriter pw = new PrintWriter(writer);
		pw.printf("Hello there, Mr. %s!", agency);
	}
	
	@RequestMapping("hello")
	public String sayHello(
			) {
		return "sayhello";
	}
	
	@RequestMapping("wombat")
	public String wombat() {
		return "redirect:agency/Wombat";
	}
	
	@ModelAttribute("agencyCodes")
	public List<String> getAgencyCodes() {
		logger.info("creating model object agencyCodes");
		return Arrays.asList("USGS","TWDB", "IL EPA");
	}
	
	@ModelAttribute("dataTypes")
	public WellDataType[] getDataTypes() {
		return WellDataType.values();
	}
	
	@RequestMapping("site")
	public String selectSite(
			@ModelAttribute SiteSelector site
	) {
		logger.info("in selectSite, site={}", site);
		return "site";
	}
}

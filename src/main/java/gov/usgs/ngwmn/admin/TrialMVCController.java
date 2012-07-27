package gov.usgs.ngwmn.admin;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trial")
public class TrialMVCController {
	
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
	public String sayHello() {
		return "sayhello";
	}
	
	@RequestMapping("wombat")
	public String wombat() {
		return "redirect:agency/Wombat";
	}
	
}

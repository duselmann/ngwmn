package gov.usgs.ngwmn.dm;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Simple Servlet implementation sends an email of current errors in the fetch log
 */
public class ErrorReportService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(ErrorReportService.class);


	private ErrorReportSender errorReport;
	private WebApplicationContext ctx;


	public ErrorReportService() {
	}


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ctx = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		errorReport = ctx.getBean("errorReportSender", ErrorReportSender.class);
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.debug("Sending Error Report Email");

		PrintWriter writer = resp.getWriter();
		try {
			errorReport.send();
			writer.append("<status>success</status>");
			writer.flush();
		} finally {
			if (writer != null ) writer.flush();
		}
	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}

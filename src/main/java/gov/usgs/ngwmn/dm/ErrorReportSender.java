package gov.usgs.ngwmn.dm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class ErrorReportSender {

	private final static Logger logger = LoggerFactory.getLogger(ErrorReportSender.class);

	private static final String DEFAULT_TO = "jlucido@usgs.gov";
	private static final int DEFAULT_MAX_AGENCY_COUNT       = 5; // max of sites with errors per agency
	private static final int DEFAULT_MAX_SITE_EMPTY_COUNT   = 2; // max days for sites with empty results
	private static final int DEFAULT_MAX_SITE_PROBLEM_COUNT = 2; // max days for sites with problems

	private JavaMailSender     jmailSender;
	private SimpleMailMessage  messageTemplate;
	private ErrorReportBuilder errorReportBuilder;


	public ErrorReportSender() {
	}


	// constructs a fetch error email and sends via email sender
	public void send() {

		try {
			// get errors message
			int agencyMax  = SessionUtil.lookup("mgwmn/ErrorRptMaxAgencyCount",  DEFAULT_MAX_AGENCY_COUNT);
			int emptyMax   = SessionUtil.lookup("mgwmn/ErrorRptMaxEmptyCount",   DEFAULT_MAX_SITE_EMPTY_COUNT);
			int problemMax = SessionUtil.lookup("mgwmn/ErrorRptMaxProblemCount", DEFAULT_MAX_SITE_PROBLEM_COUNT);
			String msg = errorReportBuilder.build(agencyMax, emptyMax, problemMax);

			// if no errors return
			if ( msg.length() == 0 ) return;
			messageTemplate.setText(msg);

			// build email request with sendTo addr from ctx
			String[] to = SessionUtil.lookup("mgwmn/ErrorRptEmailAddress", DEFAULT_TO).split(",");
			messageTemplate.setTo(to);

			jmailSender.send(messageTemplate);

			logger.info("Sent error report email: " + msg);
		} finally {

		}
	}

	public void setJmailSender(JavaMailSender jmailSender) {
		this.jmailSender = jmailSender;
	}

	public void setMessageTemplate(SimpleMailMessage messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	public void setErrorReportBuilder(ErrorReportBuilder errorReportBuilder) {
		this.errorReportBuilder = errorReportBuilder;
	}
}
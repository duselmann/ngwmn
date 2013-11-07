package gov.usgs.ngwmn.dm.io.transform;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Transformer;

public class DirectQualityCSVOutputStream extends DirectCSVOutputStream {
	
	public DirectQualityCSVOutputStream(OutputStream out) throws IOException {
		super("/gov/usgs/ngwmn/qw2csv-dates.xsl",out);
		logger.debug("created");
	}

	@Override
	protected void setupTransform(Transformer t) {
		super.setupTransform(t);		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DirectWaterlevelCSVOutputStream [");
		builder.append("beginDate=");
		builder.append(beginDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", xformResourceName=");
		builder.append(xslHelper.getXformResourceName());
		builder.append(", out=");
		builder.append(out);
		builder.append(", agency=");
		builder.append(agency);
		builder.append(", site=");
		builder.append(site);
		builder.append("]");
		return builder.toString();
	}

	
}

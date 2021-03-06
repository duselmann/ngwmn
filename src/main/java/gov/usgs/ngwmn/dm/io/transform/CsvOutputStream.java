package gov.usgs.ngwmn.dm.io.transform;

import gov.usgs.ngwmn.dm.io.parse.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


public class CsvOutputStream extends OutputStreamTransform {

	
	public CsvOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	public String formatRow(List<Element> headers, Map<String, String> rowData) {
		logEntry();
		
		StringBuilder rowText = new StringBuilder(); 
		
		String sep = "";
		for (Element header : headers) {
			// if rowData is null then we are rendering headers
			String data = (rowData==null) ? header.displayName : rowData.get(header.fullName);
			data = (data==null) ? "" : data;
			data = data.trim().replaceAll("\"", "'");
			rowText.append(sep).append('\"').append(data).append('\"');
			sep = getSeparator();
		}
		rowText.append('\n');
		
		logger.trace("formatted row {}", rowText.toString());

		return rowText.toString();
	}
	
	public String getSeparator() {
		return ",";
	}
	
	protected void logEntry() {
		logger.trace("CSV Format Row");
	}
}

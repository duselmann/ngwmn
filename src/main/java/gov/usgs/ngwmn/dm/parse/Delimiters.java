package gov.usgs.ngwmn.dm.parse;

public class Delimiters {
	public String sheetStart, sheetEnd;
	public String headerRowStart, headerRowEnd;
	public String headerCellStart, headerCellEnd, lastHeaderCellEnd;
	public String bodyRowStart, bodyRowEnd;
	public String bodyCellStart, bodyCellEnd, lastBodyCellEnd;
//	public String footerRowStart, footerRowEnd;
//	public String footerCellStart, footerCellEnd;

	// CONSTANT DELIMITERS
	public static final Delimiters HTML_DELIMITERS = makeHTMLDelimiter(false);
	public static final Delimiters CSV_DELIMITERS = makeCSVDelimiter();
	public static final Delimiters TAB_DELIMITERS = makeTabDelimiter();
	public static final Delimiters HTML_DELIMITERS_WITH_HEADER = makeHTMLDelimiter(true);

	private static final int HTML_TYPE = 1;
	private static final int CSV_TYPE = 2;
	private static final int TAB_TYPE = 3;
	private static final int EXCEL_TYPE = 4;

	public final int type;
	// ==========================
	// STATIC INITIALIZER METHODS
	// ==========================
	public static Delimiters makeHTMLDelimiter(boolean useTableHeadMarkup) {
		Delimiters delims= new Delimiters(HTML_TYPE);
		delims.sheetStart = "<table>";
		delims.sheetEnd = (useTableHeadMarkup)? "</tbody></table>" : "</table>";
		delims.headerRowStart = (useTableHeadMarkup)? "<thead><tr>": "<tr>";
		delims.headerRowEnd = (useTableHeadMarkup)? "</tr></thead><tbody>\n": "</tr>\n";
		delims.headerCellStart = "<td><b>";
		delims.headerCellEnd = "</b></td>";
		delims.lastHeaderCellEnd = delims.headerCellEnd;
		delims.bodyRowStart = "<tr>";
		delims.bodyRowEnd = "</tr>\n";
		delims.bodyCellStart = "<td>";
		delims.bodyCellEnd = "</td>";
		delims.lastBodyCellEnd = delims.bodyCellEnd;
//		delims.footerRowStart = "<tr>";
//		delims.footerRowEnd = "</tr>";
//		delims.footerCellStart = "<td>";
//		delims.footerCellEnd = "</td>";
		return delims;
	}

	public static Delimiters makeExcelDelimiter(String author, String creationDate) {
		Delimiters delims= new Delimiters(EXCEL_TYPE);
		delims.sheetStart = "<?xml version=\"1.0\"?>"
			+ "<?mso-application progid=\"Excel.Sheet\"?>"
			+ "<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:x=\"urn:schemas-microsoft-com:office:excel\""
			+ " xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\""
			+ " xmlns:html=\"http://www.w3.org/TR/REC-html40\">"
			+ " <DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">"
			+ "  <LastAuthor>" + author + "</LastAuthor>"
			+ "  <Created>" + creationDate + "</Created>" //2007-11-02T21:34:20Z
			+ "  <Version>11.8132</Version>"
			+ " </DocumentProperties>"
			+ " <OfficeDocumentSettings xmlns=\"urn:schemas-microsoft-com:office:office\"/>"
			+ " <ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">"
			+ "  <ProtectStructure>False</ProtectStructure>"
			+ "  <ProtectWindows>False</ProtectWindows>"
			+ " </ExcelWorkbook>"
			+ " <Styles>"
			+ "  <Style ss:ID=\"Default\" ss:Name=\"Normal\">"
			+ "   <Alignment ss:Vertical=\"Bottom\"/>"
			+ "  </Style>"
			+ "  <Style ss:ID=\"s21\">"
			+ "   <NumberFormat ss:Format=\"Fixed\"/>"
			+ "  </Style>"
			+ "  <Style ss:ID=\"s23\">"
			+ "   <Font x:Family=\"Swiss\" ss:Bold=\"1\"/>"
			+ "  </Style>"
			+ " </Styles>"
			+ " <Worksheet ss:Name=\"data\">"
			+ "  <Table x:FullColumns=\"1\" x:FullRows=\"1\">";
		delims.sheetEnd = "  </Table>"
			+ "  <WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">"
			+ "   <Print>"
			+ "    <ValidPrinterInfo/>"
			+ "    <HorizontalResolution>600</HorizontalResolution>"
			+ "    <VerticalResolution>600</VerticalResolution>"
			+ "   </Print>"
			+ "   <Selected/>"
			+ "   <FreezePanes/>"
			+ "   <FrozenNoSplit/>"
			+ "   <SplitHorizontal>1</SplitHorizontal>"
			+ "   <TopRowBottomPane>1</TopRowBottomPane>"
			+ "   <ActivePane>2</ActivePane>"
			+ "   <Panes>"
			+ "    <Pane>"
			+ "     <Number>3</Number>"
			+ "    </Pane>"
			+ "    <Pane>"
			+ "     <Number>2</Number>"
			+ "     <RangeSelection>R2</RangeSelection>"
			+ "    </Pane>"
			+ "   </Panes>"
			+ "   <ProtectObjects>False</ProtectObjects>"
			+ "   <ProtectScenarios>False</ProtectScenarios>"
			+ "  </WorksheetOptions>"
			+ " </Worksheet>"
			+ "</Workbook>";
		delims.headerRowStart = "<Row ss:StyleID=\"s23\">";
		delims.headerRowEnd = "</Row>\n";
		delims.headerCellStart = "<Cell><Data ss:Type=\"String\">";
		delims.headerCellEnd = "</Data></Cell>";
		delims.lastHeaderCellEnd = delims.headerCellEnd;
		delims.bodyRowStart = "<Row>";
		delims.bodyRowEnd = delims.headerRowEnd;
		delims.bodyCellStart = delims.headerCellStart;
		delims.bodyCellEnd = delims.headerCellEnd;
		delims.lastBodyCellEnd = delims.bodyCellEnd;
//		delims.footerRowStart = "<tr>";
//		delims.footerRowEnd = "</tr>";
//		delims.footerCellStart = "<td>";
//		delims.footerCellEnd = "</td>";
		return delims;
	}

	public static Delimiters makeCSVDelimiter() {
		Delimiters delims= new Delimiters(CSV_TYPE);
		// TODO csv format is actually more than using "," for delimiters
		// double-quotes allow comma-escaping, and \n's may be enclosed as well.
		// see http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
		// see http://en.wikipedia.org/wiki/Comma-separated_values
		delims.sheetStart = "";
		delims.sheetEnd = "";
		delims.headerRowStart = "";
		delims.headerRowEnd = "";
		delims.headerCellStart = "";
		delims.headerCellEnd = ",";
		delims.lastHeaderCellEnd = ""; // do not output end comma
		delims.bodyRowStart = "\n";
		delims.bodyRowEnd = "";
		delims.bodyCellStart = "";
		delims.bodyCellEnd = ",";
		delims.lastBodyCellEnd = delims.lastHeaderCellEnd;
//		delims.footerRowStart = "";
//		delims.footerRowEnd = "";
//		delims.footerCellStart = "";
//		delims.footerCellEnd = "";
		return delims;
	}

	public static Delimiters makeTabDelimiter() {
		Delimiters delims= new Delimiters(TAB_TYPE);
		delims.sheetStart = "";
		delims.sheetEnd = "";
		delims.headerRowStart = "";
		delims.headerRowEnd = "";
		delims.headerCellStart = "";
		delims.headerCellEnd = "\t";
		delims.lastHeaderCellEnd = ""; // do not output end tab
		delims.bodyRowStart = "\n";
		delims.bodyRowEnd = "";
		delims.bodyCellStart = "";
		delims.bodyCellEnd = "\t";
		delims.lastBodyCellEnd = delims.lastHeaderCellEnd;
//		delims.footerRowStart = "";
//		delims.footerRowEnd = "";
//		delims.footerCellStart = "";
//		delims.footerCellEnd = "";
		return delims;
	}
	// ================
	// CONSTRUCTORS
	// ================
	public Delimiters(int type) {
		this.type = type;
	}

	// ================
	// INSTANCE METHODS
	// ================
	public String makeWideHeaderCell(String content, int width) {
		return "";
		// TODO work out the rest
//		StringBuilder result = new StringBuilder();
//		switch (type) {
//			case EXCEL_TYPE:
//				// same as html type
//			case HTML_TYPE:
//				result.append("<td colspan=\"").append(width).append("\"><b>").append(content).append("</b></td>");
//				break;
//			case CSV_TYPE:
//				// don't do this for csv
//				break;
//			case TAB_TYPE:
//				result.append(content);
//				for (int i=0; i< width; i++) {
//					result.append("\t");
//				}
//				break;
//		}
//		return result.toString();
	}

}

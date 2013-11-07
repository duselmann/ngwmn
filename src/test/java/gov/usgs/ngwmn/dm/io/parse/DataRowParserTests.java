package gov.usgs.ngwmn.dm.io.parse;

import static org.junit.Assert.*;

import gov.usgs.ngwmn.dm.io.parse.DataRowParser;
import gov.usgs.ngwmn.dm.io.parse.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.*;

public class DataRowParserTests {

	public static final String SIMPLE_EMPTY_FIRST_ROW
	= "<get>"
			+ "<Org code='USGS'>"
			+ "<Site><Identification code=''><agency code=''></agency></Identification><Type></Type></Site>"
			+ "<Site><Identification code='04'><agency code='03'></agency></Identification><Type>water</Type></Site>"
			+ "</Org>"
			+ "</get>";
	public static final String SINGLE_ROW_AMBIGUOUS_ATTRIBUTES
	= "<get>"
			+ "<Org>"
			+ "<Site><Identification code='04'><agency code='03'></agency></Identification><Type>water</Type></Site>"
			+ "</Org>"
			+ "</get>";
	public static final String SINGLE_ROW_AMBIGUOUS_COLUMNS
	= "<get>"
			+ "<Org>"
			+ "<Site><Type>01</Type><Identification><agency>USGS</agency><Type>02</Type></Identification><related><Identification><Type>03</Type><unique>uni</unique></Identification></related></Site>"
			+ "</Org>"
			+ "</get>";
	public static final String TRIPLE_ROW_UNEVEN_DEPTH
	= "<get>"
			+ "<Org>"
			+ "<Site><Identification code='01'><agency></agency><Id level='admin'>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification code='02'><agency new='yes'>USGS2</agency></Identification><Type>clay</Type><Result>good</Result></Site>"
			+ "</Org>"
			+ "<Org><OrgID>6</OrgID>"
			+ "<jinx>"
			+ "<Site><Identification code='03'><agency></agency><Id level='user'>2172258</Id></Identification><Type>stream</Type></Site>"
			+ "</jinx>"
			+ "</Org>"
			+ "</get>";
	public static final String TRIPLE_ROW_MULTIPLE_ORG
	= "<get>"
			+ "<Org><OrgID>5</OrgID>"
			+ "<Site><Identification code='01'><agency></agency><Id level='admin'>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification code='02'><agency new='yes'>USGS2</agency></Identification><Type>clay</Type><Result>good</Result></Site>"
			+ "</Org>"
			+ "<Org><OrgID>6</OrgID>"
			+ "<Site><Identification code='03'><agency></agency><Id level='user'>2172258</Id></Identification><Type>stream</Type></Site>"
			+ "</Org>"
			+ "</get>";
	public static final String DOUBLE_ROW_TWO_ELEMENTS_WITH_NEW_ELEMENTS
	= "<get>"
			+ "<Site><Identification code='01'><agency></agency><Id level='admin'>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification code='02'><agency new='yes'>USGS2</agency></Identification><Type>clay</Type><Result>good</Result></Site>"
			+ "</get>";
	public static final String DOUBLE_ROW_TWO_ELEMENTS_WITH_ATTRIBUTES
	= "<get>"
			+ "<Site><Identification code='01'><agency></agency><Id level='admin'>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification code='02'><agency>USGS2</agency></Identification><Type>clay</Type></Site>"
			+ "</get>";
	public static final String DOUBLE_ROW_TWO_ELEMENTS_WITH_UNEQUAL_NESTING
	= "<get>"
			+ "<Site><Identification><agency></agency><Id>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification><agency>USGS2</agency></Identification><Type>clay</Type></Site>"
			+ "</get>";
	public static final String DOUBLE_ROW_TWO_ELEMENTS_WITH_NESTING
	= "<get>"
			+ "<Site><Identification><agency>USGS1</agency><Id>2172257</Id></Identification><Type>water</Type></Site>"
			+ "<Site><Identification><agency>USGS2</agency><Id>2172258</Id></Identification><Type>clay</Type></Site>"
			+ "</get>";
	public static final String DOUBLE_ROW_TWO_ELEMENTS
	= "<get><Site><Id>2172257</Id><Type>water</Type></Site><Site><Id>2172258</Id><Type>clay</Type></Site></get>";
	public static final String SIMPLE_DOUBLE_ROW
	= "<get><Site>2172257</Site><Site>2172258</Site></get>";
	public static final String TOO_MANY_VALUES
	= "<get><Site><name>abc</name></Site><Site><id>def</id></Site></get>";

	public static Pattern newLinePattern = Pattern.compile("\n", Pattern.MULTILINE);
	public static String replaceNewLine(String original) {
		return newLinePattern.matcher(original).replaceAll("");
	}


	private DataRowParser parser;



	void makeParser(String xml) {
		PostParser pp = new DefaultPostParser();
		parser = new DataRowParser(pp);
		parser.setInputStream( new ByteArrayInputStream( xml.getBytes() ) );
	}

	@Test
	public void test_SingleRowSimple() throws Exception {
		String xml = "<get><Site>2172257</Site></get>";

		makeParser(xml);
		Map<String,String> row = parser.nextRow();
		List<Element>     head = parser.headers();

		assertEquals(1, head.size());
		assertEquals("Site", head.get(0).displayName);

		assertEquals(1, row.size());
		assertEquals("2172257", row.get("Site"));
	}


	@Test
	public void test_SingleRowSimpleUnescapeXMLEntitiesXML() throws Exception {
		String xml = "<get><Site>2172257&lt;&gt;&amp;&quot;&apos;</Site></get>";

		makeParser(xml);
		Map<String,String> row = parser.nextRow();
		List<Element>     head = parser.headers();

		assertEquals(1, head.size());
		assertEquals("Site", head.get(0).displayName);

		assertEquals(1, row.size());
		assertEquals("2172257<>&\"'", row.get("Site"));
	}

	@Test
	public void test_DoubleRowSimple() throws Exception {
		String xml = SIMPLE_DOUBLE_ROW;

		makeParser(xml);
		Map<String,String> row0 = parser.nextRow();
		List<Element>      head = parser.headers();

		assertEquals(1, head.size());
		assertEquals("Site", head.get(0).displayName);

		assertEquals(1, row0.size());
		assertEquals("2172257", row0.get("Site"));

		Map<String,String> row1 = parser.nextRow();
		assertEquals(1, row1.size());
		assertEquals("2172258", row1.get("Site"));
	}

	@Test
	public void test_DoubleRowTwoElements() throws Exception {
		String xml = DOUBLE_ROW_TWO_ELEMENTS;

		makeParser(xml);
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(2, head.size());
		assertEquals("Id",      head.get(0).displayName);
		assertEquals("Type",    head.get(1).displayName);

		assertEquals(2, row0.size());
		assertEquals("2172257", row0.get("Site/Id"));
		assertEquals("water",   row0.get("Site/Type"));
		assertEquals("2172257", row0.get( head.get(0).fullName ));
		assertEquals("water",   row0.get( head.get(1).fullName ));

		Map<String,String> row1 = parser.nextRow();
		assertEquals(2, row1.size());
		assertEquals("2172258", row1.get("Site/Id"));
		assertEquals("clay",    row1.get("Site/Type"));
		assertEquals("2172258", row1.get( head.get(0).fullName ));
		assertEquals("clay",    row1.get( head.get(1).fullName ));
	}

	@Test
	public void test_DoubleRowTwoElementsWithNesting() throws Exception {
		String xml = DOUBLE_ROW_TWO_ELEMENTS_WITH_NESTING;

		makeParser(xml);
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(3, head.size());
		assertEquals("agency",  head.get(0).displayName);
		assertEquals("Id",      head.get(1).displayName);
		assertEquals("Type",    head.get(2).displayName);

		assertEquals(3, row0.size());
		assertEquals("USGS1",   row0.get( head.get(0).fullName ));
		assertEquals("2172257", row0.get( head.get(1).fullName ));
		assertEquals("water",   row0.get( head.get(2).fullName ));
		assertEquals("USGS1",   row0.get("Site/Identification/agency"));
		assertEquals("2172257", row0.get("Site/Identification/Id"));
		assertEquals("water",   row0.get("Site/Type"));

		Map<String,String> row1 = parser.nextRow();
		assertEquals(3, row1.size());
		assertEquals("USGS2",   row1.get( head.get(0).fullName ));
		assertEquals("2172258", row1.get( head.get(1).fullName ));
		assertEquals("clay",    row1.get( head.get(2).fullName ));
		assertEquals("USGS2",   row1.get("Site/Identification/agency"));
		assertEquals("2172258", row1.get("Site/Identification/Id"));
		assertEquals("clay",    row1.get("Site/Type"));
	}

	@Test
	public void test_DoubleRowTwoElementsWithNestingUnequal() throws Exception {
		String xml = DOUBLE_ROW_TWO_ELEMENTS_WITH_UNEQUAL_NESTING;

		makeParser(xml);
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(3, head.size());
		assertEquals("agency",  head.get(0).displayName);
		assertEquals("Id",      head.get(1).displayName);
		assertEquals("Type",    head.get(2).displayName);

		assertEquals(2, row0.size());
		assertEquals(null,      row0.get( head.get(0).fullName ));
		assertEquals("2172257", row0.get( head.get(1).fullName ));
		assertEquals("water",   row0.get( head.get(2).fullName ));
		assertEquals(null,      row0.get("Site/Identification/agency"));
		assertEquals("2172257", row0.get("Site/Identification/Id"));
		assertEquals("water",   row0.get("Site/Type"));

		Map<String,String> row1 = parser.nextRow();
		assertEquals(2, row1.size());
		assertEquals("USGS2",   row1.get( head.get(0).fullName ));
		assertEquals(null,      row1.get( head.get(1).fullName ));
		assertEquals("clay",    row1.get( head.get(2).fullName ));
		assertEquals("USGS2",   row1.get("Site/Identification/agency"));
		assertEquals(null,      row1.get("Site/Identification/Id"));
		assertEquals("clay",    row1.get("Site/Type"));
	}

	@Test
	public void testDoubleRowTwoElementsWithAttributes() throws Exception {
		String xml = DOUBLE_ROW_TWO_ELEMENTS_WITH_ATTRIBUTES;

		makeParser(xml);
		firstTwoRecordsWithMissingElements(3);
	}

	@Test
	public void test_DoubleRowTwoElementsWithNewElements() throws Exception {
		String xml = DOUBLE_ROW_TWO_ELEMENTS_WITH_NEW_ELEMENTS;
		makeParser(xml);

		firstTwoRecordsWithMissingElements(5);
		secondRecordNewElements();
	}

	@Test
	public void test_TripleRowTwoElementsUsingElementName() throws Exception {
		String xml = TRIPLE_ROW_MULTIPLE_ORG;
		makeParser(xml);
		parser.setKeepElderInfo(false);
		parser.setRowElementName("Site");
		firstTwoRecordsWithMissingElements(5);
		secondRecordNewElements();
		thirdRecordWithMissingElements();
	}
	/*
	@Test
	public void test_bytesReadFew() throws Exception {
		String xml = "<get><Site>2172257</Site></get>";
		makeParser(xml);

		parser.nextRow();
		assertEquals(25, parser.bytesParsed());

		Object result = parser.nextRow();
		assertEquals(31, parser.bytesParsed());
		assertNull(result);
	}
	 */

	/*
	@Test
	public void test_bytesReadMany() throws Exception {
		String xml = TRIPLE_ROW_MULTIPLE_ORG;

//		String a = "<get>"
//		+ "<Org><OrgID>5</OrgID>"
//		+ "<Site><Identification code='01'><agency></agency><Id level='admin'>2172257</Id></Identification><Type>water</Type></Site>"
//		+ "<Site><Identification code='02'><agency new='yes'>USGS2</agency></Identification><Type>clay</Type><Result>good</Result></Site>";
//
//		String b = "</Org>"
//		+ "<Org><OrgID>6</OrgID>"
//		+ "<Site><Identification code='03'><agency></agency><Id level='user'>2172258</Id></Identification><Type>stream</Type></Site>";
//
//		String c = "</Org>"
//		+ "</get>";

//		<get>
//		<Org><OrgID>5</OrgID>
//		<Site><Identification code="01"><agency></agency><Id level="admin">2172257</Id></Identification><Type>water</Type></Site>
//		<Site><Identification code="02"><agency new="yes">USGS2</agency></Identification><Type>clay</Type><Result>good</Result></Site>

		makeParser(xml);
		parser.setKeepElderInfo(false);
		parser.setRowElementName("Site");
		firstTwoRecordsWithMissingElements(5);
		assertEquals(273, parser.bytesParsed());

		secondRecordNewElements();
		thirdRecordWithMissingElements();
		assertEquals(273+148, parser.bytesParsed());

		Object result = parser.nextRow();
		assertEquals(433, parser.bytesParsed());
		assertNull(result);
	}
	 */

	@Test
	public void test_TripleRowTwoElementsUsingElementNameUnevenDepth() throws Exception {
		String xml = TRIPLE_ROW_UNEVEN_DEPTH;
		makeParser(xml);
		parser.setKeepElderInfo(false);
		parser.setCopyDown(false);
		parser.setRowElementName("Site");
		firstTwoRecordsWithMissingElements(5);
		secondRecordNewElements();
		thirdRecordWithMissingElements();
	}

	protected void firstTwoRecordsWithMissingElements(int secondRowSize) throws IOException {
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(5, head.size());
		assertEquals("code",    head.get(0).displayName);
		assertEquals("agency",  head.get(1).displayName);
		assertEquals("Id",      head.get(2).displayName);
		assertEquals("level",   head.get(3).displayName);
		assertEquals("Type",    head.get(4).displayName);

		assertEquals(4, row0.size());
		assertEquals("01",      row0.get( head.get(0).fullName ));
		assertEquals(null,      row0.get( head.get(1).fullName ));
		assertEquals("2172257", row0.get( head.get(2).fullName ));
		assertEquals("admin",   row0.get( head.get(3).fullName ));
		assertEquals("water",   row0.get( head.get(4).fullName ));

		Map<String,String>      row1 = parser.nextRow();
		assertEquals(secondRowSize, row1.size());
		assertEquals("02",      row1.get( head.get(0).fullName ));
		assertEquals("USGS2",   row1.get( head.get(1).fullName ));
		assertEquals(null,      row1.get( head.get(2).fullName ));
		assertEquals(null,      row1.get( head.get(3).fullName ));
		assertEquals("clay",    row1.get( head.get(4).fullName ));
	}
	protected void secondRecordNewElements() throws IOException {
		Map<String,String>      row = parser.currentRow();
		// notice that the size of the head list is less than 7 - these are not in there
		assertEquals("yes",     row.get( "Site/Identification/agency/new" ));
		assertEquals("good",    row.get( "Site/Result" ));
	}
	protected void thirdRecordWithMissingElements() throws IOException {
		Map<String,String>      row  = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(4, row.size());
		assertEquals("03",      row.get( head.get(0).fullName ));
		assertEquals(null,      row.get( head.get(1).fullName ));
		assertEquals("2172258", row.get( head.get(2).fullName ));
		assertEquals("user",    row.get( head.get(3).fullName ));
		assertEquals("stream",  row.get( head.get(4).fullName ));
	}

	//	 * Elements and attributes may share the same local name but appear under
	//	 * different contexts. In those cases, the DataFlattenningFormatter is
	//	 * supposed to add enough context to disambiguate the column names.
	@Test
	public void test_SimpleDisambiguationOfColumnLabels() throws Exception {
		String xml = SINGLE_ROW_AMBIGUOUS_ATTRIBUTES;

		makeParser(xml);
		parser.setCopyDown(false);
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(4, head.size());
		assertEquals("Identification/code", head.get(0).displayName);
		assertEquals("agency",       head.get(1).displayName);
		assertEquals("agency/code",  head.get(2).displayName);
		assertEquals("Type",         head.get(3).displayName);

		assertEquals(3, row0.size());
		assertEquals("04",      row0.get( head.get(0).fullName ));
		assertEquals(null,      row0.get( head.get(1).fullName ));
		assertEquals("03",      row0.get( head.get(2).fullName ));
		assertEquals("water",   row0.get( head.get(3).fullName ));
	}
	//	 * Elements and attributes may share the same local name but appear under
	//	 * different contexts. In those cases, the DataFlattenningFormatter is
	//	 * supposed to add enough context to disambiguate the column names.
	@Test
	public void test_DepthDisambiguationOfColumnLabels() throws Exception {
		String xml = SINGLE_ROW_AMBIGUOUS_COLUMNS;

		makeParser(xml);
		parser.setCopyDown(false);
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(5, head.size());
		assertEquals("Site/Type",                   head.get(0).displayName);
		assertEquals("agency",                      head.get(1).displayName);
		assertEquals("Site/Identification/Type",    head.get(2).displayName);
		assertEquals("related/Identification/Type", head.get(3).displayName);
		assertEquals("unique",                      head.get(4).displayName);

		assertEquals(5, row0.size());
		assertEquals("01",   row0.get( head.get(0).fullName ));
		assertEquals("USGS", row0.get( head.get(1).fullName ));
		assertEquals("02",   row0.get( head.get(2).fullName ));
		assertEquals("03",   row0.get( head.get(3).fullName ));
		assertEquals("uni",  row0.get( head.get(4).fullName ));
	}

	//	 * If the first target element has completely empty content, then the
	//	 * DataFlattenningFormatter assumes it was used only to provide headers for
	//	 * labelling. Note that Parent content is ignored when determining whether
	//	 * header info is used
	@Test
	public void testSkipFirstTargetIfAllEmpty() throws Exception {
		String xml = SIMPLE_EMPTY_FIRST_ROW;

		makeParser(xml);
		parser.setKeepElderInfo(false);
		parser.setCopyDown(false);
		parser.setRowElementName("Site");
		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertEquals(4, head.size());
		assertEquals("Identification/code", head.get(0).displayName);
		assertEquals("agency",              head.get(1).displayName);
		assertEquals("agency/code",         head.get(2).displayName);
		assertEquals("Type",                head.get(3).displayName);

		//row0 = parser.nextRow(); // should not need to call it twice if skipping blanks
		assertEquals(3, row0.size());
		assertEquals("04",    row0.get( head.get(0).fullName ));
		assertEquals(null,    row0.get( head.get(1).fullName ));
		assertEquals("03",    row0.get( head.get(2).fullName ));
		assertEquals("water", row0.get( head.get(3).fullName ));
	}

	@Test
	public void testFixedColumns() throws Exception {
		String xml = TRIPLE_ROW_MULTIPLE_ORG;

		String[][] columns = new String[][] {
				{"Site/Type","Site type"},
				{"Site/Result","Site result"},
				{"Site/Identification/code", "Site code"},
				{"Site/Identification/agency", null}
		};
		PostParser pp = new FixedOrderPostParser(columns);
		parser = new DataRowParser(pp);
		parser.setInputStream( new ByteArrayInputStream( xml.getBytes() ) );
		parser.setKeepElderInfo(false);
		parser.setRowElementName("Site");


		Map<String,String>      row0 = parser.nextRow();
		List<Element>           head = parser.headers();

		assertNotNull(row0);
		boolean hasResultHeader = false;
		for (Element e : head) {
			// check for site result because it's not present in first data row
			if ("Site result".equals(e.displayName)) {
				hasResultHeader = true;
			}
		}
		assertTrue("has Site result header",hasResultHeader);

		/*Map<String,String> row1 =*/ parser.nextRow();
		Map<String,String> row2 = parser.nextRow();

		assertTrue("row2 has site id", row2.containsKey("Site/Identification/code"));
	}

}

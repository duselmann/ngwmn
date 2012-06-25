package gov.usgs.ngwmn.dm.io.transform;

import static gov.usgs.ngwmn.WellDataType.WATERLEVEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgs.ngwmn.PrivateField;
import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.io.EntryDescription;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.SpecifierEntry;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.io.parse.AdditionalColumnsPostParser;
import gov.usgs.ngwmn.dm.io.parse.DataRowParser;
import gov.usgs.ngwmn.dm.io.parse.HeaderChangeListener;
import gov.usgs.ngwmn.dm.io.parse.ParseState;
import gov.usgs.ngwmn.dm.io.parse.CompositePostParser;
import gov.usgs.ngwmn.dm.io.parse.PostParser;
import gov.usgs.ngwmn.dm.io.parse.WaterPortalPostParserFactory;
import gov.usgs.ngwmn.dm.spec.Encoding;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TransformEntrySupplierTests {
	
	TransformEntrySupplier transformEntrySupplier;
	Supplier<OutputStream> transSupply;
	
	@Before
	public void setup() throws Exception {
		OutputStream os = new FilterOutputStream(null);
		Supplier<OutputStream> upstream = new SimpleSupplier<OutputStream>(os);
		transSupply = new TransformSupplier(upstream, WellDataType.WATERLEVEL, Encoding.CSV);
		transSupply.begin();
		
		Specifier spec = new Specifier("a","f",WellDataType.QUALITY);
		EntryDescription desc = new SpecifierEntry(spec);
		transformEntrySupplier = (TransformEntrySupplier) transSupply.makeEntry(desc);
	}

	@Test
	public void test_initialize_skipHeadersFalseUntilHeadersWritten() throws IOException {
		OutputStreamTransform ost = (OutputStreamTransform) transformEntrySupplier.begin();
		boolean writtenHeaders = PrivateField.getBoolean(ost, "writtenHeaders");
		
		assertFalse("headers should >NOT< be skipped for the first entry", writtenHeaders);
		
		Specifier spec = new Specifier("a","b",WellDataType.QUALITY);
		EntryDescription desc = new SpecifierEntry(spec);
		transformEntrySupplier = (TransformEntrySupplier) transSupply.makeEntry(desc);
		ost = (OutputStreamTransform) transformEntrySupplier.begin();
		writtenHeaders = PrivateField.getBoolean(ost, "writtenHeaders");

		assertFalse("headers should be skipped for the subsequent entries", writtenHeaders);

		ost.signalHeaderListeners();

		spec = new Specifier("a","c",WellDataType.QUALITY);
		desc = new SpecifierEntry(spec);
		transformEntrySupplier = (TransformEntrySupplier) transSupply.makeEntry(desc);
		ost = (OutputStreamTransform) transformEntrySupplier.begin();
		writtenHeaders = PrivateField.getBoolean(ost, "writtenHeaders");
		
		assertTrue("headers should be skipped for the subsequent entries", writtenHeaders);
	}	
	
	@Test
	public void test_makeParser_withValues() {
		DataRowParser parser = transformEntrySupplier.makeParser();
		assertNotNull(parser);
		
		ParseState state = (ParseState) PrivateField.get(parser, "state");
		assertEquals(1, state.rowElementIds.size());
		assertTrue( state.rowElementIds.contains( WellDataType.WATERLEVEL.rowElementName ) );
		
		PostParser  pp  = (PostParser) PrivateField.get(parser, "postParser");
		Set<String> set = pp.getRemoveColumns();
		Set<String> expected = new HashSet<String>( Arrays.asList(
				WaterPortalPostParserFactory.exclusions.get(WATERLEVEL) ) );
		assertEquals( expected, set);
	}
	
	@Test
	public void test_appendIdentifierColumns_withValues() {
		final List<PostParser> values = new LinkedList<PostParser>();
		
		CompositePostParser pp = new CompositePostParser() {
			@Override
			public void addPostParser(PostParser postParser) {
				super.addPostParser(postParser);
				values.add(postParser);
			}
			
			@Override
			public Set<String> getRemoveColumns() {
				throw new RuntimeException("should not be called during this test");
			}
		};
		transformEntrySupplier.appendIdentifierColumns(pp);
		System.err.println(values);
		assertEquals(1, values.size());
		assertEquals(AdditionalColumnsPostParser.class, values.get(0).getClass());
		
		@SuppressWarnings("unchecked")
		Map<String, String> cols = (Map<String, String>)PrivateField.get(values.get(0), "additionalCols");
		assertTrue(cols.keySet().contains("AgencyCd"));
		assertTrue(cols.keySet().contains("SiteNo"));
		assertEquals("a", cols.get("AgencyCd"));
		assertEquals("f", cols.get("SiteNo"));
	}

	@Test
	public void test_appendIdentifierColumns_nullEntryDesc() {
		final HashMap<String, String> values = new HashMap<String, String>();
		
		CompositePostParser pp = new CompositePostParser() {
			@Override
			public Set<String> getRemoveColumns() {
				throw new RuntimeException("should not be called during this test");
			}
		};
		transformEntrySupplier = new TransformEntrySupplier(null, null, null, true, (HeaderChangeListener)null);
		transformEntrySupplier.appendIdentifierColumns(pp);
		
		assertEquals(0, values.size());
	}
}

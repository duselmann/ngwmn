package gov.usgs.ngwmn.dm.io;

import static org.junit.Assert.*;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.dao.ContextualTest;
import gov.usgs.ngwmn.dm.io.aggregate.Flow;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QualityQueryFlowFactoryTest extends ContextualTest {

	private QualityQueryFlowFactory victim;
	
	@Before
	public void setUp() throws Exception {
		victim = new QualityQueryFlowFactory(getDataSource());		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMakeFlow() throws Exception {
		Specifier spec = new Specifier("USGS","403948085414601",WellDataType.QUALITY);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Supplier<OutputStream> out = new SimpleSupplier<OutputStream>(baos);
		Flow x = victim.makeFlow(spec, out);
		
		x.call();
		
		String result = baos.toString();
		
		assertFalse("newlines in fields", result.matches(".*[^,\"]$.*"));
		assertTrue(result.contains("Alkalinity"));
		assertTrue(result.contains("USGS"));
		assertTrue(result.contains("403948085414601"));
		assertTrue(result.contains("1988-08-04"));
		assertTrue(result.contains("Preliminary"));
		assertTrue(result.contains("USGS TWRI 5-A1/1989, p 151"));
		assertTrue(result.contains("uS/cm @25C"));
		assertTrue(result.contains("DetectionCondition"));
	}

	@Test
	public void testMakeFlowMuchData() throws Exception {
		Specifier spec = new Specifier("IL EPA","P406197",WellDataType.QUALITY);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		Supplier<OutputStream> out = new SimpleSupplier<OutputStream>(baos);
		Flow x = victim.makeFlow(spec, out);
		
		x.call();
		
		String result = baos.toString();
		assertTrue("trailing space is stripped",result.contains("detection limit\""));
		assertTrue("no newlines in fields", ! result.contains(" \n"));
		assertFalse("newlines in fields", result.matches(".*[^,\"]$.*"));
		assertTrue(result.contains("BARIUM"));
		assertTrue(result.contains("IL EPA"));
		assertTrue(result.contains("P406197"));
		assertTrue(result.contains("1984-10-17"));
		assertTrue(result.contains("2002-01-15"));
		assertTrue(result.contains("2006-01-18"));
		assertTrue(result.contains("2010-06-02"));

	}

	@Test
	public void testMakeFlowDateRange() throws Exception {
		Specifier spec = new Specifier("IL EPA","P406197",WellDataType.QUALITY);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		DateMidnight b = new DateMidnight("2002-01-15");
		DateMidnight e = new DateMidnight("2006-01-19");
		spec.setBeginDate(b.toDate());
		spec.setEndDate(e.toDate());
		
		Supplier<OutputStream> out = new SimpleSupplier<OutputStream>(baos);
		Flow x = victim.makeFlow(spec, out);
		
		x.call();
		
		String result = baos.toString();
		assertTrue("trailing space is stripped",result.contains("detection limit\""));
		assertTrue("no newlines in fields", ! result.contains(" \n"));
		assertFalse("newlines in fields", result.matches(".*[^,\"]$.*"));
		assertTrue(result.contains("BARIUM"));
		assertTrue(result.contains("IL EPA"));
		assertTrue(result.contains("P406197"));
		assertFalse(result.contains("1984-10-17"));
		assertTrue(result.contains("2002-01-15"));
		assertTrue(result.contains("2006-01-18"));
		assertFalse(result.contains("2010-06-02"));

	}
	

}

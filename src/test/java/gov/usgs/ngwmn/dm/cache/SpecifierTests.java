package gov.usgs.ngwmn.dm.cache;

import static org.junit.Assert.assertTrue;
import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.security.InvalidParameterException;

import org.junit.Before;
import org.junit.Test;

public class SpecifierTests {

	
	@Test
	public void test_check_noEmptyFeatureId() {
		try {
			Specifier spec = new Specifier("agency","",WellDataType.LOG);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void test_check_noEmptyAgencyId() {
		try {
			Specifier spec = new Specifier("","well",WellDataType.LOG);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	@Test
	public void test_check_notNullAgencyId() {
		try {
			Specifier spec = new Specifier(null,"well",WellDataType.LOG);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	@Test
	public void test_check_noNullTypeId() {
		try {
			Specifier spec = new Specifier("agency","well",null);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void test_check_noNullFeatureId() {
		try {
			Specifier spec = new Specifier("agency",null,WellDataType.LOG);
			assertTrue(false);
		} catch (NullPointerException e) {
			assertTrue(false);
		} catch (InvalidParameterException e) {
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
	}
}

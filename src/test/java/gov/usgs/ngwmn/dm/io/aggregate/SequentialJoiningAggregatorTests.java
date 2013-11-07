package gov.usgs.ngwmn.dm.io.aggregate;


import static org.junit.Assert.*;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.io.SupplyZipOutput;
import gov.usgs.ngwmn.dm.io.aggregate.Flow;
import gov.usgs.ngwmn.dm.io.aggregate.FlowFactory;
import gov.usgs.ngwmn.dm.io.aggregate.SequentialJoiningAggregator;
import gov.usgs.ngwmn.dm.spec.Encoding;
import gov.usgs.ngwmn.dm.spec.Specification;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;


public class SequentialJoiningAggregatorTests {
	public static final String ELEMENTS1
		= "<get><TimeValuePair><Id>1234</Id><Type>water</Type></TimeValuePair><TimeValuePair><Id>5678</Id><Type>clay</Type></TimeValuePair></get>";
	public static final String ELEMENTS2
		= "<get><TimeValuePair><Id>8765</Id><Type>water</Type></TimeValuePair><TimeValuePair><Id>4321</Id><Type>sandstone</Type></TimeValuePair></get>";
	public static final String ELEMENTS3
		= "<get><Result><Id>7777</Id><Type>H2O</Type></Result><Result><Id>8888</Id><Type>gray</Type></Result></get>";
	public static final String ELEMENTS4
		= "<get><Result><Id>6666</Id><Type>saltwater</Type></Result><Result><Id>9999</Id><Type>purple</Type></Result></get>";

	@Test
	public void test() throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Supplier<OutputStream> upstream;
		upstream = new SimpleSupplier<OutputStream>(baos);
		upstream = new SupplyZipOutput(upstream);
		
		FlowFactory fac = new FlowFactory() {
			@Override
			public Flow makeFlow(final Specifier spec, final Supplier<OutputStream> out) throws IOException {
				return new Flow() {
					@Override
					public Void call() throws Exception {
						OutputStream os = null;
						boolean   threw = true;
						try {
							os = out.begin();
							
							String elements="";
							
							switch (spec.getFeatureID().charAt( spec.getFeatureID().length()-1  )) {
								case 'a': elements = ELEMENTS1;
									break;
								case 'b': elements = ELEMENTS2;
									break;
								case 'c': elements = ELEMENTS3;
									break;
								case 'd': elements = ELEMENTS4;
									break;
							}
							
							os.write( elements.getBytes() );
							threw = false;
						} finally {
							out.end(threw);
						}
						return null;
					}
				};
			}
		};
		
		Specification spect;
		
		spect = new Specification();
		spect.setEncode( Encoding.CSV );
		
		spect.addWell( new Specifier("a","1234567a",WellDataType.WATERLEVEL) );
		spect.addWell( new Specifier("a","1234567b",WellDataType.WATERLEVEL) );
		
		spect.addWell( new Specifier("a","1234567c",WellDataType.LITHOLOGY) );
		spect.addWell( new Specifier("a","1234567d",WellDataType.LITHOLOGY) );
		
		new SequentialJoiningAggregator(fac,spect,upstream).call();
		
		File file = new File("/tmp","dataSJA.zip");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write( baos.toByteArray() );
		fos.flush();
		fos.close();
		
		Set<String> expectedNames = new HashSet<String>();
		expectedNames.add("WATERLEVEL.csv");
		expectedNames.add("LITHOLOGY.csv");
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ZipInputStream zis = new ZipInputStream(bais);
		for (String name : expectedNames) {
			ZipEntry entry = zis.getNextEntry();
			name = entry.getName(); // name as a loop it just to control the loop count
			assertTrue("Expected ["+name+"] in collection "+expectedNames, 
					expectedNames.contains(name) );
		}
		zis.close();

		int size = baos.toByteArray().length;
		assertTrue("expecting around 350 bytes but was " + size, size<400 && size>325);
		
	}
}
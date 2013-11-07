package gov.usgs.ngwmn.dm.io.transform;

import static org.junit.Assert.*;

import gov.usgs.ngwmn.dm.io.IoUtil;
import gov.usgs.ngwmn.dm.io.transform.ExcelXssfOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

/**
 * @author drsteini
 *
 */
public class ExcelXssfOutputStreamTest {

	private int rowCount = 1;

	@Test
	public void test_smallFileTranform() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExcelXssfOutputStream filter = new ExcelXssfOutputStream(baos);

		// Create test data
		Map < String, ? > map = getTestRow();
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos2);
		oos.writeObject(map);
		byte[] b = baos2.toByteArray();

		// Write to the filter
		filter.write(b, 0, b.length);
		filter.finishWorkbook();
		IoUtil.quiteClose(filter);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Workbook wb = new XSSFWorkbook(bais);
		assertNotNull(wb);
		Sheet sheet = wb.getSheet("report");
		assertNotNull(sheet);
		Row row1 = sheet.getRow(0);
		assertNotNull(row1);
		assertEquals("col1", row1.getCell(0).getStringCellValue());

		Row row2 = sheet.getRow(1);
		assertNotNull(row2);
		assertEquals("data1", row2.getCell(0).getStringCellValue());
	}


	@Test
	public void test_largeRowCountTransform() throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExcelXssfOutputStream filter = new ExcelXssfOutputStream(baos);

		for (int i = 0; i < 1000; i++) {
			Map < String, ? > map = getTestRow();
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos2);
			oos.writeObject(map);
			byte[] b = baos2.toByteArray();
			// Write to the filter
			filter.write(b, 0, b.length);
		}
		filter.finishWorkbook();
		IoUtil.quiteClose(filter);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Workbook wb = new XSSFWorkbook(bais);
		assertNotNull(wb);
		Sheet sheet = wb.getSheet("report");
		assertNotNull(sheet);
		Row row1 = sheet.getRow(0);
		assertNotNull(row1);
		assertEquals("col1", row1.getCell(0).getStringCellValue());

		Row lastRow = sheet.getRow(1000);
		assertNotNull(lastRow);
		assertEquals("1000", lastRow.getCell(2).getStringCellValue());
		assertEquals(1000, sheet.getLastRowNum());
	}

	public  Map<String, Object> getTestRow() {
		Map<String, Object> record = new LinkedHashMap<String, Object>();
		record.put("col1", "data1");
		record.put("col2", "data2");
		record.put("col3", rowCount++);
		record.put("col4", new Date(10000));
		record.put("col5", null);
		record.put("col6", new BigDecimal(29382.2398));

		return record;
	}

}
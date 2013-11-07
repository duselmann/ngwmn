package gov.usgs.ngwmn.dm.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import au.com.bytecode.opencsv.CSVWriter;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.io.aggregate.Flow;
import gov.usgs.ngwmn.dm.io.aggregate.FlowFactory;
import gov.usgs.ngwmn.dm.spec.Specifier;

/** Produce the results of SQL query as a CSV stream.
 * Can be shared between threads.
 * 
 * @author rhayes
 *
 */
public abstract class QueryFlowFactory implements FlowFactory {

	private DataSource datasource;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public QueryFlowFactory(DataSource datasource) {
		super();
		this.datasource = datasource;
	}

	abstract String getQuery();

	/** Throw an exception if the type is not as expected
	 * 
	 * @param wellDataType
	 */
	abstract void checkType(WellDataType wellDataType);

	private class QueryFlow implements Flow {

		private String agency;
		private String site;
		private Date beginDate;
		private Date endDate;
		private Supplier<OutputStream> sos;

		QueryFlow(Specifier spec, Supplier<OutputStream> sos) {
			agency = spec.getAgencyID();
			site = spec.getFeatureID();
			beginDate = spec.getBeginDate();
			endDate = spec.getEndDate();
			this.sos = sos;
		}

		@Override
		public Void call() throws Exception {

			Void valu;
			OutputStream os = sos.begin();

			final Writer writer = new OutputStreamWriter(os);
			try {
				ResultSetExtractor<Void> rse = new ResultSetExtractor<Void>() {

					@Override
					public Void extractData(ResultSet rs) throws SQLException,
					DataAccessException {
						@SuppressWarnings("resource") // managed by pipeline
						CSVWriter cw = new CSVWriter(writer);
						try {
							cw.writeAll(rs, true);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

						return null;
					}
				};

				String query = getQuery();

				JdbcTemplate t = new JdbcTemplate(datasource);

				String bd_s = null;
				if (beginDate != null) {
					bd_s = sdf.format(beginDate);
				}
				String ed_s = null;
				if (endDate != null) {
					ed_s = sdf.format(endDate);
				}
				valu = t.query(query, rse, agency, site, bd_s, ed_s);
			} finally {
				writer.close();
			}

			return valu;
		}

	}

	@Override
	public Flow makeFlow(Specifier spec, Supplier<OutputStream> out)
			throws IOException {

		checkType(spec.getTypeID());

		return new QueryFlow(spec,out);
	}

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}

}

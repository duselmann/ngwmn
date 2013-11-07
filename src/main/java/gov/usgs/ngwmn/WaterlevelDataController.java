package gov.usgs.ngwmn;

import gov.usgs.ngwmn.dm.DataBroker;
import gov.usgs.ngwmn.dm.SiteNotFoundException;
import gov.usgs.ngwmn.dm.dao.WellRegistry;
import gov.usgs.ngwmn.dm.dao.WellRegistryDAO;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.io.transform.HydrographFilterStream;
import gov.usgs.ngwmn.dm.spec.Specification;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WaterlevelDataController {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private DataBroker db;
	private WellRegistryDAO registry;
	protected ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor();

	public WaterlevelDataController() {
	}

	@Override
	protected void finalize() throws Throwable {
		// this shutdown should allow queued and running jobs to finish
		pipelineExecutor.shutdown();
	}

	/** Produce waterlevels for the given site, in format expected by Dygraphs:
	 * first line has headers: time,value
	 * then data as comma-separated values, with negative values

	 * @param agency
	 * @param site
	 * @throws IOException
	 */
	@RequestMapping(value="csv/{agency}/{site}", produces={"text/csv","text/plain"})
	public void generateTable(
			@PathVariable String agency,
			@PathVariable String site,
			HttpServletResponse response)
					throws IOException, ServletException {

		Specifier spec = new Specifier(agency,site,WellDataType.WATERLEVEL);

		boolean exists = true;
		try {
			db.checkSiteExists(spec);
		} catch (SiteNotFoundException snfe) {
			exists = false;
		}

		logger.info("Providing csv data for {}, exists? {}", spec, exists);

		if ( ! exists) {
			// See if changing _ to space in agency name will fix it
			// maybe it a problem with space in agency name?
			String agncy = agency.replaceAll("_", " ");
			if (! agncy.equals(agency)) {
				logger.warn("retrying with spaced-out agency name {}", agncy);
				spec = new Specifier(agncy,site,WellDataType.WATERLEVEL);
			}
			// don't check again, just let the downstream processing throw the error
		}

		try {
			db.checkSiteExists(spec);

			WellRegistry well = registry.findByKey(agency, site);
			Double altitude = (well != null) ? well.getAltVa() : null;

			response.setContentType("text/csv");
			OutputStream os = response.getOutputStream();

			HydrographFilterStream hfos = new HydrographFilterStream(os);
			hfos.setElevation(altitude);
			hfos.setExecutor(pipelineExecutor);

			Supplier<OutputStream> supp = new SimpleSupplier<OutputStream>(hfos);

			db.fetchWellData(spec, supp);
			logger.debug("Sent waterlevel csv for {} elev {}", spec, altitude);
		}
		catch (SiteNotFoundException snfe) {
			logger.info("Request for unknown site {}", spec);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such site found");
		}
		catch (Exception e) {
			throw new ServletException(e);
		}

	}

	protected Specification makeSpecification(Specifier well) {
		Specification spec = new Specification();
		spec.setBundled(false);
		spec.addWell(well);
		return spec;
	}

	public DataBroker getDataBroker() {
		return db;
	}

	public void setDataBroker(DataBroker db) {
		this.db = db;
	}

	public WellRegistryDAO getRegistry() {
		return registry;
	}

	public void setRegistry(WellRegistryDAO registry) {
		this.registry = registry;
	}


}

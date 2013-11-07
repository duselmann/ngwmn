package gov.usgs.ngwmn.admin;

import gov.usgs.ngwmn.dm.visualization.QueryDataTableGenerator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.DataSourceRequest;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;

@Controller
public class DataSourceController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private DataSource dataSource;
	
	@RequestMapping("/data")
	public void generateTable(
			@RequestParam("query") String query,
			HttpServletRequest request, 
			HttpServletResponse response) 
					throws IOException {
		
		logger.info("making a data table from query {}", query);
		try {
			QueryDataTableGenerator gen = new QueryDataTableGenerator(query, dataSource);
			DataSourceHelper.executeDataSourceServletFlow(request, response, gen, false);
			logger.info("hve sent data table");
		}
		catch (DataAccessException sql) {
			logger.error("problem getting data for table", sql);
			ResponseStatus status = new ResponseStatus(StatusType.ERROR, ReasonType.INVALID_QUERY,
					sql.getMessage());
			DataSourceRequest dsRequest = DataSourceRequest.getDefaultDataSourceRequest(request);
			DataSourceHelper.setServletErrorResponse(status, dsRequest, response);
		}
		catch (Exception rte) {
			logger.error("problem sending data as table", rte);
			ResponseStatus status = new ResponseStatus(StatusType.ERROR, ReasonType.INTERNAL_ERROR,
					rte.getMessage());
			DataSourceRequest dsRequest = DataSourceRequest.getDefaultDataSourceRequest(request);
			DataSourceHelper.setServletErrorResponse(status, dsRequest, response);
		} 
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}

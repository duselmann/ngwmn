<%@page import="javax.xml.ws.Response"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="
				javax.naming.Context,
				javax.naming.InitialContext,
				javax.sql.DataSource,
				java.sql.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Multi-Download demo</title>
</head>
<body>

	<h2>Multi-Site download demo</h2>
	<form method="GET"
		action="
		http://cida-eros-ngwmndev.er.usgs.gov:8080/ngwmn_cache/data?agencyID=USGS&listOfWells=402734087033401&listOfWells=402431075020801&type=WATERLEVEL">
		<input name="type" value="WATERLEVEL" type="hidden" />
		<input type="submit" value="download" />
		<table>
			<tr>
			<td>

		<%
		Connection connection = null;
		ResultSet rset = null;
		Statement statement = null;
		try {

			String query = " select * from "
					+ " (select my_siteid, site_no, site_name, agency_cd  "
					+ "   from well_registry "
					+ "   where rownum < 600 "
					+ "     and agency_cd not in ('ISWS','MBMG', 'USGS','NJGS') "
					+ "     and site_name is not null "
					+ " union all "
					+ " select my_siteid, site_no, site_name, agency_cd  "
					+ "   from well_registry "
					+ "   where agency_cd in ('USGS') "
					+ "     and rownum < 100 "
					+ "     and site_name is not null "
					+ " ) "
					+ " order by agency_cd, site_no ";
			
			
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/GW_DATA_PORTAL");
			connection = ds.getConnection();

			statement = connection.createStatement();
			//statement.setMaxRows(1);
			rset = statement.executeQuery(query);      
			String agency = "";
			while (rset.next()) {
				String currentAgency = rset.getString("AGENCY_CD");
				if (!currentAgency.equals(agency)) {
					if (agency.length() != 0){
						out.println("</td>");
					}
					out.println("<td valign='top'><b>" + currentAgency + "</b>");
					agency = currentAgency;
				}
				out.println("<br><input type='checkbox' name='listOfWells' value='" 
					+ rset.getString("MY_SITEID") 
					+ "'>"
					+ rset.getString("SITE_NAME")
					);
			}
			
			statement.close();
			statement = null;
			rset.close();  
			rset = null;
			connection.close();  
			connection = null;
		} catch (Exception e) {
			System.out.println("unable to print sites for multi download multi.jsp");   
			e.printStackTrace();
		} finally {
	    if (rset != null) {
	      try { rset.close(); } catch (SQLException e1) { ; }
	      rset = null;
	    }
	    if (statement != null) {
	      try { statement.close(); } catch (SQLException e2) { ; }
	      statement = null;
	    }
	    if (connection != null) {
	      try { connection.close(); } catch (SQLException e3) { ; }
	      connection = null;
	    }
		}      
		//http://cida-eros-ngwmndev.er.usgs.gov:8080/ngwmn/data?agencyID=USGS&listOfWells=402734087033401&listOfWells=402431075020801&type=WATERLEVEL	
		%>
			</td>
			</tr>
		</table>
		<input type="submit" value="download" />
	</form>

</body>
</html>
<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%@ include file="type.txt"%> Data Publication Timeline</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
<!--     <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9"></meta>  -->
	<script type="text/javascript"
		  src="http://cida.usgs.gov/js/dygraphs/2012_07_21_bc2d2/dygraph-dev.js"></script>
    
    <script type="text/javascript">

    var agency;
    
      // Load the Visualization API and some packages.
      google.load('visualization', '1.0', {'packages':['corechart','table']});

      // Set a callback to run when the Google Visualization API is loaded.
      google.setOnLoadCallback(doQuery);

      function doQuery() {
    	  var query = new google.visualization.Query(
    			  "stats"
    			  );
    	  
    	  query.setQuery(
    			  'select toDate(FETCH_DATE), count(PUBLISHED) ' +
    			  'group by toDate(FETCH_DATE) '+
    			  'pivot AGENCY_CD ' +
    			  'label toDate(FETCH_DATE) "fetch date"');
    	  
    	  query.send(handleQueryResponse);
    	  
      }
      
      function setAgency(select) {
    	  agency = select.options[select.selectedIndex].value;
      }
      
      function redraw() {
    	  var query;
    	  if (agency) {
        	  query = new google.visualization.Query(
        			  "table/" + agency
        			  );
        	      	  
    	  } else {
        	  query = new google.visualization.Query(
        			  "table"
        			  );    		  
    	  }
    	  
       		query.send(handleQueryResponse);
      }
      
      function handleQueryResponse(response) {
    	    if (response.isError()) {
    	      alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
    	      return;
    	    }

    	    var data = response.getDataTable();
    	    
    	    var visualization = new google.visualization.LineChart(document.getElementById('chart_div'));
            var options = {'title':'Fetch Statistics',
                    'width':400,
                    'height':300};
    	    visualization.draw(data, options);
    	    
    	    var table = new google.visualization.Table(document.getElementById('stats_table'));
            table.draw(data, {
            	showRowNumber: false,
            	sort: 'enable',
            	sortColumn: 0,
            	sortAscending: false
            });
            
    	    var dyChart = new Dygraph.GVizChart(
    	    		document.getElementById('dygraphs_chart'));
    	    var dyOptions = {
    	    		hideOverlayOnMouseOut: false,
    	    	      labelsDivStyles: { border: '1px solid black' },
    	    	      title: 'Fetch Statistics',
    	    	      xlabel: 'Date',
    	    	      ylabel: 'Count',
    	    	      showRangeSelector: true
    	    	    }
    	    dyChart.draw(data, dyOptions);
    	  }
            
      </script>
      
<title><%@ include file="type.txt"%> Data Publication Timeline</title>
</head>
<body>

<h1><%@ include file="type.txt"%> data publication by day</h1>
<h2>count of wells</h2>

<div id="dygraphs_chart"></div>

<div id="chart_div"></div>

<div id="stats_table"></div>
</body>
</html>
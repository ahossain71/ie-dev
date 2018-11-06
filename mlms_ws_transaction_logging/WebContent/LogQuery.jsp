<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page language="java" %>
<%@ page import="java.lang.*,
		java.util.*,
		java.sql.Connection,
        java.sql.DriverManager,
        java.sql.ResultSet,
        java.sql.ResultSetMetaData,
        java.sql.SQLData,
        java.sql.SQLException,
        java.sql.SQLWarning,
        java.sql.PreparedStatement,
        java.sql.Types,
        java.sql.Struct,
		gov.mlms.cciio.cms.util.WSLog" %>

<%!
	final String DEFAULT_COLUMN = "*";
	final String DEFAULT_FROM   = "AT_MLMS_WS_TRANS_LOG ";
	final String DEFAULT_WHERE = "WEB_SERVICE_ID='xxxxxx'\n AND ACTION_DATE > TO_DATE('20160701-000000', 'YYYYMMDD-HH24MISS')\n AND ACTION_DATE < TO_DATE('20160702-000000', 'YYYYMMDD-HH24MISS')\n AND ROWNUM <= 500";
	final String DEFAULT_ORDER = "ACTION_DATE ASC";
	
	String PARAM_ACTION      = "ACTION";
	String PARAM_COLUMN      = "COLUMN";
	String PARAM_WHERE       = "WHERE";
	String PARAM_ORDER       = "ORDER";

	String ACTION_QUERY      = "ACTION_QUERY";
	
	// Restrict to SELECT only
	public String constructQuery(String column, String where, String order) {
	    StringBuilder result = new StringBuilder("SELECT ");
	    result.append(column);
	    result.append(" FROM ");
	    result.append(DEFAULT_FROM);
	    result.append(" WHERE ");
	    result.append(where);
	    if (order != null && order.trim().length() > 0) {
	        result.append(" ORDER BY ");
	    	result.append(order);
	    }
	    return result.toString();
	}

%>
<%
	//HelpDeskSupportConfig.refreshSettings();
	boolean hasAccess = true;// HelpDeskSupportConfig.canAccessSupportTools(AKOID);
	String strJspUri = request.getRequestURI();

	String column = request.getParameter(PARAM_COLUMN);
	if (column == null) {
	    column = "";
	}
	String where = request.getParameter(PARAM_WHERE);
	if (where == null) {
	    where = "";
	}
	String order = request.getParameter(PARAM_ORDER);
	if (order == null) {
	    order = "";
	}
	String action = request.getParameter(PARAM_ACTION);
	StringBuilder output = new StringBuilder();
	StringBuilder error = new StringBuilder();
	
	if (hasAccess) {
		if (action == null) {
	    	action = "";
	    	column = DEFAULT_COLUMN;
	    	where = DEFAULT_WHERE;
	    	order = DEFAULT_ORDER;
		}
	
		if (action.length() > 0) {
			//if (action.equals(ACTION_QUERY))
		    WSLog.queryAndFormatOutput(false, "WSLog-LogQuery.jsp", constructQuery(column, where, order),
		            null, "datatable", false, output, error);
		}
	}
%>
<!DOCTYPE HTML>
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="IEFileMan.css" />
		<title>MLMS WS Log Query Page</title>
	</head>
	<body>
<%
	if (hasAccess) {
%>
	<div class="contentDiv">
<%
		if (error.length() > 0) {
%>
		    <textarea name="Error" cols="50" rows="6" style="color: red" wrap="soft"><%= error.toString() %></textarea><br/>
<%
		}
%>
		<form name="queryForm" action="<%= strJspUri %>" method="POST"> <!-- Submit to itself -->
			<table>
				<tr class="mouseout"><td valign="top" align="right">SELECT</td><td><textarea name="<%= PARAM_COLUMN %>" cols="100" rows="2" wrap="soft"><%=column%></textarea></td></tr>
				<tr class="mouseout"><td valign="top" align="right">FROM</td><td><%= DEFAULT_FROM %></td></tr>
				<tr class="mouseout"><td valign="top" align="right">WHERE</td><td><textarea name="<%= PARAM_WHERE %>" cols="100" rows="4" wrap="soft"><%=where%></textarea></td></tr>
				<tr class="mouseout"><td valign="top" align="right">ORDER BY</td><td><textarea name="<%= PARAM_ORDER %>" cols="100" rows="1" wrap="soft"><%=order%></textarea></td></tr>
				<tr class="mouseout"><td colspan=2><button name="<%= PARAM_ACTION %>" type="submit" value="<%= ACTION_QUERY %>">Execute Query</button></td></tr>
			</table>
<%= output.toString() %>
		</form>
		<br/>
		
	</div>
	<!-- For IE 9 -->
	<iframe id="CsvExpFrame" style="display: none"></iframe>
<%
	} else { // !hasAccess
		out.println(WSLog.NO_ACCESS_HTML_MESSAGE);
	} 
%>
	</body>
</html>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" import="java.util.*,
		java.net.*,
		java.text.*,
		java.util.ArrayList,
		java.io.*,
		java.sql.Connection,
		java.sql.PreparedStatement,
		java.sql.ResultSet,
		java.sql.ResultSetMetaData,
		java.sql.SQLData,
		java.sql.SQLException,
		java.sql.SQLWarning,
		gov.mlms.cciio.cms.util.WSLog" %>
<%!
	// Parameter Name
	String PARAM_ACTION         = "ACTION";
	String PARAM_WSID           = "P_W";
	String PARAM_TRANSCODE      = "P_T";
	String PARAM_PARTNERID      = "P_P";
	String PARAM_STATUSCODE     = "P_S";
	String PARAM_DESC           = "P_D";
	String PARAM_XML            = "P_X";
	
	// For Description TextArea width and height
	String ID_DESC_TA           = "DESC_TA";
	String ID_DESC_TA_WIDTH     = ID_DESC_TA + "_X";
	String ID_DESC_TA_HEIGHT    = ID_DESC_TA + "_Y";

	// For XML TextArea width and height
	String ID_XML_TA            = "XML_TA";
	String ID_XML_TA_WIDTH      = ID_XML_TA + "_X";
	String ID_XML_TA_HEIGHT     = ID_XML_TA + "_Y";

	String ACTION_LOG           = "A_L";
	
	//int SQL_EDITFIELD_COLS = 40;
	//int SQL_EDITFIELD_ROWS = 5;
	int SQL_WIDTH = 400;
	int SQL_HEIGHT = 200;
	
%>
<%
	boolean hasAccess = true; //TODO: Replace with authentication logic here
	
	// Store the value of the URI for this JSP.
	String strJspUri = request.getRequestURI();
	        
	String wsID = request.getParameter(PARAM_WSID);
	if (wsID == null) {
		wsID = "";
	}
	String transCode = request.getParameter(PARAM_TRANSCODE);
	if (transCode == null) {
		transCode = "";
	}
	String partnerID = request.getParameter(PARAM_PARTNERID);
	if (partnerID == null) {
		partnerID = "";
	}
	String statusCode = request.getParameter(PARAM_STATUSCODE);
	if (statusCode == null) {
		statusCode = "";
	}
	String desc = request.getParameter(PARAM_DESC);
	if (desc == null) {
		desc = "";
	}
	String xml = request.getParameter(PARAM_XML);
	if (xml == null) {
		xml = "";
	}
	String action = request.getParameter(PARAM_ACTION);
	if (action == null) {
		action = "";
	}
	int width = SQL_WIDTH;
	try {
		if ((width = Integer.parseInt(request.getParameter(ID_XML_TA_WIDTH))) <= 0) {
			width = SQL_WIDTH;
		}
	} catch (Exception ex) {
		width = SQL_WIDTH;
	}
	int height = SQL_HEIGHT;
	try {
		if ((height = Integer.parseInt(request.getParameter(ID_XML_TA_HEIGHT))) <= 0) {
			height = SQL_HEIGHT;
		}
	} catch (Exception ex) {
		height = SQL_HEIGHT;
	}

	StringBuilder error = new StringBuilder("");
	StringBuilder output = new StringBuilder("");
	
	if (hasAccess) {
		if (action.equals(ACTION_LOG)) {
			try {
				//System.out.println("Logging: '" + query + "'");
				WSLog.logWebService(wsID, transCode, partnerID, statusCode, desc, xml);
			} catch (Exception ex) {
				output = new StringBuilder("");
				error.append(ex.getMessage());
			}
		}
		if (width == 0) {
			width = SQL_WIDTH;
		}
		if (height == 0) {
			height = SQL_HEIGHT;
		}
	}
%>
<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<meta name="robots" content="noindex">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="pragma" content="no-cache">
		<link rel="stylesheet" type="text/css" href="IEFileMan.css"/>
		<title>Test WS Logging Page (Do NOT deploy this test page to PROD)</title>
	</head>
<body>
<%
	if (hasAccess) {
%>
	<div class="contentDiv">
	<form action="<%= strJspUri %>" method="Post">
		<table>
<%
	if (error.length() > 0) {
%>
			<tr><td><textarea name="ErrorWarning" style="color: #FF0000; font-weight: bold; width: 500px; height: 150px;" readonly><%=error.toString()%></textarea></td></tr>
<%
	}
%>
			<tr>
				<td width="50%">WS ID: <input type="text" name="<%= PARAM_WSID %>" value="<%=(wsID!=null?wsID:"")%>"/></td>
			</tr>
			<tr>
				<td width="50%">Trans. Code: <input type="text" name="<%= PARAM_TRANSCODE %>" value="<%=(transCode!=null?transCode:"")%>"/></td>
			</tr>
			<tr>
				<td width="50%">Partner ID: <input type="text" name="<%= PARAM_PARTNERID %>" value="<%=(partnerID!=null?partnerID:"")%>"/></td>
			</tr>
			<tr>
				<td width="50%">Status Code: <input type="text" name="<%= PARAM_STATUSCODE %>" value="<%=(statusCode!=null?statusCode:"")%>"/></td>
			</tr>
			<tr>
				<td width="50%">[Optional] Description:<br/><textarea id="<%= ID_DESC_TA %>" name="<%= PARAM_DESC %>" style="width:<%= width %>px; height:<%= height %>px;" wrap="soft"><%=(desc!=null?desc:"")%></textarea>
					<input type="hidden" id="<%= ID_DESC_TA_WIDTH %>" name="<%= ID_DESC_TA_WIDTH %>" value="<%= width %>"/>
					<input type="hidden" id="<%= ID_DESC_TA_HEIGHT %>" name="<%= ID_DESC_TA_HEIGHT %>" value="<%= height %>"/>
				</td>
			</tr>
			<tr>
				<td width="50%">[Optional] XML:<br/><textarea id="<%= ID_XML_TA %>" name="<%= PARAM_XML %>" style="width:<%= width %>px; height:<%= height %>px;" wrap="soft"><%=(xml!=null?xml:"")%></textarea>
					<input type="hidden" id="<%= ID_XML_TA_WIDTH %>" name="<%= ID_XML_TA_WIDTH %>" value="<%= width %>"/>
					<input type="hidden" id="<%= ID_XML_TA_HEIGHT %>" name="<%= ID_XML_TA_HEIGHT %>" value="<%= height %>"/>
				</td>
			</tr>
		</table>
		<br>
		<table>
			<tr>
				<td><button name="<%= PARAM_ACTION %>" type="submit" value="<%= ACTION_LOG %>">Log</button></td>
			</tr>
		</table>
		<%=output.toString()%>
	</form>
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
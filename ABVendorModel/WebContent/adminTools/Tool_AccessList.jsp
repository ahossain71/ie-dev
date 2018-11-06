<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.*,
            java.util.ArrayList,
            gov.cms.cciio.helpdesksupport.*"%>
<%!//import mil.army.dls.lms.test.Utilities
	// Parameter Name
	final String PARAM_ACTION = "ACTION";
	final String PARAM_USERNAME = "UN";
	final String PARAM_USE_TOOLS = "USE_TOOLS";
	final String PARAM_CTRL_ACCESS = "CONTROL";

	final String ACTION_ADD = "ADD";
	final String ACTION_REMOVE = "REMOVE";
	final String ACTION_UPDATE = "UPDATE";

	/*
	 * This form assumes IE8+, otherwise the value desired would not be submitted.
	 * Instead, the text displayed on the button will be submitted.
	 */
	private void formatAccessList(String formURL, ArrayList<HelpDeskSupportUser> userList, StringBuilder output, StringBuilder error) {
		output.append("<br/><h3>Custom Tools Access List</h3>\n");
		output.append("<table class=\"filelist\" cellspacing=\"1px\" cellpadding=\"0px\">\n<tr>\n");
		// remove button column
		output.append("<th></th>");
		// Display column headings
		output.append("<th align=\"center\"><font class=\"header\">USERNAME</font></th>");
		output.append("<th align=\"center\"><font class=\"header\">USE TOOLS</font></th>");
		output.append("<th align=\"center\"><font class=\"header\">CONTROL ACCESS</font></th>");
		// update button column
		output.append("<th></th>");
		output.append("</tr>\n");
		// First row is to add username to the list
		output.append("<form action=\"");
		output.append(formURL);
		output.append("\" method=\"Post\"><tr class=\"mouseout\">");
		output.append("<td></td>");
		output.append("<td><input type=\"text\" size=\"30\" name=\"");
		output.append(PARAM_USERNAME);
		output.append("\"/></td>");
		output.append("<td></td><td></td>");
		output.append("<td><button type=\"submit\" name=\"");
		output.append(PARAM_ACTION);
		output.append("\" value=\"");
		output.append(ACTION_ADD);
		output.append("\">Add</button></td>");
		output.append("</tr></form>\n");

		// Display data, fetching until end of the result set
		for (HelpDeskSupportUser user : userList) {
			output.append("<form action=\"");
			output.append(formURL);
			output.append("\" method=\"Post\"><tr class=\"mouseout\">");
			// Remove button
			output.append("<td><button type=\"submit\" name=\"");
			output.append(PARAM_ACTION);
			output.append("\" value=\"");
			output.append(ACTION_REMOVE);
			output.append("\">Remove</button></td>");
			// Username
			output.append("<td>");
			output.append(user.getUserName());
			output.append("<input type=\"hidden\" name=\"");
			output.append(PARAM_USERNAME);
			output.append("\" value=\"");
			output.append(user.getUserName());
			output.append("\"/></td>");
			// Can access support tools
			output.append("<td><input type=\"checkbox\" name=\"");
			output.append(PARAM_USE_TOOLS);
			output.append("\" value=\"Y\"");
			if (user.canUseTools()) {
				output.append(" checked");
			}
			output.append("/></td>");
			// Is Admininstrator
			output.append("<td><input type=\"checkbox\" name=\"");
			output.append(PARAM_CTRL_ACCESS);
			output.append("\" value=\"Y\"");
			if (user.canControlAccess()) {
				output.append(" checked");
			}
			output.append("/></td>");
			// Update button
			output.append("<td><button type=\"submit\" name=\"");
			output.append(PARAM_ACTION);
			output.append("\" value=\"");
			output.append(ACTION_UPDATE);
			output.append("\">Update</button></td>");
			output.append("</tr></form>\n");
			// Fetch the next result set row
		}
		output.append("</table>");
	}%>
<%
	/**
	 * Load IEFileMan properties
	 **/
	//HelpDeskSupportConfig.refreshSettings();

	//int SQL_EDITFIELD_COLS = HelpDeskSupportConfig.getSQLEditFieldCols();
	//int SQL_EDITFIELD_ROWS = HelpDeskSupportConfig.getSQLEditFieldRows();

	/*
	 * Check AKO Username, getting the name from header
	 * This assumes siteminder is protecting the request itself,
	 * or otherwise insecure because users can populate the header themselves
	 */
	String uid = request.getHeader("uid");
	StringBuilder error = new StringBuilder();
	StringBuilder output = new StringBuilder();
	boolean hasAccess = HelpDeskSupportConfig.isAccessListAdmin(uid, error);

	// Store the value of the URI for this JSP.
	String strJspUri = request.getRequestURI();
	String action = request.getParameter(PARAM_ACTION);
	if (action == null) {
		action = "";
	}
	String username = request.getParameter(PARAM_USERNAME);
	if (username == null) {
		username = "";
	}
	String useTools = request.getParameter(PARAM_USE_TOOLS);
	if (useTools == null) {
		useTools = "";
	}
	String controlAccess = request.getParameter(PARAM_CTRL_ACCESS);
	if (controlAccess == null) {
		controlAccess = "";
	}

	if (hasAccess) {
		if (action.equals(ACTION_ADD)) {
			HelpDeskSupportConfig.addEntry(username, error);
		} else if (action.equals(ACTION_REMOVE)) {
			HelpDeskSupportConfig.deleteEntry(username, error);
		} else if (action.equals(ACTION_UPDATE)) {
			HelpDeskSupportConfig.updateAccess(username, useTools.equals("Y"), controlAccess.equals("Y"), error);
		}
		formatAccessList(strJspUri, HelpDeskSupportConfig.getFullAccessList(error), output, error);
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<meta name="robots" content="noindex">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="pragma" content="no-cache">
		<link rel="stylesheet" type="text/css" href="MLMSStyle.css">
		<title>Access List</title>
	</head>
	<body>
		<%
		if (hasAccess) {
		%>
		<div class="toplinkDiv"><%=HelpDeskSupportConfig.TOP_LINK_HTML%></div>
		<div class="contentDiv">
			<%
				if (error.length() > 0) {
			%>
			<textarea name="Error" cols="50" rows="6" style="color: red"
				wrap="soft"><%=error.toString()%></textarea>
			<%
				}
			%>
			<br />
			<%=output.toString()%>
		</div>
		<%
			} else { // !hasAccess
				out.println(HelpDeskSupportConfig.NO_ACCESS_HTML_MESSAGE);
			}
		%>
	</body>
</html>

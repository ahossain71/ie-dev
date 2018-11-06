<!DOCTYPE HTML>
<%@page language="java"
	    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="gov.cms.cciio.common.util.SystemInit"%>
<%@ page import="com.saba.locator.ServiceLocator"%>
<%@ page import="com.saba.party.person.Employee"%>
<%@ page import="com.saba.party.person.EmployeeDetail"%>
<%@ page import="com.saba.party.PersonEntity"%>
<%@ page import="com.saba.party.Person"%>
<%@ page import="com.saba.reference.IReference"%>
<%@ page import="com.saba.primitives.SecurityInfoDetail"%>
<%@ page import="com.saba.exception.SabaException"%>
<%@ page import="gov.cms.cciio.interfaces.abmodel.ABModelCompletion"%>
<%@ page import="gov.cms.cciio.interfaces.abmodel.Response"%>
<%@ page import="gov.cms.cciio.common.auth.MLMSLoginBean"%>
<%@ page import="com.saba.ejb.IPrimaryKey"%>
<%@ page import="gov.cms.cciio.helpdesksupport.*"%>
<%
	String uid = request.getHeader("uid");
	boolean hasAccess = HelpDeskSupportConfig.canAccessSupportTools(uid);

	String PARAM_ACTION = "ACTION";
	String PARAM_USERNAME = "USERNAME";
	String PARAM_GUID = "GUID";
	String userName = request.getParameter(PARAM_USERNAME);
	String guid = request.getParameter(PARAM_GUID);
	String action = request.getParameter(PARAM_ACTION);
	ServiceLocator locator = null;

	if (guid == null) {
		guid = "";
	}
	if (userName == null) {
		userName = "";
	}
	if (action == null) {
		action = "";
	} else {
		action = action.trim();
	}
%>
<html>
	<head>
		<title>Fix Vendor Enrollments</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="robots" content="noindex">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="pragma" content="no-cache">
		<link rel="stylesheet" type="text/css" href="MLMSStyle.css">
	</head>
	<body>
<%
		if (hasAccess) {
%>
		<div class="toplinkDiv"><%=HelpDeskSupportConfig.TOP_LINK_HTML%></div>
		<div class="contentDiv">

			<form name="fixVendorEnrollments" method="POST">
<%
				boolean showReturn = false;
				if (userName == null || userName.length() == 0 || guid == null || guid.length() == 0) {
					out.println("<p align=left> Enter MLMS user id and GUID, submit</p>");
%>
				<table>
					<tr>
						<td>Enter User's ID :</td>
						<td><input type="text" name="<%=PARAM_USERNAME%>" value="<%=userName%>" style="width: 300px;" /></td>
						<td>Enter User's GUID :</td>
						<td><input type="text" name="<%=PARAM_GUID%>"value="<%=guid%>" style="width: 300px;" />
						</td>
					</tr>
				</table>
<%
				} else {
					showReturn = true;
					locator = SystemInit.loginMLMSAdmin();
					Person person = null;
					SecurityInfoDetail secInfoDetail = null;
					String password = null;
					MLMSLoginBean loginBean = new MLMSLoginBean();
					loginBean.setGuid(guid);
					loginBean.setUsername(userName);
					Employee employee = (Employee) ServiceLocator.getReference(Employee.class, userName);
					IPrimaryKey key = employee.getPrimaryKey();
					key.toString();
					PersonEntity personEntity = employee.getPersonEntity(locator);
					
					Response abResponse = ABModelCompletion.findAndProcessCompletions(locator, loginBean);

					//try {
					        //person = (Person) employee;
					        //secInfoDetail = personEntity.getSecurityInfo(person);
					        //password = secInfoDetail.getPassword();
					//} catch (SabaException e) {
					//      out.println("<p>Saba Exception caught" + e.getMessage()
					//                    + "<br></p>");
					//}

					if (locator != null) {
						out.println("<p>Locator is not null</p> ");
						if (employee != null) {
							out.println("<p>Display Name " + employee.getId() + "<br></p>");
							out.println("<p>ABResonse status code " + abResponse.getStatusCode() + "<br></p>");
							out.println("<p>ABResonse status message " + abResponse.getStatusMessage() + "<br></p>");
							out.println("<p>ABResonse error code " + abResponse.getErrorCode() + "<br></p>");
							out.println("<p>ABResonse error message " + abResponse.getErrorMessage() + "<br></p>");
							key.toString();
						}
					} else {
						out.println("");
					}
%>
				<table>
					<tr>
						<td>Enter Username :</td>
						<td><%= userName %></td>
						<td>Enter GUID :</td>
						<td><%= guid %></td>
					</tr>
				</table>
<%
				}
				if(!showReturn) {
%>
				<p align="center">
					<input type="hidden" name="<%=PARAM_ACTION%>" value="ACTION" />
					<input type="submit" value="Submit" />
				</p>
			</form>
<%
				}
				if (showReturn) {
%>
			<p align="center">
				<a href="MLMSfixit.jsp">Return to Form</a>
			</p>
<%
				}
%>
		</div>
<%
			} else { // !hasAccess
				out.println(HelpDeskSupportConfig.NO_ACCESS_HTML_MESSAGE);
			}
%>
	</body>
	<footer>
		<p align="center">version 160919 1106</p>
	</footer>
</html>

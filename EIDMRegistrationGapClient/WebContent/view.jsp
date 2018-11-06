<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="gov.hhs.cms.registrationgap.dto.RegistrationGapUserDTO"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="gov.cms.cciio.common.util.Constants"%>
<%@ page import="gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributeType"%>
<%@ page
	import="gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributesType"%>
<%@ page import="gov.hhs.cms.eidm.ws.waas.domain.role.RoleInfoType"%>

<%
	RegistrationGapUserDTO dto = (RegistrationGapUserDTO) request
			.getAttribute("userDTO");

	String title = "WaaSApplicationV6 Response View";
	SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
%>
<!DOCTYPE HTML>
<html>
<head>
<title>WaaSApplicationV6 Response View</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
<!--
main {
	vertical-align: middle;
	align: center
}

#link {
	vertical-align: bottom;
	align: center
}
-->
</style>
</head>
<body>
	<div id="main">
		<h1>
			<%
				out.println(title);
			%>
		</h1>
		<%
			if (dto.getResponseWasNull() == true) {
				out.println("<p align=left style='font-size:20px' >Null response received for user "
						+ dto.getUserName()
						+ "</p><p align=left style='font-size:20px' > Message : "
						+ dto.getErrorMessage() + "</p>");
			} else {

				out.println("<p align=left>User Name was : <u>");
				out.println(dto.getUsername() + "</u></br>");

				out.println("First Name : <u>" + dto.getFirstName()
						+ "</u></br>");
				out.println("Last Name : <u>" + dto.getLastName() + "</u></br>");
				out.println("GUID : <u>" + dto.getGuid() + "</u></p>");
				Date aDate = dto.getAccountCreateDate();
				out.println("<p align=left>");
				if (aDate != null) {

					out.println("Account Create Date : <u>"
							+ format.format(aDate) + "</u></p>");
				} else {
					out.println("Account Create Date :(MM-dd-YYYY) <u> was null </u> </p>");
				}
				Date eDate = dto.getAgentBrokerEffectiveDate();
				out.println("<p align=left>");
				if (eDate != null) {

					out.println("FFM_AGENT_BROKER Role Effective Date(MM-dd-YYYY) : <u>"
							+ format.format(eDate) + "</u></p>");
				} else {
					out.println("Role Effective Date(MM-dd-YYYY) : <u> was null </u></p>");
				}
				Date xDate = dto.getFFMExpirationDate();
				out.println("<p align=left>");
				if (xDate != null) {

					if (dto.isFFMComplete()) {

						out.println("FFMTraining attribute is current, expiration date is(MM-dd-YYYY): <u>"
								+ format.format(xDate) + "</u></p>");
					} else {
						out.println(" FFMTraining is not current, expiration date is(MM-dd-YYYY): <u>"
								+ format.format(xDate) + "</u></p>");
					}
				} else {

					out.println("FFMTraining has not been assigned");
				}
				Date sDate = dto.getShopExpirationdate();

				out.println("<p align=left>");
				if (sDate != null) {

					if (dto.isShopComplete()) {
						out.println("SHOPTraining Attribute is current, expiration date is <u>"
								+ format.format(sDate) + "</u></p>");
					} else {
						out.println("SHOPTraining is not current, expiration date is <u>"
								+ format.format(sDate) + "</u></p>");
					}
				} else {
					out.println(" AB has not been assigned SHOPTraining </p>");
				}

				out.println("<p align=left>");
				if (dto.getHistoricalRoleGrantDate() != null) {
					out.println("Historical Agent Broker Grant Date <u>"
							+ format.format(dto.getHistoricalRoleGrantDate())
							+ "</u></p>");
				}
				out.println("<p align=left>");
				out.println("User LOA (-1 indicates no value sent) <u>"
						+ dto.getLoa() + "</u></p>");
				out.println("<h3> Raw Role data </h3><sub> values seen are written as is with no formatting, unless noted</sub> <ul>");
				List<RoleInfoType> roleList = dto.getRoleList();
				List<RoleAttributeType> roleAttributeList = null;

				Iterator<RoleInfoType> roleIt = roleList.iterator();
				Iterator<RoleAttributeType> attributeIt = null;

				RoleInfoType roleInfoType = null;
				RoleAttributesType roleAttributesType = null;
				RoleAttributeType attributeType = null;

				while (roleIt.hasNext()) {
					roleInfoType = roleIt.next();

					out.println("<li>Role Name : " + roleInfoType.getRoleName()
							+ "</li>");
					if (roleInfoType.getRoleName().equalsIgnoreCase(
							Constants.FFM_AGENT_BROKER)) {
						out.println("<li>Role Grant date (MM-DD-YYYY): "
								+ roleInfoType.getRoleGrantDate().getMonth()
								+ "-"
								+ roleInfoType.getRoleGrantDate().getDay()
								+ "-"
								+ roleInfoType.getRoleGrantDate().getYear()
								+ "<sub> date is not formatted using an API formatter, the day, month, year are directly written from the date object received in the response </sub></li>");

						roleAttributesType = roleInfoType.getRoleAttributes();
						if (roleAttributesType != null) {
							roleAttributeList = roleAttributesType
									.getAttribute();
							if (roleAttributeList != null) {
								attributeIt = roleAttributeList.iterator();
								out.println("Role Attributes <ol>");
								while (attributeIt.hasNext()) {
									attributeType = attributeIt.next();
									out.println("<li>Attribute Name : "
											+ attributeType.getName() + "</li>");
									out.println("<li>Attribute Value : "
											+ attributeType.getValue()
											+ "</li>");
								}
								out.println("</ol>");
							}
						}
					}

				}
				out.println("</ul>");

			}
		%>
		<br>


	</div>
	<a href="start.jsp">Return to Form</a>
</body>
<footer>
	<p align="center">version 161110 1346</p>
</footer>
</html>
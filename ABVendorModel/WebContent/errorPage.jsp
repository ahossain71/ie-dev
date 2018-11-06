<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="gov.cms.cciio.common.util.*"%>
<jsp:useBean id="errorBean"
	class="gov.cms.cciio.common.auth.MLMSErrorBean" scope="session" />
<!DOCTYPE html PUBLIC "-//jW3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Error Page</title>
</head>
<body>
	<h> MLMS Login Error Page</h>
	<%
		if (errorBean != null) {
		System.out.println("Error Bean is not null");
			String message = errorBean.getErrorMsg();
			if (message == null && message.isEmpty()) {
				message = "No detailed information avaibable";
			}

			// mlmsGroupError = request.getParameter(Constants.MLMS_GROUP_NOT_FOUND);

			//   trainingGroupError = request.getParameter(Constants.TRAINING_GROUP_NOT_FOUND);
			//   uidError = request.getParameter("uidError");
			//   eidmHeaderError = request.getParameter("eidmHeaderError");
			//   generalError = request.getParameter("generalError");

			out.println("<table width='70%'>");
			if (errorBean.isMlmsGroupError()) {
				out.println("<tr><td> MLMS Required group not found in string :</td><td> </td></tr>");
			} else if (errorBean.isTrainingGroupError()) {
				out.println("<tr><td> Required access training group not found in portal headers  </td><td></td></tr>");
			} else if (errorBean.isUidError()) {
				out.println("<tr><td> UID not found in portal headers  </td><td></td></tr>");
			} else if (errorBean.isEidmHeaderError()) {
				out.println("<tr><td> EIDM Header Error  </td><td></td></tr>");
			} else if (errorBean.isGeneralError()) {
				out.println("<tr><td> MLMS Error  </td><td>" + message
						+ "</td></tr>");
			}
		}
		out.println("</table>");
	%>
	<!--    <a href="/Saba/index.jsp">Dev Login Page</a> -->
</body>
</html>
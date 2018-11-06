<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="gov.cms.cciio.common.util.FileReader"%>
<%@ page import="gov.cms.cciio.common.auth.MLMSLoginBean" %>
<%@ page import="gov.cms.cciio.common.auth.User" %>
<%@ page import="gov.cms.cciio.common.util.*"%>
<%@ page import="gov.cms.cciio.interfaces.abmodel.ABModelCompletion" %>
<%@ page import="gov.cms.cciio.common.registration.Registration" %>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>

<%
	String servletPath = "/Saba/DataMigrationServlet";
	String PARAM_FILE = "/home/sjmeyer/Documents/projects/CMS/data_integration/SHOP/EIDM-AgentIDs.csv";
	String PARAM_ACTION = "ACTION";
	String PARAM_NAMES = "NAMES";
	StringBuilder errorMsg = new StringBuilder();
	StringBuilder sbNames = new StringBuilder();
	
	String file = request.getParameter(PARAM_FILE);
	if (file == null) {
		file = "";
	} else {
		file = file.trim();
	}
	String action = request.getParameter(PARAM_ACTION);
	if (action == null) {
		action = "";
	} else {
		action = action.trim();
	}
	String names = request.getParameter(PARAM_NAMES);
	if (names == null) {
		names = "";
	}
	List<String> lines =  null;
	if (file != null) {
		 lines = FileReader.readFileLines(file);
		if (lines != null) {
			Iterator<String> it = lines.iterator();

			for (int i = 0; i < 10 && it.hasNext(); i++) {
				sbNames.append(it.next());
				sbNames.append(", ");
			}
			names = sbNames.toString();
		}//if
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>File Upload</title>
</head>
<body>
	<form name="fileUpload" method="POST">
		<table>
			<tr>
				<td>File:</td>
				<td><input type="text" name="<%=PARAM_FILE%>" value="<%=file%>"
					style="width: 300px;" /></td>
			</tr>
			<% if (lines != null) { %>
				<tr>
				<th>Names:</th>
				</tr>
				</table>
			<table>	
				<%
				Iterator<String> it = lines.iterator();

				while ( it.hasNext()) { 
				MLMSLoginBean lb = new MLMSLoginBean();
				
				lb.setUsername(it.next());
				lb.setRole(Constants.AGENT);
				lb.setFname("fname");
				lb.setLname("lname");
				User.LoginResult lr = User.lmsLogin(lb);
				String output = ABModelCompletion.transactionalGrantCompletion("AHIP", lb.getUsername(), "SHOP Marketplace", "2016/08/19", "English", "2017").toString();
				// Use Registration Class to register users
				
					
			
				%>
		 	<tr>
				<td>
			<% if (lr != null && lr.isSuccess()) {
				%><%= "created user " + lb.getUsername() %> <%
						
						
			} else {
				%><%= "Error reported " + lr.getLoginException()%> <%
			}
			%>
		
			
			
				</td>
				<td>
				<%  if (output != null) {%>
	        <%= output%>
	   <%  } %>
				</tr>  
		<% 	}
			
		}//if%>
			<tr>
				<td><input type="hidden" name="<%=PARAM_ACTION%>"
					value="ACTION" /><input type="submit" value="Submit" /></td>
			</tr>



		</table>

	</form>
</body>
</html>
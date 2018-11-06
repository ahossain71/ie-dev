<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="gov.cms.cciio.common.util.FileReader"%>
<%@ page import="gov.cms.cciio.common.auth.MLMSLoginBean"%>
<%@ page import="gov.cms.cciio.common.auth.User"%>
<%@ page import="gov.cms.cciio.common.util.*"%>
<%@ page import="gov.cms.cciio.interfaces.abmodel.ABModelCompletion"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="gov.cms.cciio.common.dataobject.*"%>
<%@ page import="gov.cms.cciio.helpdesksupport.*"%>

<%
	String uid = request.getHeader("uid");
	boolean hasAccess = HelpDeskSupportConfig.canAccessSupportTools(uid);

	String servletPath = "/Saba/DataMigrationServlet";
	String PARAM_VENDOR_CODE = "VENDOR_CODE";
	String PARAM_CURR_CODE = "CURR_CODE";
	String PARAM_CURR_YEAR = "CURR_YEAR";
	String PARAM_CURR_LANG = "LANG";
	String PARAM_GRANT_DATE = "GRANT_DATE";
	String PARAM_FILE = "/home/sjmeyer/Documents/projects/CMS/data_integration/SHOP/EIDM-AgentIDs.csv";
	String PARAM_ACTION = "ACTION";
	String PARAM_NAMES = "NAMES";
	StringBuilder errorMsg = new StringBuilder();
	StringBuilder sbNames = new StringBuilder();

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	String defaultGrantDate = dateFormat.format(new java.util.Date());
	String defaultCurrYear = defaultGrantDate.substring(0, 4);
	String defaultCurrLang = "English";
	
	String vendorCode = request.getParameter(PARAM_VENDOR_CODE);
	if (vendorCode == null) {
		vendorCode = "";
	} else {
		vendorCode = vendorCode.trim();
	}
	String currCode = request.getParameter(PARAM_CURR_CODE);
	if (currCode == null) {
		currCode = "";
	} else {
		currCode = currCode.trim();
	}
	String grantDate = request.getParameter(PARAM_GRANT_DATE);
	if (grantDate == null) {
		grantDate = "";
	} else {
		grantDate = grantDate.trim();
	}
	String currYear = request.getParameter(PARAM_CURR_YEAR);
	if (currYear == null) {
		currYear = "";
	} else {
		currYear = currYear.trim();
	}
	String currLang = request.getParameter(PARAM_CURR_LANG);
	if (currLang == null) {
		currLang = "";
	} else {
		currLang = currLang.trim();
	}
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
	List<String> lines = null;

	if (hasAccess) {
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
	}

	ArrayList<Curriculum> curriculumList = null;
	ArrayList<Vendor> vendorList = null;
	
	try {
		curriculumList = Curriculum.getCurriculumList();
		vendorList = Vendor.getVendorList();
	} catch (Exception ex) {
		errorMsg.append(CommonUtil.stackTraceToHTMLString(ex));
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>File Upload</title>
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
<%
			if (errorMsg.length() > 0) {
				out.println(errorMsg.toString());
			} else {
%>
	
		<form name="fileUpload" method="POST">
			<table>
				<tr>
					<td>Vendor:</td>
					<td>
						<select name="<%=PARAM_VENDOR_CODE%>">
<%
						for (Vendor v : vendorList) {
%>
							<option value="<%=v.getCode() %>" <%=(v.getCode().equals(vendorCode))?"selected":"" %>><%=v.getName() %></option>
<%
						}
%>
						</select>
					</td>
					<td>Curriculum:</td>
					<td>
						<select name="<%=PARAM_CURR_CODE%>">
<%
						for (Curriculum c : curriculumList) {
%>
							<option value="<%=c.getCode() %>" <%=(c.getCode().equals(currCode))?"selected":"" %>><%=c.getName() %></option>
<%
						}
%>
						</select>
					</td>
					<td>Curriculum Year:</td>
					<td><input type="text" name="<%= PARAM_CURR_YEAR%>" size="4" value="<%=((currYear.length()>0)?currYear:defaultCurrYear)%>"/></td>
					<td>Curriculum Lang:</td>
					<td><input type="text" name="<%= PARAM_CURR_LANG%>" size="10" value="<%=((currLang.length()>0)?currLang:defaultCurrLang)%>"/></td>
					<td>Grant Date</td>
					<td><input type="text" name="<%= PARAM_GRANT_DATE%>" size="10" value="<%=((grantDate.length()>0)?grantDate:defaultGrantDate)%>"/></td>
				</tr>
				<tr>
					<td>File:</td>
					<td colspan="9"><input type="text" name="<%=PARAM_FILE%>"
						value="<%=file%>" style="width: 300px;" />
					</td>
				</tr>
				<%
					if (lines != null) {
				%>
				<tr>
					<th>Names:</th>
				</tr>
			</table>
			<table>
				<%
					Iterator<String> it = lines.iterator();

							while (it.hasNext()) {
								MLMSLoginBean lb = new MLMSLoginBean();

								lb.setUsername(it.next());
								lb.setRole(Constants.AGENT);
								lb.setFname("fname");
								lb.setLname("lname");
								User.LoginResult lr = User.lmsLogin(lb);
								String output = ABModelCompletion.transactionalGrantCompletion(vendorCode,
												lb.getUsername(), currCode, grantDate, currLang, currYear).toString();
				%>
				<tr>
					<td>
						<%
							if (lr != null && lr.isSuccess()) {
						%>
						<%="created user " + lb.getUsername()%>
						<%
							} else {
						%>
						<%="Error reported " + lr.getLoginException()%> 
						<%
					 		}
						%>
					</td>
					<td>
						<%
							if (output != null) {
						%><%=output%>
						<%
							}
						%>
					
				</tr>
				<%
					}
						}//if
				%>
				<tr>
					<td><input type="hidden" name="<%=PARAM_ACTION%>"
						value="ACTION" /><input type="submit" value="Submit" />
					</td>
				</tr>
			</table>
		</form>
<%
			} // if errorMsg.length() > 0
%>
	</div>
<%
		} else { // !hasAccess
			out.println(HelpDeskSupportConfig.NO_ACCESS_HTML_MESSAGE);
		}
%>
</body>
</html>

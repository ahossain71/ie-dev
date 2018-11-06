<!DOCTYPE HTML>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% 	 // get the return url
	StringBuffer returnURLBuff = new StringBuffer().append(request.getServletContext().getContextPath());
	returnURLBuff.append("/submitHiosRequest.jsp");
	String returnURL = returnURLBuff.toString();
%>
<html>
	<head>
		<title>Retrieve Assister Result</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<div align="left" style="margin-top: 50px;margin-left: 25%">
			<h2>Retrieve Assister Result</h2>
		</div>

		<div align="left" style="margin-top: 25px;margin-left: 35%">		
			<br /> <br />
			<h3>Status</h3>
		</div>	

		<div align="left" style="margin-top: 20px;margin-left: 35%">
<%
	gov.cms.hios.AssisterResponse assisterResponse = (gov.cms.hios.AssisterResponse) request.getAttribute("assisterResponse");
	out.println(gov.cms.hios.service.HiosAssisterService.convertAssisterResponseToHTML(assisterResponse));
%>	
			<b>Status Code </b>${MLMSABCompletionResponse.statusCode.statusCode }
			<br />
			<b>Status Message </b>${MLMSABCompletionResponse.statusCode.statusMessage }
			<br/><a href="<%=returnURL %>">Return to Form</a>
		</div>
 
 		<div align="center" style="margin-top: 50px; font-size:60%">	
<%
	out.println("<br/>Server Name: " + request.getServerName()); 
	out.println("<br/>Context Path: " + request.getContextPath()); 
	out.println("<br/>request uri: " + request.getRequestURI()); 
	String requestURI = request.getRequestURI();
	out.println("<br/>request uri subsequence: " + requestURI.subSequence(1,requestURI.indexOf('/',2)));
	out.println("<br/>return url: " + returnURL); 
%>
		</div> 
	</body>
</html>

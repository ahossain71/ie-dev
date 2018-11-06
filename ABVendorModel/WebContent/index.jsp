<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page buffer="5kb" autoFlush="false" %>
<%@ page import="gov.cms.cciio.common.util.*"%>
<%@ page import="gov.cms.cciio.common.auth.Encrypter"%>
<%@ page import="com.saba.web.security.J2EESecurityHandler"%>
<%@ page import="gov.cms.cciio.common.auth.LoginLog"%>
<jsp:useBean id="loginBean"
	class="gov.cms.cciio.common.auth.MLMSLoginBean" scope="session" />
<jsp:useBean id="errorBean"
	class="gov.cms.cciio.common.auth.MLMSErrorBean" scope="session" />
	
<%
	StringBuffer errorString = new StringBuffer();

	String uid = null;
	String fname = null;
	String lname = null;
	String ismemberof = null;
	//String urlBase = System.getProperty("mlms.url");

	String errorURL = null;
	String loginURL = null;

	String isDev = request.getParameter("isDev");
	String MLMSDeepLink = request.getParameter("MLMSDeepLink");
	String FFMTraining = request.getParameter("FFMTraining");

	
	loginBean.setSessionId(request.getSession().getId());

	/*	if(isDev != null && isDev.trim().length() != 0){
	 loginURL =  "/Saba/ssoServlet";
	 errorURL =  "/Saba/errorPage.jsp";
	 } else {
	 if (urlBase == null || (urlBase.trim().length() == 0)) {
	 LoginLog.writeToErrorLog("ERROR!! mlms.url is not defined in the websphere jvm parameters");
	 loginURL =  "/Saba/ssoServlet";
	 errorURL =  "/Saba/errorPage.jsp";
	 } else {
	 loginURL = urlBase + "/Saba/ssoServlet";
	 errorURL = urlBase + "/Saba/errorPage.jsp";
	 }
	 }*/

	loginURL = "/Saba/ssoServlet";
	errorURL = "/Saba/errorPage.jsp";
	

	String error = "undefined";
	String groups = null;
	String password = "welcome";
	Boolean err = false;
	boolean inDev = false;

	//System.out.println(" isDev null"
	//+ (isDev == null || isDev.trim().length() == 0));
	/**
	 ** if this is not dev, get header values
	 ** confirm values are present and are not of zero length
	 **/
	if (isDev == null || isDev.trim().length() == 0) {

		uid = request.getHeader("uid");
		error = "1";
		//System.out.println(error);
		if (uid == null || uid.trim().length() == 0) {
			uid = null;

		} else {
			loginBean.setUsername(uid);
		}
		error = "1.1" + " uid = " + uid;
		fname = request.getHeader("fname");

		System.out.println(error);

		if (fname == null || fname.trim().length() == 0) {
			fname = null;
		} else {
			loginBean.setFname(fname);
		}
		error = "1.2" + " fname = " + fname;
		System.out.println(error);
		lname = request.getHeader("lname");

		if (lname == null || lname.trim().length() == 0) {
			lname = null;
		} else {
			loginBean.setLname(lname);
		}

		error = "1.3" + " lname " + lname;
		System.out.println(error);
		ismemberof = request.getHeader("ismemberof");
		if (ismemberof == null || ismemberof.trim().length() == 0) {
			ismemberof = null;

		}
		/**
		 ** more logic to determine group to set
		 **/
		error = "1.4" + " ismemberof = " + ismemberof;
		System.out.println(error);
		/***
		 ** if uid or ismember of == null, set err = true, redirect to error page
		 ** otherwise set values in login bean
		 **/
		if (uid == null || ismemberof == null) {
			/**
			 ** required parameters uid or ismemberof are null - set err = true for redirect
			 **/
			err = true;
			
			error = "one or more required headers is null";
			errorBean.setUidError(err);
			
			if (uid == null) {
				errorString.append(" uid is null <br>");
			} else {
				loginBean.setUsername(uid);
			}
			error = "1.5";
			////System.out.println(error);
			if (fname != null) {
				loginBean.setFname(fname);

			} else {

				loginBean.setFname("first name");
			}
			if (lname != null) {

				loginBean.setLname(lname);

			} else {
				loginBean.setFname("last name");
			}
			if (ismemberof == null) {
				errorString.append(" ismemberof is null");
				
			}
			error = "1.9" + " err value = " + err;
		}
		error = "2" + " err value = " + err;
		////System.out.println(error);
		/**
		 ** send to error URL because UID or ISMEMBEROF is null
		 **/
		if (err) {
			error = "2.2" + " err value = " + err + " redirecting to "
					+ errorURL;
			errorBean.setEidmHeaderError(err);
			errorBean.setErrorMsg(errorString.toString());
			response.setHeader("errorBean", errorBean);
		
		    response.sendRedirect(errorURL + "?errorBean=" + errorBean);
			
			/* reset error
			    to prevent illegal state exeception generated when one or more redirect statements are attempted
			 */

		}

		error = "2.5";
		loginBean.setDev(false);

	} else {
		////System.out.println("isDev is not null! " + isDev);
	}
	error = "3";
	////System.out.println(error);

	/**
	 ** if uid from header is null and this is dev, get required attributes from attributes
	 ** 
	 **/

	if (uid == null && isDev != null && isDev.trim().length() != 0
			&& isDev.equalsIgnoreCase("true")) {

		uid = request.getParameter("username");
		////System.out
		//	.println("3.5: Get username from development page attributes : "
		//			+ uid);
		if (uid != null) {
			loginBean.setUsername(uid);

		} else {
			/**
			 ** 
			 **/
			error = "4.1" + "dev uid is null err value = " + err;
			if (!err) {
				errorBean.setUidError(!err);
				response.sendRedirect(errorURL + "?errorBean=" + errorBean);
				
				err = true;
			}

		}

		error = "4";
		////System.out.println(error);
		if (fname == null || fname.trim().length() == 0) {
			fname = request.getParameter("fname");
			if (fname != null) {
				////System.out.println(" First name  " + fname);
				loginBean.setFname(fname);
			}
		}

		if (lname == null || lname.trim().length() == 0) {
			lname = request.getParameter("lname");
			if (lname != null) {
				////System.out.println(" Last name  " + lname);
				loginBean.setLname(lname);
			}
		}
		loginBean.setDev(true);
	}

	/**
	 * if ismember value from header isn't null determine what what role was set
	 * or if it's dev - 
	 **/
	error = "20" + " ismemberof " + ismemberof;
	if ((ismemberof != null && ismemberof.trim().length() != 0)
			|| isDev != null) {
		/**
		 ** if this came from dev get the value from the request paramat
		 **/
		if (isDev != null && isDev.equalsIgnoreCase("true")) {
			groups = request.getParameter("groups");
			groups = groups + ","
					+ request.getParameter("requiredRole");
			if (request.getParameter("password") != null
					&& request.getParameter("password").trim().length() > 0) {
				loginBean.setPassword(request.getParameter("password"));
			}
		} else {
			groups = ismemberof;
		}

		if (groups != null) {

			String tmpStr = groups.toLowerCase();
			error = "22" + " value of ismemberof = " + tmpStr;
			// check for training access role, value is agent
			/**
			 if user has FFM_TRAINING_ACCESS - assign the agent role
			 if user has agent set complete value to true
			 if user has FFM_ASSISTER - assignt the assister role
			 if user has MLMS_BUSINESS_OWNER or MLMS_ADMIN set the cciio role
			 **/
			if (tmpStr.indexOf(Constants.FFM_TRAINING_ACCESS) > -1) {

				loginBean.setRole(Constants.AGENT);

			} else if (tmpStr.indexOf(Constants.FFM_ASSISTER) > -1) {

				loginBean.setRole(Constants.ASSISTER);

			} else if (tmpStr.indexOf(Constants.MLMS_BUSINESS_OWNER) > -1) {

				loginBean.setRole(Constants.CCIIO);

			} else if (tmpStr.indexOf(Constants.MLMS_ADMIN) > -1) {

				loginBean.setRole(Constants.CCIIO);

			} else {

				if (!err) {
					errorBean.setMlmsGroupError(!err);
					errorBean.setErrorMsg(" No Groups received from headers, cannot proceed");
					response.sendRedirect(errorURL + "?errorBean=" + errorBean);
					err = true;
				}

			}

		}// groups != null
		/**
		Set flag if print certificate parameter &  was made.
		
		**/
		if (MLMSDeepLink != null) {
						
			loginBean.setCertReq(true);
		}
		if(isDev != null && FFMTraining != null){
		  loginBean.setComplete(true);
		}
		
		String strPrincipalName = null;
		try {

			strPrincipalName = J2EESecurityHandler
					.loginAsGuest(request);

		} catch (Throwable eThrowable) {
			eThrowable.printStackTrace();
			throw eThrowable;
		}
	}// ismemberof != null
	error = "30";
	//System.out.println(error);
	
	if (loginBean.isDev()) {
		System.out.println(" login url " + loginURL);
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>mlms login jsp</title>
</head>

<body onload="javascript:window.document.theForm.submit()"
	style="font-family: Verdana; font-size: 10pt;">

	<noscript>
		<p></p>
	</noscript>
	<form name="theForm" method="POST"
		action="<%=response.encodeURL(loginURL)%>"></form>
</body>

</html>
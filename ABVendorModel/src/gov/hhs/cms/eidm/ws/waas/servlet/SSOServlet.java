package gov.hhs.cms.eidm.ws.waas.servlet;

import gov.cms.cciio.common.auth.LoginLog;
import gov.cms.cciio.common.auth.MLMSErrorBean;
import gov.cms.cciio.common.auth.MLMSLoginBean;
import gov.cms.cciio.common.auth.User;
import gov.cms.cciio.common.exception.MLMSException;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.helper.UserProfileDAO;
import gov.hhs.cms.eidm.ws.waas.service.RetrieveAppDetailResponse;
import gov.hhs.cms.eidm.ws.waas.service.WaaSApplicationService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/SSOServlet")
public class SSOServlet extends HttpServlet {

	/**
	 * 
	 */
	//private String urlBase = System.getProperty("mlms.url");
	private static String errorURL = "/Saba/errorPage.jsp";
	private static final long serialVersionUID = 1L;
	private static String loginURL = "/Saba/Web/Cloud";
	static boolean isDebug = false;
	
	
	public void init(){
		
		isDebug = System.getProperty("mlms.debug","true").equalsIgnoreCase("true");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		/**
		 * <form name="theForm" method="POST"
		 * action="<%=response.encodeURL(loginURL)%>"> <!-- uncomment at some
		 * point --> <input type="hidden" name="j_security_check" value="true">
		 * <input type="hidden" name="j_username" value="<%=uid%>"> <!-- input
		 * type="hidden" name="j_password"
		 * value="<%=Encrypter.encrypt(password)%>" --> <input type="hidden"
		 * name="j_password" value="<%=password%>">
		 * 
		 * <!-- Is the following really needed? Exiting code returns empty
		 * string anyway <input type="hidden" name="usernameprefix"
		 * value="%=strUsernamePrefix%"> --> </form>
		 */
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String methodName = "doPost";
		
		MLMSLoginBean loginBean = (MLMSLoginBean) request.getSession()
				.getAttribute("loginBean");
		request.getSession().removeAttribute("loginBean");
		
		/**
		 * String username = request.getParameter("username"); String uid =
		 * username;
		 **/
		
		

		

		if (loginBean!= null && loginBean.getUsername() != null) {
			  
				loginBean = 
						  retrieveEIDMUserProfile(loginBean);
		
			try {
				if(request != null && response != null ){
				loginBean = processLoginBean(request, response, loginBean);
				StringBuilder sb = getPostContent(loginBean);
				response.setContentType("text/html");
				PrintWriter writer = response.getWriter();

				writer.println(sb.toString());
				writer.flush();
			
				} else {
					LoginLog.writeToErrorLog(SSOServlet.class.getName()+ " " + methodName +  " Request or Response null");
				}
				

			} catch (IOException e) {
				
				LoginLog.writeToErrorLog(e.getMessage());
				LoginLog.writeToErrorLog("Stack Track " + e.getStackTrace());
			} catch(Exception e){
				LoginLog.writeToErrorLog(e.getMessage());
			}
			
		} else {
			
		}

	}
	/**
	 * 
	 * @param loginBean
	 * @return
	 * @throws MalformedURLException 
	 */

	public static MLMSLoginBean retrieveEIDMUserProfile(
			MLMSLoginBean loginBean)  {
		String methodName = "retrieveEIDMUserProfile";
		WaaSApplicationService service = new WaaSApplicationService();
		RetrieveAppDetailsResponseType appDetails = null;
		/**
		 * Make the web service call
		 */
		
		
		RetrieveAppDetailResponse appResponse = new RetrieveAppDetailResponse() ;
		try {
			
			appResponse.setResponseType(service
					.retrieveApplicationDetails(loginBean.getUsername()));
		} catch (MalformedURLException e) {
			LoginLog.writeToErrorLog(SSOServlet.class.getName() + " " + methodName + " MalformedURLException thrown on web service client call, check JVM properties for WaaS Client URl");
		} catch (RetrieveAppDetailsFault e) {
			// TODO Auto-generated catch block
			LoginLog.writeToErrorLog(SSOServlet.class.getName() + " RetrieveAppDetailsFault thrown on web service client call" + e.getMessage());
			
		}
		
		/**
		 * if a response returns from the web service parse the response and
		 * populate the loginBean
		 */
		UserProfileDAO userProfileDAO = new UserProfileDAO();
		if (appResponse.getResponseType() != null) {
			appDetails = appResponse.getResponseType();
			
			try {
				loginBean = userProfileDAO.updateProfileData(appDetails, loginBean);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RetrieveAppDetailsFault faultDetails = appResponse.getFaultType();
			if (faultDetails != null) {
				LoginLog.writeToErrorLog("FWaaSClient: fault Details Message : "
						+ faultDetails.getMessage());
			}

		} else {
			LoginLog.writeToErrorLog("WaaSClient:  appResponse is null");
		} // appResponse

		return loginBean;

	}

	
	public static StringBuilder getPostContent(MLMSLoginBean loginBean)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><head>	<title>MLMS Custom Login</title>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>");
		sb.append("</head><body onload=\"javascript:window.document.theForm.submit()\"	style=\"font-family: Verdana; font-size: 10pt;\">");
		sb.append("<noscript><p></p></noscript><form name=\"theForm\" method=\"POST\" action=\"");
		sb.append(loginBean.getLoginUrl());
		sb.append("\">");
		sb.append("<input type=\"hidden\" name=\"j_security_check\" value=\"true\">");
		sb.append("<input type=\"hidden\" name=\"j_username\" value=\"");
		sb.append(loginBean.getUsername());
		sb.append("\">");
		
		sb.append("<input type=\"hidden\" name=\"j_password\" value=\"");
		
		sb.append(loginBean.getPassword());
		sb.append("\">");
		sb.append("</form></body></html>");
		LoginLog.writeToErrorLog(sb.toString());
		return sb;
	}

	public static String getUrl(HttpServletRequest request, String newPath) {
		StringBuffer sb = new StringBuffer();
		sb.append(request.getScheme());
		sb.append("://");
		sb.append(request.getServerName());
		sb.append(":");
		sb.append(request.getServerPort());

		sb.append(newPath);
		//LoginLog.writeToErrorLog("SSOServlet POST URL " + sb.toString());
		return sb.toString();

	}

	public static MLMSLoginBean processLoginBean(HttpServletRequest request,
			HttpServletResponse response, MLMSLoginBean loginBean) throws IOException {
		
		User.LoginResult loginResult = null;
		String error = null;
		MLMSErrorBean errorBean = null;
		boolean err = false;
		error = "41";
		//System.out.println(error);
		

		if (loginBean.getUsername() != null && loginBean.getRole() != null) {
			/**
			 ** first name and last name cannot be null (should be set by
			 * webservice call) if null set
			 **/
			error = "42";
			//System.out.println(error);
			/**LoginLog.writeToErrorLog("USER.PROCESSLOGINBEAN username " + loginBean.getUsername());
			LoginLog.writeToErrorLog("USER.PROCESSLOGINBEAN first name " + loginBean.getFname());
			LoginLog.writeToErrorLog("USER.PROCESSLOGINBEAN last name " + loginBean.getLname());
			LoginLog.writeToErrorLog("USER.PROCESSLOGINBEAN role name " + loginBean.getRole());
			LoginLog.writeToErrorLog("USER.PROCESSLOGINBEAN completed training " + loginBean.isComplete());
			**/
			loginResult = User.lmsLogin(loginBean);
			if (loginResult != null) {
				MLMSException me = loginResult.getLoginException();
				if(me != null){
					 errorBean = new MLMSErrorBean();
					 errorBean.setGeneralError(true);
					 errorBean.setErrorMsg(me.getMessage());
					 
				}
			} else {
				System.out.println("Login Failed");
			}
		} else {

			error = "44" + " redirecting to error page ";
			try {
				request.getSession().setAttribute("errorBean", errorBean);
				RequestDispatcher rd = request.getRequestDispatcher("/Saba/errorPage.jsp");
				rd.forward(request, response);
				//response.sendRedirect(errorURL);
				
				//request.setAttribute("errorBean", errorBean);
				//request.getRequestDispatcher(errorURL).forward(request,response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			err = true;

		}
		error = "50";
		

		if (loginResult != null && loginResult.isSuccess()) {
			if (loginResult.getLandingPageUrl() != null) {
				
				
				
				if (request.getHeader("Host").indexOf("localhost") == -1 || loginBean.isCertReq()) {
					loginURL = loginResult.getLandingPageUrl();
					loginBean.setLoginUrl(loginURL);
					
				} 
				error = "SSOServlet 606 landing page URL " + loginURL;
				//System.out.println(error);
				LoginLog.writeToErrorLog( error + " Logging in user " + loginBean.getUsername());
			}
			LoginLog.writeToErrorLog(error);
		} else {
			
				error = "An unexpected error has occurred during the login process, please notify the MLMS Helpdesk "  ;
			
			
			LoginLog.writeToErrorLog(error);

			if (!err) {

				//response.sendRedirect(errorURL + "?generalError=Login Failed "
				//		+ error);
				errorBean.setErrorMsg("Login Failed " + error);
				err = true;
				request.getSession().setAttribute("errorBean", errorBean);
				RequestDispatcher rd = request.getRequestDispatcher("/Saba/errorPage.jsp");
				try {
					rd.forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		error = "70";
		

		return loginBean;
	}
}
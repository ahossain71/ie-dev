package gov.cms.cciio.common.util;
//TODO - TO BE UPDATED FOR SABA 7, This is still used?

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.cms.cciio.common.auth.Encrypter;
import gov.cms.cciio.common.exception.DBUtilException;

import com.saba.exception.SabaException;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.party.PartyManager;
import com.saba.persist.ConnectionManager;
import com.saba.security.SabaLogin;
import com.saba.security.SabaPrincipal;

/*
 * 2010-07-21   Feilung Wong    Copied from 5.3 code: mil.army.dls.util.ScriptUtil.java
 * 2011-05-09   Feilung Wong    Using new DBUtil methods to better log and release DB resources
 * 2011-05-13   Feilung Wong    removed reference to DBUtil.queryDatabase
 * 2011-10-03   Feilung Wong    Removed reference to CommonUtil.getDateTime()
 */

/**
 * Helper class for creating JSP scripts.  These scripts can be used for functional purposes
 * (i.e. Data Migration) or for testing in DEV.  Any changes to JSP scripts can be immediately
 * updated in DEV without a re-deploy or downtime. 
 * 
 * Sample Usage of ScriptUtil to retrieve a user's UIC value:
 * 
 * <%
 * <!-- Use this section for Imports ->
 * @page
 *	import="com.saba.customattribute.ICustomAttributeValueDetail,
 *			com.saba.exception.SabaException,
 *			com.saba.party.PartyManager,
 *			com.saba.party.person.Employee,
 * 			com.saba.party.person.EmployeeDetail,
 *			com.saba.persist.ObjectId,
 *			java.sql.ResultSet,
 *			java.sql.SQLException,
 *			mil.army.dls.util.ScriptUtil;"
 * %>
 *	<%!
 *	
 *	//Use this section for declaring global variables, and defining your methods
 *	ScriptUtil testUtil;
 *	PartyManager partyManager;
 *
 *	public void getUICs() {
 *		try {
 *			ResultSet rSet = testUtil
 *					.queryDatabase("select id from CMT_Person where ss_no='100001319'");
 *			if (rSet == null) {
 *				System.out.println("Result Set returned null");
 *			}
 *			if (rSet != null) {
 *				while (rSet.next()) {
 *					
 *					String userId = rSet.getString(1);
 *					testUtil.writeToDebugLog("User id = " + userId);
 *					Employee emp = partyManager.findEmployeeByKey(new ObjectId(userId));
 *					EmployeeDetail empDetail = partyManager.getDetail(emp);
 *	
 *					ICustomAttributeValueDetail uicDetail = empDetail.getCustomValue(ICustomAttributeValueDetail.kCustom4);
 *	
 *					if (uicDetail == null) {
 *						testUtil.writeToDebugLog("User " + empDetail.getSsNo() + "s UIC returned null");
 *					} else {
 *						testUtil.writeToDebugLog("User " + empDetail.getSsNo() + "s UIC is " + uicDetail.toString());
 *					}
 *				}
 *	 		} else {
 *				testUtil.writeToErrorLog("ERROR: Null Result Set");
 *			}
 *			testUtil.closeResultSet(rSet);
 *		} catch (SabaException e) {
 *			testUtil.printException(e);
 *		} catch (SQLException e) {
 *			testUtil.printException(e);
 *		}
 *	}
 *%>	
 *<%
 *		<!-- This section is where code is first called from. ->
 *		<!-- Use this section like your main() ->
 *
 *		<!-- By construction new ScriptUtil, we are opening logs for writing, opening DB, and Authenticating with Saba ->
 *		testUtil = new ScriptUtil();
 *
 *		partyManager = testUtil.getPartyManager();
 *
 *		getUICs();
 *
 *		<!-- Make sure you call close().  This will close logs, close DB, and logout of Saba ->
 *		testUtil.close();
 *%>
 *<!-- Below section is where we display the HTML message -->
 *<%=testUtil.getHTMLMessage()%>
 *
 *
 *
 * @author rpcino
 *
 */
public class ScriptUtil {
    /** Creating className instance */
    private static final String className = ScriptUtil.class.getName();
    /** Creating logger */
    private static final Logger logger = Logger.getLogger(className);  

	/** SABA Authentication */
	private static final String SABA_USER = "atrrs";
	
	/** Whether or not to print debug messages to debug log */
	private static boolean DEBUG = true;
	

	/** Global Variables */
	private StringBuilder errString = new StringBuilder();
	private StringBuilder infoString = new StringBuilder();
	
	private ServiceLocator locator = null;
	private PartyManager partyManager = null;
	
	private Connection conn = null;
	
	private String htmlTitle = "Test Script";
	private int errMark = 0;
	
	private static final String HTML_SEPARATOR = "<br />";
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMM, d yyyy HH:mm:ss:SSSS");
    
    private ArrayList<String> fullString = new ArrayList<String>();
    
    private static final String ERROR_STRING = "ERROR:";
    private static final String DEBUG_STRING = "DEBUG:";

	
	/** 
	 * Constructor call opens Logs, Database, and authenticates with Saba
	 */
	public ScriptUtil() throws DBUtilException {
		this.initialize();
	}
	
	/**
	 * Open Debug and error logs, Authenticate with Saba, and open a database connection
	 *
	 */
	private void initialize() throws DBUtilException {
		/** Open logs for debugging */
		initiateSaba();
		openDatabase();
	}
	
	/**
	 * This method should always be called at the end of the script.
	 * This closes the logs, the Database connections, and logs out of Saba
	 *
	 */
	public void close() throws DBUtilException {
		closeDatabase();
		SabaLogin.logout();
	}
	
	/**
	 * Writes debug message to /app/bea/user_projects/domains/DLS/lms_logs 
	 * and prints the message on the HTML page.
	 * if DEBUG is true.
	 * @param line - Message to be written to debug logs
	 */
	public void writeToDebugLog(String line) {
		if (DEBUG) {
			logger.log(Level.INFO,line);
			infoString.append(DATE_FORMATTER.format(new java.util.Date()));
			infoString.append("   ");
			infoString.append(line);
			infoString.append(HTML_SEPARATOR);
			
			fullString.add(DATE_FORMATTER.format(new java.util.Date() + 
					"   " +
					DEBUG_STRING + 
					line));
			fullString.add(HTML_SEPARATOR);
			
		}
	}
	
	/**
	 * Writes error message to /app/bea/user_projects/domains/DLS/lms_logs
 	 * and prints the error message
	 * on the HTML page.
	 * @param line - Error message to be written to error log
	 */
	public void writeToErrorLog(String line) {
		logger.log(Level.SEVERE, line);
		errString.append(DATE_FORMATTER.format(new java.util.Date()));
		errString.append("  ");
		errString.append(line);
		errString.append(HTML_SEPARATOR);

		fullString.add(DATE_FORMATTER.format(new java.util.Date() + 
				"   " +
				ERROR_STRING + 
				line));
		fullString.add(HTML_SEPARATOR);
	}
	
	/**
	 * Retrieve Info String and Error String as one String Buffer, ordered by the time 
	 * the messages were logged
	 * @return - Debug and Error Strings
	 */
	public StringBuilder getFullString() {
		StringBuilder fullStringBuilder = new StringBuilder();
		for (int i = 0; i < fullString.size(); i++) {
			String string  = fullString.get(i);
			fullStringBuilder.append(string);
		}
		return fullStringBuilder;
	}
	
	/**
	 * Debug String plus Error String ordered together.  Returned in HTML format for display
	 * on HTML pages
	 * @return String in HTML format
	 */
	public String printFullString() {
		StringBuilder fullStringBuilder = new StringBuilder();
		
		for (int i = 0; i < fullString.size(); i++) {
			String string  = fullString.get(i);
			if (string.indexOf(DEBUG_STRING) >= 0) {
				/** This is a debug message.  Use Color green */
				fullStringBuilder.append("<p><font color=\"green\">");
				fullStringBuilder.append(string);
				fullStringBuilder.append("</p>");
			} else if (string.equals(HTML_SEPARATOR)){
				/** This is the newline. Don't need to add anything around this message */
				fullStringBuilder.append(string);
			}
				/** This is an error message.  Use Color red*/
				fullStringBuilder.append("<p><font color=\"red\">");
				fullStringBuilder.append(string);
				fullStringBuilder.append("</p>");
				
			}
		
		return fullStringBuilder.toString();
	}

	/**
	 * Writes the Exception to /app/bea/user_projects/domains/DLS/lms_logs
	 * and to the HTML page
	 * @param ex The Exception to be printed
	 */
	public void printException(Exception ex) {
		String exceptionString = CommonUtil.stackTraceToString(ex);
		String exceptionHTMLString = CommonUtil.stackTraceToHTMLString(ex);
		logger.log(Level.SEVERE, exceptionString);
		errString.append(exceptionHTMLString);
		errString.append(HTML_SEPARATOR);
	}
	
	/**
	 * Authenticates with saba and retrieves the Party Manager
	 *
	 */
	private void initiateSaba() {

		try {
			String password = "";
				password = Encrypter.encrypt(SABA_USER);
//			} else {
				/** This code is commented out for connecting to local Saba */
//				password = "welcome";
//			}
			locator = SabaLogin.authenticate(SABA_USER, password);
			if (locator == null) {
				logger.log(Level.SEVERE, "Locator is null");
				logger.log(Level.INFO, "Client Instance is " + ServiceLocator.getClientInstance());
			} else {
				ConnectionManager cmTest = locator.getConnectionManager();
				logger.log(Level.FINER,"ConnectionManager = " + cmTest);
				if (cmTest != null) {
					logger.log(Level.FINER, "Connection manager site name = " + cmTest.getSiteName());
				}
				SabaPrincipal principal = locator.getSabaPrincipal();
				logger.log(Level.FINER,"SabaPrincipal = " + principal);
				if (principal != null) {
					logger.log(Level.FINER,"Principal name = " + principal.getName());
					logger.log(Level.FINER,"Principal site name = " + principal.getSiteName());
				}
			}
			writeToDebugLog("Authenticated with Saba");
			errMark = 10;
			partyManager = (PartyManager) locator
			.getManager(Delegates.kPartyManager);
			writeToDebugLog("Retrieved Party Manager from Saba");

		} catch (SabaException e) {
			e.printStackTrace();
			if (errMark < 10) {
				writeToErrorLog("Error Authenticating with Saba");
				printException(e);
			} else {
				writeToErrorLog("Error retrieving Party Manager from Saba");
				printException(e);
			}

		}
	}

	/**
	 * Queries Database with given SQL Statement.
	 * Make sure to close the returned ResultSet with 
	 * ScriptUtil.closeResultSet(ResultSet);
	 * @param sqlStatement - SQL Statement for querying database
	 * @return ResultSet returned from the Database.  
	 */
	public ResultSet queryDatabase(String sqlStatement) throws DBUtilException {
		ResultSet rSet = null;

		StringBuilder strBuf = new StringBuilder(sqlStatement);
		try {
			Statement statement = conn.createStatement();
			if (statement.execute(strBuf.toString())) {
				rSet = statement.getResultSet();
			}
		} catch (SQLException e) {
			printException(e);
		}

		return rSet;
	}
	
	/**
	 * Clean up Statement by closing the ResultSet returned by queryDatabase.
	 * Should always be called after done using ResultSet from queryDatabase
	 * Calling this on a ResultSet will not allow use of ResultSet anymore.
	 * @param rSet - ResultSet 
	 * @return true if ResultSet was closed successfully.
	 */
	public boolean closeResultSet(ResultSet rSet) {
		boolean success = false;
		try {
			if (rSet != null) {
				Statement statement = rSet.getStatement();
				if (statement != null) {
					statement.close();
					success = true;
				}
			}
		} catch (SQLException e) {
			printException(e);
		}
		return success;
	}

	/**
	 * Open Database connection.  ScriptUtil.close() should be called
	 * at end of script.  ScriptUtil.close() call is necessary to close the Database
	 * connection created by this call.
	 * @throws gov.cms.cciio.common.exception.DBUtilException 
	 */
	private void openDatabase() throws  gov.cms.cciio.common.exception.DBUtilException {
		conn = DBUtil.getConnection(locator, "ScriptUtil.openDatabase");
	}

	/** 
	 * Close the Database connection
	 *
	 */
	private void closeDatabase() throws DBUtilException {
		DBUtil.freeDBResources(locator, null, null, conn, "ScriptUtil.closeDatabase");
	}
	
	/**
	 * Use this call to retrieve the HTML HEAD for displaying on HTML page
	 * @return The entire HTML Head string used for debug purposes.
	 */
	public StringBuilder getHTMLHead() {
		StringBuilder htmlHead = new StringBuilder();
		
		htmlHead.append("<head>");
		htmlHead.append('\n');
		htmlHead.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\">");
		htmlHead.append('\n');
		htmlHead.append("<meta name=\"robots\" content=\"noindex\">");
		htmlHead.append('\n');
		htmlHead.append("<meta http-equiv=\"expires\" content=\"0\">");
		htmlHead.append('\n');
		htmlHead.append("<meta http-equiv=\"pragma\" content=\"no-cache\">");
		htmlHead.append('\n');
		htmlHead.append("<style type=\"text/css\">");
		htmlHead.append('\n');
		htmlHead.append(".button { background-color: #FFFF00;");
		htmlHead.append('\n');
		htmlHead.append("color: #000000;");
		htmlHead.append('\n');
		htmlHead.append("border: 2px outset #EF9C00; }");
		htmlHead.append('\n');
		htmlHead.append(".button:Hover { color: #444444; }");
		htmlHead.append('\n');
		htmlHead.append(".normal { font-family:Verdana, Arial, Helvetica, sans-serif;");
		htmlHead.append('\n');
		htmlHead.append("font-size: 7pt;");
		htmlHead.append('\n');
		htmlHead.append("color: #FFF5E8; }");
		htmlHead.append('\n');
		htmlHead.append(".header { font-family:Verdana, Arial, Helvetica, sans-serif;");
		htmlHead.append('\n');
		htmlHead.append("font-size: 10pt;");
		htmlHead.append('\n');
		htmlHead.append("color: White; }");
		htmlHead.append('\n');
		htmlHead.append(".formErrorMessage{color:#FF0000; background-color:#ffffff;}");
		htmlHead.append('\n');
		htmlHead.append("table.attriblist { background-color:#8d8d8d; color:#000000;");
		htmlHead.append('\n');
		htmlHead.append("width:100%;");
		htmlHead.append('\n');
		htmlHead.append("border:3px solid White; }");
		htmlHead.append('\n');
		htmlHead.append("th { background-color:#003312;");
		htmlHead.append('\n');
		htmlHead.append("font-size: 10pt;");
		htmlHead.append('\n');
		htmlHead.append("color:white;");
		htmlHead.append('\n');
		htmlHead.append("}");
		htmlHead.append('\n');

		htmlHead.append("tr.mouseout { background-color:White; }");
		htmlHead.append('\n');
		htmlHead.append("tr.mouseout td {border:1px solid White;}");
		htmlHead.append('\n');


		htmlHead.append("BODY { font-family:Verdana, Arial, Helvetica, sans-serif;");
		htmlHead.append('\n');
		htmlHead.append("font-size: 8pt;");
		htmlHead.append('\n');
		htmlHead.append("color: Black;");
		htmlHead.append('\n');
		htmlHead.append("background-color:white;");
		htmlHead.append('\n');
		htmlHead.append("}");
		htmlHead.append('\n');
		htmlHead.append("</style>");
		htmlHead.append('\n');
		htmlHead.append("<title>" + htmlTitle + "</title>");
		htmlHead.append('\n');
		htmlHead.append("</head>");
		htmlHead.append('\n');

		return htmlHead;
	}

	/**
	 * Use this call to retrieve the HTML BODY for displaying on HTML page
	 * @return The entire HTML Body string used for debug purposes.
	 */
	public StringBuilder getHTMLBody() {
		StringBuilder htmlBody = new StringBuilder();

		htmlBody.append("<body>");
		htmlBody.append("<p><font color=\"green\">" + infoString.toString() + "</font></p>");
		htmlBody.append("<p><font color=\"red\">" + errString.toString() + "</font></p>");
		htmlBody.append("</body>");
		
		return htmlBody;
	}

	/**
	 * Returns the entire HTML message, including Head, Body, Info String, and Error String
	 * @return - Entire HTML String
	 */
	private StringBuilder getHTMLMessageSB() {
		StringBuilder htmlMessage = new StringBuilder();
		htmlMessage.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		htmlMessage.append('\n');
		htmlMessage.append("<html>");
		htmlMessage.append('\n');
		htmlMessage.append(getHTMLHead());
		htmlMessage.append('\n');
		htmlMessage.append(getHTMLBody());
		htmlMessage.append('\n');
		htmlMessage.append("</html>");
		htmlMessage.append('\n');
		return htmlMessage;
	}
	

	/**
	 * Use this call to retrieve the entire HTML message for displaying on HTML page.
	 * This appends HTML HEAD and HTML BODY together for one call.
	 * In script an example call would look like:
	 * <%=scriptUtil.getHTMLMessage()%>
	 * @return The entire HTML Head string used for debug purposes.
	 */
	public String getHTMLMessage() {
		return getHTMLMessageSB().toString();
	}

	/**
	 * Retrieve DEBUG mode
	 * @return true, if DEBUG mode is set, and messages are printed to /app/bea/user_projects/domains/DLS/lms_logs
	 * and to the HTML page.
	 */
	public static boolean isDEBUG() {
		return DEBUG;
	}

	/**
	 * Sets DEBUG mode
	 * @param debug.  If true, messages will be printed to /app/bea/user_projects/domains/DLS/lms_logs
	 * and to the HTML page.
	 */
	public static void setDEBUG(boolean debug) {
		DEBUG = debug;
	}

	/**
	 * Retrieve the title that will be displayed on the HTML page.
	*/
	public String getHtmlTitle() {
		return htmlTitle;
	}

	/**
	 * Set the HTML Title to be displayed on the HTML page
	 * Default: Test Script
	 * @param htmlTitle
	 */
	public void setHtmlTitle(String htmlTitle) {
		this.htmlTitle = htmlTitle;
	}

	/** 
	 * Retrieve the Saba Service Locator
	 * @return
	 */
	public ServiceLocator getLocator() {
		return locator;
	}

	/**
	 * Retrieve the Saba Party Manager for making most Saba API calls.
	 * @return
	 */
	public PartyManager getPartyManager() {
		return partyManager;
	}
	
	/**
	 * Retrieve the Debug String formed by ScriptUtil.writeToDebugLog
	 * @return
	 */
	public StringBuilder getInfoString() {
		return infoString;
	}
	
	/**
	 * Retrieve the Error String formed by ScriptUtil.writeToErrorLog
	 * @return
	 */
	public StringBuilder getErrorString() {
		return errString;
	}
}
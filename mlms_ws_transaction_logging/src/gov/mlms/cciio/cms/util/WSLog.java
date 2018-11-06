package gov.mlms.cciio.cms.util;

import gov.mlms.cciio.cms.exception.DBUtilException;
import gov.mlms.cciio.cms.exception.MLMSStatusCode;
import gov.mlms.cciio.cms.exception.WSLogException;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class WSLog {
	
    private static final String className = WSLog.class.getName();
    private static final Logger logger = Logger.getLogger(className);

    public static final String EOL = System.getProperty("line.separator");

	public static final String LMSDB_JNDI = "jdbc/lms";
	
	public static final String SQL_LOG_WS = "INSERT INTO AT_MLMS_WS_TRANS_LOG(ID,WEB_SERVICE_ID,TRANSACTION_CODE,ACTION_DATE,PARTNER_ID,STATUS_CODE,DESCRIPTION,XML,XML_CLOB) " +
			"VALUES ('wslog' || LPAD(at_mlms_ws_trans_seq.nextval,15,'0'),?,?,SYSDATE,?,?,?,?,?)";
	
    public static final String NO_ACCESS_HTML_MESSAGE = "<p><h2>You do not have permissions to access this page. Please contact the ALMS support team for access.</h2></p>";
    public static final String NOT_AVAILABLE_IN_PROD = "<p><h2>This Page is not available in PRODUCTION mode.</h2></p>";

    /**
     * Converts a string for proper display with an HTML tag
     * @param message - the string that needs to be escaped
     */
    public static String htmlEscape(String message) {
        if (message == null) {
            return "";
        }
        return message.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>");//.replaceAll("\n", "&#10;");
    }

    /**
     * Checks for SQL warnings and append to StringBuilder
     * @param ex the SQL Exception that should be logged
     * @param sb the StringBuilder that the warning, if any, should be appended to.
     * @throws SQLException
     */
    public static void appendError(SQLException ex, StringBuilder sb) {
        if (ex != null) {
            sb.append("\n*** SQLException caught ***\n");
            while (ex != null) {
                sb.append("SQLState: " + ex.getSQLState() + "\n"
                    + "Message:  " + ex.getMessage() + "\n"
                    + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                ex = ex.getNextException();
            }
        }
    }

    /**
     * Checks for SQL warnings and append to StringBuilder
     * @param conn the SQL ResultSet that should check for warnings
     * @param sb the StringBuilder that the warning, if any, should be appended to.
     * @throws SQLException
     */
    public static void appendWarning(ResultSet rSet, StringBuilder sb) throws SQLException {
        SQLWarning wSQL = rSet.getWarnings();
        if (wSQL != null) {
            sb.append("\n*** ResultSet Warnings caught ***\n");
            while (wSQL != null) {
                sb.append("SQLState: " + wSQL.getSQLState() + "\n"
                    + "Message:  " + wSQL.getMessage() + "\n"
                    + "Vendor:   " + wSQL.getErrorCode()+ "\n \n");
                wSQL = wSQL.getNextWarning();
            }
        }
    }

    /**
     * Checks for SQL warnings and append to StringBuilder
     * @param conn the SQL Connection that should check for warnings
     * @param sb the StringBuilder that the warning, if any, should be appended to.
     * @throws SQLException
     */
    public static void appendWarning(Connection conn, StringBuilder sb) throws SQLException {
        SQLWarning wSQL = conn.getWarnings();
        if (wSQL != null) {
            sb.append("\n*** Connection Warnings caught ***\n");
            while (wSQL != null) {
                sb.append("SQLState: " + wSQL.getSQLState() + "\n"
                    + "Message:  " + wSQL.getMessage() + "\n"
                    + "Vendor:   " + wSQL.getErrorCode()+ "\n \n");
                wSQL = wSQL.getNextWarning();
            }
        }
    }

    /**
     * Checks for SQL warnings and append to StringBuilder
     * @param statement the SQL Statement that should check for warnings
     * @param sb the StringBuilder that the warning, if any, should be appended to.
     * @throws SQLException
     */
    public static void appendWarning(Statement statement, StringBuilder sb) throws SQLException {
        SQLWarning wSQL = statement.getWarnings();
        if (wSQL != null) {
            sb.append("\n*** Statement Warnings caught ***\n");
            while (wSQL != null) {
                sb.append("SQLState: " + wSQL.getSQLState() + "\n"
                    + "Message:  " + wSQL.getMessage() + "\n"
                    + "Vendor:   " + wSQL.getErrorCode()+ "\n \n");
                wSQL = wSQL.getNextWarning();
            }
        }
    }

    /**
     * Obtain connection to DB defined in console
     * @param jndi the JNDI string to lookup data source
     * @param source - a String identifying the caller for logging purposes
     * @param sb the StringBuilder that the error, if any, should be appended to. Can be null
     * @return a connection to the DB, null if unsuccessful
     */
    protected static Connection getDBConnection(String jndi, String source, StringBuilder sb) throws DBUtilException {
        InitialContext ctx = null;
        DataSource ds = null;
        Connection conn = null;
        
        try {
            ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup(jndi);
            conn = ds.getConnection();
        } catch (Exception ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** Exception caught ***\n");
                sb.append(stackTraceToString(ex));
            }
            StringBuilder errMsg = new StringBuilder(source);
            errMsg.append("> WSLog.getConnection:");
            errMsg.append(EOL);
            errMsg.append(ex.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), ex);
            throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(), ex);            
        }
        return conn;
    }

    /**
     * Obtain connection to LMS DB defined in console - expecting tp2_read1 connection
     * @param source - a String identifying the caller for logging purposes
     * @param sb the StringBuilder that the error, if any, should be appended to. Can be null
     * @return a connection to the LMS DB, null if unsuccessful
     */
    public static Connection getLMSDBConnection(String source, StringBuilder sb) throws DBUtilException {
        return getDBConnection(LMSDB_JNDI, source, sb);
    }
    
    /**
     * Close ResultSet, Statement, and DB Connection.
     * @param rs - the ResultSet to close, can be null
     * @param statement - java.sql.Statement object to close, can be null
     * @param conn - Connection to close, can be null
     * @param source - a String identifying the caller for logging purposes
     * @param sb the StringBuilder that the error, if any, should be appended to. Can be null
     */
    public static void freeDBResources(ResultSet rs, Statement statement, Connection conn, String source, StringBuilder sb) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            logger.log(Level.WARNING, source + " > Utilitlies.freeDBResources: Error closing result set: " + ex);
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            logger.log(Level.WARNING, source + " > Utilitlies.freeDBResources: Error closing JDBC statement: " + ex);
            ex.printStackTrace(System.err);
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            logger.log(Level.WARNING, source + " > Utilitlies.freeDBResources: Error freeing connection: " + ex);
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Get stack trace of an exception in a String, default to system EOL
     * @param ex The Exception to be converted
     */
    public static String stackTraceToString(Exception ex) {
        return stackTraceToString(ex, EOL);
    }
    
    /**
     * Get stack trace of an exception in a String
     * @param ex The Exception to be converted
     * @param eol End of Line string (e.g. \n in Unix, \r\n in Windows, <br/> for HTML etc
     */
    public static String stackTraceToString(Exception ex, String eol) {
        StringBuilder sb = new StringBuilder();
        int i;

        sb.append(ex.toString());
        sb.append(eol);
        StackTraceElement[] st = ex.getStackTrace();
        for (i = 0; i < st.length; i++) {
            sb.append(st[i].toString());
            sb.append(eol);
        }
        Throwable cause = ex.getCause();
        if (cause != null) {
            sb.append("Caused by");
            sb.append(eol);
            sb.append(cause.toString());
            sb.append(eol);
            st = cause.getStackTrace();
            for (i = 0; i < st.length; i++) {
                sb.append(st[i].toString());
                sb.append(eol);
            }
        }
        return sb.toString();
    }
    
    /**
     * This is NOT a transaction table, just appending to a rolling log
     * @param webserviceID
     * @param transCode
     * @param partnerID
     * @param statusCode
     * @param description - optional, log message, can be null
     * @param xml - optional, can be null
     * @throws WSLogException
     */
	public static void logWebService(String webserviceID, String transCode, String partnerID, String statusCode, String description, String xml) throws WSLogException {
		Connection conn = null;
		try{
			conn = getLMSDBConnection("WSLog.logWebService", null);
			logWebService(conn, webserviceID, transCode, partnerID, statusCode, description, xml);
		} catch(DBUtilException ex) {
			StringBuilder sb = new StringBuilder("Cannot obtain connection to log WS: ");
			sb.append(ex.getMessage());
			sb.append(": -1");
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(transCode);
			sb.append(EOL);
			sb.append(new GregorianCalendar());
			sb.append(EOL);
			sb.append(partnerID);
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(description);
			sb.append(EOL);
			sb.append(xml);
			throw new WSLogException(MLMSStatusCode.NUM_DB_GENERAL, sb.toString(), ex);
		} finally {
			freeDBResources(null, null, conn, "WSLog.logWebService", null);
		}
	}
    
    /**
     * This is NOT a transaction table, just appending to a rolling log
     * If a separate/new Connection is not required, reusing an already open connection improves performance
     * @param conn use the given connection to log
     * @param webserviceID
     * @param transCode
     * @param partnerID
     * @param statusCode
     * @param description - optional, can be null
     * @param xml - optional, can be null
     * @throws WSLogException
     */
	public static void logWebService(Connection conn, String webserviceID, String transCode, String partnerID, String statusCode, String description, String xml) throws WSLogException {
		PreparedStatement statement = null;
		int rowsUpdated = 0;
		String trimmedXML = xml;
		
		if(webserviceID==null || webserviceID.isEmpty()){
			throw new WSLogException(MLMSStatusCode.NUM_GEN_VALIDATION_ERROR, "webserviceID cannot be null or an empty string");
		}
		if(transCode==null || transCode.isEmpty()){
			throw new WSLogException(MLMSStatusCode.NUM_GEN_VALIDATION_ERROR, "transCode cannot be null or an empty string");
		}
		if(partnerID==null || partnerID.isEmpty()){
			throw new WSLogException(MLMSStatusCode.NUM_GEN_VALIDATION_ERROR, "partnerID cannot be null or an empty string");
		}
		if(statusCode==null || statusCode.isEmpty()){
			throw new WSLogException(MLMSStatusCode.NUM_GEN_VALIDATION_ERROR, "statusCode cannot be null or an empty string");
		}
		
		//"INSERT INTO AT_MLMS_WS_TRANS_LOG(ID,WEB_SERVICE_ID,TRANSACTION_CODE,ACTION_DATE,PARTNER_ID,STATUS_CODE,DESCRIPTION,XML,XML_CLOB) " +
		//"VALUES ('wslog' || LPAD(at_mlms_ws_trans_seq.nextval,15,'0'),?,?,SYSDATE,?,?,?,?,?)";
		try{
			Clob clob = null;
			statement = conn.prepareStatement(SQL_LOG_WS);
			statement.setString(1, webserviceID);
			statement.setString(2, transCode);
			statement.setString(3, partnerID);
			statement.setString(4, statusCode);
			statement.setString(5, description);
			if (xml != null) {
				if (xml.length() >= 4000) {
					trimmedXML = xml.substring(0, 4000);
					clob = conn.createClob();
					clob.setString(1, xml);
				} else {
					trimmedXML = xml;
				}
			}
			statement.setString(6, trimmedXML);
			statement.setClob(7, clob);
			rowsUpdated = statement.executeUpdate();
		} catch (Exception ex){
			StringBuilder sb = new StringBuilder("Error logging WS: ");
			sb.append(ex.getMessage());
			sb.append(": ");
			sb.append(rowsUpdated);
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(transCode);
			sb.append(EOL);
			sb.append(new GregorianCalendar());
			sb.append(EOL);
			sb.append(partnerID);
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(description);
			sb.append(EOL);
			sb.append(xml);
			throw new WSLogException(MLMSStatusCode.NUM_DB_GENERAL, sb.toString(), ex);
		} finally {
			freeDBResources(null, statement, null, "WSLog.logWebService", null);
		}
		if (rowsUpdated < 1) {
			StringBuilder sb = new StringBuilder("WS log not written to DB: ");
			sb.append(rowsUpdated);
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(transCode);
			sb.append(EOL);
			sb.append(new GregorianCalendar());
			sb.append(EOL);
			sb.append(partnerID);
			sb.append(EOL);
			sb.append(webserviceID);
			sb.append(EOL);
			sb.append(description);
			sb.append(EOL);
			sb.append(xml);
			throw new WSLogException(MLMSStatusCode.NUM_DB_GENERAL, sb.toString());
		}
	}

    
    /**
     * Render the query results in simple format
     * @param source
     * @param rSet
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @throws SQLException
     */
    public static void formatResultSet_Simple(String source, ResultSet rSet, String title, String tableID, boolean exportButton, 
            StringBuilder output, StringBuilder error) throws SQLException {
        formatResultSet_Simple(source, rSet, title, tableID, exportButton, output, error, null, null, null);
    }
    
    /**
     * Render the query results in simple format
     * @param source
     * @param rSet
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @param tableStyle override CSS style at table level
     * @param rowStyle override CSS style at row level
     * @param cellStyle override CSS style at cell level
     * @throws SQLException
     */
    public static void formatResultSet_Simple(String source, ResultSet rSet, String title, String tableID, boolean exportButton, 
            StringBuilder output, StringBuilder error, String tableStyle, String rowStyle, String cellStyle) throws SQLException {
        // Get the ResultSetMetaData.  This will be used for the column headings
        ResultSetMetaData rsmd = rSet.getMetaData();
        // Get the number of columns in the result set
        int intNumCols = rsmd.getColumnCount();
        //int rowNum = 0;
        String tempStr;
        String trTag = null;
        String tdTag = null;

        if (tableID == null) {
            tableID = "datatable_0";
        }
        if (title != null) {
            output.append("<h2>");
            output.append(title);
            output.append("</h2><br/>\n");
        }
        if (intNumCols > 0){
            if (exportButton) {
                output.append("<button class=\"exportButton\" name=\"exportCSV\" onclick=\"exportTable('");
                output.append(tableID);
                output.append("');\">Download As CSV</button>\n");
            }
            output.append("<table class=\"filelist\" id=\"");
            output.append(tableID);
            output.append("\" cellspacing=\"1px\" cellpadding=\"0px\"");
            if (tableStyle != null) {
                output.append(" style=\"");
                output.append(tableStyle);
                output.append("\"");
            }
            output.append(">\n<tr>\n");

            // Display column headings
            for (int i = 1; i <= intNumCols; i++) {
                output.append("<th align=\"center\"><font class=\"header\">");
                output.append(rsmd.getColumnLabel(i));
                output.append("</font></th>\n");
            }
            output.append("</tr>\n");

            // Construct the tr tag
            trTag = "<tr class=\"mouseout\"" + ((rowStyle==null)?"":(" style=\"" + rowStyle + "\"")) + ">\n";
            // Construct the td tag
            tdTag = "<td" + ((cellStyle==null)?"":(" style=\"" + cellStyle + "\"")) + ">";
            // Display data, fetching until end of the result set
            while (rSet.next()) {
                // Loop through each column, getting the column data and displaying
                output.append(trTag);
                
                for (int i = 1; i <= intNumCols; i++) {
                    tempStr = rSet.getString(i);
                    output.append(tdTag);
                    if (tempStr != null) {
                        output.append(WSLog.htmlEscape(tempStr));
                    }
                    output.append("</td>\n");
                }
                output.append("</tr>\n");
                // Fetch the next result set row
                //rowNum++;
            }
            output.append("</table>\n");
        }
        WSLog.appendWarning(rSet, error);
    }
    
    /**
     * Render the query results in technical format. Returns student ID if found, may be useful for other function call.
     * @param source
     * @param rSet
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @return User ID if found
     * @throws SQLException
     */
    public static String formatResultSet_Technical(String source, ResultSet rSet, String title, String tableID, boolean exportButton, 
            StringBuilder output, StringBuilder error) throws SQLException {
        return formatResultSet_Technical(source, rSet, title, tableID, exportButton, output, error, null, null, null, true);
    }

    /**
     * Render the query results in technical format. Returns student ID if found, may be useful for other function call.
     * @param source
     * @param rSet
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @param tableStyle override CSS style at table level
     * @param rowStyle override CSS style at row level
     * @param cellStyle override CSS style at cell level
     * @param techViewCheckBox whether to render the "Technical View" checkbox. Might want to turn it off if this is not the only result table on a page.
     * @return User ID if found
     * @throws SQLException
     */
    public static String formatResultSet_Technical(String source, ResultSet rSet, String title, String tableID, boolean exportButton, 
            StringBuilder output, StringBuilder error, String tableStyle, String rowStyle, String cellStyle, boolean techViewCheckBox)
                    throws SQLException {
        String tempStr;
        String userID = null;
        String trTag = null;
        String tdTagPrefix = null;
        //int rowNum = 0;
        boolean[] isTech = null;
        boolean[] isCheck = null;
        boolean[] isError = null;
        boolean[] isExist = null;
        int regIDIndex = -1;
        int userIDIndex = -1;
        int b1Index = -1;
        int atrrsTransIDIndex = -1;
        boolean hasMenu = false;
        
        // Get the ResultSetMetaData.  This will be used for the column headings
        ResultSetMetaData rsmd = rSet.getMetaData();
        
        // Get the number of columns in the result set
        int intNumCols = rsmd.getColumnCount();
        if (tableID == null) {
            tableID = "datatable_0";
        }
        isTech = new boolean[intNumCols];
        isCheck = new boolean[intNumCols];
        isError = new boolean[intNumCols];
        isExist = new boolean[intNumCols];
        
        if (title != null) {
            output.append("<h2>");
            output.append(title);
            output.append("</h2><br/>\n");
        }
        if (intNumCols > 0){
            if (exportButton) {
                output.append("<button class=\"exportButton\" name=\"exportCSV\" onclick=\"exportTable('");
                output.append(tableID);
                output.append("');\">Download As CSV</button>\n");
            }
            if (techViewCheckBox) {
                output.append("<span title=\"");
                output.append("Colums start with 'T_' displayed in 'Technical View'\n");
                output.append("Colums end with '_C' (Check) expect 'Y'; 'N' requires attention\n");
                output.append("Colums end with '_E' (Error) expect 'N'; 'Y' requires attention\n");
                output.append("Colums end with '_X' (eXist) expect non-empty value");
                output.append("\"><input id=\"toggleCheckBox\" type=\"checkbox\" name=\"toggleColumns\" value=\"\" onclick=\"toggle_column()\"/>Technical View</span><br/>\n");
            }
            output.append("<table class=\"filelist\" id=\"");
            output.append(tableID);
            output.append("\" cellspacing=\"1px\" cellpadding=\"0px\"");
            if (tableStyle != null) {
                output.append(" style=\"");
                output.append(tableStyle);
                output.append("\"");
            }
            output.append(">\n<tr>\n");

            // Display column headings
            for (int i = 0; i < intNumCols; i++) {
                hasMenu = false;
                tempStr = rsmd.getColumnLabel(i + 1);
                // Check Prefix
                isTech[i] = tempStr.startsWith("T_");
                // Check Suffix
                isCheck[i] = tempStr.endsWith("_C");
                isError[i] = tempStr.endsWith("_E");
                isExist[i] = tempStr.endsWith("_X");
                // Look for special columns
                if (tempStr.indexOf("ALMS_REG_ID") > -1 && regIDIndex == -1) {
                    hasMenu = true;
                    regIDIndex = i;
                } else if (tempStr.indexOf("USER_ID") > -1 && userIDIndex == -1) {
                    hasMenu = true;
                    userIDIndex = i;
                } else if (tempStr.indexOf("B1_TRANSID") > -1 && b1Index == -1) {
                    hasMenu = true;
                    b1Index = i;
                } else if (tempStr.indexOf("ATRRS_TRANSID") > -1 && atrrsTransIDIndex == -1) {
                    hasMenu = true;
                    atrrsTransIDIndex = i;
                }
                if (isTech[i]) {
                    output.append("<th align=\"center\" class=\"v\"><font class=\"header\">");
                    output.append(tempStr);
                    output.append("</font></th>\n");
                    if (hasMenu) {
                        output.append("<th align=\"center\" class=\"v_popmenu\"><font class=\"header\">...</font></th>\n");
                    }
                } else {
                    output.append("<th align=\"center\"><font class=\"header\">");
                    output.append(tempStr);
                    output.append("</font></th>\n");
                    if (hasMenu) {
                        output.append("<th align=\"center\" class=\"popmenu\"><font class=\"header\">...</font></th>\n");
                    }
                }
            }
            output.append("</tr>\n");
            
            // Construct the tr tag
            trTag = "<tr class=\"mouseout\"" + ((rowStyle==null)?"":(" style=\"" + rowStyle + "\"")) + ">\n";
            // Construct the td tag prefix
            tdTagPrefix = "<td" + ((cellStyle==null)?"":(" style=\"" + cellStyle + "\""));
            // Display data, fetching until end of the result set
            while (rSet.next()) {
                // Loop through each column, getting the column data and displaying
                output.append(trTag);

                for (int i = 0; i < intNumCols; i++) {
                    tempStr = rSet.getString(i+1);
                    output.append(tdTagPrefix);
                    if (isTech[i]) {
                        output.append(" class=\"v\"");
                    }
                    if (isCheck[i]) {
                        output.append(" align=\"center\"");
                        if (tempStr != null && !tempStr.equals("Y")){
                            output.append(" bgcolor=\"yellow\"");
                        }
                    } else if (isError[i]) {
                        output.append(" align=\"center\"");
                        if (tempStr != null && !tempStr.equals("N")){
                            output.append(" bgcolor=\"red\"");
                        }
                    } else if (isExist[i]) {
                        output.append(" align=\"center\"");
                        if (tempStr == null){
                            output.append(" bgcolor=\"red\"");
                        }
                    }
                    output.append(">"); // close the td tag
                    
                    if (i == regIDIndex) {
                        // do not expect escape characters
                        if (tempStr != null) {
                            output.append(tempStr);
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                            output.append("<ul class=\"hoverMenu\"><li><u>...</u><ul>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_CIReg.jsp?REG_ID=").append(tempStr).append("\">Check Registration</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/UpdRegQueueAdmin.jsp?ACTION=ADD_SELECTED&amp;ID=").append(tempStr).append("\" target=\"_blank\">Update Registration</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_Order.jsp?REG_ID=").append(tempStr).append("\">Check Order(s)</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/CIRegAdmin.jsp?REG_ID=").append(tempStr).append("\" target=\"_blank\">Edit Record</a></li>");
                            output.append("</ul></li></ul>");
                        } else {
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                        }
                    } else if (i == userIDIndex) {
                        // do not expect escape characters
                        if (userID == null) {
                            userID = tempStr;
                        }
                        if (tempStr != null) {
                            output.append(tempStr);
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                            output.append("<ul class=\"hoverMenu\"><li><u>...</u><ul>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_CIReg.jsp?USER_ID=").append(tempStr).append("\">Check Registrations</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_DTR.jsp?USER_ID=").append(tempStr).append("\">Check DTR</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_Enrollment.jsp?USER_ID=").append(tempStr).append("\">Check Enrollments</a></li>");
                            output.append("\t<li><a href=\"/LMSSupport/Query_Order.jsp?USER_ID=").append(tempStr).append("\">Check Orders</a></li>");
                            output.append("</ul></li></ul>");
                        } else {
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                        }
                    } else if (i == atrrsTransIDIndex) {
                        // do not expect escape characters 
                        if (tempStr != null) {
                            output.append(tempStr);
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                            output.append("<ul class=\"hoverMenu\"><li><u>...</u><ul>");
                            output.append("\t<li><a href=\"/IESupport/Query_ATRRS.jsp?TRANSID=").append(tempStr).append("\">Check ATRRS Msg</a></li>");
                            output.append("</ul></li></ul>");
                        } else {
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                        }
                    } else if (i == b1Index) {
                        // do not expect escape characters 
                        if (tempStr != null) {
                            output.append(tempStr);
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                            output.append("<ul class=\"hoverMenu\"><li><u>...</u><ul>");
                            output.append("\t<li><a href=\"/IESupport/Query_B1.jsp?TRANSID=").append(tempStr).append("\">Check B1 Msg</a></li>");
                            output.append("</ul></li></ul>");
                        } else {
                            if (isTech[i]) {
                                output.append("</td><td class=\"v_popmenu\">");
                            } else {
                                output.append("</td><td class=\"popmenu\">");
                            }
                        }
                    } else {
                        if (tempStr != null) {
                            output.append(htmlEscape(tempStr));
                        }
                    }
                    output.append("</td>\n");
                }
                output.append("</tr>\n");
                // Fetch the next result set row
                //rowNum++;
            }
            output.append("</table>");
            appendWarning(rSet, error);
        }
        return userID;
    }
    
    
    /**
     * Query using LMS connection and then apply simple format on output
     * @param source
     * @param query
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @return USER_ID if found in technical mode, null if not found or in simple mode
     */
    public static String queryAndFormatOutput(boolean technicalView, String source, String query, String title, String tableID, 
            boolean exportButton, StringBuilder output, StringBuilder error) {
        return queryAndFormatOutput(technicalView, source, query, title, tableID, exportButton, output, error, null, null, null, technicalView);
    }
    
    /**
     * Query using LMS connection and then apply simple format on output
     * @param source
     * @param query
     * @param title rendered as the h2 tag, null to skip
     * @param tableID
     * @param exportButton
     * @param output
     * @param error
     * @param tableStyle override CSS style at table level
     * @param rowStyle override CSS style at row level
     * @param cellStyle override CSS style at cell level
     * @param techViewCheckBox whether to render the "Technical View" checkbox
     * @return USER_ID if found in technical mode, null if not found or in simple mode
     */
    public static String queryAndFormatOutput(boolean technicalView, String source, String query, String title, String tableID, boolean exportButton, 
            StringBuilder output, StringBuilder error, String tableStyle, String rowStyle, String cellStyle, boolean techViewCheckBox) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rSet = null;
        String userID = null;
        
        try {
            conn = WSLog.getLMSDBConnection(source, error);
            WSLog.appendWarning(conn, error);
            statement = conn.prepareStatement(query);
            WSLog.appendWarning(statement, error);
            if (statement.execute()) {
                rSet = statement.getResultSet();
                if (technicalView) {
                    userID = formatResultSet_Technical(source, rSet, title, tableID, exportButton, output, error, tableStyle, rowStyle, cellStyle, techViewCheckBox);
                } else {
                    formatResultSet_Simple(source, rSet, title, tableID, exportButton, output, error, tableStyle, rowStyle, cellStyle);
                }
            }
        } catch (SQLException ex) {
            output.setLength(0);
            try {
                WSLog.appendError(ex, error);
            } catch (Exception e) {
                error.append(e);
            }
        } catch (Exception ex) {
            output.setLength(0);
            error.append(ex);
        } finally {
            // close the database connection
            WSLog.freeDBResources(rSet, statement, conn, source, error);
        }
        return userID;
    }
}

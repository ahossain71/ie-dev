package gov.cms.cciio.common.util;
//TODO - TO BE UPDATED FOR SABA 7, warning log still needed?

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.cms.cciio.common.exception.MLMSStatusCode;
import gov.cms.cciio.common.exception.DBUtilException;

import com.saba.customattribute.ICustomAttributeValueDetail;
import com.saba.ejb.CustomizableDetail;
import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.party.PersonDetail;

/*
 * 2010-07-21   Feilung Wong    Copied from Saba 5.3 code: mil.army.dls.util.CommonUtil.java
 * 2013-09-20   Feilung Wong    ALMS 4 - moved from dls_common.properties to LMS DB: AT_CMN_PROPERTIES
 * 2015-07-22	Steve Meyer		Updated for MLMS use
 */
/**
 * Common utility class
 * @author Feilung Wong, Steve Meyer
 */

public class CommonUtil {

    private static final String className = CommonUtil.class.getName();
    private static final Logger logger = Logger.getLogger(className);

    public static final String EOL = System.getProperty("line.separator");
    private static final SimpleDateFormat TIME_ONLY_TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat FULL_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss.SSSS");

    /** DB Queries */
    private static final String SQL_WARNING_LOG_INSERT = "INSERT /* Common-CommonUtil */ INTO AT_INT_WARNING_LOG (AUDITLOGID, TRANSID, MESSAGENAME, TIMESTAMP, MESSAGE) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_MDB_ERROR_LOG_INSERT = "INSERT /* Common-CommonUtil */ INTO AT_MDB_ERROR_LOG (MDB_ID,MDB_NAME,MDB_ERROR) VALUES(?,?,?)";
    
    private static char[] badChars = { '_', '!', '@', '#', '$', '%', '^', '&',
	'*' };
    private static char[] replaceChar = { 'u', 'e', 'a', 't', 'd', 'p', 'c',
	'n', 's' };
    /*-----------------------------
     * Date/Time Section
     *-----------------------------*/
    /**
     * Returns a java.sql.Date when provided a String in the format of MM/DD/YYYY
     * Avoid using SimpleDateFormat - not thread safe
     * 
     * Used by interface
     * 
     * @param date - String in the format of MM/DD/YYYY
     * @return a java.sql.Date representation of the input date string
     */
    public static java.sql.Date getSqlDateFormat(String date) {
        if (date != null && date.length() == 10) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Integer.parseInt(date.substring(6, 10)),
                    Integer.parseInt(date.substring(0, 2)) - 1, // January is 0
                    Integer.parseInt(date.substring(3, 5)),
                    0, 0, 0);
            cal.clear(Calendar.MILLISECOND);
            return new java.sql.Date(cal.getTimeInMillis());
        } else {
            return null;
        }
    }

    /**
     * Convert ATRRS TRANSDATE and TRANSTIME to java.sqlTimestamp
     * Avoid using SimpleDateFormat - not thread safe
     * Used by interface
     * 
     * @param date ATRRS TRANSDATE MM/dd/yyyy
     * @param time ATRRS TRANSTIME HH:mm:ss
     * @return <i>Timestamp</i> that represents the input date and time; null if string is invalid
     */
    public static Timestamp getTimestampFromTransDateTime(String date, String time) {
        try {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Integer.parseInt(date.substring(6, 10)),
                    Integer.parseInt(date.substring(0, 2)) - 1,  // January is 0
                    Integer.parseInt(date.substring(3, 5)),
                    Integer.parseInt(time.substring(0, 2)),
                    Integer.parseInt(time.substring(3, 5)),
                    Integer.parseInt(time.substring(6, 8)));
            cal.clear(Calendar.MILLISECOND);
            return new Timestamp(cal.getTimeInMillis());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get current time as a String in this format: yyyyMMdd-HHmmss.SSSS
     * @return a formatted string representing current time
     */
    public static String getFormattedTimestamp() {
        return getFormattedTimestamp(new Date());
    }
    
    /**
     * Convert a Date object to String in this format: yyyyMMdd-HHmmss.SSSS
     * @param date the input time
     * @return a formatted date string
     */
    public static String getFormattedTimestamp(Date date) {
        
        return FULL_TIMESTAMP_FORMAT.format(date);
    }
    
    /**
     * Append a time stamp with prefix to the existing StringBuilder for performance logging
     * 
     * Used by interface and common user code
     * 
     * @param sb the StringBuilder to be appended to
     * @param prefix the prefix that goes before the time stamp
     */
    public static void appendTimestamp(StringBuilder sb, String prefix) {
        if (sb != null) {
            sb.append(prefix);
            sb.append(TIME_ONLY_TIMESTAMP_FORMAT.format(new Date()));
        }
    }
    
    /**
     * Returns the beginning date for the Government's Fiscal Year. Govt. 2006 FY
     * starts from Oct 1, 2005 to Sept 30, 2006.
     * e.g., if the current date is Aug 18, 2006, the date returned is Oct 1, 2005
     * if the current date is Nov 1, 2006, the date returned is Oct 1, 2006
     * if the current date is Oct 1, 2006, the date returned is Oct 1, 2006
     * Used by ATRRS interface
     * @return GregorianCalendar
     */
    public static GregorianCalendar getBeginDateForCurrentGovtFY(){
        GregorianCalendar current = new GregorianCalendar();       
       
        int month = current.get(Calendar.MONTH);
        int year = current.get(Calendar.YEAR);
        
        if (month >= Calendar.OCTOBER) {
            return new GregorianCalendar (year, Calendar.OCTOBER, 1);
        } else {
            return new GregorianCalendar (year-1, Calendar.OCTOBER, 1);
        }
    }
    
    /**
     * Returns the beginning date for the Government's Fiscal Year. Govt. 2006 FY
     * starts from Oct 1, 2005 to Sept 30, 2006.
     * e.g., if the current date is Aug 18, 2006, the date returned is Oct 1, 2005
     * if the current date is Nov 1, 2006, the date returned is Oct 1, 2006
     * if the current date is Oct 1, 2006, the date returned is Oct 1, 2006
     * Used by ATRRS interface
     * @return GregorianCalendar
     */
    public static GregorianCalendar getBeginDateForGovtFY(String fy){
        if (fy != null) {
            return new GregorianCalendar((new Integer(fy).intValue()-1), Calendar.OCTOBER, 1); 
        } else {
            return getBeginDateForCurrentGovtFY();
        }
    }


//    /**
//     * Returns Timestamp if the string is in the right format; null if the date is in invalid format
//     * 
//     * @param transDateTime - TransDate and TransTime in this format: 'MM/dd/yyyy HH:mm:ss'
//     * @return a Timestamp object if the string is in the right format; null otherwise
//     */
//    public static Timestamp getTimeStamp(String transDateTime) {
//        Timestamp ts = null;
//        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//        format.setLenient(false);
//        try {
//            // convert from java.util.Date to java.sql.Timestamp
//            ts = new Timestamp(format.parse(transDateTime).getTime());
//        } catch (Exception ex) {
//            ts = null;
//        }
//        return ts;
//    }

//    /**
//     * Get current time stamp in the format of HH:mm:ss
//     * 
//     * Used by interface
//     * 
//     * @return current time stamp in the format of HH:mm:ss
//     */
//    public static String getTimestamp() {
//        return TIME_ONLY_TIMESTAMP_FORMAT.format(new Date());
//    }

//    /**
//     * Convert interface TRANSDATE and TRANSTIME to LMS time stamp format <i>yyyyMMddHHmmssSSSS</i>
//     * This method assume correct format of TRANSDATE and TRANSTIME
//     * 
//     * @param transdate MM/dd/yyyy
//     * @param transtime HH:mm:ss
//     * @return Current date/time in String format of yyyyMMddHHmmssSSSS
//     */
//    public static String getLMSTimeStampAsString(String transdate, String transtime) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(transdate.substring(6, 10));
//        sb.append(transdate.substring(0, 2));
//        sb.append(transdate.substring(3, 5));
//        sb.append(transtime.substring(0,2));
//        sb.append(transtime.substring(3,5));
//        sb.append(transtime.substring(6, 8));
//        sb.append("0000");
//        return sb.toString();
//    }

//    /**
//     * For LMS, we need date/time in String format of the current time
//     * 
//     * @return Current date/time in String format of yyyyMMddHHmmssSSSS
//     */
//    public static String getTimeStampAsString() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
//        java.util.Date today = new java.util.Date();
//        java.sql.Date sqlDate = new java.sql.Date(today.getTime());
//        String timeStmp = formatter.format(sqlDate);
//        return timeStmp;
//    }

//    /**
//     * Get SQL date-time for current time stamp. Copied from LMS mil.army.dls.auth.DLSUser
//     * 
//     * @return SQL data-time for current time stamp
//     */
//    public static java.sql.Date getDateTime() {
//        java.util.Date dToday = new java.util.Date();
//        java.sql.Date dSqlDate = new java.sql.Date(dToday.getTime());
//
//        return dSqlDate;
//    } // end getDateTime
    /*-----------------------------
     * End Date/Time Section
     *-----------------------------*/

    
    /*-----------------------------
     * Error Handling Section
     *-----------------------------*/
    /**
     * Get stack trace of an exception in a String
     * 
     * Used by interface
     * 
     * @param ex - The Exception to be converted
     */
    public static String stackTraceToString(Exception ex) {
        return stackTraceToString(ex, EOL);
    }

    /**
     * Get stack trace of an exception in HTML String format
     * 
     * @param ex - The Exception to be converted
     */
    public static String stackTraceToHTMLString(Exception ex) {
        return stackTraceToString(ex, "<br />");
    }

    /**
     * Get stack trace of an exception in a String
     * 
     * @param ex - The Exception to be converted
     * @param separator - The End of Line separator to be used between each line
     *        Eg. For HTML messages use <br />
     *        For Log files use System.getProperty("line.separator");
     * 
     */
    public static String stackTraceToString(Exception ex, String separator) {
        StringBuilder sb = new StringBuilder();
        int i;

        sb.append(ex.toString());
        sb.append(separator);
        StackTraceElement[] st = ex.getStackTrace();
        for (i = 0; i < st.length; i++) {
            sb.append(st[i].toString());
            sb.append(separator);
        }
        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append("Caused by");
            sb.append(separator);
            sb.append(cause.toString());
            sb.append(separator);
            st = cause.getStackTrace();
            for (i = 0; i < st.length; i++) {
                sb.append(st[i].toString());
                sb.append(separator);
            }
            cause = cause.getCause();
        }
        return sb.toString();
    }

    /**
     * Utility method to construct the Exception message String. The format of
     * this string contains the TransactionId, Class Name, Method Name, Error
     * Marker, Error Message. This String is sent to the client through the
     * MessageProcessingException.
     * 
     * Used by interface
     * 
     * @param transactionId
     * @param className
     * @param methodName
     * @param errMarker
     * @param errorMsg
     * @return String
     */
    public static String buildExceptionMsg(String transactionId,
            String className, String methodName, int errMarker, String errorMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("TransactionId: ");
        sb.append(transactionId);
        sb.append("  ClassName: ");
        sb.append(className);
        sb.append("  MethodName: ");
        sb.append(methodName);
        sb.append("  Error Place: ");
        sb.append(errMarker);
        sb.append("  Exception: ");
        sb.append(errorMsg);
        return sb.toString();
    }
    /*-----------------------------
     * End Error Handling Section
     *-----------------------------*/

    public static void insertToWarningLog(ServiceLocator locator, String auditLogID, String transID, String messageName, String message) throws  gov.cms.cciio.common.exception.DBUtilException {
        int errMark=0;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getConnection(locator, "CommonUtil.insertToWarningLog");
            logger.fine("Inserting row for AT_INT_WARNING_LOG with auditLogID: " + auditLogID);
            statement = conn.prepareStatement(SQL_WARNING_LOG_INSERT);
            statement.setString(1, auditLogID);
            statement.setString(2, transID);
            statement.setString(3, messageName);
            statement.setString(4, getFormattedTimestamp());
            statement.setString(5, message);
            errMark = 10;
            statement.execute();
            errMark = 20;
        } catch (SQLException e) {
            StringBuilder errMsg = new StringBuilder("CommonUtil.insertToWarningLog: ");
            errMsg.append(errMark);
            errMsg.append("\n");
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(), e);
        } finally {
            DBUtil.freeDBResources(locator, null, statement, conn, "CommonUtil.insertToWarningLog");
        }
    }
    
    /**
     * called to enter error logs for MDB 
     * initially for use by AutoDisEnroll cancellations for course iterations and registrations
     * QC 1108
     */
    public static void insertToMDBErrorLog(ServiceLocator locator, String mdbID, String mdbName, String message) throws DBUtilException {
        int errMark=0;
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getConnection(locator, "CommonUtil.insertToMDBErrorLog");
            logger.fine("Inserting row for AT_MDB_ERROR_LOG with mdbID: " + mdbID);
            statement = conn.prepareStatement(SQL_MDB_ERROR_LOG_INSERT);
            statement.setString(1, mdbID);
            statement.setString(2, mdbName);
            statement.setString(3, message);
            errMark = 10;
            statement.execute();
            errMark = 20;
        } catch (SQLException e) {
            StringBuilder errMsg = new StringBuilder("CommonUtil.insertToMDBErrorLog: ");
            errMsg.append(errMark);
            errMsg.append("\n");
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(), e);
        } finally {
            DBUtil.freeDBResources(locator, null, statement, conn, "CommonUtil.insertToMDBErrorLog");
        }
    }
    
    /**
     * Saba API returns null for custom fields that are not set as display.
     * This helper method helps reveal such situation.
     * Employee Object would display SSN, so only custom fields would be displayed.
     * 
     * @param logger
     * @param detail
     * @throws SabaException
     */
    public static void debugSabaCustomizableDetail(Logger logger, CustomizableDetail detail)
        throws SabaException {
        if (logger.isLoggable(Level.FINER)) {
            if (detail instanceof PersonDetail) {
                System.out.println("Person Object: Not displaying details");
            } else {
                System.out.println("Detail Object:" + detail);
            }
            ICustomAttributeValueDetail tempCustom;
            System.out.println("Custom Fields:");
            for (Object obj: detail.getCustomValues()) {
                tempCustom = (ICustomAttributeValueDetail) obj;
                System.out.println(tempCustom.getName() + ": " + tempCustom.getValue());
            }
        }
    }
    
    /**
     * CPU % is only in Java 1.7. negative means info not available.
     *    double cpuLoad = ManagementFactory.getOperatingSystemMXBean().getSystemCpuLoad() * 100;
     *    System.out.println("CPU load: " + cpuLoad);
     * But the load average (available in 1.6) is actually a better measure.
     * A ratio of 0.7 is a common measure of whether the system needs attention.
     * if the load average information is not available, assume not busy
     * @return true if system is busy; false otherwise
     */
    public static boolean isSystemBusy() {
        OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
        double loadRatio = mxBean.getSystemLoadAverage() / mxBean.getAvailableProcessors();
        
        if (logger.isLoggable(Level.FINER)) {
            System.out.println("System Load Avg Ratio: " + loadRatio);
        }
        if (loadRatio < 0.0d) {
            logger.warning("System load average not available, assume system not busy");
            return false;
        }
        return loadRatio >= 0.7d;
    }
    /**
     * Used to remove bad characters from the usernames and produce a consistent end result
     * @param str
     * @return
     */
    public static String replaceCharacters(String str) {
		StringBuffer sb = new StringBuffer(str);
		boolean found = false;
		int count = 0;
		/* interate through the list of bad characters */
		for (int i = 0; i < badChars.length; i++) {
			/* iterate through the list of characters in the password */
			for (int k = 0; k < str.length(); k++) {

				if (str.indexOf(badChars[i]) > 0) {
					++count;
					
					str = str.replace(badChars[i], replaceChar[i]);
					//System.out.println("password fix in progress :" + str);
					found = true;
				}
			}
			
		}//for
		if(found){
			sb = new StringBuffer();
			sb.append(str);
			sb.append("=");
			sb.append(count);
		} 
		return sb.toString();
	}
    /**
     * 
     * @param date
     * @return
     */
    public static String getFormattedDateString(Date date){
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String retVal = null;
		if(date != null){
			retVal = dateSlashFormat.format(date);
		}
		
		return retVal;
	}
    public static Date getDefaultCreateDate() throws ParseException{
		String inputStr = "10-31-2014";
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date inputDate = dateFormat.parse(inputStr);
		return inputDate;
	}
}
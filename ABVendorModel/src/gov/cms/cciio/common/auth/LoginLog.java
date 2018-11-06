package gov.cms.cciio.common.auth;

import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.saba.sys.SabaSite;

/**
 * This class handles all logging related to login. 
 */
/*
 * 2013-09-18   Feilung Wong    Modified from 5.5 LDAPLog
 *                              It is used to capture all Login errors (not just LDAP)
 */
public class LoginLog {
    
    /** Creating className instance */
    private static final String className = LoginLog.class.getName();
    /** Creating logger */
    private static final Logger logger = Logger.getLogger(className);
    
    private static final int MAX_BYTES = 512000; // 5KB per file
    private static final int ROTATION_COUNT = 20; // Keep 20 files
    
    static {
        try {
            FileHandler fh = new FileHandler(SabaSite.getSystemDefaultSitePath() + "/log/Login_error.%g.log", MAX_BYTES, ROTATION_COUNT);
            fh.setFormatter(new LoginLogFormatter());
            logger.addHandler(fh);
            // If all goes well, remove the parent logger handler (to SystemOut.log or trace.log)
            logger.setUseParentHandlers(false);
        } catch (Exception ex) {
            // do nothing, default back to parent logger (SystemOut.log or trace.log)
        }
    }
    
    /**
     * Writes a string to the debug log
     * Used by LMS ALMSCustomLogin.jsp (Should force to error log for that one actually)
     * @param message the debug string
     */
    public static void writeToDebugLog(String message) {
        writeToDebugLog(null, false, message);
    }
    
    /**
     * Writes a string to the debug log
     * Used by LDAP code
     * @param title identifier (e.g. AKO username, SSN, EDIPI)
     * @param isSSN whether title is SSN, if so, will mask the first 6 characters
     * @param message the debug string
     */
    public static void writeToDebugLog(String title, boolean isSSN, String message) {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            if (isSSN && title.length() > 6) {
                sb.append("******");
                sb.append(title.substring(6));
            } else {
                sb.append(title);
            }
            sb.append(": ");
        }
        sb.append(message);
        logger.fine(sb.toString());
    }
    
    /**
     * Writes a string to the error log
     * Used by LMS ALMSCustomLogin.jsp
     * @param message the error string
     */
    public static void writeToErrorLog(String message) {
        logger.severe(message);
    }
}
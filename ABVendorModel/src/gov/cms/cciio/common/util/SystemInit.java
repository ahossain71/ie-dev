package gov.cms.cciio.common.util;

import gov.cms.cciio.common.auth.LoginLog;

import java.io.File;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.logging.Logger;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.security.SabaLogin;

public class SystemInit {
	 private static final String className = SystemInit.class.getName();
	 private static final Logger logger = Logger.getLogger(className);

	    /**
	     * For reading LDAP properties file 
	     */
	 private static final String AUTH_PROP_FILE = "properties" + File.separator + "cciio_auth.properties";
	 private static PropertyResourceBundle ldapResBundle = null;
	 public static String HOST_STRING = null;
	    
	    // cached parameters
	    public static String SABA_ADMIN_USER_NAME       = Constants.MLMS_SUPER;
	    public static String SABA_ADMIN_PASSWORD = Constants.MLMS_SUPER_PASSWORD;
	   
	    
	    // Cannot cache encrypted password since the hash is only valid for the same day.
	    //protected static String SABA_ADMIN_ENCRYPTED_PASSWORD = null;
	    public static ArrayList<String> ALMS_SYS_ADMIN_UNAME = null;
	    public static String ALMS_SYS_ADMIN_PWD       = null; 
	/**
     * Obtain a session using AKO_USR account
     * Use it ONLY for login related tasks 
     * currently called by login code or setStateCode.jsp
     * @return
     * @throws SabaException
     */
    public static ServiceLocator loginMLMSAdmin() throws SabaException {
        //return SabaLogin.authenticate(SABA_ADMIN_USER_NAME, Encrypter.encrypt(SABA_ADMIN_PASSWORD));
    	//LoginLog.writeToErrorLog("Saba Admin User name " + SABA_ADMIN_USER_NAME + " Saba Admin Password " + SABA_ADMIN_PASSWORD);
    	return SabaLogin.authenticate(SABA_ADMIN_USER_NAME, SABA_ADMIN_PASSWORD);
    }
    
    /**
     * This can used to log out any active Saba sessions
     * @param source what is calling this method for troubleshooting purposes.
     */
    public static void logoutSaba(String source) {
        try {
            SabaLogin.logout();
        } catch (Exception ex) {
            LoginLog.writeToErrorLog(source + "> SystemInit.LogoutSaba: Error logging out Saba session: " + ex);
        }
    }
}

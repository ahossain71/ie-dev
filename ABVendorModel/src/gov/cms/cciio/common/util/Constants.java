package gov.cms.cciio.common.util;
//TODO - TO BE UPDATED FOR SABA 7

/*
 * 2010-07-21   Feilung Wong    Copied from 5.3 code: mil.army.dls.util.Constants.java
 * 2010-07-29   Feilung Wong    Added JNDI related entries
 * 2010-09-21   Feilung Wong    Removed Saba ID's, use CommonReferences to obtain references instead
 * 2010-09-24   Feilung Wong    Updated UIC Query to work with the new STORE_ID field, moved entries from A5
 * 2011-10-03   Feilung Wong    QC 854: Moved SQL statements out
 * 2012-07-06   Feilung Wong    QC 1060: Added inactive status definition
 */

public interface Constants {
    /**
     * This will allow ClassVersionCheck to extract file version in case of troubleshooting.
     */
    public static final String DLS_FILE_VERSION = "12";
	//TODO: Need to check and update all the constants and queries
    
    /* JNDI related entries */
//    public static final String JNDI_LOCAL_PREFIX = "java:comp/env/";
//    public static final String JNDI_COMMON_FACADE_LOCAL = JNDI_LOCAL_PREFIX + "CommonFacadeLocal";
//    public static final String JNDI_COMMON_COMMIT_LOCAL = JNDI_LOCAL_PREFIX + "CommonCommitLocal";
    
    /* User Constants */
    public static final String USER_DEFAULT_PASSWD = "welcome";
    //TODO The DB will take any user status, 
    // but Saba7's default LOV does not have "Information Unavailable" (from 5.5), but "Unknown"
    // Check migrated LOV data
//    public static final String USER_DEFAULT_LMS_STATUS = "Information Unavailable";
//    public static final String USER_DEFAULT_ATRRS_STATUS = "Information Unavailable";
    public static final String USER_DEFAULT_LMS_STATUS = "Unknown";
    public static final String USER_DEFAULT_ATRRS_STATUS = "Unknown";
    //TODO The DB will take any user status, 
    // but Saba7's default LOV does not have "Inactive" (from 5.5), but "Terminated"
    // Check migrated LOV data
    public static final String USER_STATUS_INACTIVE = "Inactive";
    public static final String USER_STATUS_ACTIVE = "Active";
    public static final String USER_DEFAULT_FLAGS = "1000000000";
    public static final String AKO_NONE_DOMAIN = "none";
    public static final String NO_ACCESS = "No access";
    
  /* Security List */
    // Army and civilian has the same security list
   

    /* Domain */
    //public static final String DOMAIN_NOT_ASSIGNED = "not assigned";
    
    /* Different Commit log tables */
    //public static final String COMMIT_LOG_ATRRS  = "AT_ATRRS_COMMIT_LOG"; 
    //public static final String COMMIT_LOG_JROTC  = "AT_JROTC_COMMIT_LOG"; 
    //public static final String COMMIT_LOG_COMMON = "AT_COMMON_COMMIT_LOG"; 
    
    /** Default object names */
    public static final String TIMEZONE_NAME_EASTERN = "(GMT-05:00) Eastern Time (US & Canada)";

    // Note: Saba 7 out of box has "english" only, but 5.5 has "english locale"
    // Migrated data has "english locale"
    public static final String LOCALE_NAME_ENGLISH = "english locale";
    public static final String LOCALE_NAME_SPANISH = "spanish locale";
    public static final String CURRENCY_NAME_US_DOLLARS = "US Dollars";
    
    /** Registration related constants */
    //public static final String ORDER_ATRRS_CANCEL_REASON = "Cancelled by ATRRS";
    //public static final String ORDER_ATRRS_CANCEL_MARKER = "AC";        // Stored in CUSTOM13 of Order to mark ATRRS Cancel
   // public static final String ORDER_ACCP_ADD_REASON     = "ACCP Automatic Completion Notice";
    //public static final String ORDER_ACCP_OFFTEMP_MARKER = "ACCP";      // Stored in CUSTOM3 of Offering Template to mark ACCP
    //public static final int    ORDER_MAX_SEATS_INCREMENT = 10;          // Increment when a session offering reaches capacity
    
    /** Extended Custom Attributes */
    //public static final String CUSTOM_FIN = "FIN";
    
    public static String FFM_AGENT_BROKER = "ffm_agent_broker";
    public static String FFM_ASSISTER = "ffm_assister";
    public static String FFM_TRAINING_ACCESS = "ffm_training_access";
    public static String MLMS_BUSINESS_OWNER = "mlms_business_owner";
    public static String FFM_TRAINING = "FFMTraining";
    public static String MLMS_ADMIN = "mlms_admin";
    public static String MLMS_SUPER = "mlms_super";
    public static String MLMS_GROUP_NOT_FOUND = "mlmsGroupNotFound";
    public static String TRAINING_GROUP_NOT_FOUND = "trainingGroupNotFound";
    public static String IS_MEMBER_OF_HEADER_NULL = "ismemeberofIsNull";
    // used for domain assignments
    public static String AGENT = "agent";
    public static String ASSISTER = "assister";
    public static String CCIIO = "cciio";
    public static String COMPLETE = "Complete";
    public static String INCOMPLETE = "Incomplete";
    public static String WORLD = "world";
    
    public static String DEFAULT_ORG = "root";
    public static String AGENT_ORG = "AgentBroker";
    public static String ASSISTER_ORG = "Assister";
    public static String CERTIFICATE_REQ = "certificate";
    
    public static String ASSISTER_URL = "/Saba/Web/Main/goto/AssisterProfile";
	public static String AGENT_URL = "/Saba/Web/Main/goto/AgentBrokerProfile";
	public static String CERTIFICATE_URL = "/Saba/Web/Main/goto/addCertificationToLearningPlanURL ";
	public static String DEFAULT_URL = "/Saba/Web/Cloud";
	
	public static String CMS_COMMON = "CMS Common";
	public static String MLMS_PROD_MODE = "prod";
	public static String MLMS_PT_MODE = "pt";
	public static String PT_PASSWORD = "Passw0rd1";
	//public static String MLMS_SUPER_PASSWORD = "admin";
	
	public static String MLMS_SUPER_PASSWORD = "IBMCCIIO0715";
	
	public static String SABA_ADMIN_USER_NAME       = "admin";
	public static String SABA_ADMIN_PASSWORD = "$P4tEt2s";
	
	public static String MONTH_DAY_YEAR_STD = "MM-dd-YYYY";
	public static String DD_MMM_YYYY = "dd-MMM-yyyy";
	//public static String MLMS_SUPER_PASSWORD = "admin";
}

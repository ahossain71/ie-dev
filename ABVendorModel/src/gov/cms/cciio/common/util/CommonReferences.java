package gov.cms.cciio.common.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.cms.cciio.common.delegate.RosterInsert;
import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.exception.RegistrationException;

import com.saba.currency.SabaCurrency;
import com.saba.domain.Domain;
import com.saba.function.JobFamily;
import com.saba.i18n.SabaLocale;
import com.saba.i18n.SabaTimeZone;
import com.saba.learningoffering.WBTOffering;
import com.saba.locator.ServiceLocator;
import com.saba.offering.OfferingTemplate;
import com.saba.party.organization.BusinessUnit;

/*
 * 2010-09-19   Feilung Wong    Created.
 * 2011-05-09   Feilung Wong    Using new DBUtil methods to better log and release DB resources
 * 2011-05-13   Feilung Wong    Removed reference to DBUtil.queryDatabase and get PreparedStatement for closing explicitly
 * 2011-10-03   Feilung Wong    QC 854: Use Java PreparedStatement bind variables as much as possible to improve caching/performance
 */
/**
 * This class is used to search and initialize some common object references.
 * This removes the need of reading Saba ID from properties file, which would change from Env to Env.
 * @author Feilung Wong
 *
 */
public class CommonReferences {
    private static final String className = CommonReferences.class.getName();    
    private static final Logger logger = Logger.getLogger(className);
    
    private static final String SQL_COMMON_PROPERTIES = "SELECT /* Common-CommonReferences */ NAME, VALUE FROM AT_CMN_PROPERTIES";
    private static final String SQL_DUMMY_LESSON_ID = "SELECT /*Common-CommonReferences*/ s.ID SID, t.ID TID FROM LET_EXT_OFFERING_SELFPACED s, LET_EXT_OFFERING_TEMPLATE t WHERE s.OFFERING_TEMP_ID=t.ID AND t.CI_TITLE=? ORDER BY SID DESC";
    private static final String SQL_ACCP_NOTICE_ID = "SELECT /*Common-CommonReferences*/ s.ID FROM LET_EXT_OFFERING_SELFPACED s, LET_EXT_OFFERING_TEMPLATE t WHERE s.OFFERING_TEMP_ID=t.ID AND t.CI_TITLE=? ORDER BY ID ASC";
    private static final String SQL_JOB_FAMILY_ID = "SELECT /*Common-CommonReferences*/ ID FROM CMT_EXT_JOB_FAMILY WHERE CI_NAME=?";
    private static final String SQL_BU_ID = "SELECT /*Common-CommonReferences*/ ID FROM TPT_COMPANY WHERE CI_NAME2=?";
    private static final String SQL_LOCALE_ID = "SELECT /*Common-CommonReferences*/ ID FROM FGT_LOCALE WHERE CI_LOCALE_NAME=?";
    private static final String SQL_ALMS_AUD_TYPE = "SELECT /*Common-CommonReferences*/ DISTINCT AUDIENCE_TYPE, AUDIENCE_TYPE_ID FROM AV_INT_ACCOUNT_TYPE WHERE AUDIENCE_TYPE_ID IS NOT NULL";
    private static final String SQL_ALMS_SEC_LIST = "SELECT /*Common-CommonReferences*/ DISTINCT v.SECURITY_LIST, s.LIST_ID FROM AV_INT_ACCOUNT_TYPE v, FGT_SS_LISTREF s, FGT_DOMAIN d WHERE v.ROLE_ID IS NOT NULL AND v.ROLE_ID = s.PRIV_ID AND s.DOMAIN_ID = d.ID AND d.CI_NAME = 'world'";
    private static final String SQL_MANDATORY_VIDEO_ID = "SELECT /*Common-CommonReferences*/ ID FROM CNT_EXT_CONTENT_INVENTORY WHERE CI_NAME=?";
    private static final String SQL_STATE_LIST = "SELECT /*Common-CommonReferences*/ STATECODE, STATENAME FROM AT_LMS_STATECODES";
    private static final String SQL_CURRENCY_USD_ID = "SELECT /*Common-CommonReferences*/ ID FROM TPT_CCY_CURRENCY WHERE SHORTNAME='USD'";
    private static final String SQL_TIMEZONE_ID = "SELECT /*Common-CommonReferences*/ ID FROM FGT_SYS_TIMEZONE WHERE NAME=?";
    // For Roster page
    private static final String SQL_ROSTER_INPUT_OUTPUT_CODE = "SELECT /*Common-CommonReferences*/ IO,CODE,DESCRIPTION,REASON_REQUIRED,ACTION FROM AV_VER_IOSTATUS";
    private static final String SQL_ROSTER_REASON = "SELECT /*Common-CommonReferences*/ CODE,DESCRIPTION,IS_HOLD FROM AV_VER_IOREASON";
    
    /* Object references that are initialized during startup */
    private static Properties commonProperties = null;
    private static Domain WORLD_DOMAIN = null;
    private static String DUMMY_LESSON_NAME = null;
    private static String DUMMY_LESSON_OFFERING_ID = null;
    private static String DUMMY_LESSON_TEMPLATE_ID = null;
    private static OfferingTemplate OFFER_TEMPLATE_DUMMY = null;
    private static WBTOffering WBTOFFERING_DUMMY = null;
    private static WBTOffering WBTOFFERING_ACCP_AUTOMATIC_NOTICE = null;
    private static JobFamily OTHER_JOB_FAMILY = null;
    private static BusinessUnit BU_OTHER = null;
    private static String BU_ID_OTHER = null;
    private static BusinessUnit BU_DOD = null;
    private static String BU_ID_DOD = null;
    private static SabaCurrency CURRENCY_US = null;
    private static SabaLocale LOCALE_ENGLISH = null;
    private static String LOCALE_ID_ENGLISH = null;
    private static SabaTimeZone TIMEZONE_EASTERN = null;
    private static String TIMEZONE_ID_EASTERN = null;
    private static String MANDATORY_VIDEO_CNINV = null;
    //private static ArrayList<String> AUDTYPE_REMOVE_LIST_ARMY = null;
    //private static ArrayList<String> AUDTYPE_REMOVE_LIST_CIVILIAN = null;
    //private static ArrayList<String> AUDTYPE_REMOVE_LIST_FOREIGN = null;
    private static ArrayList<String> AUDTYPE_ALL_ALMS = null;
    private static ArrayList<String> SECLIST_REMOVE_LIST_ARMY = null;
    private static ArrayList<String> SECLIST_REMOVE_LIST_FOREIGN = null;
    private static ArrayList<String> SECLIST_ALL_ALMS = null;
    private static ArrayList<String> STATE_CODE_LIST = null;
    private static ArrayList<String> STATE_NAME_LIST = null;
    // For Roster Page
    private static ArrayList<RosterInsert.IOCode> ROSTER_INPUT_CODES = null;
    private static ArrayList<RosterInsert.IOCode> ROSTER_OUTPUT_CODES = null;
    private static ArrayList<RosterInsert.IOReason> ROSTER_HOLD_REASONS = null;
    private static ArrayList<RosterInsert.IOReason> ROSTER_NON_HOLD_REASONS = null;
    private static HashMap<String, String> ROSTER_STATUS_ACTION_MAP = null;

    /**
     * This will be called by the ALMSListener (ContextListener) during web application start up
     * Individual pieces can be retried later if failed    
     */
    public static void init() {
        long time1, time2;
        
        time1 = System.currentTimeMillis();
        refreshCommonProperties();
        refreshReferences();

        time2 = System.currentTimeMillis();
        if (logger.isLoggable(Level.FINE)) {
            CommonReferences.checkObjects();
            System.out.println("Time taken to initialize CommonReferences: " + (time2-time1) + "ms");
        }
        
    }
    
    /**
     * Getting properties from AT_CMN_PROPERTY
     * 
     * Used by interface and login code
     * 
     * @return Properties
     */
    public static Properties getCommonProperties() {
        if (commonProperties == null) {
            refreshCommonProperties();
        }
        return commonProperties;
    }

    /**
     * Provides a manual way to force re-read from DB
     */
    public static void refreshCommonProperties() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.refreshCommonProperties");
            statement = conn.prepareStatement(SQL_COMMON_PROPERTIES);
            rs = statement.executeQuery();
            // (Re)initialize bundle only if SQL returns successfully
            commonProperties = new Properties();
            while (rs.next()) {
                commonProperties.setProperty(rs.getString(1), rs.getString(2));
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error reading Common Properties from DB: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.refreshCommonProperties");
        }
    }

    /**
     * This initialization could be triggered without an active user session
     * e.g. state list from setStateCode.jsp during national guard user logins with no updates
     * @param locator
     */
    public static void refreshReferences() {
        getDomainWorld();
        initDummyLesson();
        getWBTOfferingACCPAutomaticNotice();
        getJobFamilyOther();
        initBUOther();
        initBUDoD();
        getCurrencyUSD();
        initLocaleEnglish();
        initTimeZoneEastern();
        initMandatoryVideoID();
        initStateLists();
        initRosterInputOutputCodes();
        initRosterReasons();
    }
    
    public static void checkObjects() {
        System.out.println("Checking CommonReferences objects...");
        System.out.println("World domain: " + WORLD_DOMAIN);
        System.out.println("Dummy template: " + OFFER_TEMPLATE_DUMMY);
        System.out.println("Dummy offering: " + WBTOFFERING_DUMMY);
        System.out.println("ACCP Notice offering: " + WBTOFFERING_ACCP_AUTOMATIC_NOTICE);
        System.out.println("'Other' Job family: " + OTHER_JOB_FAMILY);
        System.out.println("'Other' Bus. unit: " + BU_OTHER);
        System.out.println("'DoD' Bus. unit: " + BU_DOD);
        System.out.println("US Currency: " + CURRENCY_US);
        System.out.println("English locale: " + LOCALE_ENGLISH);
        System.out.println("Eastern timezone: " + TIMEZONE_EASTERN);
        System.out.println("Mandatory Video ID: " + MANDATORY_VIDEO_CNINV);
        System.out.println("Sec List, All ALMS: " + SECLIST_ALL_ALMS);
        System.out.println("Sec List, removal list for Army: " + SECLIST_REMOVE_LIST_ARMY);
        System.out.println("Sec List, removal list for Foreign: " + SECLIST_REMOVE_LIST_FOREIGN);
        System.out.println("Aud List, All ALMS: " + AUDTYPE_ALL_ALMS);
       System.out.println("State code list: " + STATE_CODE_LIST);
        System.out.println("State name list: " + STATE_NAME_LIST);
        System.out.println("Roster Input codes: " + ROSTER_INPUT_CODES);
        System.out.println("Roster Output codes: " + ROSTER_OUTPUT_CODES);
        System.out.println("Roster Hold reaons: " + ROSTER_HOLD_REASONS);
        System.out.println("Roster Non-Hold reasons: " + ROSTER_NON_HOLD_REASONS);
    }
    
    public static Domain getDomainWorld() {
        if (WORLD_DOMAIN == null) {
            try {
                WORLD_DOMAIN = (Domain) ServiceLocator.getReference("domin000000000000001");
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder("Error getting reference to Domain World: ");
                sb.append(CommonUtil.stackTraceToString(ex));
                logger.severe(sb.toString());
                WORLD_DOMAIN = null;
            }
        }
        return WORLD_DOMAIN;
    }

    public static String getDummyLessonName() {
        if (DUMMY_LESSON_NAME == null) {
            initDummyLesson();
        }
        return DUMMY_LESSON_NAME;
    }
    
    public static String getDummyLessonOfferingID() {
        if (DUMMY_LESSON_OFFERING_ID == null) {
            initDummyLesson();
        }
        return DUMMY_LESSON_OFFERING_ID;
    }
    
    public static String getDummyLessonTemplateID() {
        if (DUMMY_LESSON_TEMPLATE_ID == null) {
            initDummyLesson();
        }
        return DUMMY_LESSON_TEMPLATE_ID;
    }
    
    public static OfferingTemplate getOfferingTemplateDummyLesson() {
        if (OFFER_TEMPLATE_DUMMY == null) {
            initDummyLesson();
        }
        return OFFER_TEMPLATE_DUMMY;
    }

    public static WBTOffering getWBTOfferingDummyLesson(ServiceLocator locator) {
        if (WBTOFFERING_DUMMY == null) {
            initDummyLesson();
        }
        return WBTOFFERING_DUMMY;
    }

    protected static void initDummyLesson() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initDummyLesson");
            DUMMY_LESSON_NAME = getCommonProperties().getProperty("DUMMY_LESSON_NAME");
            statement = conn.prepareStatement(SQL_DUMMY_LESSON_ID);
            statement.setString(1, DUMMY_LESSON_NAME.toLowerCase());
            rs = statement.executeQuery();
            if (rs.next()) {
            	DUMMY_LESSON_OFFERING_ID = rs.getString(1);
                WBTOFFERING_DUMMY = (WBTOffering)ServiceLocator.getReference(DUMMY_LESSON_OFFERING_ID);
                DUMMY_LESSON_TEMPLATE_ID = rs.getString(2);
                OFFER_TEMPLATE_DUMMY = (OfferingTemplate)ServiceLocator.getReference(DUMMY_LESSON_TEMPLATE_ID);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing Dummy Lesson: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            WBTOFFERING_DUMMY = null;
            OFFER_TEMPLATE_DUMMY = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initDummyLesson");
        }
    }

    public static WBTOffering getWBTOfferingACCPAutomaticNotice() {
        if (WBTOFFERING_ACCP_AUTOMATIC_NOTICE == null) {
            Connection conn = null;
            ResultSet rs = null;
            PreparedStatement statement = null;
            try {
                conn = DBUtil.getDefaultSiteConnection("CommonReferences.getWBTOfferingACCPAutomaticNotice");
                String id = null;
                statement = conn.prepareStatement(SQL_ACCP_NOTICE_ID);
                statement.setString(1, getCommonProperties().getProperty("ACCP_NOTICE_NAME").toLowerCase());
                rs = statement.executeQuery();
                if (rs.next()) {
                    id = rs.getString(1);
                    WBTOFFERING_ACCP_AUTOMATIC_NOTICE = (WBTOffering)ServiceLocator.getReference(id);
                }
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder("Error getting reference to WBTOffering ACCP: ");
                sb.append(CommonUtil.stackTraceToString(ex));
                logger.severe(sb.toString());
                WBTOFFERING_ACCP_AUTOMATIC_NOTICE = null;
            } finally {
                DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.getWBTOfferingACCPAutomaticNotice");
            }
        }
        return WBTOFFERING_ACCP_AUTOMATIC_NOTICE;
    }

    public static JobFamily getJobFamilyOther() {
        if (OTHER_JOB_FAMILY == null) {
            Connection conn = null;
            ResultSet rs = null;
            PreparedStatement statement = null;
            try {
                conn = DBUtil.getDefaultSiteConnection("CommonReferences.getJobFamilyOther");
                String id = null;
                statement = conn.prepareStatement(SQL_JOB_FAMILY_ID);
                statement.setString(1, "other");
                rs = statement.executeQuery();
                if (rs.next()) {
                    id = rs.getString(1);
                    OTHER_JOB_FAMILY = (JobFamily)ServiceLocator.getReference(id);
                }
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder("Error getting reference to Job Family Other: ");
                sb.append(CommonUtil.stackTraceToString(ex));
                logger.severe(sb.toString());
                OTHER_JOB_FAMILY = null;
            } finally {
                DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.getJobFamilyOther");
            }
        }
        return OTHER_JOB_FAMILY;
    }

    public static BusinessUnit getBUOther() {
        if (BU_OTHER == null) {
            initBUOther();
        }
        return BU_OTHER;
    }
    
    public static String getBUIDOther() {
        if (BU_ID_OTHER == null) {
            initBUOther();
        }
        return BU_ID_OTHER;
    }

    protected static void initBUOther() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initBUOther");
            statement = conn.prepareStatement(SQL_BU_ID);
            statement.setString(1, "other");
            rs = statement.executeQuery();
            if (rs.next()) {
                BU_ID_OTHER = rs.getString(1);
                BU_OTHER = (BusinessUnit)ServiceLocator.getReference(BU_ID_OTHER);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing BU Other: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            BU_ID_OTHER = null;
            BU_OTHER = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initBUOther");
        }
    }

    public static BusinessUnit getBUDoD() {
        if (BU_DOD == null) {
            initBUDoD();
        }
        return BU_DOD;
    }

    public static String getBUIDDoD() {
        if (BU_ID_DOD == null) {
            initBUDoD();
        }
        return BU_ID_DOD;
    }
    
    protected static void initBUDoD() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initBUDoD");
            statement = conn.prepareStatement(SQL_BU_ID);
            statement.setString(1, "dod");
            rs = statement.executeQuery();
            if (rs.next()) {
                BU_ID_DOD = rs.getString(1);
                BU_DOD = (BusinessUnit)ServiceLocator.getReference(BU_ID_DOD);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing BU DoD: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            BU_ID_DOD = null;
            BU_DOD = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initBUDoD");
        }
    }

    /**
     * Get a reference to US Dollar Currency object.
     * @param locator
     * @return
     */
    public static SabaCurrency getCurrencyUSD() {
        if (CURRENCY_US == null) {
            initCurrencyUSD();
        }
        return CURRENCY_US;
    }
    
    protected static void initCurrencyUSD() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initCurrencyUSD");
            statement = conn.prepareStatement(SQL_CURRENCY_USD_ID);
            rs = statement.executeQuery();
            if (rs.next()) {
                CURRENCY_US = (SabaCurrency)ServiceLocator.getReference(rs.getString(1));
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error getting reference to Currency USD: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            CURRENCY_US = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initCurrencyUSD");
        }
    }

    /**
     * Get Saba Locale English.
     * @param locator
     * @return
     */
    public static SabaLocale getLocaleEnglish() {
        if (LOCALE_ENGLISH == null) {
            initLocaleEnglish();
        }
        return LOCALE_ENGLISH;
    }
    
    /**
     * Get Saba Locale ID for English.
     * @param locator
     * @return
     */
    public static String getLocaleIDEnglish() {
        if (LOCALE_ID_ENGLISH == null) {
            initLocaleEnglish();
        }
        return LOCALE_ID_ENGLISH;
    }

    /**
     * Initialize Saba Locale English.
     * @param locator
     * @return
     */
    protected static void initLocaleEnglish() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initLocaleEnglish");
            statement = conn.prepareStatement(SQL_LOCALE_ID);
            statement.setString(1, Constants.LOCALE_NAME_ENGLISH.toLowerCase());
            rs = statement.executeQuery();
            if (rs.next()) {
                LOCALE_ID_ENGLISH = rs.getString(1);
                LOCALE_ENGLISH = (SabaLocale)ServiceLocator.getReference(LOCALE_ID_ENGLISH);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error getting reference to Locale English: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            LOCALE_ENGLISH = null;
            LOCALE_ID_ENGLISH = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initLocaleEnglish");
        }
    }

    /**
     * Get Saba TimeZone Eastern time.
     * @param locator
     * @return
     */
    public static SabaTimeZone getTimeZoneEastern() {
        if (TIMEZONE_EASTERN == null) {
            initTimeZoneEastern();
        }
        return TIMEZONE_EASTERN;
    }
    
    /**
     * Get Saba TimeZone ID for Eastern time.
     * @param locator
     * @return
     */
    public static String getTimeZoneIDEastern() {
        if (TIMEZONE_ID_EASTERN == null) {
            initTimeZoneEastern();
        }
        return TIMEZONE_ID_EASTERN;
    }

    /**
     * Initialize Saba TimeZone Eastern time.
     * @param locator
     * @return
     */
    protected static void initTimeZoneEastern() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initTimeZoneEastern");
            statement = conn.prepareStatement(SQL_TIMEZONE_ID);
            statement.setString(1, Constants.TIMEZONE_NAME_EASTERN);
            rs = statement.executeQuery();
            if (rs.next()) {
                TIMEZONE_ID_EASTERN = rs.getString(1);
                TIMEZONE_EASTERN = (SabaTimeZone)ServiceLocator.getReference(TIMEZONE_ID_EASTERN);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error getting reference to Timezone Eastern: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            TIMEZONE_ID_EASTERN = null;
            TIMEZONE_EASTERN = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initTimeZoneEastern");
        }
    }
    
    /**
     * Gets the CNINV ID of the Mandatory training video
     * @return
     */
    public static String getMandatoryVideoID() {
        if (MANDATORY_VIDEO_CNINV == null) {
            initMandatoryVideoID();
        }
        return MANDATORY_VIDEO_CNINV;
    }
    
    protected static void initMandatoryVideoID() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initMandatoryVideoID");
            statement = conn.prepareStatement(SQL_MANDATORY_VIDEO_ID);
            statement.setString(1, getCommonProperties().getProperty("MANDATORY_VIDEO_CI_NAME").toLowerCase());
            rs = statement.executeQuery();
            if (rs.next()) {
                MANDATORY_VIDEO_CNINV = rs.getString(1);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error getting ID of mandatory training video: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
            MANDATORY_VIDEO_CNINV = null;
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initMandatoryVideoID");
        }
    }
    
  /*  public static ArrayList<String> getAudTypeRemovalListForArmy() {
        if (AUDTYPE_REMOVE_LIST_ARMY == null) {
            initALMSAudTypeLists();
        }
        return AUDTYPE_REMOVE_LIST_ARMY;
    }

    public static ArrayList<String> getAudTypeRemovalListForCivilian() {
        if (AUDTYPE_REMOVE_LIST_CIVILIAN == null) {
            initALMSAudTypeLists();
        }
        return AUDTYPE_REMOVE_LIST_CIVILIAN;
    }

    public static ArrayList<String> getAudTypeRemovalListForForeign() {
        if (AUDTYPE_REMOVE_LIST_FOREIGN == null) {
            initALMSAudTypeLists();
        }
        return AUDTYPE_REMOVE_LIST_FOREIGN;
    }

    public static ArrayList<String> getAudTypeAllALMS() {
        if (AUDTYPE_ALL_ALMS == null) {
            initALMSAudTypeLists();
        }
        return AUDTYPE_ALL_ALMS;
    }*/
    
    

 /*   public static ArrayList<String> getSecListRemoveListForArmy() {
        if (SECLIST_REMOVE_LIST_ARMY == null) {
            initMLMSSecLists();
        }
        return SECLIST_REMOVE_LIST_ARMY;
    }
    
    public static ArrayList<String> getSecListRemoveListForForeign() {
        if (SECLIST_REMOVE_LIST_FOREIGN == null) {
            initMLMSSecLists();
        }
        return SECLIST_REMOVE_LIST_FOREIGN;
    }
    
    public static ArrayList<String> getSecListAllMLMS() {
        if (SECLIST_ALL_ALMS == null) {
            initMLMSSecLists();
        }
        return SECLIST_ALL_ALMS;
    }*/
    
    

    public static ArrayList<String> getStateCodeList() {
        if (STATE_CODE_LIST == null) {
            initStateLists();
        }
        return STATE_CODE_LIST;
    }
    
    public static ArrayList<String> getStateNameList() {
        if (STATE_NAME_LIST == null) {
            initStateLists();
        }
        return STATE_NAME_LIST;
    }
    
    protected static void initStateLists() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initStateLists");
            statement = conn.prepareStatement(SQL_STATE_LIST);
            rs = statement.executeQuery();
            STATE_CODE_LIST = new ArrayList<String>();
            STATE_NAME_LIST = new ArrayList<String>();
            while (rs.next()) {
                STATE_CODE_LIST.add(rs.getString(1));
                STATE_NAME_LIST.add(rs.getString(2));
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing state lists: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initStateLists");
        }
    }

    public static ArrayList<RosterInsert.IOCode> getRosterInputCodes() {
        if (ROSTER_INPUT_CODES == null) {
            initRosterInputOutputCodes();
        }
        return ROSTER_INPUT_CODES;
    }

    public static ArrayList<RosterInsert.IOCode> getRosterOutputCodes() {
        if (ROSTER_OUTPUT_CODES == null) {
            initRosterInputOutputCodes();
        }
        return ROSTER_OUTPUT_CODES;
    }

    /**
     * Get status code action
     * @param statusCode
     * @return null if not found; non-null Action code could be empty string
     * @throws DBUtilException
     * @throws RegistrationException
     */
    public static String getActionByStatusCode(String statusCode) throws DBUtilException, RegistrationException {
        if (ROSTER_STATUS_ACTION_MAP == null) {
            initRosterInputOutputCodes();
        }
        return ROSTER_STATUS_ACTION_MAP.get(statusCode);
    }

    protected static void initRosterInputOutputCodes() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        String io, code, desc, tempStr, action;

        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initRosterInputOutputCodes");
            statement = conn.prepareStatement(SQL_ROSTER_INPUT_OUTPUT_CODE);
            rs = statement.executeQuery();
            ROSTER_INPUT_CODES = new ArrayList<RosterInsert.IOCode>();
            ROSTER_OUTPUT_CODES = new ArrayList<RosterInsert.IOCode>();
            ROSTER_STATUS_ACTION_MAP = new HashMap<String, String>();
            while (rs.next()) {
                io = rs.getString(1);
                code = rs.getString(2);
                desc = rs.getString(3);
                tempStr = rs.getString(4);
                action = rs.getString(5);
                if (action == null) {
                    action = "";
                } else {
                    action = action.trim();
                }
                if (io.equals("I")) {
                    if (!(code.equals("J") || code.equals("Q"))) {
                        ROSTER_INPUT_CODES.add(new RosterInsert.IOCode(code, desc, tempStr.equals("Y")));
                    } // ignore otherwise, from 5.5 logic
                } else if (io.equals("O") || io.equals("IO")){
                    // "IO" or "O" are counted as output codes
                    ROSTER_OUTPUT_CODES.add(new RosterInsert.IOCode(code, desc, tempStr.equals("Y")));
                } // R doesn't belong to I or O, but we will include it in the action map
                ROSTER_STATUS_ACTION_MAP.put(code, action);
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing roster page IO code lists: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initRosterInputOutputCodes");
        }
    }

    public static ArrayList<RosterInsert.IOReason> getRosterHoldReasons() {
        if (ROSTER_HOLD_REASONS == null) {
            initRosterReasons();
        }
        return ROSTER_HOLD_REASONS;
    }

    public static ArrayList<RosterInsert.IOReason> getRosterNonHoldReasons() {
        if (ROSTER_NON_HOLD_REASONS == null) {
            initRosterReasons();
        }
        return ROSTER_NON_HOLD_REASONS;
    }

    protected static void initRosterReasons() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        String code, desc, tempStr;

        try {
            conn = DBUtil.getDefaultSiteConnection("CommonReferences.initRosterReasons");
            statement = conn.prepareStatement(SQL_ROSTER_REASON);
            rs = statement.executeQuery();
            ROSTER_HOLD_REASONS = new ArrayList<RosterInsert.IOReason>();
            ROSTER_NON_HOLD_REASONS = new ArrayList<RosterInsert.IOReason>();
            while (rs.next()) {
                code = rs.getString(1);
                desc = rs.getString(2);
                tempStr = rs.getString(3);
                if (tempStr != null && tempStr.equals("H")) {
                    ROSTER_HOLD_REASONS.add(new RosterInsert.IOReason(code, desc));
                } else {
                    // "IO" is counted as output code
                    ROSTER_NON_HOLD_REASONS.add(new RosterInsert.IOReason(code, desc));
                }
            }
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder("Error initializing roster page reason code lists: ");
            sb.append(CommonUtil.stackTraceToString(ex));
            logger.severe(sb.toString());
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "CommonReferences.initRosterReasons");
        }
    }

	public static ArrayList<String> getAudTypeAllMLMS() {
		// TODO Auto-generated method stub
		return null;
	}
    
}

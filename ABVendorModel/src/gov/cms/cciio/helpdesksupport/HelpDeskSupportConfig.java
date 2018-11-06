package gov.cms.cciio.helpdesksupport;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Types;
import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Properties;
//import java.util.StringTokenizer;

import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.DBUtil;

/*
 * USERNAME token not detected in header = DEV env or behind firewall access, Page will default to username "N/A".
 * AT_MLMS_SPECIAL_ACCESS table
 * USERNAME               Username in upper case, if not detected in header, default to "N/A"
 * USE_TOOLS       (Y/N)  Whether one can use the custom tools
 * CONTROL_ACCESS  (Y/N)  Whether one can control this access list
 */
public class HelpDeskSupportConfig {
    // Hard code the following string, do not reference DEFAULT_IE_HOME because all configurations are retrieved from this file
    //protected static final String PROPERITES_FILE  = "/app/WPS_DLS/projects/IESupport.properties";
//    protected static final String PROPERITES_FILE  = "/app/Saba7/SabaWeb/web/SabaSite/properties/dls_ldap_auth.properties";
//    protected static final String BOOTUP_FILE  = "/app/Saba7/SabaWeb/properties/bootUpConfig.xml";
//    // Access list (one AKO USERNAME per line)
//    protected static final String ACCESS_LIST_FILE = "/app/Saba7/SabaWeb/web/SabaSite/properties/LMSSupport.access";

//    protected static final String DEFAULT_LMSDB_READ_JNDI = "jdbc/lmsdb_read";
//    protected static final String DEFAULT_IEDB_JNDI       = "jdbc/IEDB";
//    protected static final String DEFAULT_RPTDB_JNDI      = "jdbc/RPTDB"; // LMS downstream DB URL for report queries.

    //protected static final String DEFAULT_DB_ACCESS_LIST  = "tp2_read1";//"tp2_read1,wpscomm_read1";
    public static final String TOP_LINK_HTML = constructTopLink();
    public static final String NO_ACCESS_HTML_MESSAGE = "<p><h2>You do not have permissions to access this page.</h2></p>";

    protected static final String DEFAULT_ADMIN = "DEFAULT_ADMIN";
    protected static final String DEFAULT_USER  = "N/A";

    protected static final String SQL_CAN_USE_TOOLS          = "SELECT COUNT(*) FROM AT_MLMS_SPECIAL_ACCESS WHERE USERNAME=UPPER(?) AND USE_TOOLS='Y'";
    protected static final String SQL_IS_ACCESS_LIST_ADMIN   = "SELECT COUNT(*) FROM AT_MLMS_SPECIAL_ACCESS WHERE USERNAME=UPPER(?) AND CONTROL_ACCESS='Y'";
    //protected static final String SQL_IEFILEMANE_ACCESS_LIST = "SELECT USERNAME FROM AT_LMS_SPECIAL_ACCESS a INNER JOIN CMT_PERSON p ON a.ID=p.ID WHERE (a.INT_USER='1' OR a.IS_ADMIN='1') ORDER BY USERNAME ASC";
    protected static final String SQL_FULL_ACCESS_LIST = "SELECT USERNAME, USE_TOOLS, CONTROL_ACCESS FROM AT_MLMS_SPECIAL_ACCESS ORDER BY USERNAME ASC";
    protected static final String SQL_INSERT_ACCESS = "INSERT INTO AT_MLMS_SPECIAL_ACCESS (USERNAME) VALUES (UPPER(?))";
    protected static final String SQL_UPDATE_ACCESS = "UPDATE AT_MLMS_SPECIAL_ACCESS SET USE_TOOLS=?, CONTROL_ACCESS=? WHERE USERNAME=UPPER(?)";
    protected static final String SQL_DELETE_ACCESS = "DELETE FROM AT_MLMS_SPECIAL_ACCESS WHERE USERNAME=UPPER(?)";

//    // Properties
//    protected static boolean PRODUCTION_MODE = true;
//    protected static String LMSDB_READ_JNDI = null;
//    protected static String IEDB_JNDI = null;
//    protected static String RPTDB_JNDI = null;
//    protected static String LMS_CONN_STRING = "";
//    protected static String IE_CONN_STRING = "";
//    protected static String RPT_CONN_STRING = "";
//    //protected static String IE_HOME = "";
//    //protected static String BPM_HOME = "";
//    protected static String ENVIRONMENT = "";
    protected static int SQL_EDITFIELD_COLS = 0;
    protected static int SQL_EDITFIELD_ROWS = 0;
//    protected static HashSet<String> DB_ACC_ALLOW_LIST = new HashSet<String>();
//    /**
//     *  LMSSupport access list based on properties file
//     *  Ignored if DB Access is available
//     */ 
//    protected static HashSet<String> ACCESS_LIST_BACKUP = new HashSet<String>();
//    /**
//     *  Whether access control is on, based on properties file
//     *  Ignored if DB Access is available
//     */
//    protected static boolean ACCESS_CONTROL_BACKUP = true;
//    
//    // Internal
//    protected static long PROP_FILE_LAST_MODIFIED   = 0L;
//    protected static long ACCESS_FILE_LAST_MODIFIED = 0L;

    //static {
        //refreshSettings();
        //syncAccessListToPropFile();
    //}

    private static String constructTopLink() {
        StringBuilder sb = new StringBuilder("<ul class=\"toplink\">\n");

        sb.append("<li><a href=\"MLMSfixit.jsp\">[MLMS fixit]</a></li>\n");
        sb.append("<li><a href=\"fileUploadDialog.jsp\">[File Upload Dialog]</a></li>\n");
        sb.append("<li><a href=\"Tool_AccessList.jsp\">[Access Control]</a></li>\n");
        sb.append("</ul>");
        return sb.toString();
    }
//    protected static void readBootUpConfig() {
//        BufferedReader reader = null;
//        try {
//            // Only getting DB Connection string here
//            reader = new BufferedReader(new FileReader(BOOTUP_FILE));
//            String line;
//            int index1, index2;
//            while ((line = reader.readLine()) != null) {
//                if ((index1 = line.indexOf("jdbc:oracle:thin")) > 0) {
//                    if ((index2 = line.indexOf(']', index1)) > 0) {
//                        LMS_CONN_STRING = line.substring(index1, index2).trim();
//                        //System.out.println("Read LMS connection string: " + LMS_CONN_STRING);
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error reading " + PROPERITES_FILE + ": " + e);
//            LMS_CONN_STRING = "";
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (Exception ex) {
//                }
//            }
//        }
//    }
    
//    /**
//     *  Refresh/Read settings if the properties file is updated
//     */
//    public static void refreshSettings() {
//        boolean refreshProperties = false;
//        String dbAccListStr = null;
//        String tempStr = null;
//        File file = new File(PROPERITES_FILE);
//        try {
//            long lastMod = file.lastModified();
//            if (lastMod > PROP_FILE_LAST_MODIFIED) {
//                refreshProperties = true;
//                PROP_FILE_LAST_MODIFIED = lastMod;
//            }
//        } catch (Exception e) {
//            System.err.println("Error reading timestamp from " + PROPERITES_FILE + ": " + e);
//        }
//        if (refreshProperties) {
//            try {
//                InputStream readATTRS = null;
//                Properties prop = new Properties();
//                try {
//                    readATTRS = new FileInputStream(PROPERITES_FILE);
//                    prop.load(readATTRS);            
//                } catch (Exception e) {
//                    System.err.println(e.toString());
//                } finally {
//                    if (readATTRS != null) {
//                        try {
//                            readATTRS.close();
//                        } catch (Exception ex ){
//                        }
//                    }
//                }
//                try {
//                    ENVIRONMENT         = prop.getProperty("ENVIRONMENT");
//                    dbAccListStr        = prop.getProperty("DB_ACC_ALLOWED");
//                } catch (Exception e){
//                    System.err.println(e.toString());
//                }
//            } catch (Exception e) {
//                System.err.println("Error reading " + PROPERITES_FILE + ": " + e);
//            }
//            
//            if (ENVIRONMENT == null) {
//                ENVIRONMENT = "DEV";
//            }
//            SQL_EDITFIELD_COLS  = 50;
//            SQL_EDITFIELD_ROWS  = 5;
//            
//            // Default JNDI values
//            if (LMSDB_READ_JNDI == null || LMSDB_READ_JNDI.trim().length() == 0) {
//                LMSDB_READ_JNDI = DEFAULT_LMSDB_READ_JNDI;
//            }
//            if (IEDB_JNDI == null || IEDB_JNDI.trim().length() == 0) {
//                IEDB_JNDI = DEFAULT_IEDB_JNDI;
//            }
//            if (RPTDB_JNDI == null || RPTDB_JNDI.trim().length() == 0) {
//                RPTDB_JNDI = DEFAULT_RPTDB_JNDI;
//            }
//            // DB account allow list
//            DB_ACC_ALLOW_LIST = new HashSet<String>();
//            if (dbAccListStr == null) {
//                dbAccListStr = DEFAULT_DB_ACCESS_LIST;
//            }
//            StringTokenizer tokenizer = new StringTokenizer(dbAccListStr, ",");
//            while (tokenizer.hasMoreTokens()) {
//                tempStr = tokenizer.nextToken().trim().toLowerCase();
//                if (tempStr.length() > 0) {
//                    DB_ACC_ALLOW_LIST.add(tempStr);
//                }
//            }
//        }
//    }
    
//    /**
//     * Writes the Access List to the properties file as back up
//     */
//    public static void syncAccessListToPropFile() {
//        StringBuilder errorSB = new StringBuilder();
//        PrintWriter writer = null;
//        try {
//            writer = new PrintWriter(new FileOutputStream(ACCESS_LIST_FILE));
//            Connection conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.syncAccessListToPropFile");
//            PreparedStatement statement = null;
//            ResultSet rSet = null;
//            if (conn == null) {
//                System.err.println("HelpDeskSupportConfig.syncAccessListToPropFile: Error obtaining DB connection");
//                System.err.println(errorSB);
//            } else {
//                try {
//                    statement = conn.prepareStatement(SQL_IEFILEMANE_ACCESS_LIST);
//                    rSet = statement.executeQuery();
//                    while (rSet.next()) {
//                        writer.println(rSet.getString(1));
//                    }
//                } catch (Exception ex) {
//                    System.err.println("HelpDeskSupportConfig.syncAccessListToPropFile: Error sync access list to " + ACCESS_LIST_FILE);
//                    System.err.println(CommonUtil.stackTraceToString(ex));
//                } finally {
//                    // close the database connection
//                    DBUtil.freeDefaultSiteDBResources(rSet, statement, conn, "HelpDeskSupportConfig.syncAccessListToPropFile");
//                }
//            }
//        } catch (Exception ex) {
//            System.err.println("Error reading " + ACCESS_LIST_FILE + ": " + ex);
//        } finally {
//            if (writer != null) {
//                try {
//                    writer.close();
//                } catch (Exception ex) {
//                    // Ignore
//                }
//            }
//        }
//    }
    
//    public static String getEnvironmentName() {
//        return ENVIRONMENT;
//    }
//
//    public static boolean isProductionMode() {
//        return PRODUCTION_MODE;
//    }
//
//    public static String getLMSConnString() {
//        if (LMS_CONN_STRING == null || LMS_CONN_STRING.length() == 0) {
//            readBootUpConfig();
//        }
//        return LMS_CONN_STRING;
//    }
//
//    public static String getLMSDB_READ_JNDI() {
//        return LMSDB_READ_JNDI;
//    }
//    public static String getIEDB_JNDI() {
//        return IEDB_JNDI;
//    }
//    
//    public static String getRPTDB_JNDI() {
//        return RPTDB_JNDI;
//    }
//
//    public static String getIEConnString() {
//        return IE_CONN_STRING;
//    }
//    
//    public static String getRPTConnString() {
//        return RPT_CONN_STRING;
//    }
//
//    public static int getSQLEditFieldCols() {
//        return SQL_EDITFIELD_COLS;
//    }
//    
//    public static int getSQLEditFieldRows() {
//        return SQL_EDITFIELD_ROWS;
//    }
    
    /**
     * Try DB lookup first, fall back to properties file if DB error.
     * In case of error, it returns false without reporting the error
     * @param akoUsername
     * @return
     */
    public static boolean canAccessSupportTools(String akoUsername) {
        return canAccessSupportTools(akoUsername, null);
    }
    
    /**
     * Try DB lookup first, fall back to properties file if DB error.
     * @param username
     * @param errorSB holds error information, can be null
     * @return
     */
    public static boolean canAccessSupportTools(String username, StringBuilder errorSB) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rSet = null;
        int tempInt = -1;

        if (username == null || username.length() == 0) {
            username = DEFAULT_USER;
        }
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.canAccessSupportTools");
            statement = conn.prepareStatement(SQL_CAN_USE_TOOLS);
            // null AKO Username will trigger an exception
            // SiteMinder should prevent this from happening,
            // fall back to properties file in DEV environment
            statement.setString(1, username.toUpperCase());
            rSet = statement.executeQuery();
            rSet.next();
            tempInt = rSet.getInt(1);
        } catch (Exception ex) {
            //System.out.println("IESupporConfig.isAccessAllowed error reading DB: " + ex);
            if (errorSB != null) {
                errorSB.append("DB Error: ");
                errorSB.append(ex);
            }
            tempInt = -1;
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(rSet, statement, conn, "HelpDeskSupportConfig.canAccessSupportTools");
            // Set strBuffOutput to Null if error closing connection? Old code does.
        }
        return tempInt > 0;
    }

    public static void updateAccess(String username, boolean useTools, boolean controlAccess, StringBuilder strBuffError) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig-updateAccess");
            statement = conn.prepareStatement(SQL_UPDATE_ACCESS);
            statement.setString(1, useTools?"Y":"N");
            statement.setString(2, controlAccess?"Y":"N");
            statement.setString(3, username);
            statement.executeUpdate();
        } catch (Exception ex) {
            if (strBuffError != null) {
                strBuffError.append(CommonUtil.stackTraceToString(ex));
            }
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(null, statement, conn, "HelpDeskSupportConfig-updateAccess");
        }
    }
    
    public static void addEntry(String username, StringBuilder strBuffError) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig-add");
            statement = conn.prepareStatement(SQL_INSERT_ACCESS);
            statement.setString(1, username.trim());
            statement.executeUpdate();
        } catch (Exception ex) {
            if (strBuffError != null) {
                strBuffError.append(CommonUtil.stackTraceToString(ex));
            }
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(null, statement, conn, "HelpDeskSupportConfig-addEntry");
        }
    }

    public static void deleteEntry(String username, StringBuilder strBuffError) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig-deleteEntry");
            statement = conn.prepareStatement(SQL_DELETE_ACCESS);
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (Exception ex) {
            if (strBuffError != null) {
                strBuffError.append(CommonUtil.stackTraceToString(ex));
            }
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(null, statement, conn, "HelpDeskSupportConfig-deleteEntry");
        }
    }
    
    /**
     * Try DB lookup first, fall back to properties file if DB error.
     * @param username
     * @param errorSB holds error information, can be null
     * @return
     */
    public static boolean isAccessListAdmin(String username, StringBuilder errorSB) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rSet = null;
        int tempInt = -1;

        if (username == null || username.length() == 0) {
        	username = DEFAULT_ADMIN;
        }
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.isAccessListAdmin");
            statement = conn.prepareStatement(SQL_IS_ACCESS_LIST_ADMIN);
            statement.setString(1, username.toUpperCase());
            rSet = statement.executeQuery();
            rSet.next();
            tempInt = rSet.getInt(1);
        } catch (Exception ex) {
            //System.out.println("IESupporConfig.isAccessListAdmin error reading DB: " + ex);
            if (errorSB != null) {
                errorSB.append(CommonUtil.stackTraceToString(ex));
            }
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(rSet, statement, conn, "HelpDeskSupportConfig.isAccessListAdmin");
            // Set strBuffOutput to Null if error closing connection? Old code does.
        }
        return tempInt > 0;
    }

    public static ArrayList<HelpDeskSupportUser> getFullAccessList(StringBuilder errorSB) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rSet = null;
        ArrayList<HelpDeskSupportUser> userList = new ArrayList<HelpDeskSupportUser>();
        HelpDeskSupportUser tempUser = null;
        
        try {
            conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.getFullAccessList");
            statement = conn.prepareStatement(SQL_FULL_ACCESS_LIST);
            rSet = statement.executeQuery();
            while (rSet.next()) {
                tempUser = new HelpDeskSupportUser();
                tempUser.setUserName(rSet.getString(1));
                tempUser.setUseTools(rSet.getString(2).equals("Y"));
                tempUser.setControlAccess(rSet.getString(3).equals("Y"));
                userList.add(tempUser);
            }
        } catch (Exception ex) {
            //System.out.println("IESupporConfig.isAccessListAdmin error reading DB: " + ex);
            if (errorSB != null) {
                errorSB.append(CommonUtil.stackTraceToString(ex));
            }
        } finally {
            // close the database connection
            DBUtil.freeDefaultSiteDBResources(rSet, statement, conn, "HelpDeskSupportConfig.getFullAccessList");
            // Set strBuffOutput to Null if error closing connection? Old code does.
        }
        return userList;
    }


    /*
     ***************************************************************
     * The following are carried from older code, 
     * used by the ALMSHelpDeskTool.jsp and ALMSHelpDeskAdmin.jsp
     * Mostly deal with the LOGIN_AS_OTHERS and IS_ADMIN flags ONLY
     ***************************************************************
     */
    
    
	/**
	 * Good idea to log out of the temporary session started by the constructor
	 */
	public void logoutTempSession() {
	    /*
	    SabaLogin.logout();
	    */
	}
//	
//	public ArrayList<String> addHelpDeskUser(String akoUsername){
//		ArrayList<String> aLResultList =  new ArrayList<String>();
//		Connection conn = null;
//		CallableStatement cstmt = null;
//		String userExists = null;
//		String userNameDoesNotExist = null;
//		String addSuccess = null;
//		try{
//			conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.addHelpDeskUser");
//			/*
//			 * AP_LMS_ADD_HELPDESK_SUPPORT returns (1)userExists,(2) usernameDoesNotExist 
//			 * (3)addSuccess
//			 */
//			
//			cstmt = conn.prepareCall("{ call AP_LMS_ADD_HELPDESK_SUPPORT(?,?,?,?) }");
//			cstmt.setString(1, akoUsername);
//			cstmt.registerOutParameter(2, Types.CHAR);
//			cstmt.registerOutParameter(3, Types.CHAR);
//			cstmt.registerOutParameter(4, Types.CHAR);
//			cstmt.execute();
//			
//			userExists = (String)cstmt.getObject(2);
//			userNameDoesNotExist = (String)cstmt.getObject(3);
//			addSuccess = (String)cstmt.getObject(4);
//		} catch(Exception e) {
//			StringBuilder strBuffMessage = new StringBuilder("");
//			
//			strBuffMessage.append(HelpDeskSupportConfig.class);
//			strBuffMessage.append(" addHelpDeskUser() Exception message: ");
//			strBuffMessage.append(e.getMessage());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception string: ");
//			strBuffMessage.append(e.toString());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception stack trace: ");
//			strBuffMessage.append(e.fillInStackTrace());
//			System.err.println(strBuffMessage);
//		} finally {
//			DBUtil.freeDefaultSiteDBResources(null, cstmt, conn, "HelpDeskSupportConfig.addHelpDeskUser");
//		}
//		aLResultList.add(addSuccess);
//		aLResultList.add(userExists);
//		aLResultList.add(userNameDoesNotExist);
//		return aLResultList;
//	}
//	
//	public void removeHelpDeskUser(String userid){
//		Connection conn = null;
//		CallableStatement cstmt = null;
//		try {
//			conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.removeHelpDeskUser");
//			cstmt = conn.prepareCall("{ call AP_LMS_REMOVE_HELPDESK_SUPPORT(?) }");
//			cstmt.setString(1,userid);
//			cstmt.executeUpdate();
//		} catch(Exception e) {
//			StringBuilder strBuffMessage = new StringBuilder("");
//			strBuffMessage.append(HelpDeskSupportConfig.class);
//			strBuffMessage.append(" removeHelpDeskUser() Exception message: ");
//			strBuffMessage.append(e.getMessage());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception string: ");
//			strBuffMessage.append(e.toString());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception stack trace: ");
//			strBuffMessage.append(e.fillInStackTrace());
//			System.err.println(strBuffMessage);
//		} finally {
//			DBUtil.freeDefaultSiteDBResources(null, cstmt, conn, "HelpDeskSupportConfig.removeHelpDeskUser");
//		}
//	}
//	
//	public ArrayList<HelpDeskSupportUser> getHelpDeskUsers() {
//		ArrayList<HelpDeskSupportUser> aLUserList =  new ArrayList<HelpDeskSupportUser>();
//		Connection conn = null;
//		CallableStatement cstmt = null;
//		ResultSet rs = null;
//		String sUserName = null;
//		String sUserId = null;
//		String isAdmin = null;
//		HelpDeskSupportUser hdUser = null;
//		try {
//			conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.getHelpDeskUsers");
//			/*
//			 * AP_LMS_GET_HELPDESK_SUPPORT returns (1)userId,(2) username 
//			 * (3)helpdesk_Admin
//			 */
//			
//			cstmt = conn.prepareCall("{ call AP_LMS_GET_HELPDESK_SUPPORT(?) }");
//			cstmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
//			cstmt.execute();
//			rs = (ResultSet) cstmt.getObject(1);
//			
//			while(rs.next()) {
//                sUserId = rs.getString(1);
//				sUserName = rs.getString(2);
//				isAdmin = rs.getString(3);
//				
//				hdUser = new HelpDeskSupportUser();
//				hdUser.setUserName(sUserName);
//				hdUser.setUserId(sUserId);
//				hdUser.setControlAccess(isAdmin.equals("1"));
//				
//				aLUserList.add(hdUser);
//			}
//		} catch(Exception e) {
//			StringBuilder strBuffMessage = new StringBuilder("");
//			strBuffMessage.append(HelpDeskSupportConfig.class);
//			strBuffMessage.append(" getHelpDeskUsers() Exception message: ");
//			strBuffMessage.append(e.getMessage());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception string: ");
//			strBuffMessage.append(e.toString());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception stack trace: ");
//			strBuffMessage.append(e.fillInStackTrace());
//			System.err.println(strBuffMessage);
//		} finally {
//			DBUtil.freeDefaultSiteDBResources(rs, cstmt, conn, "HelpDeskSupportConfig.getHelpDeskUsers");
//		}
//		
//		return aLUserList;
//	}
//	
//	/** This method is to check if the user has access to the helpdesk utility
//	 */
//	public HelpDeskSupportUser isAccessAllowed(String akoUsername) {
//		Connection conn = null;
//		CallableStatement cstmt = null;
//		ResultSet rs = null;
//		HelpDeskSupportUser hdUser = null;
//		hdUser = new HelpDeskSupportUser();
//		hdUser.setUserName(akoUsername);
//			
//		int err = 0;
//		
//		if (akoUsername == null) {
//			return hdUser;
//		}
//		
//		try {
//			err = 10;
//			conn = DBUtil.getDefaultSiteConnection("HelpDeskSupportConfig.isAccessAllowed");
//			cstmt = conn.prepareCall("{ call AP_LMS_HELPDESK_SUPPORT(?,?,?,?,?) }");
//			cstmt.setString(1, akoUsername);
//			cstmt.registerOutParameter(2, Types.VARCHAR);
//			cstmt.registerOutParameter(3, Types.VARCHAR);
//			cstmt.registerOutParameter(4, Types.VARCHAR);
//            cstmt.registerOutParameter(5, Types.VARCHAR);
//			cstmt.execute();
//			
//			err = 20;
//			
//			hdUser.setUseTools(cstmt.getString(2).equals("1"));
//			hdUser.setControlAccess(cstmt.getString(3).equals("1"));
//			hdUser.setHasLMSAdminPageAccess(cstmt.getString(4).equals("1"));
//			hdUser.setCanAccessSupportTools(cstmt.getString(5).equals("1"));
//		} catch (SQLException se) {
//			StringBuilder strBuffMessage = new StringBuilder();
//			strBuffMessage.append(HelpDeskSupportConfig.class);
//			strBuffMessage.append(" err postion = ");
//			strBuffMessage.append(err);
//			strBuffMessage.append(" isAccessAllowed SQLException message: ");
//			strBuffMessage.append(se.getMessage());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("SQLException state: ");
//			strBuffMessage.append(se.getSQLState());
//			strBuffMessage.append("SQLException error code: ");
//			strBuffMessage.append(se.getErrorCode());
//			System.err.println(strBuffMessage);
//		} catch (Exception e) {
//			StringBuilder strBuffMessage = new StringBuilder();
//			strBuffMessage.append(HelpDeskSupportConfig.class);
//			strBuffMessage.append(" err postion = ");
//			strBuffMessage.append(err);
//			strBuffMessage.append(" isAccessAllowed Exception message: ");
//			strBuffMessage.append(e.getMessage());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception string: ");
//			strBuffMessage.append(e.toString());
//			strBuffMessage.append("\n");
//			strBuffMessage.append("Exception stack trace: ");
//			strBuffMessage.append(e.fillInStackTrace());
//			System.err.println(strBuffMessage);
//		} finally {
//			DBUtil.freeDefaultSiteDBResources(rs, cstmt, conn, "HelpDeskSupportConfig.isAccessAllowed");
//		}
//		return hdUser;
//	}
}

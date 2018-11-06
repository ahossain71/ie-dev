package gov.cms.cciio.common.util;

/*
 * 2013-07-16   Feilung Wong    For Saba 7 
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.cms.cciio.common.exception.MLMSStatusCode;
import gov.cms.cciio.common.exception.DBUtilException;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
import com.saba.persist.ConnectionManager;
import com.saba.persist.business.ConnectionManagerDelegate;
import com.saba.sys.SabaSite;
import com.saba.util.Debug;

public class DBUtil {
    private static final String className = DBUtil.class.getName();
    private static final Logger logger = Logger.getLogger(className);
    
    private static final String DEFAULT_SITE_NAME = SabaSite.getSystemDefaultSiteName();
    
    /**
     * Retrieve DB Connection based on locator.  Use this connection
     * for DB queries.
     * 
     * @param locator - Service Locator retrieved at login. 
     * @param source - a String identifying the caller for logging purposes
     * @return DB Connection
     */
    public static Connection getConnection(ServiceLocator locator, String source) throws DBUtilException {
        try {
            ConnectionManager cm = locator.getConnectionManager();
            // Don't cache the SabaPrincipal().getSiteName() since different site (e.g. DemoSite) could be calling it
            Connection conn = cm.getConnection(locator.getSabaPrincipal().getSiteName());
            String tempStr = source + "> DBUtil.getConnection: obtain connection";
            logger.log(Level.FINER, tempStr);
            Debug.trace(tempStr);
            return conn;
        } catch (SabaException e) {
            StringBuilder errMsg = new StringBuilder(source);
            errMsg.append("> DBUtil.getConnection:");
            errMsg.append(CommonUtil.EOL);
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_GENERIC_INTERNAL, errMsg.toString(), e);
        } 
    }

    /**
     * Retrieve DB Connection through the default site.  Use this while no user login is required
     * 
     * @param source - a String identifying the caller for logging purposes
     * @return DB Connection
     */
    public static Connection getDefaultSiteConnection(String source) throws DBUtilException {
        try {
            ConnectionManagerDelegate connManager = new ConnectionManagerDelegate(DEFAULT_SITE_NAME);
            Connection conn = connManager.getConnection(DEFAULT_SITE_NAME);
            String tempStr = source + "> DBUtil.getDefaultSiteConnection: obtain connection";
            logger.log(Level.FINER, tempStr);
            Debug.trace(tempStr);
            return conn;
        } catch (SabaException e) {
            StringBuilder errMsg = new StringBuilder(source);
            errMsg.append("> DBUtil.getDefaultSiteConnection:");
            errMsg.append(CommonUtil.EOL);
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_GENERIC_INTERNAL, errMsg.toString(), e);
        } 
    }

    /**
     * Close ResultSet, Statement, and DB Connection.  Must pass the
     * same Service Locator used to retrieve Connection.
     * @param locator - Service Locator retrieved at login
     * @param rs - the ResultSet to close, can be null
     * @param statement - java.sql.Statement object to close, can be null
     * @param conn - Connection to close, can be null
     * @param source - a String identifying the caller for logging purposes
     * @throws DBUtilException - thrown if there is no connection manager associated with locator.
     */
    public static void freeDBResources(ServiceLocator locator, ResultSet rs, Statement statement, Connection conn, String source) {
        ConnectionManager cm = null;
        String tempStr;
        try {
            cm = locator.getConnectionManager();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "> DBUtil.freeDBResources: Error obtaining Connection Manager", ex);
        }
        try {
            if (rs != null) {
                rs.close();
                tempStr = source + "> DBUtil.freeDBResources: closed result set";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDBResources: Error closing result set: ", ex);
        }
        try {
            if (statement != null) {
                statement.close();
                tempStr = source + "> DBUtil.freeDBResources: closed JDBC statement";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDBResources: Error closing JDBC statement: ", ex);
        }
        try {
            if (cm != null && conn != null) {
                cm.freeConnection(conn);
                tempStr = source + "> DBUtil.freeDBResources: freed connection";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDBResources: Error freeing connection: ", ex);
        }
    }

    /**
     * Close ResultSet, Statement, and DB Connection obtained from getDefaultSiteConnection
     * @param rs - the ResultSet to close, can be null
     * @param statement - java.sql.Statement object to close, can be null
     * @param conn - Connection to close, can be null
     * @param source - a String identifying the caller for logging purposes
     * @throws DBUtilException - thrown if there is no connection manager associated with locator.
     */
    public static void freeDefaultSiteDBResources(ResultSet rs, Statement statement, Connection conn, String source) {
        ConnectionManagerDelegate cm = null;
        String tempStr;
        try {
            cm = new ConnectionManagerDelegate(DEFAULT_SITE_NAME);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "> DBUtil.freeDefaultSiteDBResources: Error obtaining Connection Manager", ex);
        }
        try {
            if (rs != null) {
                rs.close();
                tempStr = source + "> DBUtil.freeDefaultSiteDBResources: closed result set";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDefaultSiteDBResources: Error closing result set: ", ex);
        }
        try {
            if (statement != null) {
                statement.close();
                tempStr = source + "> DBUtil.freeDefaultSiteDBResources: closed JDBC statement";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDefaultSiteDBResources: Error closing JDBC statement: ", ex);
        }
        try {
            if (cm != null && conn != null) {
                cm.freeConnection(conn);
                tempStr = source + "> DBUtil.freeDefaultSiteDBResources: freed connection";
                logger.log(Level.FINER, tempStr);
                Debug.trace(tempStr);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, source + "> DBUtil.freeDefaultSiteDBResources: Error freeing connection: ", ex);
        }
    }
}

package gov.cms.cciio.common.util;
//TODO - TO BE UPDATED FOR SABA 7
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.saba.locator.ServiceLocator;
import com.saba.sys.SabaSite;

/*
 * 2012-10-10   Feilung Wong    Created for ECP 503 / QC 1144 WebTrends implementation
 * 2013-07-30   Feilung Wong    Enhance performance tradeoff, do not check file time stamp.
 *                              Any changes would require bouncing (staggered restart would work)
 */
/**
 * Description: Utility class for generating page tagging needed by web analytics solution<br/>
 * 
 * NOTE: wdk/xsl/view/wdk_defaultview.xsl uses this utility<br/>
 * That page would look for the following tags in the .xml file and generate the necessary META tag for WebTrend as long as wdk_defaultview.xsl is included in that page.<br/>
 * 
 * &lt;wdk:page&gt;<br/>
 *    ...<br/>
 *    &lt;wdk:almsmeta&gt;&lt;name&gt;PARAM_1&lt;/name&gt;&lt;content&gt;VALUE_1&lt;/content&gt;&lt;/wdk:almsmeta&gt;<br/>
 *    &lt;wdk:almsmeta&gt;&lt;name&gt;PARAM_2&lt;/name&gt;&lt;content&gt;VALUE_2&lt;/content&gt;&lt;/wdk:almsmeta&gt;<br/>
 * &lt;/wdk:page&gt;
 * 
 * @author Feilung Wong
 * 
 */

public class WebStatUtil {
    /** Creating className instance */
    private static final String className = WebStatUtil.class.getName();
    /** Creating logger */
    private static final Logger logger = Logger.getLogger(className);  
    /**
     * This will allow ClassVersionCheck to extract file version in case of troubleshooting.
     */
    public static final String DLS_FILE_VERSION = "1";

    /**
     * Custom tag definition: All starts with "DCSext."
     */
    //public static final String CONTENT_LAUNCH_ID = "DCSext.cnt_lnh_id";
    //public static final String CONTENT_PREVIEW_ID = "DCSext.cnt_prv_id";
    

    protected static String FILENAME = "page_mapping.properties";
    protected static String FILE_FULL_PATH = SabaSite.getSystemDefaultSite().getPropertiesHome() + File.separator + FILENAME;
    protected static Properties MAPPING = new Properties();
    
    // Internal
    protected static long FILE_LAST_MODIFIED = 0L;
        
    static {
        refreshSettings();
    }

    // Refresh if the properties file is updated
    public static void refreshSettings() {
        boolean refreshProperties = false;
        File file = new File(FILE_FULL_PATH);
        try {
            long lastMod = file.lastModified();
            if (lastMod == 0L || lastMod > FILE_LAST_MODIFIED) {
                refreshProperties = true;
                FILE_LAST_MODIFIED = lastMod;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error reading timestamp from " + FILE_FULL_PATH, ex);
        }
        if (refreshProperties) {
            try {
                InputStream inStream = new FileInputStream(file);
                if (inStream != null) {
                    MAPPING = new Properties();
                    try {
                        MAPPING.load(inStream);            
                        inStream.close();
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    } finally {
                        if (inStream != null) {
                            try {
                                inStream.close();
                            } catch (Exception ex) {
                                logger.log(Level.WARNING, "Error closing " + FILENAME, ex);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error reading " + FILE_FULL_PATH, ex);
            }
        }
    }
    
    /**
     * Gets a more meaning page title for web report by looking up a custom map
     * 
     * @param controlFilePath control file path as returned by the Saba engine
     * @return a custom page title if defined in the custom map; returns the control file path itself if not defined.
     */
    public static String getPageTitle(String controlFilePath) {
        // Do NOT refresh every time, manually triggered.
        //refreshSettings();
        return MAPPING.getProperty(controlFilePath, controlFilePath);
    }
    
    /**
     * Gets the Saba ID for this user
     * @return Saba ID of this current session, empty string if exception.
     */
    public static String getUserID() {
        try {
            return ServiceLocator.getClientInstance().getSabaPrincipal().getID();
        } catch (Exception ex) {
            return "";
        }
    }
}

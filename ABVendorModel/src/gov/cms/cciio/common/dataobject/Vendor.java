package gov.cms.cciio.common.dataobject;

import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.exception.MLMSStatusCode;
import gov.cms.cciio.common.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vendor {
    private static final String className = Vendor.class.getName();
    private static final Logger logger = Logger.getLogger(className);

	private String id;
	private String name;
	private String code;
	private String type;
	
	private Vendor(String id, String code, String name, String type) {
		// You cannot instantiate this object
		this.id = id;
		this.code = code;
		this.name = name;
		this.type = type;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getType() {
		return type;
	}
	
	public static ArrayList<Vendor> getVendorList() throws DBUtilException {
		ArrayList<Vendor> list = new ArrayList<Vendor>();
		int errMark=0;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("Vendor.getVendorList");
            logger.fine("Reading from AT_MLMS_VENDOR_REF");
            statement = conn.prepareStatement("SELECT ID, VENDOR_CODE, VENDOR_NAME, PATH_TYPE FROM AT_MLMS_VENDOR_REF ORDER BY VENDOR_CODE ASC");
            rs = statement.executeQuery();
            while (rs.next()) {
            	list.add(new Vendor(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            StringBuilder errMsg = new StringBuilder("Vendor.getVendorList: ");
            errMsg.append(errMark);
            errMsg.append("\n");
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(), e);
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "Vendor.getVendorList");
        }
	 
		return list;
	}
}

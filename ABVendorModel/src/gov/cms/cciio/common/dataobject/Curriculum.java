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

public class Curriculum {
    private static final String className = Curriculum.class.getName();
    private static final Logger logger = Logger.getLogger(className);

    private String id;
	private String name;
	private String code;
	
	private Curriculum(String id, String code, String name) {
		// You cannot instantiate this object
		this.id = id;
		this.code = code;
		this.name = name;
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
	
	public static ArrayList<Curriculum> getCurriculumList() throws DBUtilException {
		ArrayList<Curriculum> list = new ArrayList<Curriculum>();
		int errMark=0;
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
        try {
            conn = DBUtil.getDefaultSiteConnection("Curriculum.getCurriculumList");
            logger.fine("Reading from AT_MLMS_CURRICULUM_REF");
            statement = conn.prepareStatement("SELECT ID, CURRICULUM_CODE, CURRICULUM_NAME FROM AT_MLMS_CURRICULUM_REF ORDER BY CURRICULUM_CODE ASC");
            rs = statement.executeQuery();
            while (rs.next()) {
            	list.add(new Curriculum(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            StringBuilder errMsg = new StringBuilder("Curriculum.getCurriculumList: ");
            errMsg.append(errMark);
            errMsg.append("\n");
            errMsg.append(e.getMessage());
            logger.log(Level.SEVERE, errMsg.toString(), e);
            throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(), e);
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "Curriculum.getCurriculumList");
        }
	 
		return list;
	}

}

package gov.cms.cciio.mlms.vendor.ab.DAL;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

import gov.cms.cciio.mlms.vendor.ab.file.exception.ABVendorFileProcessException;
import gov.cms.cciio.mlms.vendor.ab.model.VendorTransferFileInfo;
import gov.cms.cciio.mlms.vendor.ab.util.VendorDBUtil;

public class VendorModelDAL {

	private Logger logger = Logger.getLogger("VendorModelDAL");

	private VendorTransferFileInfo vtFileInfo;

	public VendorModelDAL() {
		this.vtFileInfo = null;
	}

	public VendorModelDAL(VendorTransferFileInfo fileInfo) {
		this.vtFileInfo = fileInfo;
	}

	public void processFile() throws ABVendorFileProcessException {
		ArrayList<String> aLStatus = new ArrayList<String>();
		if (vtFileInfo != null) {
			// Start calling the private methods to enter data in to the DB

			String sVendorLoadId = vendorLoadInsert();
			ArrayList<String> recList = (ArrayList<String>) vtFileInfo.getRecords();
			// loop through all the records
			String sRecord = null;
			for (int itr = 0; itr < recList.size(); itr++) {
				sRecord = (String) recList.get(itr);
				String sStatus = vendorRawFeedInsert(sVendorLoadId, sRecord);
				aLStatus.add(sStatus);
			}
			if (aLStatus.contains("E") || aLStatus.contains("W")) {
				ABVendorFileProcessException e1 = new ABVendorFileProcessException("File processed with errors and warnings FileIdentifier: "
						+ vtFileInfo.toString());
				throw e1;
			}
		}
	}

	// Calls the procedure AP_MLMS_VENDOR_LOAD_INS and returns the vendorLoadId
	// created in the DB
	private String vendorLoadInsert() throws ABVendorFileProcessException {
		Connection conn = null;
		CallableStatement cstmt = null;
		String sVendorLoadId = null;
		try {
			conn = VendorDBUtil.getConnection("VendorModelDAL.vendorLoadInsert");
			/*
			 * AP_MLMS_VENDOR_LOAD_INS returns (1)vendorLoadID
			 */

			cstmt = conn.prepareCall("{ call AP_MLMS_VENDOR_LOAD_INS(?,?,?,?,?,?) }");
			cstmt.setString(1, vtFileInfo.getVendorName());
			cstmt.setString(2, vtFileInfo.getFileID());
			cstmt.setString(3, vtFileInfo.getFileDate());
			cstmt.setString(4, vtFileInfo.getFileTime());
			cstmt.setString(5, vtFileInfo.getFileRecordCount());
			cstmt.registerOutParameter(6, Types.CHAR);
			cstmt.execute();

			sVendorLoadId = (String) cstmt.getObject(6);
		} catch (SQLException e) {
			ABVendorFileProcessException e1 = new ABVendorFileProcessException(
					"SQLException encountered when executing the stored procedure AP_MLMS_VENDOR_LOAD_INS" + "in method VendorModelDAL.vendorLoadInsert() : "
							+ e.getMessage());
			logger.log(Level.SEVERE,"VendorModelDAL.vendorLoadInsert caught Exception: " + e.getMessage(), e);
			throw e1;
		} catch (Exception e2) {
			ABVendorFileProcessException e1 = new ABVendorFileProcessException("Exception encountered " + "in method VendorModelDAL.vendorLoadInsert() : "
					+ e2.getMessage());
			logger.log(Level.SEVERE,"VendorModelDAL.vendorLoadInsert caught Exception: " + e2.getMessage(), e2);
			throw e1;
		} finally {
			try {
				VendorDBUtil.freeDBResources(null, cstmt, conn, "VendorModelDAL.vendorLoadInsert");
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Exception when freeing connection in VendorModelDAL.vendorLoadInsert: " + e.getMessage(), e);
			}
		}
		return sVendorLoadId;

	}

	private String vendorRawFeedInsert(String sVendorLoadId, String sRecord) throws ABVendorFileProcessException {
		Connection conn = null;
		CallableStatement cstmt = null;
		String sStatus = null;

		try {
			conn = VendorDBUtil.getConnection("VendorModelDAL.vendorRawFeedInsert");
			/*
			 * AP_MLMS_VENDOR_RAW_FEED_INS returns (1)vendorFeedID
			 */
			/*
			 * String sVendorCode = sRecord.substring(0,1); String
			 * sCurriculumCode = sRecord.substring(1,2); String sCurriculumYear
			 * = sRecord.substring(2, 4); String sLanguage =
			 * sRecord.substring(4,5);
			 */
			cstmt = conn.prepareCall("{ call AP_MLMS_VENDOR_RAW_FEED_INS(?,?,?) }");
			cstmt.setString(1, sRecord);
			cstmt.setString(2, sVendorLoadId);
			cstmt.registerOutParameter(3, Types.CHAR);
			cstmt.execute();

			sStatus = (String) cstmt.getObject(3);
		} catch (SQLException e) {
			ABVendorFileProcessException e1 = new ABVendorFileProcessException(
					"SQLException encountered when executing the stored procedure AP_MLMS_VENDOR_RAW_FEED_INS"
							+ "in method VendorModelDAL.vendorRawFeedInsert() : " + e.getMessage());
			logger.log(Level.SEVERE,"VendorModelDAL.vendorRawFeedInsert caught Exception: " + e.getMessage(), e);
			throw e1;
		} catch (Exception e2) {
			ABVendorFileProcessException e1 = new ABVendorFileProcessException("Exception encountered " + "in method VendorModelDAL.vendorRawFeedInsert() : "
					+ e2.getMessage());
			logger.log(Level.SEVERE,"VendorModelDAL.vendorRawFeedInsert caught Exception: " + e2.getMessage(), e2);
			throw e1;
		} finally {
			try {
				VendorDBUtil.freeDBResources(null, cstmt, conn, "VendorModelDAL.vendorRawFeedInsert");
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Exception when freeing connection in VendorModelDAL.vendorRawFeedInsert: " + e.getMessage(), e);
			}
		}
		return sStatus;

	}

}

package gov.cms.cciio.common.delegate;

import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.exception.MLMSException;
import gov.cms.cciio.common.exception.MLMSStatusCode;
import gov.cms.cciio.common.exception.RegistrationException;
import gov.cms.cciio.common.response.DelegateResponse;
import gov.cms.cciio.common.session.MLMSDelegates;
import gov.cms.cciio.common.session.MLMSDynamicManager;
import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.saba.exception.SabaException;
import com.saba.locator.ServiceLocator;
//import gov.cms.cciio.registration.Registration;

/*
 * 2012-04-16   Feilung Wong    Created
 */
/**
 * This delegate handles LMS Roster page related functions
 * It provides several static helper method to provide transaction support over some Registration.* methods
 * @author Feilung
 *
 */
public class RosterInsert extends MLMSDelegate {
    private static final Logger pLogger = Logger.getLogger("performance.common.RosterInsert");

    /**
     * This will allow ClassVersionCheck to extract file version in case of troubleshooting.
     */
    public static final String DLS_FILE_VERSION = "1";

    private static final String SQL_RECYC_IN_CI_CHOICES = "SELECT t.CERTIFICATION_ID CERT_ID, p.ID PROG_ID, v.COURSE_CLASS_ID, p.NAME "
            + "FROM AV_INT_ATRRS_COURSE_A14 v, TPT_EXT_CE_TRACK t, LET_EXT_PACKAGE p, (SELECT COURSE_ID FROM AV_INT_ATRRS_COURSE_A14 WHERE COURSE_CLASS_ID=?) temp "
            + "WHERE t.ID = v.CE_PATH_ID AND v.PACKAGE_ID = p.ID AND v.COURSE_ID = temp.COURSE_ID";
    
    private static final int ACTION_ATRRS_RECYCLE_IN           = 1;
    private static final int ACTION_ATRRS_CHANGE_INPUT_STATUS  = 2;
    private static final int ACTION_ATRRS_CHANGE_OUTPUT_STATUS = 3;
    
    private String transID;
    private String regID;
    private String inputCode;
    private String inputReason;
    private String outputCode;
    private String outputReason;
    private String auditReason;
    private String sessionUserID;
    private String newCrsClassID;
    private String oldCrsClassID;
    private String programID;
    private String studentID;
    private String certID;
    private boolean removeFromHolds;
    private int action;
    private boolean isATRRS;
    private StringBuilder timeSB;
    
    /**
     * For ATRRS recycle in
     * @param locator
     * @param transID
     * @param newCrsClassID
     * @param programID
     * @param studentID
     * @param certID
     * @param recycleInCode Input code
     * @param recycleInReason Input Reason code
     * @param recycleOutCrsClassID
     * @param recycleOutCode
     * @param recycleOutReason
     * @param timeSB
     */
    private RosterInsert(ServiceLocator locator, String transID, String newCrsClassID, String programID, 
            String studentID, String certID, String recycleInCode, String recycleInReason,
            String recycleOutCrsClassID, String recycleOutCode, String recycleOutReason, StringBuilder timeSB) {
        this.transID = transID;
        this.locator = locator;
        this.newCrsClassID = newCrsClassID;
        this.programID = programID;
        this.studentID = studentID;
        this.certID = certID;
        this.inputCode = recycleInCode;
        this.inputReason = recycleInReason;
        this.oldCrsClassID = recycleOutCrsClassID;
        this.outputCode = recycleOutCode;
        this.outputReason = recycleOutReason;
        this.action = ACTION_ATRRS_RECYCLE_IN;
        this.isATRRS = true;
        this.timeSB = timeSB;
        CommonUtil.appendTimestamp(this.timeSB, "\tcreated:\t");
    }

    /**
     * For changing ATRRS input status code
     * @param locator
     * @param transID
     * @param regID
     * @param isATRRS
     * @param inputStatusCode
     * @param inputReasonCode
     * @param cancelAuditReason
     * @param timeSB
     */
    private RosterInsert(ServiceLocator locator, String transID, String regID, boolean isATRRS,
            String inputStatusCode, String inputReasonCode, String cancelAuditReason, StringBuilder timeSB) {
        this.transID = transID;
        this.locator = locator;
        this.regID = regID;
        this.inputCode = inputStatusCode;
        this.inputReason = inputReasonCode;
        this.auditReason = cancelAuditReason;
        this.action = ACTION_ATRRS_CHANGE_INPUT_STATUS;
        this.isATRRS = isATRRS;
        this.timeSB = timeSB;
        CommonUtil.appendTimestamp(this.timeSB, "\tcreated:\t");
    }

    /**
     * For changing ATRRS output status code
     * @param locator
     * @param transID
     * @param regID
     * @param isATRRS
     * @param outputStatusCode
     * @param outputReasonCode
     * @param cancelAuditReason
     * @param sessionUserID
     * @param removeFromHolds
     * @param timeSB
     */
    private RosterInsert(ServiceLocator locator, String transID, String regID, boolean isATRRS,
            String outputStatusCode, String outputReasonCode, String cancelAuditReason, String sessionUserID, boolean removeFromHolds, StringBuilder timeSB) {
        this.transID = transID;
        this.locator = locator;
        this.regID = regID;
        this.outputCode = outputStatusCode;
        this.outputReason = outputReasonCode;
        this.auditReason = cancelAuditReason;
        this.sessionUserID = sessionUserID;
        this.removeFromHolds = removeFromHolds;
        this.action = ACTION_ATRRS_CHANGE_OUTPUT_STATUS;
        this.isATRRS = isATRRS;
        this.timeSB = timeSB;
        CommonUtil.appendTimestamp(this.timeSB, "\tcreated:\t");
    }
    
    @Override
    public DelegateResponse processMessage() throws MLMSException {
        DelegateResponse response = null;
        switch (action) {
            case ACTION_ATRRS_RECYCLE_IN :
               // response = Registration.atrrsRecycleIn(locator, newCrsClassID, programID, studentID, certID, inputCode, inputReason, oldCrsClassID, outputCode, outputReason);
                break;
            case ACTION_ATRRS_CHANGE_INPUT_STATUS :
               // response = Registration.changeInputCode(locator, transID, regID, isATRRS, inputCode, inputReason, auditReason);
                break;
            case ACTION_ATRRS_CHANGE_OUTPUT_STATUS :
              //  response = Registration.changeOutputCode(locator, transID, regID, isATRRS, outputCode, outputReason, auditReason, sessionUserID, removeFromHolds);
                break;
            default:
                throw new MLMSException(MLMSStatusCode.NUM_GEN_VALIDATION_ERROR, "Unknown action code");
        }
        CommonUtil.appendTimestamp(timeSB, "\tprocReq exit:\t");
        pLogger.fine(timeSB.toString());
        return response;
    }
    
    /**
     * It will set Recycle Out status code and trigger B1 for old registration, and then create new registration
     *
     * Used by LMS Roster Page: /learning/sabapackage/dls_packageRosterInfo.xml
     *
     * @param locator
     * @param newCrsClassID ATRRS course class ID to recycle in
     * @param programID the program ID
     * @param studentID student ID
     * @param certID Certification ID
     * @param recycleInCode Input code
     * @param recycleInReason Input Reason code
     * @param recycleOutCrsClassID Course class ID of the Recycled out record, null to skip setting recycle out code
     * @param recycleOutCode Recycle out status code, ignored if recycleOutCrsClassID is null
     * @param recycleOutReason Recycle out reason code, ignored if recycleOutCrsClassID is null
     * @return DelegateResponse object
     * @throws MLMSException
     * @throws SabaException
     */
    public static DelegateResponse transactionalATRRSRecycleIn(ServiceLocator locator, String newCrsClassID, 
            String programID, String studentID, String certID, String recycleInCode, String recycleInReason, 
            String recycleOutCrsClassID, String recycleOutCode, String recycleOutReason) throws MLMSException, SabaException {
        String idString = "RP-RECYCLE_IN-" + studentID;
        String transID = idString + "-" + System.currentTimeMillis();
        StringBuilder timeSB = new StringBuilder(idString);
        MLMSDynamicManager manager = (MLMSDynamicManager) locator.getManager(MLMSDelegates.kMLMSDynamicManager);
        RosterInsert ri = new RosterInsert(locator, transID, newCrsClassID, programID, studentID, certID, recycleInCode, recycleInReason, 
                recycleOutCrsClassID, recycleOutCode, recycleOutReason, timeSB);
        return manager.processMessage(locator, ri, null, null, null);
    }

    /**
     * Change input code on EXISTING registration. If status code indicates a cancel, the registration would be canceled.
     * It should not involve putting something on hold.
     * Note: Removing a registration from hold should be done through either
     *    1) atrrsRecycleIn with recycleOutCrsClassID, OR
     *    2) changeATRRSOutputCode with removeFromHolds set to true
     * 
     * Used by LMS code /learning/sabapackage/dls_ChangeOutputstatus.xml
     * 
     * @param locator
     * @param regID
     * @param isATRRS
     * @param inputStatusCode
     * @param inputReasonCode
     * @param cancelAuditReason used if cancel is involved
     * @return
     * @throws MLMSException
     * @throws SabaException
     */
    public static DelegateResponse transactionalChangeInputCode(ServiceLocator locator, String regID, boolean isATRRS,
            String inputStatusCode, String inputReasonCode, String cancelAuditReason) throws MLMSException, SabaException {
        String idString = "RP_CHG_INPUT_BY-" + locator.getSabaPrincipal().getUsername();
        String transID = idString + "-" + System.currentTimeMillis();
        StringBuilder timeSB = new StringBuilder(idString);
        MLMSDynamicManager manager = (MLMSDynamicManager) locator.getManager(MLMSDelegates.kMLMSDynamicManager);
        RosterInsert ri = new RosterInsert(locator, transID, regID, isATRRS, inputStatusCode, inputReasonCode, cancelAuditReason, timeSB);
        return manager.processMessage(locator, ri, null, null, null);
    }
    
    /**
     * Change output code on EXISTING registration. If status code indicates a cancel, the registration would be canceled.
     * If the status code for Hold is used, the registration would be put on hold.
     * set removeFromHolds to true to remove a registration from hold
     * 
     * Note: Removing someone from Holds table only makes sense for order that is on Hold (Order already canceled)
     * Therefore, if Cancel order happens, the removeFromHolds field is ignored.
     * 
     * Used by LMS code /learning/sabapackage/dls_ChangeOutputstatus.xml
     * 
     * @param locator
     * @param regID
     * @param isATRRS
     * @param outputStatusCode
     * @param outputReasonCode
     * @param cancelAuditReason used if cancel is involved
     * @param removeFromHolds true to removal from ATRRS Holds table; false to skip. Ignored if cancel order is involved
     * @return
     * @throws MLMSException
     * @throws SabaException
     */
    public static DelegateResponse transactionalChangeOutputCode(ServiceLocator locator, String regID, boolean isATRRS,
            String outputStatusCode, String outputReasonCode, String cancelAuditReason, boolean removeFromHolds)
                    throws MLMSException, SabaException {
        String idString = "RP_CHG_OUTPUT_BY-" + locator.getSabaPrincipal().getUsername();
        String transID = idString + "-" + System.currentTimeMillis();
        StringBuilder timeSB = new StringBuilder(idString);
        MLMSDynamicManager manager = (MLMSDynamicManager) locator.getManager(MLMSDelegates.kMLMSDynamicManager);
        RosterInsert ri = new RosterInsert(locator, transID, regID, isATRRS, outputStatusCode, outputReasonCode, 
                cancelAuditReason, locator.getSabaPrincipal().getID(), removeFromHolds, timeSB);
        return manager.processMessage(locator, ri, null, null, null);
    }
    
    /**
     * Given the course class ID of the OLD C.I. returns a list of C.I. that a student can be recycled in
     * @param oldCrsClassID
     * @return a list of C.I. that a student can be recycled in
     * @throws DBUtilException
     * @throws RegistrationException
     */
    public static ArrayList<RecycleInCIChoice> getRecycleInCIChoices(String oldCrsClassID)
            throws DBUtilException, RegistrationException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stat = null;
        ArrayList<RecycleInCIChoice> results = new ArrayList<RecycleInCIChoice>();

        try {
            conn = DBUtil.getDefaultSiteConnection("RosterInsert.getRecycleInCIChoices");
            stat = conn.prepareStatement(SQL_RECYC_IN_CI_CHOICES);
            stat.setString(1, oldCrsClassID);
            rs = stat.executeQuery();
            while (rs.next()) {
                results.add(new RecycleInCIChoice(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException ex) {
            throw new RegistrationException(MLMSStatusCode.NUM_DB_GENERAL,
                    "Error getting recycle in C.I. choices from existing A4 " + oldCrsClassID, ex);
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, stat, conn, "RosterInsert.getRecycleInCIChoices");
        }
        return results;
    }
    
    public static class IOCode {
        private String code;
        private String desc;
        private boolean reasonRequired;
        
        public IOCode(String code, String desc, boolean reasonRequired) {
            this.code = code;
            this.desc = desc;
            this.reasonRequired = reasonRequired;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public boolean isReasonRequired() {
            return reasonRequired;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(code);
            sb.append(",");
            sb.append(desc);
            sb.append(",");
            sb.append(reasonRequired);
            sb.append("]");
            return sb.toString();
        }
    }

    public static class IOReason {
        private String code;
        private String desc;
        
        public IOReason(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(code);
            sb.append(",");
            sb.append(desc);
            sb.append("]");
            return sb.toString();
        }
    }
    
    public static class RecycleInCIChoice {
        private String certID;
        private String progID;
        private String crsClassID;
        private String progName;
        
        public RecycleInCIChoice(String certID, String progID, String crsClassID, String progName) {
            this.certID = certID;
            this.progID = progID;
            this.crsClassID = crsClassID;
            this.progName = progName;
        }
        
        public String getCertID() {
            return certID;
        }
        
        public String getProgID() {
            return progID;
        }
        
        public String getCrsClassID() {
            return crsClassID;
        }
        
        public String getProgName() {
            return progName;
        }
    }
}

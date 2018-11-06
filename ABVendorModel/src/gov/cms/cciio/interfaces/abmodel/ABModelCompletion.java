package gov.cms.cciio.interfaces.abmodel;

/*
 * Change Log
 * 
 * 2015-07-23   Feilung Wong    Initial version.
 */

import gov.cms.cciio.common.auth.LoginLog;
import gov.cms.cciio.common.auth.MLMSLoginBean;
import gov.cms.cciio.common.auth.User;
import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.registration.Registration;
import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.Constants;
import gov.cms.cciio.common.util.DBUtil;
import gov.hhs.cms.eidm.ws.waas.servlet.SSOServlet;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.saba.ejb.SabaMessageDrivenBean;
import com.saba.locator.ServiceLocator;
import com.saba.security.SabaLogin;
import com.saba.util.BaseDataBaseUtil;

// Extending it just to inherit the transaction helper methods
public class ABModelCompletion extends SabaMessageDrivenBean {

    private static final String CLASSNAME = ABModelCompletion.class.getName();
    protected static final String SQL_CHECK_USERNAME = "SELECT COUNT(*) FROM CMT_PERSON WHERE USERNAME=UPPER(?)";
    //protected static final String SQL_AP_MLMS_AB_MODEL = "BEGIN AP_MLMS_AB_MODEL(@001, @002, @003, @004, @005, @006, @007, @008, @009, @010, @011, @012, @013, @014, @015, @016); END;";
    protected static final String SQL_AP_MLMS_AB_MODEL_LOG = "BEGIN AP_MLMS_AB_MODEL_LOG(@001, @002, @003, @004, @005, @006, @007, @008, @009, @010); END;";
    
    //protected static final String SQL_GET_RECORD = "SELECT vc.VENDOR_COMP_ID, vc.VENDOR_NAME, cf.CURRICULUM_NAME, TO_CHAR(vc.RECEIVED_DATE, 'YYYY/MM/DD') REC_DATE, vc.CURR_LANG, vc.CURR_YEAR "
    //		+ "FROM AT_MLMS_VENDOR_COMP vc LEFT OUTER JOIN AT_MLMS_CURRICULUM_REF cf ON vc.CURR_CODE = cf.CURRICULUM_CODE WHERE vc.LEARNER_ID=? AND vc.STATUS IN ('N', 'F')";
    protected static final String SQL_GET_RECORD = "SELECT VENDOR_COMP_ID, VENDOR_NAME, CURR_CODE, CURR_LANG, CURR_YEAR, TO_CHAR(RECEIVED_DATE, 'YYYY/MM/DD') REC_DATE "
    		+ "FROM AT_MLMS_VENDOR_COMP vc WHERE upper(LEARNER_ID)=upper(?) AND STATUS IN ('N', 'F')";

    public static final String STATUS_CODE_SUCCESS = "MS200";
    public static final String STATUS_CODE_FAILURE = "MS500";
    
    public static final String STATUS_KEY_SUCCESS = "operation.successful";
    public static final String STATUS_KEY_FAILURE = "operation.unsuccessful";
    /*
     * Status Status Message              Status Key                 Reason
     * --------------------------------------------------------------------------------------------------------
     * MS200  Operation Successful        operation.successful       Operation Completed Normally
     * MS500  An Error Occurred           operation.unsuccessful     Operation did not complete successfully
     * 
     * Error  Error Message               Error Key                  Reason
     * --------------------------------------------------------------------------------------------------------
     * ME800  Invalid data                invalid.data               data received was invalid or unreadable
     * ME810  No Data Found               no.data.found              no data exists for data
     * ME820  Unexpected Error            unexpected.error           The system encountered an unexpected error
     * ME825  Invalid Curriculum          curriculum.not.found       The named Curriculum was not found within the MLMS database, the user cannot be processed. 
     * ME830  Database Unavailable        database.unavailable       The Integration Bus was unable to access the database
     * ME840  System Maintenance:         system.maintenance.window  The Integration Bus functionality is undergoing planned maintenance, try back later
     *        Contact MLMS Administration 
     *        for expected uptime  
     */

    /**
     * For Web Services, NOT to be used as part of SSO process
     * It will call the login code (to create account if not already exist). That's why it CANNOT be used by the login code (infinite loop)
     * @param vendorName - Vendor Acronyms as in AT_MLMS_VENDOR_REF.VENDOR_NAME, not the 1 character vendor code
     * @param username 
     * @param curriculumCode - AT_MLMS_CURRICULUM_REF.CURRICULUM_CODE
     * @param completionDateStr - yyyy/mm/dd
     * @param curriculumLanguage - English, Spanish - logic in Stored Procedure AP_MLMS_REG_FIND_ID
     * @param trainYear - yyyy
     * @return
     */
    public static Response transactionalGrantCompletion(String vendorName, String username, String curriculumCode, 
            String completionDateStr, String curriculumLanguage, String trainYear) {
        String methodName = "transactionalGrantCompletion";
        Response response = null;
        ABModelCompletion completion = new ABModelCompletion();
        boolean loggedIn = false;
        int errMark = 0;
       
        if(vendorName != null && vendorName.length()>0){
        	vendorName=vendorName.trim();
        }
        if(username != null && username.length()> 0){
        	username = username.trim();
        }
        if(curriculumCode != null && curriculumCode.length()>0){
        	curriculumCode = curriculumCode.trim();
        }
        if(curriculumLanguage != null && curriculumLanguage.length()>0){
        	curriculumLanguage = curriculumLanguage.trim();
        }
        if(trainYear != null && trainYear.length()>0){
        	trainYear = trainYear.trim();
        }
        LoginLog.writeToErrorLog(methodName);
        LoginLog.writeToErrorLog(" Vendor " + vendorName );
        LoginLog.writeToErrorLog(" Username " + username );
        LoginLog.writeToErrorLog(" Curriculumm Code " + curriculumCode );
        LoginLog.writeToErrorLog(" Completion date  " +  completionDateStr );
        LoginLog.writeToErrorLog(" Language " +  curriculumLanguage );
        LoginLog.writeToErrorLog(" Training year " +  trainYear );
        
        try {
            try {
                MLMSLoginBean loginBean = new MLMSLoginBean();
                loginBean.setUsername(username);
                //loginBean = SSOServlet.retrieveEIDMUserProfile(loginBean);
                loginBean.setRole(Constants.AGENT);
                loginBean.setEIDM(true);
                LoginLog.writeToErrorLog(" loginBean.isEIDM " + loginBean.isEIDM());
                if (!loginBean.isEIDM()) {
                    // Not found in EIDM
                    throw new RuntimeException("Unknown user");
                } else {
                    // The following call would create the account if it does not already exist
                    User.LoginResult loginResult = User.lmsLogin(loginBean); 
                    if (!loginResult.isSuccess()) {
                        throw loginResult.getLoginException();
                    }
                }
            } catch (Exception ex) {
                System.err.println(CommonUtil.buildExceptionMsg("Error validating user account " + username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                        CommonUtil.stackTraceToString(ex)));
                response = new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", "Error validating user account");
            }
            if (response == null) {
                // Log in as some manager/admin/super user
                // TODO what password to use???
                // TODO It would help if there is a LoginSuperUser method in SystemInit like the one for "Admin"
            	
                ServiceLocator locator = SabaLogin.authenticate(Constants.MLMS_SUPER, Constants.MLMS_SUPER_PASSWORD);
                loggedIn = true;
                completion.beginTransaction();
                //response = completion.grantCompletion(locator, vendorName, username, curriculumName, completionDateStr, curriculumLocale, trainYear);
                response = new Response(Registration.register(locator, vendorName, username, curriculumCode, curriculumLanguage, trainYear, true, completionDateStr));
                /*
                // For testing transactions
                if (curriculumLocale.equals("english")) {
                    response.setStatusCode(STATUS_CODE_FAILURE);
                }
                */
            }
        } catch (Exception ex) {
            // Log to server
            System.err.println(CommonUtil.buildExceptionMsg(username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
            response = new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
        } finally {
            if (loggedIn) {
                if (response.getStatusCode().equals(STATUS_CODE_SUCCESS)) {
                    errMark = 20;
                    try {
                        completion.commitTransaction();
                    } catch (Exception ex) {
                        // Log to server
                        System.err.println(CommonUtil.buildExceptionMsg("Commit " + username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                                CommonUtil.stackTraceToString(ex)));
                    }
                } else {
                    errMark = 30;
                    try {
                        completion.rollbackTransaction();
                    } catch (Exception ex) {
                        // Log to server
                        System.err.println(CommonUtil.buildExceptionMsg("Rollback " + username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                                CommonUtil.stackTraceToString(ex)));
                    }
                }
                // Don't leave the session open...
                try {
                    SabaLogin.logout();
                } catch (Exception ex) {
                    // Log to server
                    System.err.println(CommonUtil.buildExceptionMsg("Error logging out " + username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                            CommonUtil.stackTraceToString(ex)));
                }
            }
        }
        
        // This is outside of the transaction, log this processing regardless of results
        logTransaction(null, vendorName, username, curriculumCode, completionDateStr, curriculumLanguage, trainYear, response);
        
        return response;
    }

    /**
     * To be used as part of SSO process
     * @param locator already logged in as someone with privileges to grant completion
     * @param loginBean already checked profile and went through the lmsLogin process - Account valid and active
     * @return
     */
    public static Response findAndProcessCompletions(ServiceLocator locator, MLMSLoginBean loginBean) {
        String methodName = "findAndProcessCompletions";
        Response response = null;
        ABModelCompletion completion = new ABModelCompletion();
        int errMark = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String[]> records = new ArrayList<String[]>();
		
		try {
            if (loginBean == null) {
            	throw new IllegalArgumentException("LoginBean cannot be null");
            }

			conn = DBUtil.getConnection(locator, "ABModelCompletion.findAndProcessCompletions");
			stmt = conn.prepareStatement(SQL_GET_RECORD);
			stmt.setString(1, loginBean.getGuid());
			
			if ((rs = stmt.executeQuery()) != null) {
				while (rs.next()) {
					// VENDOR_COMP_ID, VENDOR_NAME, CURR_CODE, CURR_LANG, CURR_YEAR, TO_CHAR(RECEIVED_DATE, 'YYYY/MM/DD') REC_DATE
					records.add(new String[] {rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)});
				}
			}
		} catch (Exception ex) {
            // Log to server
            System.err.println(CommonUtil.buildExceptionMsg("Error retreiving vendor completion code from DB - " + loginBean.getUsername(), CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
            return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
		} finally {
			 DBUtil.freeDBResources(locator, rs, stmt, conn,
			 "ABModelCompletion.findAndProcessCompletions");
		}
		
		try {			 
			for (String[] params : records) {
				try {
					completion.beginTransaction();
					//response = completion.grantCompletion(locator, params[1], loginBean.getUsername(), params[2], params[3], params[4], params[5]);
					response = new Response(Registration.register(locator, params[1], loginBean.getUsername(), params[2], params[3], params[4], true, params[5]));
				} catch (Exception ex) {
					// Log to server
					System.err.println(CommonUtil.buildExceptionMsg(params[0], CLASSNAME, methodName, errMark,
							CommonUtil.stackTraceToString(ex)));
					response = new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
				} finally {
					if (response.getStatusCode().equals(STATUS_CODE_SUCCESS)) {
						errMark = 20;
						try {
							completion.commitTransaction();
						} catch (Exception ex) {
							// Log to server
							System.err.println(CommonUtil.buildExceptionMsg("Commit " + params[0], CLASSNAME, methodName, errMark,
									CommonUtil.stackTraceToString(ex)));
	                    }
	                } else {
	                    errMark = 30;
	                    try {
	                        completion.rollbackTransaction();
	                    } catch (Exception ex) {
	                        // Log to server
	                        System.err.println(CommonUtil.buildExceptionMsg("Rollback " + params[0], CLASSNAME, methodName, errMark,
	                                CommonUtil.stackTraceToString(ex)));
	                    }
	                }
	            }
		        // This is outside of the transaction, log this processing regardless of results
		        logTransaction(params[0], params[1], loginBean.getUsername(), params[2], params[3], params[4], params[5], response);
			}
		} catch (Exception ex) {
			System.err.println(CommonUtil.buildExceptionMsg(loginBean.getUsername(), CLASSNAME, methodName, errMark,
					CommonUtil.stackTraceToString(ex)));
			response = new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
		}
		
		if (response == null) {
			// Nothing to process
			response = new Response(STATUS_CODE_SUCCESS, STATUS_KEY_SUCCESS);
		}
		
        return response;
    }

    
    
    protected static void logTransaction(String vendorCompletionID, String vendorName, String username, String curriculumCode, 
    		String curriculumLang, String trainYear, String completionDateStr, Response response) {
        String methodName = "logTransaction";
        Connection conn = null;
        CallableStatement callablestatement = null;
        int errMark = 0;
        String errorMsg = null;
        
        try {
            //logger.finer("About to call AP_MLMS_AB_MODEL_LOG");
            // Log even without a valid login, don't rely on locator
            conn = DBUtil.getDefaultSiteConnection("ABModelCompletion.logTransaction");
            errMark = 20;
            callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_AB_MODEL_LOG));
            errMark = 30;

            callablestatement.setString(1, vendorCompletionID);
            callablestatement.setString(2, vendorName);
            callablestatement.setString(3, username);
            callablestatement.setString(4, curriculumCode);
            callablestatement.setString(5, completionDateStr);
            callablestatement.setString(6, curriculumLang);
            callablestatement.setString(7, trainYear);
            callablestatement.setString(8, response.getStatusCode());
            callablestatement.setString(9, response.getErrorCode());
            if ((errorMsg = response.getErrorMessage()) != null) {
                if (errorMsg.length() > 4000) {
                    errorMsg = errorMsg.substring(0, 4000);
                }
            }
            callablestatement.setString(10, errorMsg);
            errMark = 40;
            callablestatement.execute();
            errMark = 50;
        } catch (Exception ex) {
            System.err.println(CommonUtil.buildExceptionMsg("Logging AB_Model: " + username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
        } finally {
            DBUtil.freeDefaultSiteDBResources(null, callablestatement, conn, "ABModelCompletion.logTransaction");
        }
    }
    
    protected User.LoginResult checkUserAccount(String username) {
        // Check if username exists, or create the account
        MLMSLoginBean loginBean = new MLMSLoginBean();
        loginBean.setUsername(username);
        loginBean = SSOServlet.retrieveEIDMUserProfile(loginBean);
        if (!loginBean.isEIDM()) {
            // Not found in EIDM
            throw new RuntimeException("Unknown user");
        } else {
            return User.lmsLogin(loginBean); // This call would create the account if it does not already exist
        }
    }
    
    /*
     * 
     * @param locator
     * @param vendor
     * @param username
     * @param curriculumName
     * @param completionDateStr yyyy/mm/dd
     * @param curriculumLocale
     * @param trainYear
     * @return
     * @throws DBUtilException
     *
    protected Response grantCompletion(ServiceLocator locator, String vendor, String username, String curriculumName, 
            String completionDateStr, String curriculumLocale, String trainYear) 
            throws DBUtilException {
        String methodName = "grantCompletion";
        LoginLog.writeToErrorLog(methodName);
        LoginLog.writeToErrorLog(" vendor " + vendor );
        LoginLog.writeToErrorLog(" username " + username );
        LoginLog.writeToErrorLog(" curriculumm name " + curriculumName );
        LoginLog.writeToErrorLog(" completion date  " +  completionDateStr );
        LoginLog.writeToErrorLog(" locale " +  curriculumLocale );
        LoginLog.writeToErrorLog(" training year " +  trainYear );
        Connection conn = null;
        CallableStatement callablestatement = null;
        boolean isError = false;
        int errMark = 0;
        Date completionDate = null;
        String userID = null;
        String certID = null;
        String pathID = null;
        String agreementIDsStr = null;
        String[] agreementIDs = null;
        String agreementPlanIDsStr = null;
        String[] agreementPlanIDs = null;
        String openRegExistStr = null;
        String[] openRegExist = null;
        String templateID = null;
        String errorCode = null;
        String errorKey = null;
        String errorMsg = null;
        
        // Basic validations
        if (username == null || (username=username.trim()).length() == 0) {
            return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME800", "invalid.data", "Username cannot be empty");
        }
        
        try {
            int year = Integer.parseInt(completionDateStr.substring(0, 4));
            int month = Integer.parseInt(completionDateStr.substring(5, 7)) - 1; // month is zero based
            int day = Integer.parseInt(completionDateStr.substring(8, 10));
            GregorianCalendar calendar = new GregorianCalendar(year, month, day);
            completionDate = calendar.getTime();
        } catch (Exception ex) {
            return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME800", "invalid.data", "Invalid Completion Date");
        }
        
        try {
            //logger.finer("About to call AP_MLMS_AB_MODEL");
            conn = DBUtil.getConnection(locator, "Completion.grantCompletion");
            errMark = 50;
            //callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_AB_MODEL));
            callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_REG_FIND_ID));

            callablestatement.setString(1, vendor);
            callablestatement.setString(2, username);
            callablestatement.setString(3, curriculumName);
            callablestatement.setDate(4, new java.sql.Date(completionDate.getTime()));
            callablestatement.setString(5, curriculumLocale);
            callablestatement.setString(6, trainYear);
            callablestatement.registerOutParameter(7, Types.VARCHAR); // User ID
            callablestatement.registerOutParameter(8, Types.VARCHAR); // Cert ID
            callablestatement.registerOutParameter(9, Types.VARCHAR); // Path ID
            callablestatement.registerOutParameter(10, Types.VARCHAR); // Agreement IDs
            callablestatement.registerOutParameter(11, Types.VARCHAR); // Agreement Plan IDs
            callablestatement.registerOutParameter(12, Types.VARCHAR); // Open Reg Exist (Y/N)
            callablestatement.registerOutParameter(13, Types.VARCHAR); // Template ID to mark completion
            callablestatement.registerOutParameter(14, Types.VARCHAR); // Error Code
            callablestatement.registerOutParameter(15, Types.VARCHAR); // Error Key
            callablestatement.registerOutParameter(16, Types.VARCHAR); // Error Message
            errMark = 54;
            callablestatement.execute();
            errMark = 55;
            userID = callablestatement.getString(7);
            certID = callablestatement.getString(8);
            pathID = callablestatement.getString(9);
            LoginLog.writeToErrorLog(methodName + " userID " + userID);
            LoginLog.writeToErrorLog(methodName + " certID " + certID);
            LoginLog.writeToErrorLog(methodName + " pathID " + pathID);
            LoginLog.writeToErrorLog(methodName + " agreementIDsStr " + callablestatement.getString(10));
            LoginLog.writeToErrorLog(methodName + " agreementPlanIDsStr " + callablestatement.getString(11));
            LoginLog.writeToErrorLog(methodName + " openRegExistStr " + callablestatement.getString(12));
            if ((agreementIDsStr = callablestatement.getString(10)) == null) {
                agreementIDsStr = "";
            }
            if ((agreementPlanIDsStr = callablestatement.getString(11)) == null) {
                agreementPlanIDsStr = "";
            }
            if ((openRegExistStr = callablestatement.getString(12)) == null) {
                openRegExistStr = "";
            }
            templateID = callablestatement.getString(13);
            // Error flag
            isError = ((errorCode = callablestatement.getString(14)) != null);
            errorKey = callablestatement.getString(15);
            errorMsg = callablestatement.getString(16);
        } catch (Exception ex) {
            isError = true;
            errorCode = "ME820";
            errorKey = "unexpected.error";
            errorMsg = "Unexpected Error: " + ex.toString();
            // Log to server
            System.err.println(CommonUtil.buildExceptionMsg(username + " " + curriculumName, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
        } finally {
            DBUtil.freeDBResources(locator, null, callablestatement, conn, "Completion.grantCompletion");
        }
        // Handle Error and Warning messages
        if (isError) {
            return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, errorCode, errorKey, errorMsg);
        }

        try {
            errMark = 60;
            // Data check
            agreementIDs = agreementIDsStr.split(",", -2);
            agreementPlanIDs = agreementPlanIDsStr.split(",", -2);
            openRegExist = openRegExistStr.split(",", -2);
            
            if (agreementIDs.length != agreementPlanIDs.length || agreementPlanIDs.length != openRegExist.length) {
                return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", "Error retrieving agreement offering");
            }
            int numItems = agreementIDs.length;
            LoginLog.writeToErrorLog(methodName + " length of returned agreementIDS " + numItems);
           
            for (int i = 0; i < numItems; i++) {
            	 LoginLog.writeToErrorLog(methodName + " agreementIDs[i].trim()).length() " + agreementIDs[i].trim().length());
                if ((agreementIDs[i] = agreementIDs[i].trim()).length() == 0) {
                    StringBuilder sb = new StringBuilder("Cannot find agreement offering for: '");
                    sb.append(curriculumName);
                    sb.append("', '");
                    sb.append(trainYear);
                    sb.append("', '");
                    sb.append(vendor);
                    sb.append("' in '");
                    sb.append(curriculumLocale);
                    sb.append("'");
                    System.err.println(sb.toString() + " - " + agreementPlanIDs[i]);
                    return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME810", "no.data.found", sb.toString());
                }
            }
            
            errMark = 70;
            // Assign certification to student
            Employee student = (Employee)ServiceLocator.getReference(Employee.class, userID);
            Certification cert = (Certification) ServiceLocator.getReference(Certification.class, certID);
            
            HeldCertificationManager heldCertManager = (HeldCertificationManager) locator.getManager(Delegates.kHeldCertificationManager);
            HeldCertificationDetail heldCertDetail = heldCertManager.findActiveHeldCertificationDetailsForLearner(student, cert);
            if (heldCertDetail == null) {
                heldCertManager.assignCertification(student, cert, 
                        (Employee)ServiceLocator.getReference(Employee.class, locator.getSabaPrincipal().getID()));
                heldCertDetail = heldCertManager.findActiveHeldCertificationDetailsForLearner(student, cert);
            }
            HeldCertification heldCert = (HeldCertification)ServiceLocator.getReference(HeldCertification.class, heldCertDetail.getId());
            
            if (!heldCertDetail.getStatus().equals(HeldCertificationStatus.kACQUIRED)) {
                // If not already acquired
                errMark = 80;
                // Set default path
                Path path = (Path) ServiceLocator.getReference(Path.class, pathID);
                //command.setDefaultPath(heldCert, path);
                heldCertDetail.setPath(path);
                heldCertManager.updateHeldCertification(heldCert, heldCertDetail);
                
                errMark = 90;
                // Register for offerings if not already completed and there is no open registration for it already
                PartyManager partyManager = (PartyManager) locator.getManager(Delegates.kPartyManager);
                EmployeeDetail employeeDetail = partyManager.getDetail(student);

                // get currency in US Dollars
                SabaCurrency usCurrency = (SabaCurrency) ServiceLocator.getReference(SabaCurrency.class, "crncy000000000000167");

                // create Order detail
                errMark = 100;
                LearningOrderDetail learningOrderDetail = new LearningOrderDetail(
                        null,                              // client party -- null OK
                        employeeDetail.getCompany(),       // bill to party
                        null,                              // contact party -- null OK
                        usCurrency,                        // currency in US dollars
                        employeeDetail.getHomeDomain(),
                        student,                           // base customer for order
                        null);                             // soldBy party -- null OK
                

                DownloadableDetail downloadableDetail = null;
                
                ArrayList<DownloadableDetail> orderList = new ArrayList<DownloadableDetail>();
                LearningOrderServices learningOrderServices = (LearningOrderServices)locator.getHome(Delegates.kLearningOrderServices);

                for (int i = 0; i < numItems; i++) {
                    if (!openRegExist[i].equals("Y") && !heldCertManager.isLearningInterventionCompleteInTimeFrame(heldCert, 
                            (LearningIntervention)ServiceLocator.getReference(LearningIntervention.class, agreementPlanIDs[i]))) {
                        // Register only if it is not already completed and there is no open registration for it
                        downloadableDetail = new DownloadableDetail(
                                null, // desc 
                                new BigDecimal(0), 
                                1, // Qty 
                                (WBTOffering) ServiceLocator.getReference(agreementIDs[i]), 
                                null, // note, 
                                student);
                        downloadableDetail.setPrerequisiteWaived(true);
                        orderList.add(downloadableDetail);
                    }
                }
                if (orderList.size() > 0) {
                    //Order order = 
                    learningOrderServices.createOrder(learningOrderDetail, orderList);
                }
            }
            
            errMark = 110;
            // Grant AdHoc completion of the template regardless of whether the student has acquired the cert already or not
            // Do this after assigning the certification in case "Past Credit Days" is set to 0
            OfferingTemplateManager otManager = (OfferingTemplateManager) locator.getManager(Delegates.kOfferingTemplateManager);
            OfferingTemplate offeringTemplate = (OfferingTemplate) ServiceLocator.getReference(OfferingTemplate.class, templateID);
            OfferingTemplateDetail otDetail = otManager.getOfferingTemplateDetail(offeringTemplate);

            String deliveredByID = locator.getSabaPrincipal().getID();
            
            Integer iStatus = new Integer(ParticipationStatus.kCOMPLETED.getKey());
            Integer iMastery = new Integer(MasteryStatus.kSUCCESSFUL.getKey());

            errMark = 115;
            OfferingActionProfileDetail oapDetail = new OfferingActionProfileDetail(
                    null,                           // String actionNo, 
                    offeringTemplate,               // OfferingTemplate h, 
                    iStatus,                        // Integer status, 
                    student,                        // Person p, 
                    null,                           // String grade, 
                    iMastery,                       // Integer mastery,  
                    null,                           // BigDecimal score, 
                    completionDate,                 // Date startDate, 
                    completionDate,                 // Date completionDate,  
                    completionDate,                 // Date addedToProfileOn, 
                    completionDate,                 // Date targetDate, 
                    null,                           // String location, 
                    deliveredByID,                  // String deliveredBy (expects Saba Person ID), 
                    null,                           // String startTime, 
                    null,                           // String endTime, 
                    null,                           // String type,
                    completionDate,                 // Date endDate,
                    0.0f,                           // float duration,
                    null                            // Date offeringStartDate
            );
                        
            errMark = 120;
            // Actually create the AdHoc completion
            oapDetail.setCompletionStatus(CompletionStatus.kSUCCESSFUL);    
            AdHocLearningManager ahlManager = (AdHocLearningManager) locator.getManager(Delegates.kAdHocLearningManager);
            ahlManager.updateCatalogAdHocLearning(offeringTemplate, otDetail, oapDetail, locator.getSabaPrincipal().getID());
            //ahlManager.createAdHocLearning(otDetail, oapDetail); // Don't use this one, will create another template!!!

        } catch (Exception ex) {
            // Log to server
            System.err.println(CommonUtil.buildExceptionMsg(username + " " + curriculumName, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
            return new Response(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", errMark + ": " + ex.toString());
        }
        return new Response(STATUS_CODE_SUCCESS, STATUS_KEY_SUCCESS); 
    }
    */
    
    protected boolean usernameExists(String username) throws SQLException, DBUtilException {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        boolean exists = false;

        try {
            conn = DBUtil.getDefaultSiteConnection("ABModelCompletion.usernameExists");
            statement = conn.prepareStatement(SQL_CHECK_USERNAME);
            statement.setString(1, username);
            rs = statement.executeQuery();
            rs.next();
            exists = (rs.getInt(1) > 0);
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "ABModelCompletion.usernameExists");
        }
        
        return exists;
    }

}

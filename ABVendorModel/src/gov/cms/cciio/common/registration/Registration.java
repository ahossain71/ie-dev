package gov.cms.cciio.common.registration;

/*
 * Change Log
 * 
 * 2016-05-14   Feilung Wong    Modified from ABModelCompletion. This is now a standardized registration code for both AB Vendor and Refreshers
 * 
 */

import gov.cms.cciio.common.auth.LoginLog;
import gov.cms.cciio.common.auth.MLMSLoginBean;
import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.response.MLMSResponse;
import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.DBUtil;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import com.saba.certification.Certification;
import com.saba.certification.HeldCertification;
import com.saba.certification.HeldCertificationDetail;
import com.saba.certification.HeldCertificationManager;
import com.saba.certification.HeldCertificationStatus;
import com.saba.certification.LearningIntervention;
import com.saba.certification.Path;
import com.saba.currency.SabaCurrency;
import com.saba.ejb.SabaMessageDrivenBean;
import com.saba.learning.order.DownloadableDetail;
import com.saba.learning.order.LearningOrderServices;
import com.saba.learningoffering.WBTOffering;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.offering.OfferingTemplate;
import com.saba.offering.OfferingTemplateDetail;
import com.saba.offering.OfferingTemplateManager;
import com.saba.offering.adhoclearning.AdHocLearningManager;
import com.saba.offering.offeringaction.CompletionStatus;
import com.saba.offering.offeringaction.MasteryStatus;
import com.saba.offering.offeringaction.OfferingActionProfileDetail;
import com.saba.offering.offeringaction.ParticipationStatus;
import com.saba.order.LearningOrderDetail;
import com.saba.party.PartyManager;
import com.saba.party.person.Employee;
import com.saba.party.person.EmployeeDetail;
import com.saba.util.BaseDataBaseUtil;

// Extending it just to inherit the transaction helper methods
public class Registration extends SabaMessageDrivenBean {

    private static final String CLASSNAME = Registration.class.getName();
    protected static final String SQL_AP_MLMS_REG_FIND_ID = "BEGIN AP_MLMS_REG_FIND_ID(@001, @002, @003, @004, @005, @006, @007, @008, @009, @010, @011, @012, @013, @014, @015, @016); END;";
    protected static final String SQL_AP_MLMS_AUTOENROLL_LOG = "BEGIN AP_MLMS_AUTOENROLL_LOG(@001, @002, @003, @004); END;";

    protected static final String SQL_GET_NUM_CURRENT_YEAR_VENDOR_COMP = "SELECT COUNT(*) FROM AT_MLMS_VENDOR_COMP v INNER JOIN AT_MLMS_CURRENT_YEAR y ON v.CURR_YEAR=y.CURRENT_YEAR "
    		+ "WHERE USERNAME=? AND v.CURR_CODE IN ('I', 'J')";
    protected static final String SQL_GET_AUTOENROLL_CURRENT_YEAR = "SELECT AUTOENROLL_ID, VENDOR_NAME, CURR_CODE, CURR_LANG, CURR_YEAR FROM AT_MLMS_AUTOENROLL a "
    		+ "INNER JOIN AT_MLMS_CURRENT_YEAR y ON a.CURR_YEAR=y.CURRENT_YEAR WHERE a.USERNAME=? AND STATUS IN ('N', 'F')";
    
    protected static final String SQL_GET_CURRENT_YEAR_COMPLETIONS = "SELECT DISTINCT CURR_CODE FROM AT_MLMS_VENDOR_COMP vc INNER JOIN CMT_PERSON p ON vc.USERNAME=p.USERNAME "
    		+ "INNER JOIN AT_MLMS_CURRENT_YEAR y ON y.CURRENT_YEAR = vc.CURR_YEAR WHERE p.ID=? AND vc.STATUS IN ('S', 'K')";

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
     * 
     * @param locator
     * @param vendorName Vendor name from AT_MLMS_VENDOR_REF.VENDOR_NAME
     * @param username
     * @param curriculumCode AT_MLMS_CURRICULUM_REF.CURRICULUM_CODE
     * @param curriculumLanguage Currently expecting English/Spanish (logic in Stored Procedure AP_MLMS_REG_FIND_ID
     * @param trainYear
     * @param grantVendorTemplateCompletion whether this is a vendor path that expects granting completion to the special offering template
     * @param completionDateStr yyyy/mm/dd, can be null if grantVendorTemplateCompletion is false
     * @return
     * @throws DBUtilException
     */
    public static MLMSResponse register(ServiceLocator locator, String vendorName, String username, String curriculumCode, 
    		String curriculumLanguage, String trainYear, boolean grantVendorTemplateCompletion, String completionDateStr) 
            throws DBUtilException {
        String methodName = "register";
        LoginLog.writeToErrorLog(methodName);
        LoginLog.writeToErrorLog(" Vendor " + vendorName );
        LoginLog.writeToErrorLog(" Username " + username );
        LoginLog.writeToErrorLog(" Curriculumm Code " + curriculumCode );
        LoginLog.writeToErrorLog(" Language " +  curriculumLanguage );
        LoginLog.writeToErrorLog(" Training year " +  trainYear );
        LoginLog.writeToErrorLog(" Vendor grant " +  grantVendorTemplateCompletion );
        LoginLog.writeToErrorLog(" Completion date  " +  completionDateStr );
        Connection conn = null;
        CallableStatement callablestatement = null;
        boolean isError = false;
        int errMark = 0;
        Date completionDate = null;
        String userID = null;
        String certID = null;
        String pathID = null;
        String offeringIDsStr = null;
        String[] offeringIDs = null;
        String eduPlanIDsStr = null;
        String[] eduPlanIDs = null;
        String openRegExistStr = null;
        String[] openRegExist = null;
        String templateID = null;
        String errorCode = null;
        String errorKey = null;
        String errorMsg = null;
        
        // Basic validations
        if (username == null || (username=username.trim()).length() == 0) {
            return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME800", "invalid.data", "Username cannot be empty");
        }
        
        username = username.trim().toUpperCase();
        
        if (grantVendorTemplateCompletion) {
	        try {
	            int year = Integer.parseInt(completionDateStr.substring(0, 4));
	            int month = Integer.parseInt(completionDateStr.substring(5, 7)) - 1; // month is zero based
	            int day = Integer.parseInt(completionDateStr.substring(8, 10));
	            GregorianCalendar calendar = new GregorianCalendar(year, month, day);
	            completionDate = calendar.getTime();
	        } catch (Exception ex) {
	            return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME800", "invalid.data", "Invalid Completion Date");
	        }
        }
        
        try {
            //logger.finer("About to call AP_MLMS_AB_MODEL");
            conn = DBUtil.getConnection(locator, "Registration.register");
            errMark = 50;
            //callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_AB_MODEL));
            callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_REG_FIND_ID));

            callablestatement.setString(1, vendorName);
            callablestatement.setString(2, username);
            callablestatement.setString(3, curriculumCode);
            callablestatement.setDate(4, (completionDate==null)?null:new java.sql.Date(completionDate.getTime()));
            callablestatement.setString(5, curriculumLanguage);
            callablestatement.setString(6, trainYear);
            callablestatement.registerOutParameter(7, Types.VARCHAR); // User ID
            callablestatement.registerOutParameter(8, Types.VARCHAR); // Cert ID
            callablestatement.registerOutParameter(9, Types.VARCHAR); // Path ID
            callablestatement.registerOutParameter(10, Types.VARCHAR); // Offering IDs
            callablestatement.registerOutParameter(11, Types.VARCHAR); // Education Plan IDs
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
            LoginLog.writeToErrorLog(methodName + " offeringIDsStr " + callablestatement.getString(10));
            LoginLog.writeToErrorLog(methodName + " EducationPlanIDsStr " + callablestatement.getString(11));
            LoginLog.writeToErrorLog(methodName + " openRegExistStr " + callablestatement.getString(12));
            if ((offeringIDsStr = callablestatement.getString(10)) == null) {
                offeringIDsStr = "";
            }
            if ((eduPlanIDsStr = callablestatement.getString(11)) == null) {
                eduPlanIDsStr = "";
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
            LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg(username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
        } finally {
            DBUtil.freeDBResources(locator, null, callablestatement, conn, "Registration.register");
        }
        // Handle Error and Warning messages
        if (isError) {
            return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, errorCode, errorKey, errorMsg);
        }
        
        if (grantVendorTemplateCompletion && templateID == null) {
        	return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME810", "no.data.found", "Vendor Offering Template not found");
        }

        try {
            errMark = 60;
            // Data check
            offeringIDs = offeringIDsStr.split(",", -2);
            // offeringIDs length is normally 20
            eduPlanIDs = eduPlanIDsStr.split(",", -2);
            openRegExist = openRegExistStr.split(",", -2);
            
            if (offeringIDs.length != eduPlanIDs.length || eduPlanIDs.length != openRegExist.length) {
                return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", "Error retrieving offering");
            }
            int numItems = offeringIDs.length;
            LoginLog.writeToErrorLog(methodName + " length of returned offeringIDS " + numItems);
           
            for (int i = 0; i < numItems; i++) {
            	 LoginLog.writeToErrorLog(methodName + " offeringIDs[i].trim()).length() " + offeringIDs[i].trim().length());
                if ((offeringIDs[i] = offeringIDs[i].trim()).length() == 0) {
                    StringBuilder sb = new StringBuilder("Cannot find agreement offering for: '");
                    sb.append(curriculumCode);
                    sb.append("', '");
                    sb.append(trainYear);
                    sb.append("', '");
                    sb.append(vendorName);
                    sb.append("' in '");
                    sb.append(curriculumLanguage);
                    sb.append("'");
                    System.err.println(sb.toString() + " - " + eduPlanIDs[i]);
                    return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME810", "no.data.found", sb.toString());
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
                            (LearningIntervention)ServiceLocator.getReference(LearningIntervention.class, eduPlanIDs[i]))) {
                        // Register only if it is not already completed and there is no open registration for it
                        downloadableDetail = new DownloadableDetail(
                                null /* desc */, 
                                new BigDecimal(0), 
                                1 /* Qty */, 
                                (WBTOffering) ServiceLocator.getReference(offeringIDs[i]), 
                                null /* note */, 
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
            // Vendor path, mark the special vendor offering template complete
            if (grantVendorTemplateCompletion && templateID != null) {
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
        	}
        } catch (Exception ex) {
            // Log to server
        	LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg(username + " " + curriculumCode, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
            return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", errMark + ": " + ex.toString());
        }
        return new MLMSResponse(STATUS_CODE_SUCCESS, STATUS_KEY_SUCCESS); 
    }
    
    public static MLMSResponse findAndAutoEnrollCurrentYear(ServiceLocator locator, MLMSLoginBean loginBean) {
        String methodName = "findAndAutoEnrollCurrentYear";
        MLMSResponse response = null;
        Registration registration = new Registration();
        int errMark = 0;
        int numVendorCompletionsThisYear = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String[]> records = new ArrayList<String[]>();
		String username = "<null>";
		
		
		LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear");
		
		try {
	        // Basic validations
            if (loginBean == null) {
            	throw new IllegalArgumentException("LoginBean cannot be null");
            }

            username = loginBean.getUsername();
            if (username == null || (username=username.trim()).length() == 0) {
                return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME800", "invalid.data", "Username cannot be empty");
            }
            
            username = username.trim().toUpperCase();


			conn = DBUtil.getConnection(locator, "Registration.findAndAutoEnroll");
			
			stmt = conn.prepareStatement(SQL_GET_NUM_CURRENT_YEAR_VENDOR_COMP);
			stmt.setString(1, username);
			
			if ((rs = stmt.executeQuery()) != null) {
				if (rs.next()) {
					numVendorCompletionsThisYear = rs.getInt(1);
				}
			}
			DBUtil.freeDBResources(locator, rs, stmt, null, "Registration.findAndAutoEnroll");

			LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear - vendor completions this year: " + numVendorCompletionsThisYear);

			stmt = conn.prepareStatement(SQL_GET_AUTOENROLL_CURRENT_YEAR);
			stmt.setString(1, username);
			
			if ((rs = stmt.executeQuery()) != null) {
				while (rs.next()) {
					// Although this process support auto-registration for anything, we currently only expect auto-enrolling MLMS refresher. Thus:
					// Vendor Name = MLMS, Curriculum Code = R (i.e. Curriculum Name: Individual Marketplace Refresher)

					// AUTOENROLL_ID, VENDOR_NAME, CURR_CODE, CURR_LANG, CURR_YEAR
					records.add(new String[] {rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)});
					
					LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear: " + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5));

				}
			}
		} catch (Exception ex) {
            // Log to server
			LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg("Error retreiving auto-enroll from DB - " + username, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
            return new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
		} finally {
			 DBUtil.freeDBResources(locator, rs, stmt, conn, "Registration.findAndAutoEnroll-finally");
		}
		
		try {			 
			for (String[] params : records) {
				// Special Rule, if the student has Vendor completion for this year already, just skip enrollment to MLMS Refresher.
				if (params[1].equals("MLMS") && params[2].equals("R") && numVendorCompletionsThisYear > 0) {

					LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear: Skipping " + params[0] + "," + params[1] + "," + params[2] + "," + params[3] + "," + params[4]);
					
					logAutoEnroll(params[0], "K", "skip", "Vendor Completion");
				} else {
					try {
						registration.beginTransaction();

						LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear: Processing " + params[0] + "," + params[1] + "," + params[2] + "," + params[3] + "," + params[4]);
						
						response = register(locator, params[1], username, params[2], params[3], params[4], false, null);
					} catch (Exception ex) {
						// Log to server
						LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg(params[0], CLASSNAME, methodName, errMark,
								CommonUtil.stackTraceToString(ex)));
						response = new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
					} finally {
						if (response.getStatusCode().equals(STATUS_CODE_SUCCESS)) {
							errMark = 20;
							try {
								registration.commitTransaction();
							} catch (Exception ex) {
								// Log to server
								LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg("Commit " + params[0], CLASSNAME, methodName, errMark,
										CommonUtil.stackTraceToString(ex)));
		                    }
		                } else {
		                    errMark = 30;
		                    try {
		                        registration.rollbackTransaction();
		                    } catch (Exception ex) {
		                        // Log to server
		                    	LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg("Rollback " + params[0], CLASSNAME, methodName, errMark,
		                                CommonUtil.stackTraceToString(ex)));
		                    }
		                }
		            }

					LoginLog.writeToErrorLog("Registration.findAndAutoEnrollCurrentYear: Result " + params[0] + ", " + response.getStatusCode());
					
			        // This is outside of the transaction, log this processing regardless of results
			        logAutoEnroll(params[0], (response.getStatusCode().equals("MS200")?"S":"F"), response.getErrorCode(), response.getErrorMessage());
				}
			}
		} catch (Exception ex) {
			LoginLog.writeToErrorLog(CommonUtil.buildExceptionMsg(username, CLASSNAME, methodName, errMark,
					CommonUtil.stackTraceToString(ex)));
			response = new MLMSResponse(STATUS_CODE_FAILURE, STATUS_KEY_FAILURE, "ME820", "unexpected.error", ex.toString());
		}
		
		if (response == null) {
			// Nothing to process
			response = new MLMSResponse(STATUS_CODE_SUCCESS, STATUS_KEY_SUCCESS);
		}
		
        return response;
    }

    protected static void logAutoEnroll(String autoEnrollID, String statusCode, String errorCode, String errorMsg) {
        String methodName = "logAutoEnroll";
        Connection conn = null;
        CallableStatement callablestatement = null;
        int errMark = 0;
        
        try {
            //logger.finer("About to call AP_MLMS_AUTOENROLL_LOG");
            // Log even without a valid login, don't rely on locator
            conn = DBUtil.getDefaultSiteConnection("Registration.logAutoEnroll");
            errMark = 20;
            callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(SQL_AP_MLMS_AUTOENROLL_LOG));
            errMark = 30;

            callablestatement.setString(1, autoEnrollID);
            callablestatement.setString(2, statusCode);
            callablestatement.setString(3, errorCode);
            if (errorMsg != null) {
                if (errorMsg.length() > 4000) {
                    errorMsg = errorMsg.substring(0, 4000);
                }
            }
            callablestatement.setString(4, errorMsg);
            errMark = 40;
            callablestatement.execute();
            errMark = 50;
        } catch (Exception ex) {
            System.err.println(CommonUtil.buildExceptionMsg("Logging Auto-Enroll: " + autoEnrollID, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
        } finally {
            DBUtil.freeDefaultSiteDBResources(null, callablestatement, conn, "Registration.logAutoEnroll");
        }
    }
    
    public static Completions getCurrentYearCompletions(String studentID) {
        String methodName = "getCurrentYearCompletions";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int errMark = 0;
        String tempStr = null;
        boolean imVendor = false;
        boolean imVendorRefresher = false;
        boolean shopVendor = false;
        
        try {
            conn = DBUtil.getDefaultSiteConnection("Registration.getCurrentYearCompletions");
            errMark = 20;
            statement = conn.prepareStatement(SQL_GET_CURRENT_YEAR_COMPLETIONS);
            statement.setString(1, studentID);
            errMark = 30;
            if ((rs = statement.executeQuery()) != null) {
            	while (rs.next()) {
            		tempStr = rs.getString(1);
            		if (tempStr.equals("I")) {
            			imVendor = true;
            		} else if (tempStr.equals("J")) {
            			imVendorRefresher = true;
            		} else if (tempStr.equals("S")) {
            			shopVendor = true;
            		}
            	}
            }
        } catch (Exception ex) {
            System.err.println(CommonUtil.buildExceptionMsg("Register.getCurrentYearCompletions: " + studentID, CLASSNAME, methodName, errMark,
                    CommonUtil.stackTraceToString(ex)));
        } finally {
            DBUtil.freeDefaultSiteDBResources(rs, statement, conn, "Registration.getCurrentYearCompletions");
        }
        
        return new Completions(imVendor, imVendorRefresher, shopVendor);
    }
}

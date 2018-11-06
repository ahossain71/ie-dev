package gov.cms.cciio.common.auth;

/*
 * 2010-07-21   Feilung Wong    Copied from 5.3 code: mil.army.dls.auth.User.java
 * 2010-09-21   Feilung Wong    Get Saba object references from CommonReferences.
 * 2010-10-14   Feilung Wong    Refactored timestamp helper methods, minimize uses of SimpleDateFormat
 * 2011-04-19   Feilung Wong    Added logic to avoid unnecessary updates to DB, reduce DB load
 * 2011-05-09   Feilung Wong    Using new DBUtil methods to better log and release DB resources
 * 2011-05-13   Feilung Wong    Removed reference to DBUtil.queryDatabase and get PreparedStatement for closing explicitly
 *                              ATRRS should not need to update AT_LMS_CMT_PERSON_EXT
 *                              findLMSUserRecord() consolidates DB connections, and it returns fields from extension table as well,
 *                              existing calls to findUser() should be migrated to use findLMSUserRecord()
 * 2011-06-03   Feilung Wong    QC 794: in assignBusinessUnit(), instead of looking at getDisplayName(), use getId()
 * 2011-07-13   Feilung Wong    Updated AP_LMS_FIND_USER for QC 789, checks for null and error message from stored procedure
 * 2011-10-03   Feilung Wong    QC 854: Use Java PreparedStatement bind variables as much as possible to improve caching/performance
 * 2011-10-06   Feilung Wong    QC 868 LMS start recording EDIPI in CUSTOM2
 *                              Checking LDAP values for null as well (allowing setting LMS values to null if LDAP is null)
 * 2011-10-13   Feilung Wong    Modified from version 23. With changes from QC 875 but extended to cover more fields
 *                              QC 875: should not fail login due to length/validity of data unless it's critical user identifier
 * 2012-07-06   Feilung Wong    QC 1060: Do not throw exception when there is an access issue due to AKO account type.
 *                              We do not want the transaction to roll back but to go ahead and disable the account.
 *                              Also, the ability to reactivate account is added.
 * 2012-10-19   Feilung Wong    QC 1060: Added a method to do quick disable
 * 2013-02-11   Feilung Wong    QC 1060: Fixed the bug which security list is not added if acc type stays the same 
 *                              while only status code changes from deactivate to active
 * 2013-03-27   Feilung Wong    Added retry for ATRRS user update due to NOWAIT
 * 2013-04-03   Feilung Wong    QC 1125: Added mandatory tutorial video flag in extension table
 * 2013-04-15   Feilung Wong    Make sure Custom8 is in upper case
 * 2015-05-22   Steve Meyer		Modify for CMS
 * 2015-07-09	Steve Meyer		Add Encryption
 */

import gov.cms.cciio.common.exception.DBUtilException;
import gov.cms.cciio.common.exception.LoginException;
import gov.cms.cciio.common.exception.MLMSException;
import gov.cms.cciio.common.exception.MLMSStatusCode;
import gov.cms.cciio.common.exception.NoAccessException;
import gov.cms.cciio.common.exception.UserException;
import gov.cms.cciio.common.registration.Registration;
import gov.cms.cciio.common.response.MLMSResponse;
import gov.cms.cciio.common.util.CommonReferences;
import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.Constants;
import gov.cms.cciio.common.util.DBUtil;
import gov.cms.cciio.common.util.SystemInit;
import gov.cms.cciio.interfaces.abmodel.ABModelCompletion;
import gov.cms.cciio.interfaces.abmodel.Response;

import java.net.InetAddress;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.saba.auth.TokenEncryption;
import com.saba.customattribute.CustomAttributeValueDetail;
import com.saba.customattribute.ICustomAttributeValueDetail;
import com.saba.domain.Domain;
import com.saba.ejb.IPrimaryKey;
import com.saba.exception.SabaDBException;
import com.saba.exception.SabaException;
import com.saba.exception.SabaRuntimeException;
import com.saba.function.JobFamily;
import com.saba.function.JobType;
import com.saba.function.JobTypeDetail;
import com.saba.function.JobTypeEntity;
import com.saba.i18n.SabaLocale;
import com.saba.i18n.SabaTimeZone;
import com.saba.learning.learninggroup.AudienceType;
import com.saba.learning.learninggroup.AudienceTypeManager;
import com.saba.locator.Delegates;
import com.saba.locator.ServiceLocator;
import com.saba.party.Gender;
import com.saba.party.PartyManager;
import com.saba.party.organization.BusinessUnit;
import com.saba.party.person.Employee;
import com.saba.party.person.EmployeeDetail;
import com.saba.primitives.AddressDetail;
import com.saba.primitives.ContactInfoDetail;
import com.saba.primitives.NameDetail;
import com.saba.primitives.SecurityInfoDetail;
import com.saba.security.IComplexPrivilege;
import com.saba.security.ISecurityList;
import com.saba.security.SabaSecurityManager;

/**
 * Helper class that provides a unified way to create MLMS user accounts. All
 * customization by the LMS and Interface should use this class to create users.
 * 
 * Sample usage:
 * 
 * 
 * 
 * @author Feilung Wong, Steve Meyer updated Jul 18, updated SQL insert to
 *         include eidm account date
 */
public class User {
	/**
	 * This will allow ClassVersionCheck to extract file version in case of
	 * troubleshooting.
	 */
	public static final String MLMS_FILE_VERSION = "2.2";

	public static final String PAGE_CODE = "SSO01";

	/** Creating className instance */
	private static final String sourceClass = User.class.getName();
	
	/** Creating logger */
	private static final Logger logger = Logger.getLogger(sourceClass);

	private static final int NOWAIT_RETRIES = 5;
	/** Number of milliseconds to wait before retrying due to NOWAIT */
	private static final int NOWAIT_RETRY_MILLI = 500;
	private static final int NOWAIT_IE_RETRY_SEC = 900; // 15 minutes

	private static final SimpleDateFormat dd_MMM_yyyy_formatter = new SimpleDateFormat(
			Constants.DD_MMM_YYYY);
	private static final SimpleDateFormat MM_dd_yyyy_formatter = new SimpleDateFormat(Constants.MONTH_DAY_YEAR_STD);
	
	private static boolean isDebug = false;

	/**
	 * Created for MLMS, Portal passes fname,lname,cmsRoles,ismemberof and uid
	 * in the request header
	 * 
	 * @param loginBean
	 * @return
	 * @throws ParseException
	 */
	public static LoginResult lmsLogin(MLMSLoginBean loginBean) {
		LoginLog.writeToErrorLog("USER: version 2.2 11.21.2016 15:23");
		String username = loginBean.getUsername();
		String mlmsMode = null;
		

		LoginResult loginResult;
		AccountType accType;
		Employee employee;

		UserInfo userInfo;
		ProfilesLookUpResults lookUpResults;
		String methodName = "lmsLogin";
		Boolean isEIDM = false;
		Boolean isNewUser = false;
		int errMark = 0;
		ServiceLocator locator = null;
		// The followings two variables are for measuring performance only
		long time1 = 0;
		long time2;

		try {
			/**
			 * logs in to Saba as Admin to create user.
			 */
			locator = SystemInit.loginMLMSAdmin();

			errMark = 10;
			time1 = System.currentTimeMillis();

			// - todo - check for user in EIDM
			isEIDM = true;

			errMark = 20;
			/**
			 * Create userInfo object and set values from loginBean
			 */
			userInfo = new UserInfo();

			userInfo.setUserName(username);
			userInfo.setFName(loginBean.getFname());
			userInfo.setLName(loginBean.getLname());
			userInfo.setEmail(loginBean.getEmail());
			userInfo.setAddr1(loginBean.getStreetAddress());
			userInfo.setAddr2(loginBean.getStreetAddress2());
			userInfo.setCity(loginBean.getCity());
			userInfo.setZip(loginBean.getZipcode());
			/** set unencrypted password to set in security detail **/
			mlmsMode = System.getProperty("mlms.mode", "prod");
			isDebug = System.getProperty("mlms.debug", "true")
					.equalsIgnoreCase("true");
			LoginLog.writeToErrorLog("MLMS Mode : should not be 'pt' in prod!!"
					+ mlmsMode);
			Calendar calendar = Calendar.getInstance();
			Long lTime = calendar.getTimeInMillis();

			/*
			 * put back in the loginBean this posts the encrypted password to
			 * Saba
			 */
			/* password is sent encrypted and decrypted */
			try {
				String encryptedPassword = null;
				if (mlmsMode.equalsIgnoreCase(Constants.MLMS_PT_MODE)) {
					/* get passwords identical for PT testing */
					userInfo.setPassword(Constants.PT_PASSWORD);
					encryptedPassword = TokenEncryption.encrypt(userInfo
							.getPassword());

				} else {
					/*
					 * password set in userInfo object is set within Saba as the
					 * user password this includes the timestamp
					 */
					userInfo.setPassword(lTime
							+ CommonUtil.replaceCharacters(loginBean
									.getUsername()) + loginBean.getRole());
					encryptedPassword = TokenEncryption.encrypt(userInfo
							.getPassword());

				}
				/*
				 * password in login bean is passed to Saba security filter
				 * chain vi Post
				 */
				loginBean.setPassword(encryptedPassword);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			errMark = 25;
			// locates user in the database
			employee = null;
			accType = null;
			lookUpResults = User.findLMSProfiles(locator, userInfo);

			if (lookUpResults.getLMSUserRecord() == null && isEIDM) {
				LoginLog.writeToErrorLog(User.class.getSimpleName()
						+ "New User");
				/* create lookupResults object */
				lookUpResults = new ProfilesLookUpResults();

				accType = new AccountType();
				accType.setSystem("eidm");
				/**
				 * for agents check value of isComplete and set appropriately
				 */
				if (loginBean.getRole().indexOf(Constants.AGENT) > -1) {
					if (loginBean.isComplete()) {

						accType.setFfmRoleStatus(Constants.COMPLETE);

						

						
					} else {
						accType.setFfmRoleStatus(Constants.INCOMPLETE);
						
					}
					if(loginBean.isSHOPComplete()){
						accType.setShopRoleStatus(Constants.COMPLETE);
					} else {
						accType.setShopRoleStatus(Constants.INCOMPLETE);
					}
					if (loginBean
							.getAgentBrokerRoleEffectiveDate() != null) {
						LoginLog.writeToErrorLog(User.class.getSimpleName()
								+ " "
								+ methodName
								+ "259 Effective Role Grant Date format MM/dd/YYYY"
								+ loginBean
								.getAgentBrokerRoleEffectiveDate());

						accType.setFfmEffectiveRoleGrantDate(loginBean
								.getAgentBrokerRoleEffectiveDate());

					}
					
				} else {
					accType.setFfmRoleStatus(null);
				}

				/**
				 * confirm domain
				 */
				accType = lookupDomain(locator, loginBean, accType);

				accType.setPersonType("Regular");
				userInfo.setAccountActive(true);
				userInfo.setStatus("Active");

				/*
				 * Required privileges for a user to login Internal Person Login
				 * Privileges, Common Domain User, Internal Person Basic
				 * Privileges ,report privileges in world domain
				 */

				accType = lookupSecurityRole(locator, loginBean, accType);

				lookUpResults.setPrimaryAccountType(accType);

			} else {

				employee = lookUpResults.getLMSEmployee();
				//PersonEntity personEntity = employee.getPersonEntity(locator);
				//Person person = (Person)employee;
				//SecurityInfoDetail secInfoDetail = personEntity.getSecurityInfo(person);

				accType = lookUpResults.getPrimaryAccountType();
				if (loginBean.getRole().indexOf(Constants.AGENT) > -1) {
					if (loginBean.isComplete()) {
						accType.setFfmRoleStatus(Constants.COMPLETE);
					} else {
						accType.setFfmRoleStatus(Constants.INCOMPLETE);
						
					}
					
						LoginLog.writeToErrorLog(User.class.getSimpleName()
								+ " "
								+ methodName
								+ " Effective Role Grant Date format MM/dd/YYYY "
								+ loginBean
								.getAgentBrokerRoleEffectiveDate());

						accType.setFfmEffectiveRoleGrantDate(loginBean
								.getAgentBrokerRoleEffectiveDate());
					
					
					//User.setABMiscData(locator, loginBean);
				} else {
					accType.setFfmRoleStatus(null);
				}

			}// determine new user

			errMark = 40;

			if ((accType = lookUpResults.getPrimaryAccountType()) == null) {
				throw new UserException(
						MLMSStatusCode.NUM_USER_ACC_TYPE_UNKNOWN,
						"No primary account type found.");
			}
			loginResult = null;

			LoginLog.writeToErrorLog(User.class + " acctype default domain "
					+ accType.getDefaultDomain());
			LoginLog.writeToErrorLog(User.class
					+ " assign certificate  isComplete "
					+ loginBean.isComplete() + " isCertReq"
					+ loginBean.isCertReq());
			LoginLog.writeToErrorLog(User.class
					+ " assign certificate  date "
					+ CommonUtil.getFormattedDateString(loginBean
							.getAgentBrokerRoleHistoricalEffectiveDate()));

			if (employee == null) {
				/*
				 * New User
				 */
				isNewUser = true;
				errMark = 50;
				loginResult = new LoginResult(loginBean.getUsername(),
						employee, accType, userInfo.getAccountTypes());
				employee = createUser(locator, userInfo, lookUpResults,
						accType, loginBean);
				/*
				 * insert new values for new employee now - record exists in cmt_person
				 */
				User.setABMiscData(locator, loginBean);
				errMark = 55;
				/**
				 * set redirect URL for agent, assister or admin if certificate
				 * request was made confirm user complete set
				 */

				if (accType.getDefaultDomain()
						.equalsIgnoreCase(Constants.AGENT)) {
					if (loginBean.isComplete() && loginBean.isCertReq()) {
						loginResult
								.setLandingPageUrl(Constants.CERTIFICATE_URL);
					} else {
						loginResult.setLandingPageUrl(Constants.AGENT_URL);
					}
				} else if (accType.getDefaultDomain().equalsIgnoreCase(
						Constants.ASSISTER)) {
					loginResult.setLandingPageUrl(Constants.ASSISTER_URL);
				} else {
					loginResult.setLandingPageUrl(Constants.DEFAULT_URL);
				}

			} else {
				/*
				 * Existing User!!! Found matching record, now check to see if
				 * it belongs to
				 */
				errMark = 60;
				// LoginLogwriteToErrorLog("Before update");
				String lmsUsername = lookUpResults.getLMSUsername();

				if (username.equalsIgnoreCase(lmsUsername)
						&& !username.equalsIgnoreCase(Constants.MLMS_SUPER)) {
					// Do not up date MLMS Super
					// Owns this account, go ahead an update it
					// Depending on account status and account type, might
					// actually deactivate and fail login
					errMark = 70;
					loginResult = updateUser(locator, lookUpResults, accType,
							userInfo, loginBean);
					// LoginLogwriteToErrorLog("after update");
					if (accType.getDefaultDomain().equalsIgnoreCase(
							Constants.AGENT)) {
						if (loginBean.isComplete() && loginBean.isCertReq()) {
							loginResult
									.setLandingPageUrl(Constants.CERTIFICATE_URL);
						} else {
							loginResult.setLandingPageUrl(Constants.AGENT_URL);
						}
					} else if (accType.getDefaultDomain().equalsIgnoreCase(
							Constants.ASSISTER)) {
						loginResult.setLandingPageUrl(Constants.ASSISTER_URL);
					} else {
						loginResult.setLandingPageUrl(Constants.DEFAULT_URL);
					}
				} else {
					// The LMS record belongs to another USERNAME,
					// update ONLY IF this username is active AND the other
					// is
					// NOT.
					if (!userInfo.isAccountActive()) {
						errMark = 80;
						// Do not touch the LMS record
						throw new UserException(
								MLMSStatusCode.NUM_USER_ACCOUNT_INACTIVE, "'"
										+ username
										+ "' is inactive according to AKO.");
					}//if
				} // if
				LoginLog.writeToErrorLog(User.class.getName() + " "
						+ methodName + "\n loginResult Landing page URL "
						+ loginResult.getLandingPageUrl());

			}

			// TODO: AB Vendor
			errMark = 100;

			LoginLog.writeToErrorLog("User - Pre ABModelCompletion");

			if (loginResult != null && loginResult.isSuccess()) {
				Response response = ABModelCompletion
						.findAndProcessCompletions(locator, loginBean);
				if (!response.getStatusCode().equals(
						ABModelCompletion.STATUS_CODE_SUCCESS)) {
					loginResult = new LoginResult(username, new UserException(
							MLMSStatusCode.NUM_GENERIC_INTERNAL,
							response.getErrorMessage()));
					String errorString = CommonUtil.buildExceptionMsg(username,
							sourceClass, methodName, errMark,
							response.getErrorMessage());
					LoginLog.writeToErrorLog(errorString);
				}

				LoginLog.writeToErrorLog("User - Post ABModelCompletion - "
						+ loginResult.isSuccess());

			} else {
				LoginLog.writeToErrorLog("User - Post ABModelCompletion - Login Result is null: "
						+ (loginResult == null));
			}
			// End AB Vendor

			// TODO: AutoEnrollment
			errMark = 110;

			LoginLog.writeToErrorLog("User - Pre AutoEnroll");

			if (loginResult != null && loginResult.isSuccess()) {
				MLMSResponse response = Registration
						.findAndAutoEnrollCurrentYear(locator, loginBean);
				if (!response.getStatusCode().equals(
						Registration.STATUS_CODE_SUCCESS)) {
					loginResult = new LoginResult(username, new UserException(
							MLMSStatusCode.NUM_GENERIC_INTERNAL,
							response.getErrorMessage()));
					String errorString = CommonUtil.buildExceptionMsg(username,
							sourceClass, methodName, errMark,
							response.getErrorMessage());
					LoginLog.writeToErrorLog(errorString);
				}

				LoginLog.writeToErrorLog("User - Post AutoEnroll - "
						+ loginResult.isSuccess());

			} else {
				LoginLog.writeToErrorLog("User - Post AutoEnroll - Login Result is null: "
						+ (loginResult == null));
			}
			// End AutoEnrollment
		} catch (SabaRuntimeException ex){
			loginResult = new LoginResult(username, new UserException(
					MLMSStatusCode.NUM_GENERIC_INTERNAL, "Error logging in: "
							+ ex.getMessage(), ex));
			String errorString = CommonUtil.buildExceptionMsg(username,
					sourceClass, methodName, errMark,
					CommonUtil.stackTraceToString(ex));
			LoginLog.writeToErrorLog(errorString);
		} catch (RuntimeException ex) {
			loginResult = new LoginResult(username, new UserException(
					MLMSStatusCode.NUM_GENERIC_INTERNAL, "Error logging in: "
							+ ex.getMessage(), ex));
			String errorString = CommonUtil.buildExceptionMsg(username,
					sourceClass, methodName, errMark,
					CommonUtil.stackTraceToString(ex));
			LoginLog.writeToErrorLog(errorString);
		} catch (SabaException ex) {
			loginResult = new LoginResult(username, new UserException(
					MLMSStatusCode.NUM_GENERIC_INTERNAL, "Error logging in: "
							+ ex.getMessage(), ex));
			String errorString = CommonUtil.buildExceptionMsg(username,
					sourceClass, methodName, errMark,
					CommonUtil.stackTraceToString(ex));
			LoginLog.writeToErrorLog(errorString);
		} catch (MLMSException ex) {
			loginResult = new LoginResult(username, ex);
			if (ex.getErrorCode() != MLMSStatusCode.NUM_GENERIC_INTERNAL
					&& ex.getErrorCode() != MLMSStatusCode.NUM_DB_GENERAL) {
				LoginLog.writeToErrorLog(ex.getErrorCode() + ": "
						+ ex.getMessage());
			} else {
				String errorString = CommonUtil.buildExceptionMsg(username,
						sourceClass, methodName, errMark,
						CommonUtil.stackTraceToString(ex));
				LoginLog.writeToErrorLog(errorString);
			}
		} catch (ParseException e) {
			loginResult = new LoginResult(username, new MLMSException(79));
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		time2 = System.currentTimeMillis();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(username + " Login time: " + (time2 - time1) + "ms");
		}
		// Log successful login

		if (loginResult != null && loginResult.isSuccess()) {
			Connection conn = null;
			PreparedStatement stmt = null;
			try {
				String hostName = InetAddress.getLocalHost().getHostName();
				conn = DBUtil.getConnection(locator, "User.lmsLogin");
				stmt = conn
						.prepareStatement("INSERT /* Common-User */ INTO AT_MLMS_LOGIN_LOG(LOGIN_ID,LOGIN_DATE, MLMS_HOST, SESSION_ID, PAGE_CODE, LOGIN_ACTION) SELECT id, sysdate, ? , ? , ? , ? FROM cmt_person WHERE username = UPPER(?) ");
				stmt.setString(1, hostName);
				stmt.setString(5, username);
				stmt.setString(2, loginBean.getSessionId());
				stmt.setString(3, PAGE_CODE);
				stmt.setString(4, getLoginActionCode(loginBean, isNewUser));
				stmt.execute();
			} catch (Exception ex) {
				StringBuilder sb = new StringBuilder(
						"Error logging successful login:");
				sb.append(CommonUtil.stackTraceToString(ex));
				LoginLog.writeToErrorLog(sb.toString());

			} finally {
				DBUtil.freeDBResources(locator, null, stmt, conn,
						"User.lmsLogin");
			}
		} else {
			LoginLog.writeToErrorLog("Login Result is null"
					+ (loginResult == null));
		}
		// Done with resources, log out the admin user!
		if (locator != null) {
			SystemInit.logoutSaba("User.lmsLogin");
		}

		return loginResult;
	}

	/**
	 * @throws mil.army.dls.common.exception.DBUtilException
	 *             Given the LDAP user information, determine which account type
	 *             should be used
	 * 
	 * @param locator
	 *            ServiceLocator object
	 * @param userInfo
	 *            the user information from LDAP
	 * @return The account type to use for this user, return null if userInfo
	 *         does not contain any account types.
	 * @throws UserException
	 *             in case of any errors or no account type found in mapping
	 *             table
	 * @throws DBUtilException
	 * @throws
	 */
	public static AccountType getPrimaryAccountType(ServiceLocator locator,
			UserInfo userInfo) throws UserException,
			gov.cms.cciio.common.exception.DBUtilException {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		StringBuilder sql = null;
		StringBuilder codeString = new StringBuilder();
		ArrayList<String> list = userInfo.getAccountTypes();
		AccountType accType = new AccountType();
		String tempStr;
		int num = list.size();
		int errMark = 0;
		int i;

		if (num == 0) {
			return null;
		}
		try {
			// Rely on the weight column in AT_INT_ACCOUNT_TYPE TABLE, then sort
			// by code
			// We only need the first account type after sorting
			sql = new StringBuilder(
					"SELECT /*Common-User*/ SYS, CODE, DEFAULT_DOMAIN, DOMAIN_ID, SECURITY_LIST, ROLE_ID, AUDIENCE_TYPE, ");
			sql.append("AUDIENCE_TYPE_ID, PERSON_TYPE, WEIGHT FROM (SELECT * FROM AV_INT_ACCOUNT_TYPE WHERE SYS = ? AND CODE IN (?");
			for (i = 1; i < num; i++) {
				sql.append(",?");
			}
			sql.append(") ORDER BY WEIGHT DESC, CODE ASC) WHERE ROWNUM = 1");
			errMark = 10;
			conn = DBUtil.getConnection(locator, "User.getPrimaryAccountType");
			statement = conn.prepareStatement(sql.toString());
			// statement.setString(1, (userInfo.foundInLDAP() ? "AKO" :
			// "ATRRS"));
			for (i = 0; i < num; i++) {
				tempStr = list.get(i);
				codeString.append(tempStr);
				codeString.append(" ");
				statement.setString(i + 2, tempStr);
			}

			rs = statement.executeQuery();
			errMark = 40;
			if (rs.next()) {
				accType.setSystem(rs.getString(1));
				accType.setCode(rs.getString(2));
				accType.setDefaultDomain(rs.getString(3)); // If equals
															// AKO_NONE_DOMAIN
															// or null, user
															// will not be
															// created
				accType.setDomainID(rs.getString(4));
				accType.setSecurityList(rs.getString(5));
				accType.setRoleID(rs.getString(6));
				// accType.setAudienceType(rs.getString(7));
				// accType.setAudienceTypeID(rs.getString(8));
				accType.setPersonType(rs.getString(9)); // If equals NO_ACCESS,
														// may not be created
				accType.setWeight(rs.getString(10));
			} else {
				codeString.append(" not found in account type mapping table");
				throw new UserException(
						MLMSStatusCode.NUM_USER_ACC_TYPE_UNKNOWN,
						codeString.toString());
			}
		} catch (SQLException ex) {
			// Make sure it releases resources.
			StringBuilder errMsg = new StringBuilder(
					"User.getPrimaryAccountType: ");
			errMsg.append(errMark);
			errMsg.append(" Query = '");
			errMsg.append(sql.toString());
			errMsg.append("'\n");
			errMsg.append(ex.getMessage());
			logger.log(Level.SEVERE, errMsg.toString(), ex);
			// Wraps the inner exception with our custom exception + custom
			// message
			throw new UserException(MLMSStatusCode.NUM_DB_GENERAL,
					errMsg.toString(), ex);
		} finally {
			DBUtil.freeDBResources(locator, rs, statement, conn,
					"User.getPrimaryAccountType");
		}

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("User.getPrimaryAccountType: " + accType);
		}
		return accType;
	}

	// /**
	// * Find LMS user record with SSN and FIN from extended table first by AKO
	// user name, then CMT_PERSON.SS_NO, then AT_LMS_CMT_PERSON_EXT
	// * This method should replace findUser() because it consolidates 3
	// connections into 1, and it retrieves data from extended table
	// * @param locator ServiceLocator object
	// * @param userInfo the user information from LDAP
	// * @return LMSUserRecord if found, null otherwise
	// * @throws UserException in case of any errors
	// * @throws DBUtilException
	// */
	// public static LMSUserRecord findLMSUserRecord(ServiceLocator locator,
	// UserInfo userInfo) throws UserException, DBUtilException {
	// Connection conn = null;
	// CallableStatement callablestatement = null;
	// User.LMSUserRecord lmsUserRecord = null;
	// String sql =
	// "BEGIN AP_LMS_FIND_USER(@001, @002, @003, @004, @005, @006, @007, @008, @009, @010, @011, @012, @013, @014, @015, @016, @017, @018, @019, @020); END;";
	// boolean isFound = false;
	// boolean isError = false;
	// boolean isByUsername = false;
	// boolean isByEDIPI = false;
	// boolean isByATRRS_SSN = false;
	// boolean isBySSN_FIN = false;
	// boolean isByExtTable = false;
	// String tempUsername = userInfo.getUserName();
	// String errorDesc = null;
	// int errMark = 10;
	//
	// try {
	// logger.finer("About to call AP_LMS_FIND_USER");
	// conn = DBUtil.getConnection(locator, "User.FindLMSUserRecord");
	// errMark = 20;
	// callablestatement = conn.prepareCall(BaseDataBaseUtil.makeCallable(sql));
	// errMark = 30;
	// if (tempUsername != null) {
	// tempUsername = tempUsername.toUpperCase();
	// }

	// if (isByUsername) {
	// logger.log(Level.FINE, "User.findLMSUserRecord: Found user by USERNAME");
	// } else if (isByEDIPI) {
	// logger.log(Level.FINE, "User.findLMSUserRecord: Found user by EDIPI");
	// } else if (isByATRRS_SSN) {
	// logger.log(Level.FINE,
	// "User.findLMSUserRecord: Found user by ATRRS SSN");
	// } else if (isBySSN_FIN) {
	// logger.log(Level.FINE, "User.findLMSUserRecord: Found user by SSN/FIN");
	// } else if (isByExtTable) {
	// userInfo.setFoundInCustomTable(true);
	// logger.log(Level.FINE,
	// "User.findLMSUserRecord: Found user by AT_LMS_CMT_PERSON_EXT");
	// }
	// } else {
	// logger.log(Level.FINE, "User.findLMSUserRecord: user not found");
	// }
	// } catch (SQLException ex) {
	// // Make sure it releases resources.
	// StringBuilder errMsg = new StringBuilder("User.findLMSUserRecord: ");
	// errMsg.append(errMark);
	// errMsg.append(" Querying for user based on username, SSN, and/or FIN.  ");
	// errMsg.append(ex.toString());
	// logger.log(Level.SEVERE, errMsg.toString(), ex);
	// // Wraps the inner exception with our custom exception + custom message
	// throw new UserException(MLMSStatusCode.NUM_DB_GENERAL, errMsg.toString(),
	// ex);
	// } catch (SabaException ex) {
	// // Make sure it releases resources.
	// StringBuilder errMsg = new StringBuilder("User.findLMSUserRecord: ");
	// errMsg.append(errMark);
	// errMsg.append(" Querying for user based on username, SSN, and/or FIN.  ");
	// errMsg.append(ex.toString());
	// logger.log(Level.SEVERE, errMsg.toString(), ex);
	// // Wraps the inner exception with our custom exception + custom message
	// throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// errMsg.toString(), ex);
	// } finally {
	// DBUtil.freeDBResources(locator, null, callablestatement, conn,
	// "User.findLMSUserRecord");
	// }
	// return lmsUserRecord;
	// }
	/**
	 * loce user in database
	 * 
	 * @param locator
	 * @author Steve Meyer
	 * @throws DBUtilException
	 * @throws SQLException
	 */
	public static ProfilesLookUpResults findLMSDBProfile(
			ServiceLocator locator, String username) throws DBUtilException,
			SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String db_username = null;
		int count = -1;
		conn = DBUtil.getConnection(locator, "findLMSDBProfile");
		if (conn != null) {
			// stmt = conn.createStatement();
			String sql = "select username  from cmt_person where username = ?";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();

			while (rs.next()) {
				db_username = rs.getString("username");
			}
		}
		// System.out.println(" Username from database " + db_username);

		return new ProfilesLookUpResults();
	}

	/**
	 * Find LMS user record with SSN and FIN from extended table first by AKO
	 * user name, then CMT_PERSON.SS_NO, then AT_LMS_CMT_PERSON_EXT This method
	 * should replace findLMSUserRecord() because it further consolidates DB
	 * connections It fetches job types, audience types, security roles, account
	 * types, and business units data as well.
	 * 
	 * @param locator
	 *            ServiceLocator object
	 * @param userInfo
	 *            the user information from LDAP
	 * @return ProfilesLookUpresults getLMSUserRecord() would be null if not
	 *         found
	 * @throws UserException
	 *             in case of any errors
	 * @throws DBUtilException
	 * @throws mil.army.dls.common.exception.DBUtilException
	 */

	public static ProfilesLookUpResults findLMSProfiles(ServiceLocator locator,
			UserInfo userInfo) throws UserException, DBUtilException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User.ProfilesLookUpResults lookUpResults = null;
		boolean isFound = false;

		String lms_id = null;
		String domain_id = null;
		String person_type = null;
		String db_username = null;
		String status = null;
		String domain_name = null;

		String username = userInfo.getUserName();
		if (username != null) {
			username = username.toUpperCase();
		}
		String errorDesc = null;
		String tempStr;
		StringBuilder sb;
		ArrayList<String> tempList;
		int tempInt, i;
		int errMark = 10;
		StringBuilder timeSB = new StringBuilder();

		String sql = "select p.id, p.home_domain domain_id, d.name domain_name, p.PERSON_TYPE, p.username, p.status"
				+ " from cmt_person p inner join fgt_domain d on p.home_domain = d.id "
				+ " where username = ?";

		CommonUtil.appendTimestamp(timeSB, username + "\tfindProfile\t");
		try {
			/**
			 * getConnection(ServiceLocator, String) - string is using for
			 * debugging simply returns a DB connection
			 */
			conn = DBUtil.getConnection(locator, "User.findLMSProfiles");
			errMark = 20;
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			// LoginLog.writeToErrorLog("find profile id " + username);
			rs = stmt.executeQuery();

			while (rs.next()) {
				lms_id = rs.getString("id");
				domain_id = rs.getString("domain_id");
				person_type = rs.getString("PERSON_TYPE");
				db_username = rs.getString("username");
				status = rs.getString("status");
				domain_name = rs.getString("domain_name");
				isFound = true;
				// LoginLog.writeToErrorLog("findLMSProfiles: find profile id "
				// + lms_id);
			}
			;

			errMark = 30;

			lookUpResults = new ProfilesLookUpResults();
			if (isFound) {

				userInfo.setAccountActive(true);
				Employee employee = (Employee) ServiceLocator.getReference(
						Employee.class, lms_id);

				lookUpResults.setLMSUserRecord(new User.LMSUserRecord(employee,
						db_username));

			}
			CommonUtil.appendTimestamp(timeSB, "\t6\t");
			// Primary account type
			if (domain_id != null) {

				AccountType accType = new AccountType();
				accType.setDefaultDomain(domain_name); // If equals
														// AKO_NONE_DOMAIN or
														// null, user will not
														// be created
				accType.setSystem("mlms");
				accType.setDomainID(domain_id);
				accType.setSecurityList(null);
				accType.setRoleID(null);
				// accType.setAudienceType(null);
				// accType.setAudienceTypeID(null);
				accType.setPersonType(person_type); // If equals NO_ACCESS, may
													// not be created

				lookUpResults.setPrimaryAccountType(accType);

			}
			CommonUtil.appendTimestamp(timeSB, "\t7\t");
			// Job Type ID History list

			// lookUpResults.setJobTypeHistList(tempList);
			CommonUtil.appendTimestamp(timeSB, "\t8\t");
			// MOS Job Type Look up
			ArrayList<String[]> tempArray = new ArrayList<String[]>();

			CommonUtil.appendTimestamp(timeSB, "\t9\t");

		} catch (SQLException ex) {
			// Make sure it releases resources.
			StringBuilder errMsg = new StringBuilder("User.findLMSProfiles: ");
			errMsg.append(errMark);
			errMsg.append(" Querying for user based on username, SSN, and/or FIN.  ");
			errMsg.append(ex.toString());
			logger.log(Level.SEVERE, errMsg.toString(), ex);
			// Wraps the inner exception with our custom exception + custom
			// message
			throw new UserException(MLMSStatusCode.NUM_DB_GENERAL,
					errMsg.toString(), ex);
		} catch (SabaException ex) {
			// Make sure it releases resources.
			StringBuilder errMsg = new StringBuilder("User.findLMSProfiles: ");
			errMsg.append(errMark);
			errMsg.append(" Querying for user based on username, SSN, and/or FIN.  ");
			errMsg.append(ex.toString());
			logger.log(Level.SEVERE, errMsg.toString(), ex);
			// Wraps the inner exception with our custom exception + custom
			// message
			throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
					errMsg.toString(), ex);
		} finally {
			DBUtil.freeDBResources(locator, rs, stmt, conn,
					"User.findLMSProfiles");
			logger.fine(timeSB.toString());
		}
		return lookUpResults;
	}

	// /**
	// * Calls findLMSUserRecord, can be used if the caller does not care
	// * Used by LMS page: MLMSDeepLink.jsp
	// * @param locator ServiceLocator object
	// * @param userInfo the user information from LDAP
	// * @return A reference to the Employee object, return null if not found.
	// * @throws UserException in case of any errors
	// * @throws DBUtilException
	// */
	// public static Employee findUser(ServiceLocator locator, UserInfo
	// userInfo) throws UserException, DBUtilException {
	// //LMSUserRecord lmsUserRecord = findLMSUserRecord(locator, userInfo);
	// ProfilesLookUpResults lookUpResults = findLMSProfiles(locator, userInfo);
	// if (lookUpResults != null) {
	// return lookUpResults.getLMSEmployee();
	// } else {
	// return null;
	// }
	// }

	/**
	 * Creates a user, given the information from LDAP. It is assumed that the
	 * caller has determined which account type a user should be using. This
	 * method does not determine whether a user should have access to MLMS or
	 * not. The caller should have called <i>getPrimaryAccountType()</i> and
	 * determine access right before calling this method.
	 * 
	 * @param locator
	 *            ServiceLocator object
	 * @param userInfo
	 *            the user information from LDAP
	 * @param accountType
	 *            the appropriate account type to use for this user
	 * @param loginBean
	 *            TODO
	 * @return a reference to the created Employee object
	 * @throws UserException
	 * @throws DBUtilException
	 * @throws NoAccessException
	 */
	// public static Employee createUser(ServiceLocator locator, UserInfo
	// userInfo, AccountType accountType)
	// throws UserException, DBUtilException, NoAccessException {
	// Employee employee = null;
	// String tempStr = null;
	// int errMark = 0;
	//
	// if (userInfo == null) {
	// throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// "UserInfo is Null. Object should be populated before calling User.createUser()");
	// }
	//
	// // This is the value that will be saved in CMT_PERSON.SS_NO field
	// String cmtPersonSS_NO = userInfo.getSSNo();
	// //QC-ID 603:
	// //Validate SSN is NOT null
	// if (userInfo.isATRRS()) {
	// //We're guaranteed this is set. This is SSN passed in by ATRRS
	// cmtPersonSS_NO = userInfo.getAtrrsSSN();
	// } else {
	// //This is NOT ATRRS
	// if (userInfo.getSSNo() == null) {
	// if (userInfo.getArmyFIN() == null) {
	// throw new UserException(MLMSStatusCode.NUM_USER_NULL_SSN_FIN,
	// "User.createUser: AKO SSN and FIN for user " + userInfo.getUserName() +
	// " is NULL.");
	// } else {
	// //QC-ID 627:
	// //Use FIN as SSN
	// cmtPersonSS_NO = userInfo.getArmyFIN();
	// }
	// }
	// }
	//
	// try {
	// PartyManager partyManager =
	// (PartyManager)locator.getManager(Delegates.kPartyManager);
	// EmployeeDetail empDetail = new EmployeeDetail();
	//
	// // Set varies default values
	// setDefaultAttributes(userInfo);
	//
	// if (userInfo.isATRRS()) {
	// errMark = 10;
	// if (!userInfo.foundInLDAP()) {
	// // Temporary user name
	// userInfo.addUsername(userInfo.getFName() + "_" + userInfo.getLName() +
	// "_" + (new Date()).getTime());
	// // for SGLMS interface: Accepts email in valid AKO FORMAT only
	// tempStr = userInfo.getEmail();
	// if (tempStr != null) {
	// tempStr = tempStr.trim().toLowerCase();
	// if (tempStr.endsWith("@us.army.mil")) {
	// userInfo.resetEmail(tempStr);
	// } else {
	// userInfo.resetEmail(null);
	// }
	// }
	// }
	// empDetail.setStatus(Constants.USER_DEFAULT_ATRRS_STATUS);
	// errMark = 20;
	// interfaceToLMS(userInfo, empDetail);
	// } else {
	// empDetail.setStatus(Constants.USER_DEFAULT_LMS_STATUS);
	// }
	//
	// errMark = 30;
	// // Check for access
	// // Unlike updateUser, just throw exception and don't create the account
	// if no access
	// if (!hasAccess(userInfo, accountType)) {
	// throw new NoAccessException(MLMSStatusCode.NUM_USER_NO_ACCESS,
	// "This user should have no access to the MLMS system according to AKO account type");
	// }
	//
	// /** Copy LDAP Attributes over to Employee Detail */
	// ldapToLMS(empDetail, userInfo, accountType);
	//
	// //This value determined above. If AKO SSN was null
	// //and FIN not null, this value will equal FIN
	// empDetail.setSsNo(cmtPersonSS_NO);
	//
	// if (accountType.getCode() != null) {
	// // Custom 8 is used to the store the Account Type/ATRRS Split
	// empDetail.setCustomValue(new CustomAttributeValueDetail(
	// ICustomAttributeValueDetail.kCustom8,
	// accountType.getCode().toUpperCase()));
	// }
	//
	// errMark = 40;
	// SecurityInfoDetail securityInfoDetail = new
	// SecurityInfoDetail(userInfo.getUserName(), userInfo.getPassword());
	// empDetail.setSecurityInfoDetail(securityInfoDetail);
	//
	// errMark = 45;
	// SabaTimeZone timezone = (SabaTimeZone)
	// ServiceLocator.getReference(SabaTimeZone.class,
	// userInfo.getTimezone());
	// empDetail.setTimezone(timezone);
	//
	// errMark = 50;
	// Domain domain = (Domain) ServiceLocator.getReference(Domain.class,
	// accountType.getDomainID());
	// empDetail.setHomeDomain(domain);
	// empDetail.setSecurityDomain(domain);
	//
	// errMark = 60;
	// updateUserUIC(locator, empDetail, userInfo, false);
	// errMark = 100;
	// employee = partyManager.createEmployee(empDetail);
	// errMark = 105;
	// assignJobTypes(locator, employee, empDetail, userInfo, false); //TO-DO
	// Need to set active Job Type before creating!
	// errMark = 110;
	// SabaSecurityManager secMgr = (SabaSecurityManager)
	// locator.getManager(Delegates.kSabaSecurityManager);
	// assignSecurityListToUser(secMgr, accountType, employee, domain);
	// errMark = 120;
	// AudienceTypeManager audTypeMgr =
	// (AudienceTypeManager)locator.getManager(Delegates.kAudienceTypeManager);
	// addAudienceTypeToUser(audTypeMgr, accountType, employee);
	// errMark = 130;
	//
	// if (userInfo.getSSNo() != null || userInfo.getArmyFIN() != null) {
	// //Only want to call update if there is a value to update
	// //Since this is create, DB table will have 2 null values
	// //There is a chance that both are null if this was a call from ATRRS
	// updateExtTable(locator, employee.getId(), userInfo);
	// }
	//
	// logger.log(Level.INFO, "User {0} successfully Created",
	// userInfo.getUserName());
	// } catch (SabaException ex) {
	// StringBuilder errMsg = new StringBuilder("User.createUser: ");
	// errMsg.append(errMark);
	// errMsg.append("\n");
	// errMsg.append(ex.getMessage());
	// logger.log(Level.SEVERE, errMsg.toString(), ex);
	// throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// errMsg.toString(), ex);
	// }
	// logger.finer("End of creating a new user");
	//
	// return employee;
	// }
	public static Employee createUser(ServiceLocator locator,
			UserInfo userInfo, ProfilesLookUpResults lookUpResults,
			AccountType accountType, MLMSLoginBean loginBean)
			throws UserException, DBUtilException, NoAccessException,
			LoginException {

		return createUser(locator, userInfo, lookUpResults, accountType, null,
				loginBean);
	}

	public static Employee createUser(ServiceLocator locator,
			UserInfo userInfo, ProfilesLookUpResults lookUpResults,
			AccountType accountType, StringBuilder timeSB,
			MLMSLoginBean loginBean) throws UserException, DBUtilException,
			NoAccessException, LoginException {
		String sourceMethod = "createUser";
		Employee employee = null;
		String tempStr = null;
		int errMark = 0;

		if (userInfo == null) {
			throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
					"UserInfo is Null. Object should be populated before calling User.createUser()");
		}

		// This is the value that will be saved in CMT_PERSON.SS_NO field

		try {
			PartyManager partyManager = (PartyManager) locator
					.getManager(Delegates.kPartyManager);
			EmployeeDetail empDetail = new EmployeeDetail();

			// Set varies default values
			setDefaultAttributes(userInfo);

			errMark = 30;
			// Check for access
			// Unlike updateUser, just throw exception and don't create the
			// account if no access
			if (!hasAccess(userInfo, accountType)) {
				// LoginLog.writeToErrorLog(" user should not have access");
				throw new NoAccessException(MLMSStatusCode.NUM_USER_NO_ACCESS,
						"This user should have no access to the MLMS system");
			}

			/** Copy  Attributes over to Employee Detail */
			beanToLMS(empDetail, userInfo, accountType, loginBean);

			empDetail.setStatus(Constants.USER_STATUS_ACTIVE);

			Date currDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String DateToStr = format.format(currDate);

			try {
				empDetail.setStartedOn(format.parse(DateToStr));
			} catch (ParseException e) {
				LoginLog.writeToErrorLog(e.getMessage());
			}

			empDetail.setEmployeeNo("00001025");

			empDetail.setGenderEnum(getGender('N'));

			empDetail.setSsNo("123456789");

			/*
			 * if (accountType.getCode() != null) { // Custom 8 is used to the
			 * store the Account Type/ATRRS Split empDetail.setCustomValue(new
			 * CustomAttributeValueDetail( ICustomAttributeValueDetail.kCustom8,
			 * accountType .getCode().toUpperCase())); }
			 */
			// empDetail.getCustomValue(ICustomAttributeValueDetail.kCustom1);
			errMark = 40;
			if (accountType.getFfmRoleStatus() != null) {
				// Custom 0 is used to store whether the FFM role is granted
				empDetail.setCustomValue(new CustomAttributeValueDetail(
						ICustomAttributeValueDetail.kCustom0, accountType
								.getFfmRoleStatus()));
			}
			errMark = 41;
			
			if (accountType.getFfmEffectiveRoleGrantDate() != null) {
				LoginLog.writeToErrorLog(sourceClass + " " + sourceMethod + " "+ loginBean.getUsername()+ " effective role date " + dd_MMM_yyyy_formatter.format(accountType.getFfmEffectiveRoleGrantDate()));
				empDetail.setCustomValue(new CustomAttributeValueDetail(
						ICustomAttributeValueDetail.kCustom9, dd_MMM_yyyy_formatter
								.format(accountType.getFfmEffectiveRoleGrantDate())));
				
			}
			SecurityInfoDetail securityInfoDetail = new SecurityInfoDetail(
					userInfo.getUserName(), userInfo.getPassword());

			empDetail.setSecurityInfoDetail(securityInfoDetail);
			/**
			 * gets default organization
			 * 
			 */
			BusinessUnit businessUnit = lookupBusinessUnit(locator, loginBean);
			empDetail.setCompany(businessUnit);

			SabaLocale defaultLocale = (SabaLocale) ServiceLocator
					.getReference(SabaLocale.class, "local000000000000001");

			empDetail.setLocale(defaultLocale);

			errMark = 45;
			SabaTimeZone timezone = (SabaTimeZone) ServiceLocator.getReference(
					SabaTimeZone.class, userInfo.getTimezone());
			empDetail.setTimezone(timezone);

			errMark = 50;
			Domain domain = (Domain) ServiceLocator.getReference(Domain.class,
					accountType.getDomainID());

			empDetail.setHomeDomain(domain);

			empDetail.setSecurityDomain(domain);

			empDetail.getNameDetail().setFname(loginBean.getFname());
			empDetail.getNameDetail().setLname(loginBean.getLname());
			empDetail.getAddressDetail().setAddr1(loginBean.getStreetAddress());
			empDetail.getAddressDetail()
					.setAddr2(loginBean.getStreetAddress2());
			empDetail.getAddressDetail().setCity(loginBean.getCity());
			empDetail.getAddressDetail().setState(loginBean.getState());
			empDetail.getAddressDetail().setZip(loginBean.getZipcode());
			empDetail.getContactInfoDetail().setEmail(loginBean.getEmail());
			empDetail.getContactInfoDetail().setPhone1(
					loginBean.getPrimaryPhone());
			/**
			 * LoginLog.writeToErrorLog("userInfo.username " +
			 * userInfo.getUserName()); LoginLog.writeToErrorLog("empDetail : "
			 * + empDetail.toString()); LoginLog.writeToErrorLog("First name: "
			 * + empDetail.getNameDetail().getFname());
			 * LoginLog.writeToErrorLog("Last name: " +
			 * empDetail.getNameDetail().getLname());
			 * LoginLog.writeToErrorLog("Status: " + empDetail.getStatus());
			 **/

			errMark = 60;
			CommonUtil.appendTimestamp(timeSB, "\tBefore_UIC:\t");

			errMark = 70;

			// Actually creating the user object now
			CommonUtil.appendTimestamp(timeSB, "\tBefore_Create:\t");
			employee = partyManager.createEmployee(empDetail);
			errMark = 105;
			CommonUtil.appendTimestamp(timeSB, "\tBefore_JobHist:\t");
			processJobHistory(locator, employee, lookUpResults);
			errMark = 110;
			CommonUtil.appendTimestamp(timeSB, "\tBefore_SecList:\t");
			SabaSecurityManager secMgr = (SabaSecurityManager) locator
					.getManager(Delegates.kSabaSecurityManager);
			assignSecurityListToUser(secMgr, accountType, employee, domain);

			errMark = 120;
			CommonUtil.appendTimestamp(timeSB, "\tBefore_AudType:\t");
			// AudienceTypeManager audTypeMgr = (AudienceTypeManager) locator
			// .getManager(Delegates.kAudienceTypeManager);
			// addAudienceTypeToUser_NoCheck(audTypeMgr, accountType, employee);
			errMark = 130;

			/*
			 * if (userInfo.getSSNo() != null || userInfo.getArmyFIN() != null)
			 * { // Only want to call update if there is a value to update //
			 * Since this is create, DB table will have 2 null values // There
			 * is a chance that both are null if this was a call from // ATRRS
			 * CommonUtil.appendTimestamp(timeSB, "\tBefore_Ext:\t");
			 * updateExtTable(locator, employee.getId(), userInfo,
			 * lookUpResults); }
			 */

			logger.log(Level.FINE, "User {0} successfully Created",
					userInfo.getUserName());
			logger.log(Level.FINE, "User {0} successfully Created",
					userInfo.getUserName());
			LoginLog.writeToErrorLog("User {0} successfully Created: "
					+ userInfo.getUserName());
		} catch (SabaException ex) {
			StringBuilder errMsg = new StringBuilder("User.createUser: ");
			errMsg.append(errMark);
			errMsg.append("\n");
			errMsg.append(ex.getMessage());
			logger.log(Level.SEVERE, errMsg.toString(), ex);
			throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
					errMsg.toString(), ex);
		}
		return employee;
	}

	/**
	 * Updated Jul 18, added EIDM account date
	 * 
	 * @param locator
	 * @param loginBean
	 * @throws UserException
	 * @throws DBUtilException
	 * @throws ParseException
	 */
	public static void setABMiscData(ServiceLocator locator,
	MLMSLoginBean loginBean) throws DBUtilException, UserException,
			ParseException {
		String methodName = "setABMiscData";
		String id = getPersonId(locator, loginBean);
		String shopComplStatus = Constants.INCOMPLETE;
		/*
		 * BUG FOUND - if new user this returns false
		 */
		if(isDebug){
		LoginLog.writeToErrorLog(User.class.getSimpleName() + " method : "
				+ methodName + " setting AB Miscellaneous Data ");
		}
		if(loginBean.isSHOPComplete()){
			shopComplStatus = Constants.COMPLETE;
		}
		if(id != null){
		setABMiscData(id,
				loginBean.getEidmAccountCreateDate(),
				CommonUtil.getFormattedDateString(loginBean.getAgentBrokerRoleHistoricalEffectiveDate()),
				CommonUtil.getFormattedDateString(loginBean.getAgentBrokerRoleEffectiveDate()),
				loginBean.getRawRoleEffectiveDate(),
				locator,
				loginBean.getGuid(), shopComplStatus);
		}

	}

	/**
	 * 
	 * @param locator
	 * @param loginBean
	 * @param acctType
	 *            TODO
	 * @return
	 */
	public static AccountType lookupDomain(ServiceLocator locator,
			MLMSLoginBean loginBean, AccountType acctType)
			throws LoginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String userDomain = loginBean.getRole().toLowerCase();
		if (loginBean.getUsername().equalsIgnoreCase(Constants.MLMS_SUPER)) {
			userDomain = Constants.WORLD;
		}
		String defaultDomain = null;
		String domainID = null;

		String sql = "select id, name from fgt_domain where LOWER(name) = ? ";

		try {
			conn = DBUtil.getConnection(locator, "User.lookupDomain");
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userDomain);

			rs = stmt.executeQuery();
			if (rs.isBeforeFirst()) {
				while (rs.next()) {

					domainID = rs.getString("id");
					defaultDomain = rs.getString("name").toLowerCase();

					acctType.setDefaultDomain(defaultDomain);
					acctType.setDomainID(domainID);
					// LoginLog.writeToErrorLog("lookupDomain:  domain id " +
					// domainID);

				}
			} else {
				LoginLog.writeToErrorLog("lookupDomain:  domain name not found "
						+ userDomain);
				throw new LoginException(MLMSStatusCode.NUM_DOMAIN_NOT_FOUND,
						" Matching domain not found for " + userDomain);
			}
		} catch (SQLException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} catch (DBUtilException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} finally {
			DBUtil.freeDBResources(locator, rs, stmt, conn, "User.lookupDomain");
		}
		return acctType;

	}

	/**
	 * 
	 * @param locator
	 * @param loginBean
	 * @param acctType
	 * @return
	 */
	public static AccountType lookupSecurityRole(ServiceLocator locator,
			MLMSLoginBean loginBean, AccountType acctType)
			throws LoginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String userRole = null;
		boolean isDebug = System.getProperty("mlms.debug", "true")
				.equalsIgnoreCase("true");
		if (loginBean.getRole() != null && loginBean.getRole().length() > 0) {
			userRole = loginBean.getRole().toLowerCase();
		} else {
			throw new LoginException(333, "User Role from login bean is null");
		}
		String securityRole = null;
		String securityRoleID = null;
		if (isDebug) {
			LoginLog.writeToErrorLog(User.class.getName()
					+ " DEBUG lookupSecurity Role, role " + userRole);
		}
		String sql = "select id, name from fgt_ss_cpriv where name = ? ";

		try {
			conn = DBUtil.getConnection(locator, "User.lookupSecurityRole");
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userRole);
			rs = stmt.executeQuery();
			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					securityRoleID = rs.getString("id");
					securityRole = rs.getString("name");

					acctType.setSecurityList(securityRole);
					acctType.setRoleID(securityRoleID);

				}
			} else {
				LoginLog.writeToErrorLog("lookupSecurityRole: security role not found"
						+ userRole);
				throw new LoginException(904, "A security role matching "
						+ userRole
						+ " was not found, user could not be created");

			}

		} catch (SQLException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} catch (DBUtilException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} finally {
			DBUtil.freeDBResources(locator, rs, stmt, conn,
					"User.lookupSecurityRole");
		}
		return acctType;

	}

	/**
	 * 
	 * @param id
	 * @param eidmAcctCreateDate
	 * @param locator
	 * @param guid
	 * @param shopComplStatus TODO
	 * @param histEffectiveGrantDate
	 * @param roleEffectiveDate
	 * @param rawRolEffDate
	 * @return
	 * @throws ParseException
	 * @throws DBUtilException
	 * @throws UserException
	 * Anu - added role effective and raw role effective dates 04/26/2017
	 */
	public static int setABMiscData(String id, Date eidmAcctCreateDate,
			String histEffectiveGrantDate, String roleEffectiveDate,String rawRolEffDate, ServiceLocator locator, String guid, String shopComplStatus)
			throws ParseException, DBUtilException, UserException {
		String methodName = "setABMiscData";
		/*String updatesql = "update AT_MLMS_AB_MISC_ATTRIBUTES set EIDM_ACCT_CREATE_DATE = ?, HIST_EFFECTIVE_DATE = ?, EIDM_GUID = ?, SHOP_COMPLETION_STATUS = ?, UPDATED_ON = sysdate where person_id = ?";
		String insertsql = "insert into AT_MLMS_AB_MISC_ATTRIBUTES (ID, PERSON_ID, HIST_EFFECTIVE_DATE, EIDM_ACCT_CREATE_DATE, EIDM_GUID, SHOP_COMPLETION_STATUS, UPDATED_ON)  "
				+ "values  ('ABDATA'||AT_MLMS_AB_MISC_SEQ.NEXTVAL,?,?,?,?,?,sysdate)";
		**/
		/*
		 *  -- The following are the input from Web Services  
			  pPersonId                 IN VARCHAR2,
			  pHistEffectiveDate        IN VARCHAR2,  
			  pEidmAcctCreateDate       IN DATE,
			  pEidmGuid                 IN VARCHAR2,
			  pShopCompletionStatus     IN VARCHAR2,
			  pRoleEffectiveDate        IN VARCHAR2,
			  pRawEffectiveDate         IN VARCHAR2,
			  oError                    OUT INT,
			  oDESC                     OUT VARCHAR2
		 */
		String sqlStmt = "{ call AP_MLMS_MISC_ATTR_INS_UPD(?,?,?,?,?,?,?,?,?) }";
		
		Connection con = null;
		con = DBUtil.getConnection(locator, "User.setABMiscData");
		CallableStatement pstmt = null;
		ResultSet rs = null;
		int sqlResult = -9;
		int sqlErr = 0;
		String sqlErrDesc = "";
		
		LoginLog.writeToErrorLog(User.sourceClass + " " + methodName
				+ " setting values person id " + id + " EIDM Acct Create Date : "
				+  CommonUtil.getFormattedDateString(eidmAcctCreateDate)
				+ " HistoricalEffectiveGrant date  " + histEffectiveGrantDate
				+ " guid " + guid);

		try {
			
			pstmt = con.prepareCall(sqlStmt);
			pstmt.setString(1,id);
			pstmt.setString(2, histEffectiveGrantDate);
			pstmt.setDate(3, eidmAcctCreateDate == null ? null :new java.sql.Date(eidmAcctCreateDate.getTime()));
			pstmt.setString(4, guid);
			pstmt.setString(5, shopComplStatus);
			pstmt.setString(6, roleEffectiveDate);
			pstmt.setString(7,rawRolEffDate);
			pstmt.registerOutParameter(8,Types.INTEGER);
			pstmt.registerOutParameter(9,Types.CHAR);
			pstmt.execute();
 
			sqlErr = ((Integer)pstmt.getObject(8)).intValue();
			sqlErrDesc = (String)pstmt.getObject(9);
			
			LoginLog.writeToErrorLog(User.sourceClass + " " + methodName
					+ "Insert/Update SQL Result code " + sqlErr + " if 0, no error...");
			
			if(sqlErr > 0){
				LoginLog.writeToErrorLog(User.sourceClass + " " + methodName
						+ "SQL Error Insert/Update SQL Result code " + sqlErrDesc);
				throw new SQLException(sqlErrDesc);
				
			}

			
		} catch (SQLException e) {

			// if ME820 unexpected database error

			e.printStackTrace(System.out);
			throw new UserException(830, methodName + " " + " "
					+ e.getMessage());

		} finally {

			DBUtil.freeDBResources(locator, rs, pstmt, con,
					"User.setABMiscData");
		}
		return sqlResult;
	}
	public static String getPersonId(ServiceLocator locator, MLMSLoginBean loginBean){
		String methodName = "getPersonId";
		String resultStr = "";
		String sql = "select id from cmt_person where username = upper(?)";
		
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DBUtil.getConnection(locator, "User.getPersonId");
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, loginBean.getUsername());
			rs = pstmt.executeQuery();
			while(rs.next()){
				resultStr = rs.getString("id");
			}
		} catch (DBUtilException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DBUtil.freeDBResources(locator, rs, pstmt, con,
					"User.getPersonId");
		}
		
		
		return resultStr;
		
	}

	/**
	 * 
	 * @param locator
	 * @param loginBean
	 * @return
	 * @throws LoginException
	 */
	public static BusinessUnit lookupBusinessUnit(ServiceLocator locator,
			MLMSLoginBean loginBean) throws LoginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		BusinessUnit businessUnit = null;
		String role = null;
		boolean isDebug = false;
		isDebug = System.getProperty("mlms.debug", "true").equalsIgnoreCase(
				"true");

		if (loginBean.getRole() != null && loginBean.getRole().length() > 0) {
			role = loginBean.getRole().toLowerCase();

		} else {
			throw new LoginException(333, User.class.getSimpleName()
					+ " DEBUG lookup business unit, value of role " + role);
		}
		if (isDebug) {
			LoginLog.writeToErrorLog(User.class.getName()
					+ " DEBUG lookupBusinessUnit, role " + role);
		}

		String company = null;
		String companyID = null;
		String searchVal = null;
		/**
		 * set role to agent - though EIDM has two roles FFM_TRAINING_ACCESS and
		 * FFM_AGENT - MLMS only has one - if a user has FFM_TRAINING_ACCESS and
		 * FFM_AGENT - set custom0 = Complete
		 */
		if (role.equalsIgnoreCase(Constants.AGENT)) {
			searchVal = Constants.AGENT_ORG.toLowerCase();
		} else if (role.equalsIgnoreCase(Constants.ASSISTER)) {
			searchVal = Constants.ASSISTER_ORG.toLowerCase();
		} else {
			searchVal = Constants.DEFAULT_ORG.toLowerCase();
		}

		String sql = "select id, name2 from tpt_company where lower(name2) = ?";

		try {
			conn = DBUtil.getConnection(locator, "User.lookupBusinessUnit");
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, searchVal);
			
			rs = stmt.executeQuery();
			
			if (rs.isBeforeFirst()) {
				while (rs.next()) {
					companyID = rs.getString("id");
					company = rs.getString("name2");
					businessUnit = (BusinessUnit) ServiceLocator.getReference(
							BusinessUnit.class, companyID);

				}
			} else {
				LoginLog.writeToErrorLog("lookupBusinessUnit: organization not found "
						+ searchVal);
				throw new LoginException(MLMSStatusCode.NUM_COMPANY_NOT_FOUND,
						" Uable to find organization " + searchVal);
			}

		} catch (SQLException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} catch (DBUtilException ex) {
			LoginLog.writeToErrorLog(ex.getMessage());
		} catch (SabaException ex) {

			LoginLog.writeToErrorLog(ex.getMessage());
		} finally {
			DBUtil.freeDBResources(locator, rs, stmt, conn,
					"User.lookupSecurityRole");
		}
		return businessUnit;

	}

	


	public static LoginResult updateUser(ServiceLocator locator,
			ProfilesLookUpResults lookUpResults, AccountType accountType,
			UserInfo userInfo, MLMSLoginBean loginBean) throws UserException,
			DBUtilException, LoginException {
		String sourceMethod = "updateUser";
		LoginLog.writeToErrorLog("updating...");
		// LMSUserRecord lmsUserRecord = lookUpResults.getLMSUserRecord();
		// Employee employee = lmsUserRecord.getEmployee();
		Employee employee = lookUpResults.getLMSEmployee();
		EmployeeDetail empDetail = null;
		String ldapValue = null;
		int errMark = 10;
		PartyManager partyManager = null;
		LoginResult loginResult = null;
		// Update only if there's a change, minimize DB load
		boolean hasUpdated = false;

		try {
			partyManager = (PartyManager) locator
					.getManager(Delegates.kPartyManager);
			empDetail = partyManager.getDetail(employee);

			// QC 1060: Flag for activating account
			boolean activate = false;
			// QC 1060: Flag for deactivating account
			boolean deactivate = false;

			errMark = 40;
			// If user does not have access, deactivate and Remove

			if (loginBean.getFname() != null
					&& empDetail.getNameDetail().getFname() != null) {
				if (!(empDetail.getNameDetail().getFname()
						.equalsIgnoreCase(loginBean.getFname()))) {
					empDetail.getNameDetail().setFname(loginBean.getFname());
				}
			}

			if (loginBean.getLname() != null) {
				empDetail.getNameDetail().setLname(loginBean.getLname());

			}

			if (loginBean.getStreetAddress() != null) {

				empDetail.getAddressDetail().setAddr1(
						loginBean.getStreetAddress());
			}
			if (loginBean.getStreetAddress2() != null) {
				empDetail.getAddressDetail().setAddr2(
						loginBean.getStreetAddress2());
			}
			if (loginBean.getCity() != null) {
				empDetail.getAddressDetail().setCity(loginBean.getCity());
			}
			if (loginBean.getState() != null) {
				empDetail.getAddressDetail().setState(loginBean.getState());
			}
			if (loginBean.getZipcode() != null) {
				empDetail.getAddressDetail().setZip(loginBean.getZipcode());
			}
			if(loginBean.getEmail() != null){
			empDetail.getContactInfoDetail().setEmail(loginBean.getEmail());
			}
			if(loginBean.getPrimaryPhone() != null){
			empDetail.getContactInfoDetail().setPhone1(
					loginBean.getPrimaryPhone());
			}
			
			/**
			 * Anu - No need to write effective or grant dates in to custom9 anymore
			if (accountType.getFfmEffectiveRoleGrantDate() != null) {
				LoginLog.writeToErrorLog(sourceClass + " " + sourceMethod + " "+ loginBean.getUsername()+ " effective role date " + dd_MMM_yyyy_formatter.format(accountType.getFfmEffectiveRoleGrantDate()));
				empDetail.setCustomValue(new CustomAttributeValueDetail(
						ICustomAttributeValueDetail.kCustom9, dd_MMM_yyyy_formatter
								.format(accountType.getFfmEffectiveRoleGrantDate())));
				
			}*/
			// LoginLogwriteToErrorLog("USER UpdateUser locale " +
			// empDetail.getLocale().getId());
			hasUpdated = true;

			// Roles/Privileges
			if (!(userInfo.isAccountActive() && hasAccess(userInfo, accountType))) {
				// QC 1060 Deactivate user account
				userInfo.setStatus(Constants.USER_STATUS_INACTIVE);
				if (empDetail.getTerminatedOn() == null) {
					empDetail.setTerminatedOn(new Date());
					deactivate = true;
					hasUpdated = true;
				}
				loginResult = new LoginResult(
						userInfo.getUserName(),
						new NoAccessException(
								MLMSStatusCode.NUM_USER_NO_ACCESS,
								"This user should have no access to the MLMS system according to AKO status and account type"));
			} else {
				// QC 1060: reactivate user account, Status is set in
				// ldapToLMS
				if (empDetail.getTerminatedOn() != null) {
					empDetail.setTerminatedOn(null);
					activate = true;
					hasUpdated = true;
				}
			}

			// change the user password, will allow users to login
			SecurityInfoDetail securityInfoDetail = empDetail
					.getSecurityInfoDetail();

			// securityInfoDetail.setPassword(loginBean.getUsername()
			// + loginBean.getRole());

			securityInfoDetail.setPassword(userInfo.getPassword());
			empDetail.setSecurityInfoDetail(securityInfoDetail);

			hasUpdated = true;

			errMark = 50;

			accountType = lookupSecurityRole(locator, loginBean, accountType);

			accountType = lookupDomain(locator, loginBean, accountType);

			Domain domain = (Domain) ServiceLocator.getReference(Domain.class,
					accountType.getDomainID());
			/** call update AccountTypeInfo to set security roles **/
			hasUpdated = updateAccountTypeInfo(locator, accountType, employee,
					empDetail, userInfo.isAccountActive(), activate, deactivate);

			errMark = 60;

			if (hasUpdated) {
				errMark = 70;
				// LoginLogwriteToErrorLog("errMark 70: updating");
				/**
				 * 6/16/2010 - Check for NOWAIT error when updating employee
				 */
				int retry = 0;
				boolean isRetry = true;
				while (retry < NOWAIT_RETRIES && isRetry) {
					
					try {
						partyManager.updateEmployee(employee, empDetail);
						isRetry = false;

						SabaSecurityManager secMgr = (SabaSecurityManager) locator
								.getManager(Delegates.kSabaSecurityManager);

						assignSecurityListToUser(secMgr, accountType, employee,
								domain);
						User.setABMiscData(locator, loginBean);
						// LoginLogwriteToErrorLog("errMark 70: updating");
					} catch (SabaDBException e ){
						if (CommonUtil.stackTraceToString(e).indexOf("NOWAIT") > -1) {
							/**
							 * NOWAIT error was thrown. Wait .5 seconds, and try
							 * again
							 */
							retry++;
							logger.log(
									Level.INFO,
									"User {0} is locked in DB.  Waiting .5 seconds before retry",
									userInfo.getUserName());
							try {
								Thread.sleep(NOWAIT_RETRY_MILLI);
							} catch (InterruptedException e1) {
								//
							}
						} else {
							throw new UserException(
									MLMSStatusCode.NUM_DB_GENERAL,
									e.getMessage(), e);
						}
					} catch (SabaException e) {
						LoginLog.writeToErrorLog("SabaException thrown, exception is " + e.getMessage());
					}
				}
				if (isRetry) {
					/** Employee was never updated in this login */
					UserException ue = new UserException(
							MLMSStatusCode.NUM_DB_NOWAIT, "User "
									+ userInfo.getUserName()
									+ " retried 5 times and was never updated");
					ue.setRetrySeconds(NOWAIT_IE_RETRY_SEC);
					throw ue;
				}

				errMark = 78;
				processJobHistory(locator, employee, lookUpResults);

				errMark = 80;
				// Update AT_LMS_CMT_PERSON_EXT, applicable to LDAP records only
				// Don't update EXT table if the record is not read from LDAP

			}
			errMark = 100;
			logger.log(Level.FINE, "User {0} Updated", userInfo.getUserName());
		} catch (SabaException ex) {
			StringBuilder errMsg = new StringBuilder("User.updateUser: ");
			errMsg.append(errMark);
			errMsg.append("\n");
			errMsg.append(ex.getMessage());
			logger.log(Level.SEVERE, errMsg.toString(), ex);
			throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
					errMsg.toString(), ex);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// QC 1060
		if (loginResult == null) {

			loginResult = new LoginResult(userInfo.getUserName(), employee,
					accountType, userInfo.getAccountTypes());
		}
		return loginResult;
	}

	/**
	 * returns login action code CAB01 created agent broker CAS01 created
	 * assister CAA01 created admin UAB01 updated agent broker UAS01 updated
	 * assister UAA01 updated admin UER01 error
	 * 
	 * @param loginBeam
	 * @param isNewUser
	 * @return
	 */
	public static String getLoginActionCode(MLMSLoginBean loginBean,
			Boolean isNewUser) {

		StringBuilder sb = new StringBuilder();
		if (isNewUser) {
			sb.append('C');
		} else {
			sb.append('U');
		}
		if (loginBean != null && loginBean.getRole() != null) {
			if (loginBean.getRole().equalsIgnoreCase(Constants.AGENT)) {
				sb.append("AB");
			} else if (loginBean.getRole().equalsIgnoreCase(Constants.ASSISTER)) {
				sb.append("AS");
			} else {
				sb.append("AA");
			}
		}
		sb.append("01");
		return sb.toString();
	}

	/**
	 * Adds all MOS to job history This method assumes that processMOS has been
	 * called and therefore ProfilesLookUpResults's MOS Job Type list is
	 * populated with actual Saba ID's
	 * 
	 * @param locator
	 * @param employee
	 * @param lookUpResults
	 * @throws SabaException
	 * @throws DBUtilException
	 */
	private static void processJobHistory(ServiceLocator locator,
			Employee employee, ProfilesLookUpResults lookUpResults)
			throws SabaException, DBUtilException {
		// ArrayList<String[]> mosJobTypes = lookUpResults.getMOSLookUpList();
		ArrayList<String> histJobTypeIDs = lookUpResults.getJobTypeHistList();
		String tempID;

		PartyManager partyManager = (PartyManager) locator
				.getManager(Delegates.kPartyManager);

	}

	private static boolean updateAccountTypeInfo(ServiceLocator locator,
			AccountType accountType, Employee employee,
			EmployeeDetail empDetail, boolean accountActive, boolean activate,
			boolean deactivate) throws SabaException, DBUtilException {
		// remove existing old domain privileges before updating with new domain
		// privileges
		PartyManager partyManager = (PartyManager) locator
				.getManager(Delegates.kPartyManager);
		String audTypeID = null;
		String defaultDomain = accountType.getDefaultDomain();
		Domain ldapDomain = null;
		ICustomAttributeValueDetail custom0 = empDetail
				.getCustomValue(ICustomAttributeValueDetail.kCustom0);
		String lmsCode = "";
		String newCode = "";
		boolean hasUpdated = false;
		boolean updateAccountType = false;

		/**
		 * Custom 0 is used to store whether FFM_AGENT role is granted set to
		 * null if user is assister
		 **/

		updateAccountType = true;
		logger.log(Level.FINER, "User.updateUser setting custom0 ({0} > {1}):",
				new Object[] { custom0, accountType.getFfmRoleStatus() });

		empDetail.setCustomValue(new CustomAttributeValueDetail(
				ICustomAttributeValueDetail.kCustom0, (accountType
						.getFfmRoleStatus() == null ? null : accountType
						.getFfmRoleStatus())));
		empDetail.setCustomValue(new CustomAttributeValueDetail(
				ICustomAttributeValueDetail.kCustom9, (accountType
						.getFfmEffectiveRoleGrantDate() == null ? null : dd_MMM_yyyy_formatter.format(accountType
						.getFfmEffectiveRoleGrantDate()))));
		int count = 9;
		for (int i = 0; i < count; i++) {
			String customName = "ICustomAttributeValueDetail.kCustom" + i;
			
			 

			ICustomAttributeValueDetail iCustomAttributeValueDetail = empDetail
					.getCustomValue(customName);
			if (iCustomAttributeValueDetail != null) {
				 LoginLog.writeToErrorLog("Custom field  name " +
				 iCustomAttributeValueDetail.getName());
				 LoginLog.writeToErrorLog("Custom field value " +
				 iCustomAttributeValueDetail.getValue());
			}

		}

		hasUpdated = true;

		// QC 1060 Remove MLMS security lists and audience types if account is
		// inactive
		if (!accountActive || deactivate) {
			updateAccountType = true;
			accountType.setDefaultDomain(Constants.AKO_NONE_DOMAIN);

			accountType.setRoleID(null);
		}

		if (activate) {
			updateAccountType = true;
		}

		if (updateAccountType) {
			// AudienceTypeManager audTypeMgr = (AudienceTypeManager) locator
			// .getManager(Delegates.kAudienceTypeManager);
			/*
			 * audience types are not currently used at CMS
			 * //removeAudienceTypeFromUser(audTypeMgr, accountType, employee);
			 * audTypeID = accountType.getAudienceTypeID(); if (audTypeID !=
			 * null) { addAudienceTypeToUser(audTypeMgr, accountType, employee);
			 * logger.log( Level.FINER,
			 * "User.updateAccoutTypeInfo updating new audience type {0}, {1}",
			 * new Object[] { accountType.getAudienceType(), audTypeID }); }
			 */

			// Remove security roles that are defined by AKO account type
			SabaSecurityManager secMgr = (SabaSecurityManager) locator
					.getManager(Delegates.kSabaSecurityManager);
			removeSecurityListFromUser(secMgr, accountType, employee);

			// Assign new domain to user.
			// QC 1060 Don't bother with updating domain if the new account type
			// grants no access
			if (!defaultDomain.equalsIgnoreCase(Constants.AKO_NONE_DOMAIN)) {
				ldapDomain = (Domain) ServiceLocator.getReference(Domain.class,
						accountType.getDomainID(), defaultDomain);

				Domain oldHomeDomain = empDetail.getHomeDomain();
				Domain oldSecurityDomain = empDetail.getSecurityDomain();

				if (ldapDomain != null && !ldapDomain.equals(oldSecurityDomain)) {
					// New Domain is different from old Domain

					logger.log(
							Level.FINER,
							"User.updateAccountTypeInfo updating sec. domain to {0}",
							ldapDomain.getDisplayName());
					partyManager.changeSecurityDomain(employee, ldapDomain);
					if (oldSecurityDomain != null
							&& oldSecurityDomain.equals(oldHomeDomain)) {
						// Old Security Domain == Old Home Domain, so we also
						// want to update old home domain
						logger.log(
								Level.FINER,
								"User.updateAccountTypeInfo updating home domain to {0}",
								ldapDomain.getDisplayName());
						empDetail.setHomeDomain(ldapDomain);
						hasUpdated = true;
					} else {
						// Old Security Domain != Old Home Domain, so we don't
						// want to change it
						logger.log(Level.FINER,
								"User.updateAccountTypeInfo old home domain NOT being updated");
					}
				}
			}

			// Assign new security roles to user
			if (audTypeID != null && ldapDomain != null) {
				assignSecurityListToUser(secMgr, accountType, employee,
						ldapDomain);
			}
		} else {
			logger.finer("No updates to account type");
		}

		return true;
	}

	/**
	 * Creates a new job type in MLMS, with security domain, name and
	 * description being provided from parameters. This should be called if a
	 * job type does not exist for mosCode.
	 * 
	 * @param domain
	 *            Domain to be set as the Security Domain
	 * @param locator
	 * @param mosCode
	 *            - The name of the job type
	 * @param mosDesc
	 *            - Description of the job type
	 * @return Newly created job type
	 * @throws SabaException
	 */
	private static JobType createJobType(Domain domain, ServiceLocator locator,
			String mosCode, String mosDesc) throws SabaException {
		JobType jobType = null;
		JobTypeDetail newJobTypeDetail;
		// TO-DO Change from
		// JobTypeHome oJobTypeHome = (JobTypeHome)
		// locator.getHome(Delegates.kJobType);
		JobTypeEntity oJobTypeHome = (JobTypeEntity) locator
				.getHome(Delegates.kJobType);

		newJobTypeDetail = new JobTypeDetail();
		newJobTypeDetail.setSecurityDomain(domain);
		newJobTypeDetail.initializeDefaults(locator);
		newJobTypeDetail.setName(mosCode);
		if (mosDesc != null) {
			newJobTypeDetail.setDescription(mosDesc);
		}
		JobFamily jobFamily = CommonReferences.getJobFamilyOther();
		newJobTypeDetail.setJobFamily(jobFamily);

		jobType = oJobTypeHome.create(newJobTypeDetail);
		logger.log(Level.FINER,
				"User.createJobType Job Type created. New jobType id {0}",
				jobType.getId());

		return jobType;
	}

	/**
	 * Returns an appropriate Saba Gender object given a character code
	 * 
	 * @param gender
	 *            'M' male, 'F' female, all other characters means Info. Not
	 *            Avail.
	 * @return com.saba.party.person.Gender
	 */
	private static Gender getGender(char gender) {
		if (gender == 'M') {
			return Gender.MALE;
		} else if (gender == 'F') {
			return Gender.FEMALE;
		} else {
			return Gender.INFORMATIONUNAVAILABLE;
		}
	}

	/**
	 * Assign Security List to the user specified in the domain.
	 * 
	 * @param secMgr
	 * @param accountType
	 *            the account type that the employee belongs
	 * @param employee
	 *            a reference to the employee
	 * @param domain
	 *            the domain to which the security list should be assigned
	 * @throws SabaException
	 */
	private static void assignSecurityListToUser(SabaSecurityManager secMgr,
			AccountType accountType, Employee employee, Domain domain)
			throws SabaException {

		// TO-DO IComplexPrivilege complexPriv =
		// secMgr.findComplexPrivilegeByKey(accountType.getRoleID());

		IComplexPrivilege complexPriv = (IComplexPrivilege) ServiceLocator
				.getReference(IComplexPrivilege.class, accountType.getRoleID());

		// secListMgr.assignSecurityRoleToUser(complexPriv, domain, employee);
		// Assign the security role to the world domain
		// If user already belongs to security role, no action will be taken. No
		// error thrown either
		secMgr.assignSecurityRoleToUser(complexPriv, employee);

	}

	// private static void removeSecurityListFromUser_Old(ServiceLocator
	// locator, AccountType akoAccountType, Employee employee)
	// throws SabaException, DBUtilException {
	// SabaSecurityManager sabaSecurityManager = (SabaSecurityManager)
	// locator.getManager(Delegates.kSabaSecurityManager);
	// //TO-DO Change from
	// //DomainHome domainHome = (DomainHome)
	// locator.getHome(Delegates.kDomain);
	// DomainDelegate domainHome = (DomainDelegate)
	// locator.getManager(Delegates.kDomainManager);
	// //Retrieve world domain, since security lists have been assigned to world
	// domain
	// Domain worldDomain = domainHome.findRootDomain();
	// String roleID = akoAccountType.getRoleID();
	//
	// Collection<String> collPrivs = getAKOSecurityListsExceptID(locator,
	// (roleID == null)?"0":roleID);
	// Iterator<String> privItr = collPrivs.iterator();
	// logger.fine("User.removeUserPrivs collPrivs Collection size: " +
	// collPrivs.size());
	//
	// while (privItr.hasNext()) {
	// String privString = (String)privItr.next();
	// IComplexPrivilege priv =
	// sabaSecurityManager.findComplexPrivilegeByKey(privString);
	// logger.finer("User.removeSecurityListFromUser removeUserPrivs priv name:"
	// + priv.getDisplayName() + " priv ID:" + priv.getId());
	// ISecurityList oISecuList = null;
	//
	// String strPrivListId = findPrivListId(locator, priv.getId(),
	// worldDomain.getId());
	//
	// logger.fine("User.removeSecurityListFromUser removeUserPrivs  strPrivListId:"
	// + strPrivListId);
	//
	// if (strPrivListId != null) {
	// oISecuList = (ISecurityList)
	// ServiceLocator.getReference(ISecurityList.class, strPrivListId);
	// }
	//
	// Person oPers = (Person) employee;
	//
	// if(oISecuList != null && oPers != null &&
	// sabaSecurityManager.isMember(oISecuList, oPers)) {
	// sabaSecurityManager.removeMember(oISecuList, oPers, null,
	// "Update for new account type");
	// logger.fine("User.removeSecurityListFromUser removeUserPrivs after removeMember");
	// } else if (oISecuList != null && oPers != null) {
	// //User is not member of this Security List
	// logger.fine("User.removeSecurityListFromUser removeUserPrivs Not removing user, since user is not member of "
	// + oISecuList.getId());
	// }
	// } // privItr
	// }

	private static void removeSecurityListFromUser(SabaSecurityManager secMgr,
			AccountType akoAccountType, Employee employee)
			throws SabaException, DBUtilException {
		ISecurityList oISecuList;
		ArrayList<String> secRemoveList;
		String tempStr = akoAccountType.getSecurityList();
		if (tempStr == null) {
			// secRemoveList = CommonReferences.getSecListAllMLMS();
		} else if (tempStr.equals(null)) {
			// secRemoveList = CommonReferences.getSecListRemoveListForArmy();
		} else if (tempStr.equals(null)) {
			// secRemoveList =
			// CommonReferences.getSecListRemoveListForForeign();
		} else {
			// secRemoveList = CommonReferences.getSecListAllMLMS();
		}
		/*
		 * for (String strPrivListId : secRemoveList) { oISecuList =
		 * (ISecurityList) ServiceLocator.getReference( ISecurityList.class,
		 * strPrivListId); if (secMgr.isMember(oISecuList, employee)) {
		 * secMgr.removeMember(oISecuList, employee, null,
		 * "Update for new account type"); logger.log(Level.FINER,
		 * "Removed security list {0}", strPrivListId); } }
		 */
	}

	/**
	 * Adds Audience Type to the user. This method takes 15 seconds in DEV!
	 * 
	 * @param audTypeMgr
	 * @param accountType
	 *            the account type that the employee belongs
	 * @param employee
	 *            a reference to the employee
	 * @throws SabaException
	 */
	/**
	 * private static void addAudienceTypeToUser(AudienceTypeManager audTypeMgr,
	 * AccountType accountType, Employee employee) throws SabaException {
	 * AudienceType audienceType = (AudienceType) ServiceLocator.getReference(
	 * AudienceType.class, accountType.getAudienceTypeID()); // Check that user
	 * doesn't belong to audience type already //if
	 * (!audTypeMgr.isPartyPresent(employee, audienceType)) {
	 * //audTypeMgr.addPerson(employee, audienceType); //}
	 * 
	 * /** Adds Audience Type to the user without checking if the association
	 * exists first This method now runs under a second!!
	 * 
	 * @param audTypeMgr
	 * @param accountType
	 *            the account type that the employee belongs
	 * @param employee
	 *            a reference to the employee
	 * @throws SabaException
	 */
	/**
	 * private static void addAudienceTypeToUser_NoCheck( AudienceTypeManager
	 * audTypeMgr, AccountType accountType, Employee employee) throws
	 * SabaException { AudienceType audienceType = (AudienceType)
	 * ServiceLocator.getReference( AudienceType.class,
	 * accountType.getAudienceTypeID()); // audTypeMgr.addPerson(employee,
	 * audienceType); }
	 **/

	/**
	 * This procedure initializes the user information attributes with the
	 * default values. These values are set only for a new user. The user record
	 * fields that are not present in the AKO LDAP fall under the scope of this
	 * procedure.
	 * 
	 * @param oUserInfo
	 *            the UserInfo object
	 */
	private static void setDefaultAttributes(UserInfo oUserInfo) {
		// default locale_id to English
		oUserInfo.setLocaleId(CommonReferences.getLocaleIDEnglish());

		// default flags
		oUserInfo.setFlags(Constants.USER_DEFAULT_FLAGS);

		// oUserInfo.setGender(Gender.INFORMATIONUNAVAILABLE);
		oUserInfo.setRate("0");
		oUserInfo.setMaxDiscount("100.0");
		oUserInfo.setQuota("0");
		oUserInfo.setCompanyId(CommonReferences.getBUIDOther());
		oUserInfo.setTerritoryId(null);
		oUserInfo.setTimezone(CommonReferences.getTimeZoneIDEastern());

	} // end set default attributes method

	/**
	 * This class simply takes values from LDAP and transfers them to Employee
	 * Detail. As a precaution, NameDetail, ContactInfoDetail, and AddressDetail
	 * should already be added to the employeeDetail. If not, this method will
	 * create new ones. The LDAP attributes are: "title", "ArmyRank",
	 * "givenname", "sn", "armyMiddleName", "armySuffix","armyPhoneNumber",
	 * "armyGrade", "armyPGRAD", "postaladdress", "country", "armySSN",
	 * "armyEDIPI","mail", MOS and UIC should be handled separately from this
	 * method, as they require more logic This goes to: NameDetail: title,
	 * fname, Mname, lname ContactInfoDetail: phone2 (armyPhone), email
	 * AddressDetail: addr1, addr2, addr3, city, state, zip, country
	 * EmployeeDetail: Custom0 (armyGrade), Custom1 (armyPGRAD), Custom2
	 * (armyEDIPI), suffix, empType
	 * 
	 * NOTE: Parameter errors such as string too long for non-essential fields
	 * should NOT fail login: Log, Trim, and continue All length limits are
	 * obtained through the UI configuration in Saba. (Limit generally = DB
	 * column size / 4 for unicode)
	 * 
	 * @param empDetail
	 *            - EmployeeDetail object that will be updated
	 * @param userInfo
	 *            - LDAP Object that contains all the values which will be used
	 *            to update EmployeeDetial
	 * @param newParam
	 *            TODO
	 * @return true if there's an update, false otherwise
	 * @throws SabaException
	 */
	private static boolean beanToLMS(EmployeeDetail empDetail,
			UserInfo userInfo, AccountType accountType, MLMSLoginBean loginBean)
			throws SabaException {
		NameDetail nameDetail = empDetail.getNameDetail();
		ContactInfoDetail contactInfoDetail = empDetail.getContactInfoDetail();
		AddressDetail addressDetail = empDetail.getAddressDetail();
		ICustomAttributeValueDetail tempCustom;
		String lmsValue = null;
		String beanValue = null;
		boolean hasUpdated = false;
		IPrimaryKey key = null;

		if (nameDetail == null) {
			nameDetail = new NameDetail();
			nameDetail.setFname(loginBean.getFname());
			nameDetail.setLname(loginBean.getLname());
			nameDetail.setTitle("");
			empDetail.setNameInfo(nameDetail);
		}
		if (contactInfoDetail == null) {
			contactInfoDetail = new ContactInfoDetail();
			empDetail.setContactInfoDetail(contactInfoDetail);
		}
		if (addressDetail == null) {
			addressDetail = new AddressDetail();
			empDetail.setAddressDetail(addressDetail);
		}

		if (empDetail.getLocale() == null) {
			empDetail.setLocale(CommonReferences.getLocaleEnglish());
			hasUpdated = true;
		}

		// Rank is preferred over title
		if ((lmsValue = nameDetail.getTitle()) == null) {
			lmsValue = "";
		}
		if (userInfo.getTitle() != null) {
			if (!userInfo.getTitle().equals(lmsValue)) {
				if (userInfo.getTitle().length() > 10) {
					logger.log(
							Level.WARNING,
							"Title too long {0} {1}",
							new Object[] { userInfo.getUserName(),
									userInfo.getTitle() });
					nameDetail.setTitle(userInfo.getTitle().substring(0, 10));
				} else {
					nameDetail.setTitle(userInfo.getTitle());
				}
				hasUpdated = true;
			}
		} else {
			if (!lmsValue.equals("")) {
				nameDetail.setTitle(null);
				hasUpdated = true;
			}
		}
		// First name
		if ((lmsValue = nameDetail.getFname()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getFName()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "FName too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				nameDetail.setFname(userInfo.getFName().substring(0, 25));
			} else {
				nameDetail.setFname(userInfo.getFName());
			}
			hasUpdated = true;
		}
		// Middle name
		if ((lmsValue = nameDetail.getMname()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getMName()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "MName too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				nameDetail.setMname(userInfo.getMName().substring(0, 25));
			} else {
				nameDetail.setMname(userInfo.getMName());
			}
			hasUpdated = true;
		}
		// Last name
		if ((lmsValue = nameDetail.getLname()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getLName()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "LName too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				nameDetail.setLname(userInfo.getLName().substring(0, 25));
			} else {
				nameDetail.setLname(userInfo.getLName());
			}
			hasUpdated = true;
		}
		// Suffix
		if ((lmsValue = empDetail.getSuffix()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getSuffix()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "Suffix too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				empDetail.setSuffix(userInfo.getSuffix().substring(0, 25));
			} else {
				empDetail.setSuffix(userInfo.getSuffix());

			}
			hasUpdated = true;
		} else {
			empDetail.setSuffix("");
		}

		// User Status, required field in Saba, do not set to null
		if ((lmsValue = empDetail.getStatus()) == null) {
			lmsValue = "";
		}
		String userStatus = userInfo.getStatus();
		if (userStatus == null || userStatus.trim().equals("")) {
			userStatus = null;
		}
		if (userStatus != null && !userStatus.equals(lmsValue)) {
			if (userStatus.length() > 25) {
				logger.log(Level.WARNING, "Status too long {0} {1}",
						new Object[] { userInfo.getUserName(), userStatus });
				empDetail.setStatus(userStatus.substring(0, 25));
			} else {
				empDetail.setStatus(userStatus);
			}
			hasUpdated = true;
		} else if (userStatus == null
				&& lmsValue.equalsIgnoreCase(Constants.USER_STATUS_INACTIVE)) {
			// QC 1060 reactivate account
			empDetail.setStatus(Constants.USER_DEFAULT_LMS_STATUS);
			hasUpdated = true;
		}

		// Contact
		// Work Phone
		if ((lmsValue = contactInfoDetail.getPhone2()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getWorkPhone()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "WorkPhone too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				contactInfoDetail.setPhone2(userInfo.getWorkPhone().substring(
						0, 25));
			} else {
				contactInfoDetail.setPhone2(userInfo.getWorkPhone());
			}
			hasUpdated = true;
		}
		// Fax
		if ((lmsValue = contactInfoDetail.getFax()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getFax()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "Fax too long {0} {1}", new Object[] {
						userInfo.getUserName(), beanValue });
				contactInfoDetail.setFax(userInfo.getFax().substring(0, 25));
			} else {
				contactInfoDetail.setFax(userInfo.getFax());
			}
			hasUpdated = true;
		}
		// Email
		if ((lmsValue = contactInfoDetail.getEmail()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getEmail()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			contactInfoDetail.setEmail(userInfo.getEmail());
			hasUpdated = true;
		}

		// Address
		// Addr1
		if ((lmsValue = addressDetail.getAddr1()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getAddr1()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 255) {
				logger.log(Level.WARNING, "Addr1 too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail.setAddr1(userInfo.getAddr1().substring(0, 255));
			} else {
				addressDetail.setAddr1(userInfo.getAddr1());
			}
			hasUpdated = true;
		}
		// Addr2
		if ((lmsValue = addressDetail.getAddr2()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getAddr2()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 255) {
				logger.log(Level.WARNING, "Addr2 too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail.setAddr2(userInfo.getAddr2().substring(0, 255));
			} else {
				addressDetail.setAddr2(userInfo.getAddr2());
			}
			hasUpdated = true;
		}
		// Addr3
		if ((lmsValue = addressDetail.getAddr3()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getAddr3()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 255) {
				logger.log(Level.WARNING, "Addr3 too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail.setAddr3(userInfo.getAddr3().substring(0, 255));
			} else {
				addressDetail.setAddr3(userInfo.getAddr3());
			}
			hasUpdated = true;
		}
		// City
		if ((lmsValue = addressDetail.getCity()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getCity()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 50) {
				logger.log(Level.WARNING, "City too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail.setCity(userInfo.getCity().substring(0, 50));
			} else {
				addressDetail.setCity(userInfo.getCity());
			}
			hasUpdated = true;
		}
		// State
		if ((lmsValue = addressDetail.getState()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getState()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 50) {
				logger.log(Level.WARNING, "State too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail.setState(userInfo.getState().substring(0, 50));
			} else {
				addressDetail.setState(userInfo.getState());
			}
			hasUpdated = true;
		}
		// Zip
		if ((lmsValue = addressDetail.getZip()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getZip()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 50) {
				logger.log(Level.WARNING, "Zip too long {0} {1}", new Object[] {
						userInfo.getUserName(), beanValue });
				addressDetail.setZip(userInfo.getZip().substring(0, 50));
			} else {
				addressDetail.setZip(userInfo.getZip());
			}
			hasUpdated = true;
		}
		// Country
		if ((lmsValue = addressDetail.getCountry()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getCountry()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 50) {
				logger.log(Level.WARNING, "Country too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				addressDetail
						.setCountry(userInfo.getCountry().substring(0, 50));
			} else {
				addressDetail.setCountry(userInfo.getCountry());
			}
			hasUpdated = true;
		}

		// Employee Type
		if ((lmsValue = empDetail.getEmpType()) == null) {
			lmsValue = "";
		}
		if ((beanValue = userInfo.getEmpType()) == null) {
			beanValue = "";
		}
		if (!beanValue.equals(lmsValue)) {
			if (beanValue.length() > 25) {
				logger.log(Level.WARNING, "EmpType too long {0} {1}",
						new Object[] { userInfo.getUserName(), beanValue });
				empDetail.setEmpType(userInfo.getEmpType().substring(0, 25));
			} else {
				empDetail.setEmpType(userInfo.getEmpType());
			}
			hasUpdated = true;
		}

		// Custom Fields
		// Custom 0 is used to store the Pay Plan information of the user.
		tempCustom = empDetail
				.getCustomValue(ICustomAttributeValueDetail.kCustom0);
		if (tempCustom == null || (lmsValue = tempCustom.toString()) == null) {
			lmsValue = "";
		}

		// Custom 1 is used to store the Pay Level information of the user
		tempCustom = empDetail
				.getCustomValue(ICustomAttributeValueDetail.kCustom1);
		if (tempCustom == null || (lmsValue = tempCustom.toString()) == null) {
			lmsValue = "";
		}

		logger.finer("ldapToLMS has updated User: " + hasUpdated);
		return hasUpdated;
	}

	

	/**
	 * Check if the user should have access to MLMS base on account type
	 * 
	 * @param userInfo
	 *            the LDAP user information
	 * @param accountType
	 *            the account type object
	 * 
	 * @return true if the account type allows access, false otherwise
	 */
	private static boolean hasAccess(UserInfo userInfo, AccountType accountType) {
		userInfo.setEmpType(accountType.getPersonType());

		String userName = userInfo.getUserName();
		logger.log(
				Level.FINER,
				"User.hasAccess Username {0}, Domain {1}, SecList {2}, AudType {3}, PerType {4}.",
				new Object[] { userName, accountType.getDefaultDomain(),
						accountType.getSecurityList(),
						accountType.getAudienceType(), userInfo.getEmpType() });

		/*
		 * if (accountType.getDefaultDomain() != null) { if
		 * (accountType.getDefaultDomain().equalsIgnoreCase(
		 * Constants.AKO_NONE_DOMAIN)) {
		 * userInfo.setEmpType(Constants.NO_ACCESS); } } else {
		 * userInfo.setEmpType(Constants.NO_ACCESS); } if (userInfo.getEmpType()
		 * == null) { logger.log(Level.WARNING,
		 * "User: {0} does not have an Employee Type", userName); // return
		 * false; }
		 */
		// The domain ID check is probably the most reliable check
		// return !userInfo.getEmpType().equals(NO_ACCESS);
		// LoginLog.writeToErrorLog(" get domain id " +
		// accountType.getDomainID());
		return (accountType.getDomainID() != null);
	}

	/**
	 * Update Saba User's UIC value based on LDAP. If UIC is new or different,
	 * than also change user's organization, and update UIC-Organization mapping
	 * NOTE: Failure to update UIC should not prevent user from logging in
	 * 
	 * @param locator
	 *            - ServiceLocator
	 * @param empDetail
	 *            - MLMS Employee Information
	 * @param userInfo
	 *            - LDAP user information
	 * @param isUpdateUser
	 *            - True if user is being updated; False if user is being
	 *            created
	 * @return true if there has been an update, false otherwise
	 * @throws UserException
	 *             in case of errors
	 */
	// private static boolean updateUserUIC(ServiceLocator locator,
	// EmployeeDetail empDetail,
	// UserInfo userInfo, boolean isUpdateUser) {
	// String ldapUIC = userInfo.getUIC();
	// String MLMSUIC = null;
	// boolean hasUpdated = false;
	//
	// int errMark = 10;
	// try {
	//
	// ICustomAttributeValueDetail uicDetail =
	// empDetail.getCustomValue(ICustomAttributeValueDetail.kCustom4);
	// if (uicDetail != null) {
	// // If the custom field is not set, the Detail object could be null
	// MLMSUIC = uicDetail.toString();
	// }
	//
	// //When user's profile is updated in MLMS, it can change a null
	// //value for UIC to an empty string. Thus, we need to treat an empty
	// //string as null
	// if (MLMSUIC != null && MLMSUIC.trim().equals("")) {
	// MLMSUIC = null;
	// }
	// //Want to treat empty string the same as null
	// if (ldapUIC != null && ldapUIC.trim().equals("")) {
	// ldapUIC = null;
	// }
	// errMark = 20;
	// // Do nothing if NONE of them is null AND they are the same
	// if (empDetail.getCompany() != null && ldapUIC != null && MLMSUIC != null
	// && ldapUIC.equals(MLMSUIC)) {
	// logger.log(Level.FINE,
	// "Not Updating User UIC or Org. since UIC did not change");
	// return false;
	// }
	// // If user is being updated and the UIC hasn't changed, don't continue
	// with logic
	// // Added to prevent users from being assigned to Other org. every time
	// they log in
	// if (ldapUIC == null && MLMSUIC == null && isUpdateUser) {
	// logger.log(Level.FINE,
	// "Not Updating User UIC or Org. since UIC is null and did not change");
	// return false;
	// }
	//
	// logger.log(Level.INFO, "Updating User {0} UIC from {1} to {2}",
	// new Object[]{userInfo.getUserName(), MLMSUIC, ldapUIC});
	// errMark = 30;
	// // Custom 4 is used to store UIC. Need to perform some logic to assign
	// UIC
	// empDetail.setCustomValue(new CustomAttributeValueDetail(
	// ICustomAttributeValueDetail.kCustom4, ldapUIC));
	// hasUpdated = true;
	//
	// errMark = 40;
	// // Update user's organization based on UIC value
	// assignBusinessUnit(locator, empDetail, userInfo.getUserName(), ldapUIC);
	//
	// return hasUpdated;
	// } catch (Exception e) {
	// logger.log(Level.SEVERE,"Could not update Employee in Saba.  Error Mark = {0}",
	// errMark);
	// logger.log(Level.SEVERE, e.getMessage(), e);
	// return false;
	// }
	// }

	/**
	 * Removed audience type from Saba user.
	 * 
	 * Parameters:
	 * 
	 * @param audienceTypeID
	 *            Saba audience type ID
	 * @param employee
	 *            Saba employee
	 * 
	 *            Returns:
	 * @return none
	 * 
	 *         Throws:
	 * @throws SabaException
	 * @throws DBUtilException
	 */
	// private static void removeAudienceTypeFromUser_Old(ServiceLocator
	// locator, AccountType akoAccountType,
	// Employee employee) throws SabaException, DBUtilException {
	// String audTypeID = akoAccountType.getAudienceTypeID();
	// AudienceTypeManager audienceTypeManager = (AudienceTypeManager)
	// locator.getManager(Delegates.kAudienceTypeManager);
	// //Gets list of distinct audience type ids that are not null, or NOT new
	// audience type id
	// //We don't want to remove the audience type id we're about to add. This
	// prevents extra logging in audit trail
	// Collection<String> akoAudienceTypes =
	// getAKOAudienceTypesExceptID(locator, (audTypeID == null)?"0":audTypeID);
	//
	// logger.log(Level.FINER, "BEGIN audience type id removal");
	// Iterator<String> audTypeIter = akoAudienceTypes.iterator();
	// while (audTypeIter.hasNext()) {
	// String tempAudTypeID = (String)audTypeIter.next();
	// AudienceType audienceType =
	// audienceTypeManager.findAudienceTypeByKey(tempAudTypeID);
	// //TO-DO changed from
	// //if (audienceTypeManager.isPersonPresent(employee, audienceType)) {
	// if (audienceTypeManager.isPartyPresent(employee, audienceType)) {
	// logger.log(Level.FINER,"Removing audience type id {0}", tempAudTypeID);
	// audienceTypeManager.removePerson(employee, audienceType);
	// }
	// }
	// }

	private static void removeAudienceTypeFromUser(
			AudienceTypeManager audTypeMgr, AccountType akoAccountType,
			Employee employee) throws SabaException, DBUtilException {
		AudienceType audienceType = null;
		ArrayList<String> audRemoveList = null;
		String tempStr = akoAccountType.getAudienceType();

		if (tempStr == null) {
			// audRemoveList = CommonReferences.getAudTypeAllMLMS();
		}
		/**
		 * for (String tempAudTypeID : audRemoveList) { // audienceType = //
		 * audienceTypeManager.findAudienceTypeByKey(tempAudTypeID);
		 * audienceType = (AudienceType) ServiceLocator.getReference(
		 * AudienceType.class, tempAudTypeID); if
		 * (audTypeMgr.isPartyPresent(employee, audienceType)) {
		 * audTypeMgr.removePerson(employee, audienceType);
		 * logger.log(Level.FINER, "Removed audience type id {0}",
		 * tempAudTypeID); } }
		 **/
	}

	// /**
	// * Check to determine that Extended Custom table even needs
	// * to be updated, before resources are wasted on updating Custom table.
	// *
	// * Check to determine if AKO FIN OR SSN are different from LMS FIN OR SSN
	// *
	// * @param locator ServiceLocator
	// * @param employeeID - Employee ID
	// * @param UserInfo userInfo - Containing armyFIN and SSN values to add
	// * @return true if update is needed; false otherwise
	// */
	// private static boolean shouldUpdateExtTable(ServiceLocator locator,
	// String employeeID, UserInfo userInfo) throws DBUtilException,
	// UserException {
	// boolean shouldUpdateExtTable = true;
	// ArrayList<String> customAttributes = queryExtTable(locator, employeeID);
	// if (customAttributes.size() != 2) {
	// throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// "Row does not exist in AT_LMS_CMT_PERSON_EXT table with employee id of: "
	// + employeeID);
	// }
	// String lmsSSN = (String)customAttributes.get(0);
	// String lmsFIN = (String)customAttributes.get(1);
	// String akoSSN = userInfo.getSSNo();
	// String akoFIN = userInfo.getArmyFIN();
	//
	// if (lmsSSN == null) {
	// lmsSSN = "";
	// }
	// if (lmsFIN == null) {
	// lmsFIN = "";
	// }
	// if (akoSSN == null) {
	// akoSSN = "";
	// }
	// if (akoFIN == null) {
	// akoFIN = "";
	// }
	// if (lmsSSN.equals(akoSSN) && lmsFIN.equals(akoFIN)) {
	// shouldUpdateExtTable = false;
	// }
	//
	// return shouldUpdateExtTable;
	// }

	/**
	 * This call assumes that Employee exists in the DB DB triggers ensures that
	 * a corresponding record exists in the extenstion table
	 * 
	 * @param locator
	 *            Service locator
	 * @param id
	 *            Employee ID
	 * @param userInfo
	 *            Contains LDAP FIN and SSN values
	 * @param lookUpResults
	 *            Contains FIN and SSN in LMS Ext table
	 */

	// private static Collection<String>
	// getAKOSecurityListsExceptID(ServiceLocator locator, String id) throws
	// DBUtilException {
	// Connection conn = null;
	// ResultSet rs = null;
	// PreparedStatement statement = null;
	// Collection<String> securityLists = new ArrayList<String>();
	//
	// try {
	// conn = DBUtil.getConnection(locator, "User.getAKOSecurityListsExceptID");
	// logger.log(Level.FINE, "User.getAKOSecurityListsExceptID {0}", id);
	//
	// statement = conn.prepareStatement(SQL_AKO_SECURITY_LIST_EXCEPT);
	// statement.setString(1, id);
	// rs = statement.executeQuery();
	//
	// String tempList = "";
	// while (rs.next()) {
	// tempList = rs.getString(1);
	// if (tempList != null) {
	// securityLists.add(tempList);
	// }
	// }
	// } catch (SQLException ex) {
	// logger.log(Level.SEVERE,
	// "User.getAKOSecurityListsExceptID Error finding security lists for AKO {0}",
	// id);
	// throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL,
	// "Error finding security lists: " + id, ex);
	// } finally {
	// DBUtil.freeDBResources(locator, rs, statement, conn,
	// "User.getAKOSecurityListsExceptID");
	// }
	// return securityLists;
	// }
	//
	// /**
	// * Returns a list of Audience type ID's except the one specified
	// * @param locator
	// * @param id
	// * @return
	// * @throws DBUtilException
	// */
	// private static Collection<String>
	// getAKOAudienceTypesExceptID(ServiceLocator locator,
	// String id) throws DBUtilException {
	// Connection conn = null;
	// ResultSet rs = null;
	// PreparedStatement statement = null;
	// Collection<String> audienceTypes = new ArrayList<String>();
	//
	// try {
	// conn = DBUtil.getConnection(locator, "User.getAKOAudienceTypesExceptID");
	// logger.log(Level.FINE, "User.getAKOAudienceTypesExceptID {0}", id);
	//
	// statement = conn.prepareStatement(SQL_AKO_AUDIENCE_TYPE_EXCEPT);
	// statement.setString(1, id);
	// rs = statement.executeQuery();
	//
	// String tempType = "";
	// while (rs.next()) {
	// tempType = rs.getString(1);
	// if (tempType != null) {
	// audienceTypes.add(tempType);
	// }
	// }
	// } catch (SQLException ex) {
	// logger.log(Level.SEVERE,
	// "User.getAKOAudienceTypesExceptID Error finding audience types for AKO {0}",
	// id);
	// throw new DBUtilException(MLMSStatusCode.NUM_DB_GENERAL,
	// "Error finding audience type: " + id, ex);
	// } finally {
	// DBUtil.freeDBResources(locator, rs, statement, conn,
	// "User.getAKOAudienceTypesExceptID");
	// }
	// return audienceTypes;
	// }

	// /**
	// * Retrieves both SSN and FIN values from custom table based on employee
	// ID.
	// *
	// * @param locator
	// * @param employeeID - The Employee ID to search custom table on
	// * @return A collection with SSN and FIN values from custom table
	// (AT_LMS_CMT_PERSON_EXT).
	// * Collection will NEVER be null, but it could be empty, if no value
	// returned for employee ID
	// * Collection.get(0) will hold SSN
	// * Collection.get(1) will hold FIN
	// */
	// public static ArrayList<String> queryExtTable(ServiceLocator locator,
	// String employeeID) throws DBUtilException {
	// int errMark=0;
	// Connection conn = null;
	// ResultSet rs = null;
	// PreparedStatement statement = null;
	// ArrayList<String> customAttributes = new ArrayList<String>();
	// try {
	// conn = DBUtil.getConnection(locator, "User.queryExtTable");
	// logger.fine("Retrieving Extended Custom value for SSN and FIN with ID: "
	// + employeeID);
	// statement = conn.prepareStatement(SQL_EXT_TABLE);
	// statement.setString(1, employeeID);
	// errMark = 10;
	// rs = statement.executeQuery();
	// errMark = 20;
	// if (rs.next()) {
	// customAttributes.add(rs.getString("SSN"));
	// customAttributes.add(rs.getString("FIN"));
	// errMark=30;
	// }
	// } catch (SQLException e) {
	// StringBuilder errMsg = new StringBuilder("User.queryExtTable: ");
	// errMsg.append(errMark);
	// errMsg.append("\n");
	// errMsg.append(e.getMessage());
	// logger.log(Level.SEVERE, errMsg.toString(), e);
	// throw new DBUtilException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// errMsg.toString(), e);
	// } finally {
	// DBUtil.freeDBResources(locator, rs, statement, conn,
	// "User.queryExtTable");
	// }
	// return customAttributes;
	// }

	// /**
	// * Finds Saba privilege list id from the privileges id.
	// *
	// * @param strPrivId - Saba privilege id
	// * @return String
	// * @throws DBUtilException
	// */
	// public static String findPrivListId(ServiceLocator locator, String
	// strPrivId, String strDomId) throws DBUtilException {
	// Connection conn = null;
	// ResultSet rs = null;
	// PreparedStatement statement = null;
	// String strListId = null;
	//
	// if (strPrivId != null) {
	// conn = DBUtil.getConnection(locator, "User.findPrivListId");
	//
	// logger.log(Level.FINE, "UserfindPrivListId strPrivId {0}, strDomId {1}",
	// new Object[]{strPrivId, strDomId});
	//
	// try {
	// statement = conn.prepareStatement(SQL_SECURITY_LIST_ID);
	// statement.setString(1, strPrivId);
	// statement.setString(2, strDomId);
	// rs = statement.executeQuery();
	// if (rs.next()) {
	// strListId = rs.getString(1);
	// } else {
	// logger.log(Level.WARNING,
	// "User.findPrivListId No rows found with strPrivId: {0}", strPrivId);
	// }
	// } catch (SQLException e) {
	// logger.log(Level.SEVERE,
	// "User.findPrivListId Error finding rows with priv ID: strPrivId {0}, strDomId {1}, Ex {2}",
	// new Object[]{strPrivId, strDomId, e.getMessage()});
	// throw new DBUtilException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
	// "Error finding priv list. priv ID " + strPrivId + ", domID " + strDomId,
	// e);
	// } finally {
	// DBUtil.freeDBResources(locator, rs, statement, conn,
	// "User.findPrivListId");
	// }
	// } // if priv ID is not null
	// return strListId;
	// } // end findPrivListId

	/**
	 * Should be used by setStateCode.jsp only
	 * 
	 * @param employeeID
	 * @param newStateCode
	 */
	public static void updateStateCode(String employeeID, String newStateCode)
			throws SabaException {
		ServiceLocator locator = null;

		try {
			locator = SystemInit.loginMLMSAdmin();

			PartyManager partyManager = (PartyManager) locator
					.getManager(Delegates.kPartyManager);
			Employee employee = (Employee) ServiceLocator.getReference(
					Employee.class, employeeID);
			EmployeeDetail empDetail = partyManager.getDetail(employee);

			// Custom 7 is used to the store the State code for National Guard
			/*
			 * empDetail.setCustomValue(new CustomAttributeValueDetail(
			 * ICustomAttributeValueDetail.kCustom7, newStateCode));
			 */

			partyManager.updateEmployee(employee, empDetail);
		} finally {
			if (locator != null) {
				SystemInit.logoutSaba("User.updateStateCode");
			}
		}
	}

	/***
	 * 
	 * @param username
	 * @return
	 */
	/**
	 * public static RetrieveAppDetailsResponseType retrieveEIDMUserProfile(
	 * String username) { WaaSApplicationService service = new
	 * WaaSApplicationService(); RetrieveAppDetailsResponseType appDetails =
	 * null;
	 ** 
	 * Make the web service call
	 * 
	 * if (service != null) { RetrieveAppDetailResponse appResponse = service
	 * .retrieveApplicationDetails(username);
	 * 
	 * if (appResponse != null) { appDetails = appResponse.getResponseType(); if
	 * (appDetails != null) { UserProfileV4Type userProfile =
	 * appDetails.getUserProfile(); if (userProfile != null) { /**
	 * LoginLog.writeToErrorLog("WaaSClient: user name from eidm " +
	 * userProfile.getUserInfo().getUserId());
	 * LoginLog.writeToErrorLog("WaaSClient: Name from eidm " +
	 * userProfile.getUserInfo().getFirstName() + " " +
	 * appDetails.getUserProfile().getUserInfo() .getLastName());
	 * LoginLog.writeToErrorLog("WaaSClient: address from eidm " +
	 * userProfile.getAddress()); ;
	 * LoginLog.writeToErrorLog("WaaSClient: email from eidm " +
	 * userProfile.getEmail());
	 * LoginLog.writeToErrorLog("WaaSClient: phone from eidm " +
	 * userProfile.getPhone());
	 * 
	 * } else { LoginLog.writeToErrorLog("WaaSClient: user profile is null"); }
	 * RolesType rolesType = appDetails.getRolesInfo(); if (rolesType != null) {
	 * 
	 * ArrayList<RoleInfoType> list = (ArrayList<RoleInfoType>) rolesType
	 * .getRoleInfo();
	 * 
	 * if (list != null) { Iterator<RoleInfoType> it = list.iterator();
	 * 
	 * while (it.hasNext()) {
	 * LoginLog.writeToErrorLog("WaasClient: role info from eidm " + it.next());
	 * }// while }// list
	 * 
	 * }// if rolesType RetrieveAppDetailsFault faultDetails = appResponse
	 * .getFaultType();
	 * LoginLog.writeToErrorLog("FWaaSClient: ault Details Message : " +
	 * faultDetails.getMessage());
	 * 
	 * }/** appDetails else {
	 * .writeToErrorLog("WaaSClient:  appDetails is null"); } }/** if
	 * appResponse else { .writeToErrorLog("WaaSClient:  appResponse is null");
	 * } // appResponse }/** service else {
	 * .writeToErrorLog("WaaSClient:  service is null"); } // service
	 * 
	 * return appDetails;
	 * 
	 * }
	 **/

	/**
	 * Quick disabling (set terminated_on to current time and status = inactive)
	 * an account if AKO status is inactive It does not remove MLMS defined
	 * roles and audience types Currently not used
	 * 
	 * @param partyManager
	 *            Pre-fetched PartyManager for performance reasons
	 * @param employee
	 *            Employee object
	 * @exception UserException
	 */
	public static void quickDisableAccount(PartyManager partyManager,
			Employee employee) throws UserException {
		EmployeeDetail empDetail = null;
		int errMark = 10;

		logger.entering(sourceClass, "quickDisableAccount");
		try {
			empDetail = partyManager.getDetail(employee);
			empDetail.setTerminatedOn(new Date());
			empDetail.setStatus(Constants.USER_STATUS_INACTIVE);
			partyManager.updateEmployee(employee, empDetail);
			// Check for NOWAIT error when updating employee
			int retry = 0;
			boolean isRetry = true;
			while (retry < NOWAIT_RETRIES && isRetry) {
				try {
					partyManager.updateEmployee(employee, empDetail);
					isRetry = false;
				} catch (SabaDBException e) {
					if (CommonUtil.stackTraceToString(e).indexOf("NOWAIT") > -1) {
						/**
						 * NOWAIT error was thrown. Wait .5 seconds, and try
						 * again
						 */
						retry++;
						logger.log(
								Level.INFO,
								"User {0} is locked in DB.  Waiting .5 seconds before retry",
								employee.getId());
						try {
							Thread.sleep(NOWAIT_RETRY_MILLI);
						} catch (InterruptedException e1) {
							//
						}
					} else {
						throw new UserException(MLMSStatusCode.NUM_DB_GENERAL,
								e.getMessage(), e);
					}
				}
			}
			if (isRetry) {
				/** Employee was never updated in this login */
				UserException ue = new UserException(
						MLMSStatusCode.NUM_DB_NOWAIT, "User "
								+ employee.getId()
								+ " retried 5 times and was never updated");
				ue.setRetrySeconds(NOWAIT_IE_RETRY_SEC);
				throw ue;
			}
		} catch (SabaException ex) {
			StringBuilder errMsg = new StringBuilder(
					"User.quickDisableAccount: ");
			errMsg.append(errMark);
			errMsg.append("\n");
			errMsg.append(ex.getMessage());
			throw new UserException(MLMSStatusCode.NUM_GENERIC_INTERNAL,
					errMsg.toString(), ex);
		}
	}

	public static class ProfilesLookUpResults {
		private ArrayList<String> jobTypeHistList;
		private AccountType primaryAccountType;
		private String lmsStateCode;
		private LMSUserRecord lmsUserRecord;

		private ProfilesLookUpResults() {
			jobTypeHistList = null;
			primaryAccountType = null;
			lmsStateCode = null;
			lmsUserRecord = null;
		}

		private void setJobTypeHistList(ArrayList<String> jobTypeHistList) {
			this.jobTypeHistList = jobTypeHistList;
		}

		protected ArrayList<String> getJobTypeHistList() {
			return jobTypeHistList;
		}

		private void setPrimaryAccountType(AccountType primaryAccountType) {
			this.primaryAccountType = primaryAccountType;
		}

		public AccountType getPrimaryAccountType() {
			return primaryAccountType;
		}

		private void setLMSUserRecord(LMSUserRecord lmsUserRecord) {
			this.lmsUserRecord = lmsUserRecord;
		}

		public LMSUserRecord getLMSUserRecord() {
			return lmsUserRecord;
		}

		public Employee getLMSEmployee() {
			if (lmsUserRecord != null) {
				return lmsUserRecord.getEmployee();
			} else {
				return null;
			}
		}

		public String getLMSUsername() {
			if (lmsUserRecord != null) {
				return lmsUserRecord.getUsername();
			} else {
				return null;
			}
		}

		protected void setLMSStateCode(String lmsStateCode) {
			this.lmsStateCode = lmsStateCode;
		}

		public String getLMSStateCode() {
			return lmsStateCode;
		}
	}

	/**
	 * A wrapper class that holds a reference to Employee and the associated SSN
	 * and FIN from extension table Only returned by findLMSUserRecord,
	 * immutable
	 */
	public static class LMSUserRecord {
		private Employee employee;
		private String username; // for verifying account status

		private LMSUserRecord(Employee employee, String username) {
			if (employee == null) {
				throw new IllegalArgumentException(
						"Employee reference cannot be null");
			}
			this.username = username;
			this.employee = employee;

		}

		/**
		 * Gets the Employee reference
		 * 
		 * @return
		 */
		public Employee getEmployee() {
			return employee;
		}

		/**
		 * Gets the USERNAME from DB
		 * 
		 * @return
		 */
		public String getUsername() {
			return username;
		}

	}

	/*
	 * 2012-07-06 Feilung Wong QC 1060: Do not throw exception when there is an
	 * access issue due to AKO account type. We do not want the transaction to
	 * roll back but to go ahead and disable the account. Also, the ability to
	 * reactivate account is added. This class is used by User.updateUser code
	 * to indicate login attempt status
	 */
	public static class LoginResult {

		protected boolean success;
		protected String id;
		protected Employee employee;
		protected MLMSException loginException;

		protected AccountType primaryAccountType;
		protected ArrayList<String> accountTypes;
		private String landingPageUrl;

		/**
		 * This constructor is used for successful login
		 * 
		 * @param id
		 * @param employee
		 * @param primaryAccountType
		 * @param accountTypes
		 */
		public LoginResult(String id, Employee employee,
				AccountType primaryAccountType, ArrayList<String> accountTypes) {
			this.success = true;
			this.id = id;
			this.employee = employee;
			this.primaryAccountType = primaryAccountType;
			this.accountTypes = accountTypes;
			this.loginException = null;
		}

		/**
		 * This constructor is used for failure
		 * 
		 * @param id
		 * @param loginException
		 */
		public LoginResult(String id, MLMSException loginException) {
			this.success = false;
			this.id = id;
			this.employee = null;

			this.primaryAccountType = null;
			this.accountTypes = null;
			if (loginException != null) {
				this.loginException = loginException;
			} else {
				throw new IllegalArgumentException(
						"LoginResult must take a valid Exception object if login fails");
			}
		}

		/**
		 * Gets the identifier, usually username
		 * 
		 * @return
		 */
		public String getID() {
			return id;
		}

		/**
		 * 
		 * @return true to indicate successful login, false otherwise
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * 
		 * @return a reference to the Saba Employee object
		 */
		public Employee getEmployee() {
			return employee;
		}

		/**
		 * 
		 * @return exception that causes the login to fail
		 */
		public MLMSException getLoginException() {
			return loginException;
		}

		/**
		 * @return null if user does not exists in AKO LDAP
		 */
		public AccountType getPrimaryAccountType() {
			return primaryAccountType;
		}

		/**
		 * Could be null if user does not exists in AKO LDAP
		 * 
		 * @return
		 */
		public ArrayList<String> getAccountTypes() {
			return accountTypes;
		}

		/**
		 * Gets the HasWatchedTutorial flag
		 * 
		 * @return "Y" if the student has watched the tutorial; "N" otherwise
		 */

		/**
		 * @return the landingPageUrl
		 */
		public String getLandingPageUrl() {
			return landingPageUrl;
		}

		/**
		 * @param landingPageUrl
		 *            the landingPageUrl to set
		 */
		public void setLandingPageUrl(String landingPageUrl) {
			this.landingPageUrl = landingPageUrl;
		}

	}
}
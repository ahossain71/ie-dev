package com.saba.auth;

import gov.cms.cciio.common.auth.LoginLog;
import gov.cms.cciio.common.util.CommonUtil;
import gov.cms.cciio.common.util.Constants;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.saba.auth.common.PasswordCache;
import com.saba.exception.SabaException;
import com.saba.exception.SabaFailedLoginException;

import com.saba.properties.Security;

public class DBAuthenticationProvider extends SabaAuthenticationProvider {
	private static final String className = DBAuthenticationProvider.class
			.getName();
	private static final Logger logger = Logger.getLogger(className);
	private String strSiteName = null;
	
	/*
	 * The original method does nothing more than the followings
	 * SabaLoginModule.verifySabaUser(siteName, userName, "welcome");
	 */
	@Override
	public void verifyUser(String siteName, String userName, String password)
			throws SabaException {
		String methodName = "verifyUser";
		boolean mlmsUser = false;
		// String decryptedUser = null;
		String strDecrPass = null;
		strSiteName = siteName;
		String mlmsMode = System.getProperty("mlms.mode","prod");
		LoginLog.writeToErrorLog("MLMS mode : " + mlmsMode);
		String version = "DBAuthenticationProvider V3.3.1";
		// LoginLog.writeToErrorLog(className + " " + version + " "+ methodName + " username " + userName + " password " + password);

		if (logger.isLoggable(Level.FINER)) {
			logger.finer("Custom DBAuthenticationProvider: " + siteName + ","
					+ userName + "," + password);
		}

		if (userName != null
				&& !userName.equalsIgnoreCase(Security.getAnonymousUsername())
				&& !userName.equalsIgnoreCase(Security.getAdminUsername())
				&& !userName.equalsIgnoreCase(Constants.MLMS_SUPER)) {
			mlmsUser = true;
			

			// Let Saba handles anonymous user (ie. sabaguest)/admin (admin)
			// Custom check before handing off to Saba Login

			try {

				strDecrPass = TokenEncryption.decrypt(password);
			//	 LoginLog.writeToErrorLog(className + " " +methodName + " "+
			//	userName + " password " + strDecrPass); 
				 
				if (mlmsMode.equalsIgnoreCase(Constants.MLMS_PROD_MODE ) && strDecrPass.indexOf(CommonUtil.replaceCharacters(userName)) > -1) {
					
					/*
					 * strTS contains the timestamp from the password token
					 * contains the username + role
					 */
					String strTS = strDecrPass.substring(0, 13);
					String token = strDecrPass.substring(13,strDecrPass.length());
					//LoginLog.writeToErrorLog("DBAuthenticationProvider: ts: " + strTS + " token " + token );
					Calendar calendar = Calendar.getInstance();
					/*
					 * loginTS is the timestamp put into the password before
					 * sending
					 */
					Timestamp loginTS = new Timestamp(Long.parseLong(strTS));
					
					/*String guess = com.saba.security.SabaPropertyEncryption.encrypt(strDecrPass.getBytes());
					LoginLog.writeToErrorLog("DBAuthenticationProvider: guess " + guess);*/
					

					/*
					 * create validity window of fifteen minutes for logged in
					 * users
					 */
					Timestamp windowTS = new Timestamp(
							calendar.getTimeInMillis() - 15 * 60 * 1000);
					/*
					 * if login timestamp is less than windowTS the password is
					 * stale (15 minutes)
					 */
					if (loginTS.compareTo(windowTS) < 0) {
						throw new Exception("password is stale");
					} else {
						/*LoginLog.writeToErrorLog("DBAuthenticationProvider: user " +
						 *			userName + " password " + token);
						
						 * Update - send the decrypted password to be verified
						 * */
						//SabaLoginModule.verifySabaUser(siteName, userName, strDecrPass);
						//PasswordCache.cache(siteName, userName, token);

						LoginLog.writeToErrorLog(" MLMS Password verified");
					}
				} else if(mlmsMode.equalsIgnoreCase(Constants.MLMS_PT_MODE)){
					
					
				} else {
					throw new Exception("password does not match");
				}

			} catch (Exception ex) {
				// not an encrypted string by our utility
				// LoginLog.writeToErrorLog("DBAuthenticationProvider: user " +
				// userName + " password " + strDecrPass);
				LoginLog.writeToErrorLog("DBAuthenticaionProvider: Does not look like encrypted password: "
						+ userName + "/******" + ex.getMessage());
				throw new SabaFailedLoginException(new SabaException(
						new Exception("Invalid credential:" + ex)));

			}

			// Change to default password and let Saba handle the rest
			// password = "welcome";
			// Not everyone has the same default password
			// And Saba doesn't seem to be tracking user login anyway.
			// Bypass Saba login for users that we recognize
		}
		// Let Saba handles the rest: set Admin flag, logging etc.
		// The original method does nothing more than the followings
		if (!mlmsUser) {
			
			try {
				SabaLoginModule.verifySabaUser(siteName, userName, password);
			} catch (SabaException ex) {
				StringBuilder sb = new StringBuilder(
						"SabaLoginModule.verifySabaUser(");
				sb.append(siteName);
				sb.append(",");
				sb.append(userName);
				sb.append(",");
				sb.append(password == null ? "<null>" : String.valueOf(password
						.length()));
				sb.append(") throws exception\n");
				sb.append(CommonUtil.stackTraceToString(ex));
				LoginLog.writeToErrorLog(sb.toString());
				throw ex;
			}
		}
	}

	public Timestamp getCurrentTimeStamp() {
		Calendar calendar = Calendar.getInstance();
		Timestamp currentTS = new Timestamp(calendar.getTimeInMillis());
		return currentTS;
	}

}

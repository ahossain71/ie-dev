package gov.hhs.cms.eidm.ws.waas.helper;

import gov.cms.cciio.common.auth.LoginLog;
import gov.cms.cciio.common.auth.MLMSLoginBean;
import gov.cms.cciio.common.auth.User;
import gov.cms.cciio.common.exception.UserException;
import gov.cms.cciio.common.util.Constants;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributeType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributesType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleInfoType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RolesType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserProfileV6Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.xml.datatype.XMLGregorianCalendar;

public class UserProfileDAO {
	final String AGENT_BROKER_ROLE_EFFECTIVE_DATE = "AgentBrokerRoleEffectiveDate";
	final String FFM_TRAINING = "FFMTraining";
	final String SHOP_TRAINING = "SHOPTraining";
	Calendar cal = Calendar.getInstance();

	public UserProfileDAO() {
		// TODO Auto-generated constructor stub
	}

	public MLMSLoginBean updateProfileData(
			RetrieveAppDetailsResponseType appDetails, MLMSLoginBean loginBean)
			throws ParseException {
		String methodName = "updateProfileData";
		boolean isFFMTraining = false;
		boolean isSHOPTraining = false;
		boolean isAgentBrokerRoleEffectiveDate = false;
		RoleInfoType roleInfoType = null;
		String rollAttributeName = null;
		RoleAttributeType rollAttribute = null;
		Date userTrainingExpDate = null;
		Date shopTrainingExpDate = null;
		Date historicalDate = null;
		RoleAttributesType roleAttributesType = null;
		ArrayList<RoleAttributeType> roleAttributeTypeList = null;
		XMLGregorianCalendar eidmAcctCreateXMLGregCal = null;
		String strEidmAcctCreateDate = null;
		Date eidmAcctCreateDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(
				Constants.MONTH_DAY_YEAR_STD);
		
		if (appDetails != null) {
			boolean isDebug = System.getProperty("mlms.debug", "true")
					.equalsIgnoreCase("true");
			LoginLog.writeToErrorLog(UserProfileDAO.class.getSimpleName()
					+ " version 2016-08-04-14:07 ");
			UserProfileV6Type userProfile = appDetails.getUserProfile();
			if (userProfile != null) {

				if (this.validateString(userProfile.getUserInfo()
						.getFirstName())) {
					loginBean
							.setFname(userProfile.getUserInfo().getFirstName());
				}
				if (this.validateString(userProfile.getUserInfo().getLastName())) {
					loginBean.setLname(userProfile.getUserInfo().getLastName());
				}
				if (this.validateString(userProfile.getAddress()
						.getStreetAddress())) {
					loginBean.setStreetAddress(userProfile.getAddress()
							.getStreetAddress());
				}
				if (this.validateString(userProfile.getAddress()
						.getStreetAddress2())) {
					loginBean.setStreetAddress2(userProfile.getAddress()
							.getStreetAddress2());
				}
				if (this.validateString(userProfile.getAddress().getCity())) {
					loginBean.setCity(userProfile.getAddress().getCity());
				}
				if (this.validateString(userProfile.getAddress().getState())) {
					loginBean.setState(userProfile.getAddress().getState());
				}
				if (this.validateString(userProfile.getAddress().getZipcode())) {
					loginBean.setZipcode(userProfile.getAddress().getZipcode());
				}
				if (this.validateString(userProfile.getAddress()
						.getZipcodeExtension())) {
					loginBean.setZipcodeExtension(userProfile.getAddress()
							.getZipcodeExtension());
				}
				if (this.validateString(userProfile.getEmail().getEmailId())) {
					loginBean.setEmail(userProfile.getEmail().getEmailId());
				}
				if (this.validateString(userProfile.getPhone().getCountryCode())) {
					loginBean.setCountryCode(userProfile.getPhone()
							.getCountryCode());
				}
				if (this.validateString(userProfile.getUserInfo().getGuid())) {
					loginBean.setGuid(userProfile.getUserInfo().getGuid());
					LoginLog.writeToErrorLog(UserProfileDAO.class
							.getSimpleName()
							+ " methodName: "
							+ methodName
							+ " guid " + loginBean.getGuid());
				} else {
					LoginLog.writeToErrorLog(UserProfileDAO.class
							.getSimpleName()
							+ " methodName: "
							+ methodName
							+ " GUID was null or zero length "
							+ userProfile.getUserInfo().getGuid());
				}
				if (userProfile.getUserInfo().getAccountCreateDate()!=null) {
					eidmAcctCreateXMLGregCal = userProfile
							.getUserInfo().getAccountCreateDate();
					if (eidmAcctCreateXMLGregCal != null) {
						StringBuffer buff = new StringBuffer();
						buff.append(eidmAcctCreateXMLGregCal.getDay());
						buff.append("/");
						buff.append(eidmAcctCreateXMLGregCal.getMonth());
						buff.append("/");
						buff.append(eidmAcctCreateXMLGregCal.getYear());
						strEidmAcctCreateDate = buff.toString();
						eidmAcctCreateDate = this.getFormattedDate(strEidmAcctCreateDate);
						LoginLog.writeToErrorLog(UserProfileDAO.class
								.getSimpleName()
								+ " methodName: "
								+ methodName + " "+loginBean.getUsername()+" Account Create Date " + strEidmAcctCreateDate);
						LoginLog.writeToErrorLog(UserProfileDAO.class
								.getSimpleName()
								+ " methodName: "
								+ methodName + " "+loginBean.getUsername()+" Account Create Date " + this.getddMMMyyFormattedDateString(eidmAcctCreateDate));
						loginBean.setEidmAccountCreateDate(eidmAcctCreateDate);
						

					} 
				} else {
					LoginLog.writeToErrorLog(UserProfileDAO.class
							.getSimpleName()
							+ " methodName: "
							+ methodName
							+ " GUID was null or zero length "
							+ userProfile.getUserInfo().getGuid());
				}
				if (this.validateString(userProfile.getPhone()
						.getPrimaryPhone())) {
					loginBean.setPrimaryPhone(userProfile.getPhone()
							.getPrimaryPhone());
				}
				if (isDebug) {
					LoginLog.writeToErrorLog(" WaaSClient: Username: "
							+ userProfile.getUserInfo().getUserId()
							+ "\n First Name from eidm "
							+ userProfile.getUserInfo().getFirstName()

							+ " Last Name "
							+ appDetails.getUserProfile().getUserInfo()
									.getLastName());
					/*
					 * LoginLog.writeToErrorLog(" WaaSClient: First Name from eidm "
					 * + loginBean.getFname()
					 * 
					 * + " Last Name " + loginBean.getLname());
					 * 
					 * LoginLog.writeToErrorLog(" WaaSClient: address from eidm "
					 * + userProfile.getAddress().getStreetAddress() + "\n\t\t"
					 * + userProfile.getAddress().getStreetAddress2() + "\n\t\t"
					 * + userProfile.getAddress().getCity() + ", " +
					 * userProfile.getAddress().getState() + " " +
					 * userProfile.getAddress().getZipcode() + "-" +
					 * userProfile.getAddress().getZipcodeExtension()); ;
					 * LoginLog.writeToErrorLog(" WaaSClient: email from eidm "
					 * + userProfile.getEmail().getEmailId());
					 * LoginLog.writeToErrorLog(" WaaSClient: phone from eidm "
					 * + userProfile.getPhone().getCountryCode() + " " +
					 * userProfile.getPhone().getPrimaryPhone());
					 */
				}
			}/** if user profile **/
			else {
				LoginLog.writeToErrorLog(UserProfileDAO.class.getName() + " "
						+ methodName + " WaaSClient: user profile is null");
			}
			/*
			 * rolesType getRoleInfo returns a list of of RoleInfoTypes
			 */
			RolesType rolesType = appDetails.getRolesInfo();
			if (rolesType != null) {

				ArrayList<RoleInfoType> roleInfoTypeList = (ArrayList<RoleInfoType>) rolesType
						.getRoleInfo();
				/**
				 * Role Info Type returns role name, value, list of
				 * RoleAttributesType
				 */
				if (roleInfoTypeList != null) {
					/**
					 * retrieve iterator
					 */
					if (isDebug) {
						LoginLog.writeToErrorLog(UserProfileDAO.class
								.getSimpleName()
								+ "Size of roleInfoTypeList "
								+ roleInfoTypeList.size());
					}

					Iterator<RoleInfoType> roleInfoIterator = roleInfoTypeList
							.iterator();
					int count = 0;
					while (roleInfoIterator.hasNext()) {
						count++;

						/**
						 * get role name, value and list of RolettributesType
						 * objects
						 */
						roleInfoType = roleInfoIterator
								.next();

						String roleName = roleInfoType.getRoleName();
						/** Role name will be either FFM Training role or **/
						if (isDebug) {
							LoginLog.writeToErrorLog(UserProfileDAO.class
									.getSimpleName()
									+ " role iterator "
									+ count);
							LoginLog.writeToErrorLog(UserProfileDAO.class
									.getSimpleName()
									+ " role turned from WaaSClient "
									+ roleName);
						}
						if (roleName.toLowerCase().indexOf(
								Constants.FFM_TRAINING_ACCESS) > -1) {

							loginBean.setRole(Constants.AGENT);

						} else if (roleName.toLowerCase().indexOf(
								Constants.FFM_ASSISTER) > -1) {

							loginBean.setRole(Constants.ASSISTER);

						} else if (roleName.toLowerCase().indexOf(
								Constants.MLMS_BUSINESS_OWNER) > -1) {

							loginBean.setRole(Constants.CCIIO);

						} else if (roleName.toLowerCase().indexOf(
								Constants.MLMS_ADMIN) > -1) {

							loginBean.setRole(Constants.CCIIO);

						} else if (roleName.toLowerCase().indexOf(
								Constants.AGENT) > -1) {
							historicalDate = null;
							if (roleInfoType.getRoleGrantDate() != null) {
								historicalDate = roleInfoType
										.getRoleGrantDate()
										.toGregorianCalendar().getTime();
								loginBean
										.setAgentBrokerRoleHistoricalEffectiveDate(historicalDate);
								LoginLog.writeToErrorLog(UserProfileDAO.class
										.getSimpleName()
										+ " "
										+ methodName
										+ " Historical Effective Role Grant Date "
										+ loginBean
												.getAgentBrokerRoleHistoricalEffectiveDate());

							}
							

							if (isDebug) {
								System.out.println(UserProfileDAO.class
										.getSimpleName()
										+ " User : "
										+ loginBean.getUsername()
										+ " Historical Role Grant Date: "
										+ formatter.format(historicalDate));
							}

							 roleAttributesType = roleInfoType
									.getRoleAttributes();

							if (roleAttributesType != null) {

								 roleAttributeTypeList = (ArrayList<RoleAttributeType>) roleAttributesType
										.getAttribute();

								Iterator<RoleAttributeType> attributeIterator = roleAttributeTypeList
										.iterator();
								loginBean.setComplete(false);
								/*
								 * iterate through role attributes looking for -
								 * FFMTraining - indicates user has access to
								 * FFM, value is expiration date - should be
								 * equal or greater than date set by policy
								 * looking for -
								 */
								while (attributeIterator.hasNext()) {

									rollAttribute = attributeIterator
											.next();
									rollAttributeName = rollAttribute
											.getName().trim();
									isFFMTraining = rollAttributeName
											.equalsIgnoreCase(FFM_TRAINING);
									isSHOPTraining = rollAttributeName.equalsIgnoreCase(SHOP_TRAINING);

									if (isDebug) {
										LoginLog.writeToErrorLog(UserProfileDAO.class
												.getSimpleName()
												+ " User : "
												+ loginBean.getUsername()
												+ " Role Attribute Name "
												+ rollAttribute.getName()
												+ ", value "
												+ rollAttribute.getValue());
										if (isFFMTraining) {
											LoginLog.writeToErrorLog(" Role Attribute "
													+ rollAttributeName
													+ " Found");
										}
										if (isSHOPTraining) {
											LoginLog.writeToErrorLog(" Role Attribute "
													+ rollAttributeName
													+ " Found");
										}

									}

									/*
									 * if user has the FFM_Training role
									 * attribute mark complete
									 */
									if (isFFMTraining) {
										
										if (rollAttribute.getValue() != null) {
											userTrainingExpDate = this
													.getFormattedDate(rollAttribute
															.getValue());
											loginBean
													.setFfmTrainingDate(userTrainingExpDate);
											// compare dates
											
												if (userTrainingExpDate != null && this.isTrainingCurrent(
														this.getCurrentTrainingDate(),
														userTrainingExpDate)) {

													loginBean.setComplete(true);
												} else {
													loginBean
															.setComplete(false);
												}
											
										}

										/**
										 * need to determine if assigned
										 * expiration date is greater than or
										 * equal to the current expiration date
										 */

										if (isDebug) {
											String regisrationStatsuMsg = null;
											if (loginBean.isComplete()) {
												regisrationStatsuMsg = " MLMS will assign \"Complete\",";
											}

											LoginLog.writeToErrorLog(UserProfileDAO.class
													.getSimpleName()
													+ " "
													+ methodName
													+ " User : "
													+ loginBean.getUsername()
													+ " FFM_Training expiration date: "
													+ this.getFormattedDateString(loginBean
															.getFfmTrainingDate()));

											LoginLog.writeToErrorLog(UserProfileDAO.class
													.getSimpleName()
													+ " Current Year FFM Access Training expiration date "
													+ getFormattedDateString(getCurrentTrainingDate())
													+ " User : "
													+ loginBean.getUsername()
													+ " EIDM Assigned Expiration date "
													+ this.getFormattedDateString(loginBean
															.getFfmTrainingDate())
													+ " equal "
													+ loginBean.isComplete()
													+ regisrationStatsuMsg
													+ " certificate date will be "
													+ this.getFormattedDateString(loginBean
															.getAgentBrokerRoleHistoricalEffectiveDate()));
										}// if

									} // if FFM Training
									/*
									 * if user has the FFM_Training role
									 * attribute mark complete
									 */
									if (isSHOPTraining) {
										
										if (rollAttribute.getValue() != null) {
											shopTrainingExpDate = this
													.getFormattedDate(rollAttribute
															.getValue());
											
											loginBean.setSHOPTrainingDate(shopTrainingExpDate);
											// compare dates
											
												if (shopTrainingExpDate != null && this.isTrainingCurrent(
														this.getCurrentTrainingDate(),
														shopTrainingExpDate)) {

													loginBean.setSHOPComplete(true);
												} else {
													loginBean
															.setSHOPComplete(false);
												}
											
										}

										/**
										 * need to determine if assigned
										 * expiration date is greater than or
										 * equal to the current expiration date
										 */

										if (isDebug) {
											String regisrationStatsuMsg = null;
											if (loginBean.isComplete()) {
												regisrationStatsuMsg = " MLMS will assign \"Complete\",";
											}

											LoginLog.writeToErrorLog(UserProfileDAO.class
													.getSimpleName()
													+ " "
													+ methodName
													+ " User : "
													+ loginBean.getUsername()
													+ " FFM_Training expiration date: "
													+ this.getFormattedDateString(loginBean
															.getFfmTrainingDate()));

											LoginLog.writeToErrorLog(UserProfileDAO.class
													.getSimpleName()
													+ " Current Year FFM Access Training expiration date "
													+ getFormattedDateString(getCurrentTrainingDate())
													+ " User : "
													+ loginBean.getUsername()
													+ " EIDM Assigned Expiration date "
													+ this.getFormattedDateString(loginBean
															.getFfmTrainingDate())
													+ " equal "
													+ loginBean.isComplete()
													+ regisrationStatsuMsg
													+ " certificate date will be "
													+ this.getFormattedDateString(loginBean
															.getAgentBrokerRoleHistoricalEffectiveDate()));
										}// if

									} // if FFM Training

									isAgentBrokerRoleEffectiveDate = rollAttributeName
											.equalsIgnoreCase(AGENT_BROKER_ROLE_EFFECTIVE_DATE);

									if (isAgentBrokerRoleEffectiveDate) {
										Date effectiveDate = null;
										try{
											loginBean.setRawRoleEffectiveDate(rollAttribute.getValue());
										 effectiveDate = this.getEffectiveRoleDateFormattedDate(rollAttribute
												.getValue());
												
										} catch (ParseException ex){
											LoginLog.writeToErrorLog(User.class.getCanonicalName() + " " + methodName + " Agent Broker role attribute " + AGENT_BROKER_ROLE_EFFECTIVE_DATE + " threw parse exception, expecting dd-MMM-yy format :" + rollAttribute
												.getValue());
										}
										loginBean
												.setAgentBrokerRoleEffectiveDate(effectiveDate);
										if (isDebug && effectiveDate != null) {
											System.out
													.println(UserProfileDAO.class
															.getSimpleName()
															+ " User : "
															+ loginBean
																	.getUsername()
															+ " Current Effective Role Grant Date: "
															+ formatter
																	.format(effectiveDate));
										}// if
									}// if

								}

							}

						} else {
							LoginLog.writeToErrorLog("WaaS Client: no valid roles found for user :"
									+ loginBean.getUsername());
						}
						if (isDebug) {
							LoginLog.writeToDebugLog(UserProfileDAO.class
									.getSimpleName()
									+ " User : "
									+ loginBean.getUsername()
									+ " DEBUG Role Name " + loginBean.getRole());

						}

					}

				}// list

			}// if rolesType

		}/** appDetails **/
		else {
			LoginLog.writeToErrorLog("WaaSClient:  appDetails is null");
		}
		return loginBean;
	}

	private boolean validateString(String string) {
		return (string != null && string.trim().length() > 0);
	}

	/**
	 * EIDM is sending the current effective date in dd-MMM-yy format
	 * 
	 * @param strDate
	 * @return
	 * @throws ParseException
	 */
	private Date getEIDMDate(String strDate) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Date retVal = null;
		if(strDate != null){
			retVal = formatter.parse(strDate.trim());
		}
		return retVal;

	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private String getFormattedDateString(Date date) {
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd/MM/yyyy");
		String retVal = null;
		if (date != null) {
			retVal = dateSlashFormat.format(date);
		}

		return retVal;
	}
	/**
	 * 
	 * @param date
	 * @return
	 */
	private String getddMMMyyFormattedDateString(Date date) {
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd-MMM-yy");
		String retVal = null;
		if (date != null) {
			retVal = dateSlashFormat.format(date);
		}

		return retVal;
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 * 
	 *             private Date getCurrentTrainingDate() throws ParseException {
	 *             String date = System.getProperty("mlms.ffm.access.date",
	 *             "31/10/2016"); return new
	 *             SimpleDateFormat("yyyy-MM-dd").parse(date); }
	 */
	/**
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getCurrentTrainingDate() throws ParseException {
		String methodName = "getCurrentTrainingDateString";
		String dateFormatStr = "yyyy-MM-dd";
		String currDateStr = System.getProperty("mlms.ffm.access.date",
				"2017-10-31");
		Date currDate = null;
		try {
			currDate = getFormattedDate(currDateStr);
			// retVal = dateFormat.format(currDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new ParseException(
					UserProfileDAO.class.getSimpleName()
							+ " MLMS Exception, format of mlms.ffm.access.date should be "
							+ dateFormatStr + " received " + currDateStr, 394);
		}

		return currDate;
		// return dateStr;
	}

	/**
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getFormattedDate(String str) throws ParseException {
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dateHyphenFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date retVal;
		if (str.indexOf("/") > -1) {
			retVal = dateSlashFormat.parse(str);
		} else if (str.indexOf("-") > -1) {
			retVal = dateHyphenFormat.parse(str);
		} else {
			throw new ParseException("MLMS Exception, format of string " + str
					+ " not recognized ", 371);
		}
		return retVal;
	}
	/** EIDM will send formats as either 
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getEffectiveRoleDateFormattedDate(String str) throws ParseException {
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat dateHyphenFormat = new SimpleDateFormat("dd-MMM-yyyy");
		Date retVal;
		if (str.indexOf("/") > -1) {
			retVal = dateSlashFormat.parse(str);
		} else if (str.indexOf("-") > -1) {
			retVal = dateHyphenFormat.parse(str);
		} else {
			throw new ParseException("MLMS Exception, format of string " + str
					+ " not recognized ", 371);
		}
		return retVal;
	}

	/**
	 * returns true if assigned Training date is greater than or equal to the
	 * mmlmsDefinedExpirationDate
	 * 
	 * @param mlmsDefinedExpirationDate
	 * @param assignedTrainingExpirationDate
	 * @return
	 * @throws ParseException
	 */
	private boolean isTrainingCurrent(String mlmsDefinedExpirationDate,
			String assignedTrainingExpirationDate) throws ParseException {
		boolean retVal = false;
		Date mlmsExpDate = this.getFormattedDate(mlmsDefinedExpirationDate);
		Date assignedExpDate = this
				.getFormattedDate(assignedTrainingExpirationDate);
		if (assignedExpDate.after(mlmsExpDate)) {
			retVal = true;
		} else if (assignedExpDate.equals(assignedTrainingExpirationDate)) {
			retVal = true;
		}

		return retVal;
	}

	/**
	 * 
	 * @param mlmsDefinedExpirationDate
	 * @param assignedTrainingExpirationDate
	 * @return
	 * @throws Exception
	 */
	private boolean isTrainingCurrent(Date mlmsDefinedExpirationDate,
			Date assignedTrainingExpirationDate) {
		String methodName = "isTrainingCurrent";
		boolean retVal = false;
		if (mlmsDefinedExpirationDate != null
				&& assignedTrainingExpirationDate != null) {
			if (assignedTrainingExpirationDate.after(mlmsDefinedExpirationDate)) {
				retVal = true;
			} else if (assignedTrainingExpirationDate
					.equals(mlmsDefinedExpirationDate)) {
				retVal = true;
			}
		}
		
		return retVal;

	}

}

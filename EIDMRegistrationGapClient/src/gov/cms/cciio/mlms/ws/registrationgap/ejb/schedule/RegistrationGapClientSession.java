package gov.cms.cciio.mlms.ws.registrationgap.ejb.schedule;

/**
 * written by Steve Meyer
 * Purpose:
 * creates a schedule to make registration gap client calls
 */

import gov.cms.cciio.common.util.Constants;
import gov.cms.cciio.mlms.ws.registrationgap.dao.RegistrationGapClientDAO;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.domain.common.ErrorStatus;
import gov.hhs.cms.eidm.ws.waas.domain.common.FaultType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributeType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributesType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleInfoType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RolesType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserProfileV6Type;
import gov.hhs.cms.eidm.ws.waas.service.WaaSApplicationService;
import gov.hhs.cms.registrationgap.bean.UserIdBean;
import gov.hhs.cms.registrationgap.dto.RegistrationGapUserDTO;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.xml.datatype.XMLGregorianCalendar;

import cms.cciio.ws.registrationgap.exception.RegistrationGapException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapSetUserDataException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUserTrainingSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersConnectionSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersCountSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersListSQLException;

//import gov.hhs.cms.eidm.ws.waas.helper.UserProfileDAO;

/**
 * To do: must sort roles by name and persist to the database
 * 
 * @author sjmeyer 
 *
 */
@Singleton
@Startup
/* use of @Startup enables @PostContruct */
@LocalBean
public class RegistrationGapClientSession {
	private final static String sourceClass = RegistrationGapClientSession.class
			.getSimpleName();

	private int runcount = 0;
	/**
	 * @ Resource registers an instance of the TimerService class, this is
	 * "dependency injection"
	 */

	@Resource
	TimerService timerService;
	// class variable

	Timer timer;

	boolean isDebug = System.getProperty("mlms.debug", "true")
			.equalsIgnoreCase("true");
	Logger logger = Logger.getLogger("RegistrationGapClientSession");

	/**
	 * Must have public constructor that takes no parameters
	 */
	public RegistrationGapClientSession() {

	}

	/**
	 * @PostContruct identifies this method to be executed
	 */

	@PostConstruct
	void createTimer() {

		boolean runClient = Boolean.parseBoolean(System.getProperty(
				"reggap.schedule.client.run", "false"));
		if (runClient) {

			String minute = System.getProperty("reggap.schedule.client.minute",
					"0");

			String hour = System
					.getProperty("reggap.schedule.client.hour", "*");
			String day = System.getProperty("reggap.schedule.client.dayOfWeek",
					"*");

			timer = this.createScheduledTimer(day, hour, minute);

			System.out.println("Registration Gap Client version 1.1\n Created Timer ");
			this.printTimerInfo(timer);
		} else {
			System.out.println(" Registration gap will not run on this node");
		}
	}

	/**
	 * 
	 */
	void printTimerInfo(Timer timer) {
		String methodName = "printTimerInfo";
		// List<Timer> timerList = (List<Timer>)timerService.getTimers();

		// System.out.println(" there are " + timerList.size() + " timers ");
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss:SS");
		String minute = timer.getSchedule().getMinute();
		String hour = timer.getSchedule().getHour();
		String day = timer.getSchedule().getDayOfWeek();
		/*
		 * System.out.println(sourceClass + " " + methodName +
		 * "\n\t schedule day " + timer.getSchedule().getDayOfWeek() +
		 * " schedule hour " + timer.getSchedule().getHour() +
		 * " schedule minute " + timer.getSchedule().getMinute());
		 * 
		 * System.out.println(" Timer is persistent " + timer.isPersistent());
		 */
		System.out.println(" Next scheduled run "
				+ formatter.format(timer.getNextTimeout()));

		logger.logp(Level.INFO, sourceClass, methodName,
				" " + this.getFormattedDateStringTime(timer.getNextTimeout())
						+ " PostContruct Executed  CreatedTimer ");
		logger.logp(Level.INFO, sourceClass, methodName, " " + " minute "
				+ minute);
		logger.logp(Level.INFO, sourceClass, methodName, " " + " hour " + hour);
		logger.logp(Level.INFO, sourceClass, methodName, " " + " date " + day);
		logger.logp(
				Level.INFO,
				sourceClass,
				methodName,
				" " + " next scheduled run "
						+ formatter.format(timer.getNextTimeout()));

	}

	/*
	 * @Timeout identifies this method to be executed by the created timer
	 * object.
	 */
	@Timeout
	public void runRegistrationGapCheck() {
		String methodName = "method: runRegistrationGapCheck";

		logger.logp(Level.INFO, sourceClass, methodName,
				" starting run registration gap check, version 1.1.0 count = " + ++runcount);

		RegistrationGapResponseType registrationGapResponseType = new RegistrationGapResponseType();
		// get list of user training response types
		RegistrationGapClientDAO registrationGapDAO = new RegistrationGapClientDAO();

		RetrieveAppDetailsResponseType response = null;

		StatusType status = new StatusType();
		String username = null;

		String strMaxSize = System.getProperty(
				"registrationgap.client.maxsize", "50");
		logger.logp(Level.INFO, sourceClass, methodName, " max size "+ strMaxSize);
		//System.out.println("RegistrationGapClientSession + runRegistrationGapCheck + maxsize = " +strMaxSize);
		int maxReturnLength = Integer.valueOf(strMaxSize);

		ArrayList<RegistrationGapUserDTO> updateList = new ArrayList<RegistrationGapUserDTO>(
				maxReturnLength);

		List<UserIdBean> userList = null;

		RegistrationGapUserDTO dto = null;

		try {
			/*
			 * get list of users based on 
			 */
			userList = registrationGapDAO
					.getMLMSRegistrationGAPList(maxReturnLength);

			logger.logp(Level.INFO, sourceClass, methodName,"203 Registration Gap Client list has " + userList.size()+ " users ");
			//System.out.println("203 Registration Gap Client list has " + userList.size()+ " users ");
			ListIterator<UserIdBean> it = userList.listIterator();
			/*
			 * iterate through the gap list, make call to WaaSApplicationV6
			 */

			while (it.hasNext()) {
				// call WaaS
				UserIdBean userIdBean = it.next();
				if (userIdBean != null) {
					username = userIdBean.getUsername();
				}
				// get RetrieveAppDetailsResponseType object from web service
				//System.out.println("ANU REMOVE username = " +  username);
				//System.out.println("ANU REMOVE isDebug = " +  isDebug);
				
				try {

					if (isDebug) {
						logger.logp(Level.INFO, sourceClass, methodName,
								"....Calling web service for user " + username);
						//System.out.println("ANU REMOVE ....Calling web service for user = " +  isDebug);
						
					}
					WaaSApplicationService service = new WaaSApplicationService();

					response = service.retrieveApplicationDetails(username);
					
					//System.out.println("ANU REMOVE ....response = " +  response.toString());
					

				} catch (MalformedURLException e) {

					logger.logp(
							Level.SEVERE,
							sourceClass,
							methodName,
							MalformedURLException.class.getSimpleName()
									+ " "
									+ methodName
									+ " MalformedURLException Exception caught "
									+ e.getMessage());
				} catch (RetrieveAppDetailsFault e) {
					// TODO Auto-generated catch block
					FaultType fault = e.getFaultInfo();
					gov.hhs.cms.eidm.ws.waas.domain.common.StatusType statusType = fault
							.getStatus();

					List<ErrorStatus> errorList = statusType.getErrors();
					for (ErrorStatus errorStatus : errorList) {
						logger.logp(
								Level.SEVERE,
								sourceClass,
								methodName,
								" RetrieveAppDetailsFault Exception caught "
										+ "\n fault error code "
										+ errorStatus.getErrorCode()
										+ "\n fault error key "
										+ errorStatus.getErrorKey()
										+ "\n fault error message  "
										+ errorStatus.getErrorMessage()
										+ "\n fault status code  "
										+ errorStatus.getStatusCode()
										+ "\n fault status message code  "
										+ errorStatus.getStatusMessage());

					}// for
				} catch (Exception e) {
					logger.logp(Level.SEVERE, sourceClass, methodName,
							" Genercal Exception caught with WaaSApplication call : "
									+ e.getMessage());
				}
				// persist data to MLMS DB
				if (response != null) {

					try {
						if (isDebug) {
							logger.logp(Level.SEVERE, sourceClass, methodName, " Response received for user " + username);
							//System.out.println("Level.SEVERE, " + sourceClass + " " + methodName + " Response received for user " + username);

						}
						/*
						 * process role data
						 */
						if (userIdBean != null
								&& userIdBean.getPersonId() != null) {
							dto = processResponse(response, userIdBean);
						}
						/**
						 * process response does not create dto if user has no
						 * completed registration
						 */
						if (dto != null) {
							/*
							 * System.out.println(sourceClass + " " +
							 * sourceMethod + " 279 data transfer object for " +
							 * username +
							 * " added to update list\n if user has completed registration  "
							 * + dto.isFFMComplete());
							 */
							if (dto.isFFMComplete() || dto.isShopComplete()) {
								updateList.add(dto);
							}

						} else {
							if (dto == null) {
								logger.logp(Level.SEVERE, sourceClass,
										methodName,
										" data transfer object for " + username
												+ " is null");
							}
						}

					} catch (Exception e) {
						logger.logp(
								Level.SEVERE,
								sourceClass,
								methodName,
								" 294 Exception caught in  message: "
										+ e.getMessage() + " Exception class "
										+ e.getClass());
						e.printStackTrace(System.out);
					}
				}// response != null

			}// while interator of gap list
			RegistrationGapClientDAO dao = new RegistrationGapClientDAO();

			logger.logp(Level.SEVERE, sourceClass, methodName,
					" submitting list of " + updateList.size()
							+ " users for db updates ");
			
			//System.out.println("Level.SEVERE, " + sourceClass + " " + methodName + " Before calling the set methods " );


			dao.setFFMRegistrationGapDataBatch(updateList);

			dao.auditRegGapBatch(updateList);
			/*
			 * 
			 */
			dao.setABShopCompletionStatusBatch(updateList);
			/*
			 * 
			 */
			dao.setABMiscDataAccountCreateBatch(updateList);
			/*
			 * 
			 */
			dao.setABMiscDataHistDateBatch(updateList);
			/**
			 * 
			 */
			dao.setABMiscDataGUIDBatch(updateList);

		} catch (RegistrationGapUsersListSQLException e) {
			logger.logp(Level.SEVERE, sourceClass, methodName,
					"RegistrationGapUsersListSQLException " + e.getMessage());
			if (e.getMessage().equalsIgnoreCase(ErrorCodeType.ME_810.value())) {
				registrationGapResponseType
						.setErrorCode((ErrorCodeType.ME_810));
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorCode(ErrorCodeType.ME_810);
				status.setErrorMessage(ErrorMessageType.ME_810_QUERY_RETURNED_NO_DATA
						.value());
			} else if (e.getMessage().equalsIgnoreCase(
					ErrorCodeType.ME_820.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_820);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
						.value());
			} else {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
						.value());
			}
		} catch (RegistrationGapUserTrainingSQLException e) {
			logger.logp(Level.SEVERE, sourceClass, methodName,
					"RegistrationGapUserTrainingSQLException " + e.getMessage());
			if (e.getMessage().equalsIgnoreCase(ErrorCodeType.ME_810.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_810);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorCode(ErrorCodeType.ME_810);
				status.setErrorMessage(ErrorMessageType.ME_810_QUERY_RETURNED_NO_DATA
						.value());
			} else if (e.getMessage().equalsIgnoreCase(
					ErrorCodeType.ME_820.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_820);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
						.value());
			} else {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
						.value());
			}
		} catch (ParseException e) {

			logger.logp(Level.SEVERE, sourceClass, methodName,
					" " + e.getClass().getSimpleName() + " thrown, message "
							+ e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {

			logger.logp(
					Level.SEVERE,
					sourceClass,
					methodName,
					"RegistrationGapUsersConnectionSQLException "
							+ e.getMessage());
			registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830);

			status.setStatusCode(StatusCodeType.MS_500);
			status.setStatusMessage("Unsuccessful");
			status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
					.value());
		} catch (RegistrationGapException e) {
			logger.logp(Level.SEVERE, sourceClass, methodName, e.getClass()
					.getSimpleName() + " thrown, message " + e.getMessage());
		}
		this.printTimerInfo(timer);
	}

	/**
	 * 
	 * @param response
	 * @param userBean
	 *            TODO
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 * @throws RegistrationGapUsersCountSQLException
	 * @throws ParseException
	 * @throws RegistrationGapException
	 * @throws RegistrationGapSetUserDataException
	 */
	private RegistrationGapUserDTO processResponse(
			RetrieveAppDetailsResponseType response, UserIdBean userBean)
			throws RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException,
			RegistrationGapUsersCountSQLException, ParseException,
			RegistrationGapException, RegistrationGapSetUserDataException {

		String sourceMethod = "processResponse";

		String historicalRoleGrantDate = null;

		String effectiveRoleGrantDate = null;

		Date eidmAcctCreateDate = null;

		String username = null;

		String guid = null;

		List<RoleInfoType> roleList = null;

		UserProfileV6Type userProfileV6Type = response.getUserProfile();

		RegistrationGapUserDTO dto = null;

		RolesType rolesType = null;

		RoleInfoType roleInfoType = null;

		RoleAttributeType roleAttribute = null;

		String roleAttributeName = null;

		String roleAttributeValue = null;

		if (userProfileV6Type != null) {

			XMLGregorianCalendar eidmAcctCreateXMLGregCal = userProfileV6Type
					.getUserInfo().getAccountCreateDate();

			username = userProfileV6Type.getUserInfo().getUserId();

			guid = userProfileV6Type.getUserInfo().getGuid();

			if (eidmAcctCreateXMLGregCal != null) {
				StringBuffer buff = new StringBuffer();
				buff.append(eidmAcctCreateXMLGregCal.getMonth());
				buff.append("/");
				buff.append(eidmAcctCreateXMLGregCal.getDay());
				buff.append("/");
				buff.append(eidmAcctCreateXMLGregCal.getYear());
				eidmAcctCreateDate = this.getMMDDYYYYDate(buff.toString());

			} else {
				logger.logp(Level.INFO, sourceClass, sourceMethod,
						" EIDM account create date is null (follows): "
								+ eidmAcctCreateXMLGregCal);
			}
		}

		rolesType = response.getRolesInfo();

		roleList = rolesType.getRoleInfo();

		if (dto == null) {
			/*
			 * System.out.println(sourceClass + " " + sourceMethod +
			 * " 640 creating data transfer object for  " + username);
			 */
			dto = new RegistrationGapUserDTO();
		}
		dto.setUsername(username);

		ListIterator<RoleInfoType> rIt = roleList.listIterator();
		//System.out.println("ANU!!! Need to be removed processResponse Method: ");
		while (rIt.hasNext()) {
			roleInfoType = rIt.next();
			// check role name
			if (roleInfoType != null) {
				String roleName = roleInfoType.getRoleName();

				if (roleName.toLowerCase().equalsIgnoreCase(
						Constants.FFM_AGENT_BROKER)
						&& roleInfoType.getRoleGrantDate() != null) {
					StringBuffer buf = new StringBuffer();
					int day = roleInfoType.getRoleGrantDate().getDay();
					int month = roleInfoType.getRoleGrantDate().getMonth();
					int year = roleInfoType.getRoleGrantDate().getYear();

					buf.append(month);
					buf.append("/");
					buf.append(day);
					buf.append("/");
					buf.append(year);

					historicalRoleGrantDate = buf.toString();

				}
				/**
				 * get Role Grant date
				 */

				if (roleName.toLowerCase().equalsIgnoreCase(
						Constants.FFM_AGENT_BROKER)) {
					// user has been assigned the agent role - get the value
					dto.setFfmAgentBroker(true);
					RoleAttributesType roleAttributesType = roleInfoType
							.getRoleAttributes();

					ArrayList<RoleAttributeType> roleAttributeTypeList = (ArrayList<RoleAttributeType>) roleAttributesType
							.getAttribute();

					Iterator<RoleAttributeType> aIt = roleAttributeTypeList
							.iterator();
					boolean isFFMTraining = false;
					boolean isSHOPTraining = false;
					boolean isFFMTrainingCurrent = false;
					boolean isSHOPTrainingCurrent = false;

					while (aIt.hasNext()) {
						/*
						 * get role attribute if role attribute is FFMTraining
						 * user has role
						 */
						roleAttribute = aIt.next();

						roleAttributeName = roleAttribute.getName();
						roleAttributeValue = roleAttribute.getValue();

						logger.logp(Level.INFO, sourceClass, sourceMethod,
								" user " + username + " attribute name "
										+ roleAttributeName
										+ " attribute value "
										+ roleAttributeValue);
						

						isFFMTraining = roleAttributeName
								.equalsIgnoreCase(Constants.FFM_TRAINING);

						isSHOPTraining = roleAttributeName
								.equalsIgnoreCase(Constants.SHOP_TRAINING);

						boolean isAgentBrokerRoleEffectiveDate = false;

						if (isFFMTraining) {
							isFFMTrainingCurrent = checkTrainingComplete(roleAttribute);
							logger.logp(
									Level.INFO,
									sourceClass,
									sourceMethod,
									" user "
											+ username
											+ " has FFMTraining attribute, value "
											+ roleAttribute.getValue() + ", logic result " + isFFMTrainingCurrent);
							
							
							dto.setFFMComplete(isFFMTrainingCurrent);
							logger.logp(
									Level.INFO,
									sourceClass,
									sourceMethod,
									" user "
											+ username
											+ " has FFMTraining attribute, dto value " + dto.isFFMComplete());
						}
						if (isSHOPTraining) {
							isSHOPTrainingCurrent = checkTrainingComplete(roleAttribute);
							logger.logp(
									Level.INFO,
									sourceClass,
									sourceMethod,
									" user "
											+ username
											+ " has SHOPTraining attribute, value "
											+ roleAttribute.getValue() + ", logic result " + checkTrainingComplete(roleAttribute));
							dto.setShopComplete(isSHOPTrainingCurrent);

						}

						isAgentBrokerRoleEffectiveDate = roleAttributeName
								.equalsIgnoreCase(Constants.AB_ROLE_EFFECTIVE_DATE);

						if (isAgentBrokerRoleEffectiveDate) {
							effectiveRoleGrantDate = roleAttribute.getValue();

						}

					}// while attribute iterator
					if (isDebug) {
						logger.logp(
								Level.INFO,
								sourceClass,
								sourceMethod,
								"\n user " + username
										+ " has completed FFM Training "
										+ dto.isFFMComplete() + "\n user "
										+ username
										+ " has completed SHOP Training  "
										+ dto.isShopComplete());

					}

				}// if FF_TRAINING_ACCESS

				/*
				 * Update to put all users in list - need - username, person_id,
				 * registration status (Complete / Incomplete), effective role
				 * date
				 */

			}// if roleInfoType is not null
		}// while role list iterator

		String personId = userBean.getPersonId();
		if (isDebug) {
			logger.logp(
					Level.INFO,
					sourceClass,
					sourceMethod,
					" setting username: "
							+ username
							+ ", personId "
							+ personId
							+ "\n FFM Registration Status "
							+ (dto.isFFMComplete() ? "Complete" : "Incomplete")
							+ "\t SHOP Registration Status "
							+ (dto.isShopComplete() ? "Complete" : "Incomplete")
							+ "\n Effective Role Grant Date "
							+ effectiveRoleGrantDate + "\t guid " + guid
							+ "\n Historical Role Grant Date "
							+ historicalRoleGrantDate
							+ "\t Account Create Date " + eidmAcctCreateDate
							+ "  in DTO");
		}

		dto.setPersonId(personId);
		// dto.setFFMComplete(isFFMTrainingCurrent);
		// dto.setShopComplete(isSHOPTrainingCurrent);
		if (effectiveRoleGrantDate != null) {
			dto.setAgentBrokerEffectiveDate(this
					.getMMDDYYYYDate(effectiveRoleGrantDate));
			dto.setRawAgentBrokerEffectiveDate(effectiveRoleGrantDate);
		}
		dto.setGuid(guid);
		dto.setAccountCreateDate(eidmAcctCreateDate);
		if (historicalRoleGrantDate != null) {
			if (historicalRoleGrantDate.indexOf('/') > -1) {

				dto.setHistoricalRoleGrantDate(this
						.getMMDDYYYYDate(historicalRoleGrantDate));
			} else {

				dto.setHistoricalRoleGrantDate(this
						.getDDMMMYYYYDate(historicalRoleGrantDate));
			}
		}
		return dto;
	}

	/**
	 * create a timer requires timer config value for day may be * (all), "Mon",
	 * "Mon,Tue,Fri", Mon-Fri will set a default schedule to run every day,
	 * every hour on the hour (1:00, 2:00,3:00,....)
	 * 
	 * @param day
	 * @param hour
	 * @param minute
	 * @return
	 */
	public Timer createScheduledTimer(String day, String hour, String minute) {
		String sourceMethod = "createScheduledTimer";
		if (day == null)
			day = "*";
		day = (day == null) ? "*" : day;
		// if (hour == null)
		// hour = "*";
		hour = (hour == null) ? "*" : hour;
		// if (minute == null)
		// minute = "0";
		minute = (minute == null) ? "0" : minute;

		ScheduleExpression schedule = null;
		TimerConfig timerConfig = null;
		/**
		 * get the timer in use
		 */

		/*
		 * if there is not a timer create one
		 */
		if (timer == null) {
			logger.logp(
					Level.INFO,
					sourceClass,
					sourceMethod,
					"\n\t\t Registration Gap Client Version 11.29.2016 14.28 \n\t\t A timer has not been created, creating.... ");

			schedule = new ScheduleExpression();
			schedule.dayOfWeek(day);
			schedule.hour(hour);
			schedule.minute(minute);

			timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);

			timer = timerService.createCalendarTimer(schedule, timerConfig);

		} else {
			/* clear timer and create new one */
			TimerHandle timerHandle = timer.getHandle();
			timer = timerHandle.getTimer();
			System.out.println("Timer exists, canceling");
			timer.cancel();

			schedule = new ScheduleExpression();
			schedule.dayOfWeek(day);
			schedule.hour(hour);
			schedule.minute(minute);

			timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);

			timer = timerService.createCalendarTimer(schedule, timerConfig);
		}

		return timer;

	}

	/**
	 * 
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
			currDate = getFormattedExpDate(currDateStr);
			// retVal = dateFormat.format(currDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new ParseException(
					sourceClass
							+ " MLMS Exception, format of mlms.ffm.access.date should be "
							+ dateFormatStr + " received " + currDateStr, 394);
		}

		return currDate;
		// return dateStr;
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private String getFormattedDateStringTime(Date date) {
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat(
				"dd/MM/yyyy HH:mm:ss");
		String retVal = null;
		if (date != null) {
			retVal = dateSlashFormat.format(date);
		}

		return retVal;
	}

	/**
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getFormattedExpDate(String str) throws ParseException {
		String sourceMethod = "getFormattedDate";
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd/MM/yyyy");
		// dateSlashFormat.setLenient(false);
		SimpleDateFormat dateHyphenFormat = new SimpleDateFormat("yyyy-MM-dd");
		// dateHyphenFormat.setLenient(false);
		Date retVal;
		if (str.indexOf("/") > -1) {
			Calendar cal = dateSlashFormat.getCalendar();
			int month = cal.MONTH;
			if (month > 12) {
				logger.logp(Level.WARNING, sourceClass, sourceMethod,
						"  Month value in date  is out of range " + month);
				throw new ParseException(sourceClass + " " + sourceMethod
						+ " Month value in date  is out of range " + month, 500);
			}

			retVal = dateSlashFormat.parse(str);

		} else if (str.indexOf("-") > -1) {
			retVal = dateHyphenFormat.parse(str);
		} else {
			throw new ParseException("MLMS Exception, format of string " + str
					+ " not recognized ", 500);
		}
		return retVal;
	}

	/**
	 * 
	 * @param strDate
	 * @return
	 * @throws ParseException
	 */
	private Date getMMDDYYYYDate(String strDate) {
		String sourceMethod = "getMMDDYYYYDate";
		String strFormat = "MM/dd/yyyy";
		try {
			return new SimpleDateFormat(strFormat).parse(strDate);
		} catch (ParseException ex) {
			this.logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					"ParseException thrown when parsing " + strDate
							+ " expecting format " + strFormat);
		}
		return null;

	}

	private Date getDDMMMYYYYDate(String strDate) throws ParseException {
		String sourceMethod = "getDDMMMYYYYDate";
		String strFormat = "dd-MMM-yyyy";
		try {
			return new SimpleDateFormat("strFormat").parse(strDate);
		} catch (ParseException ex) {
			this.logger.logp(Level.SEVERE, sourceClass, sourceMethod,
					"ParseException thrown when parsing " + strDate
							+ " expecting format " + strFormat);
		}
		return null;
	}

	private boolean isTrainingCurrent(Date mlmsDefinedExpirationDate,
			Date assignedTrainingExpirationDate)
			throws RegistrationGapException {
		String sourceMethod = "isTrainingCurrent";
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-YYYY");

		boolean retVal = false;
		if (mlmsDefinedExpirationDate != null
				&& assignedTrainingExpirationDate != null) {
			if (isDebug) {
				logger.logp(
						Level.INFO,
						sourceClass,
						sourceMethod,
						" System Exp date "
								+ dateFormat.format(mlmsDefinedExpirationDate)
								+ " user attr exp date "
								+ dateFormat
										.format(assignedTrainingExpirationDate));
			}
			if (assignedTrainingExpirationDate.after(mlmsDefinedExpirationDate)) {
				retVal = true;
			} else if (assignedTrainingExpirationDate
					.equals(mlmsDefinedExpirationDate)) {
				retVal = true;
			}
			return retVal;
		} else {
			throw new RegistrationGapException("Exception " + sourceClass + " "
					+ " null value passed ");
		}

	}

	/**
	 * return true if expiration date matches current training date
	 * 
	 * @param roleAttribute
	 * @return
	 * @throws RegistrationGapException
	 * @throws ParseException
	 */
	private boolean checkTrainingComplete(RoleAttributeType roleAttribute)
			throws RegistrationGapException, ParseException {
		String sourceMethod = "checkTrainingComplete";

		boolean retVal = false;
		Date userTrainingExpDate = null;
		if (roleAttribute.getValue() != null) {
			String eidmTrainingExpDtStr = roleAttribute.getValue();

			userTrainingExpDate = this
					.getFormattedExpDate(eidmTrainingExpDtStr);

			// compare dates

			retVal = this.isTrainingCurrent(this.getCurrentTrainingDate(),
					userTrainingExpDate);
			if (isDebug) {
				logger.logp(
						Level.INFO,
						sourceClass,
						sourceMethod,
						" attribute name " + roleAttribute.getName()
								+ " attribute value "
								+ roleAttribute.getValue() + " returning "
								+ retVal);
			}
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

}

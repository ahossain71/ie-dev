package gov.hhs.cms.registrationgap.servlet;

import gov.cms.cciio.common.util.Constants;
import gov.cms.cciio.mlms.ws.registrationgap.ejb.schedule.RegistrationGapClientSession;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributeType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributesType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleInfoType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RolesType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserInfoV6Type;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserProfileV6Type;
import gov.hhs.cms.eidm.ws.waas.service.WaaSApplicationService;
import gov.hhs.cms.registrationgap.dto.RegistrationGapUserDTO;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import cms.cciio.ws.registrationgap.exception.RegistrationGapException;


/**
 * Servlet implementation class RetrieveUserAppDetailsServlet
 */
@WebServlet(name = "RetrieveUserAppDetailsServlet", urlPatterns = { "/EIDMUserProfileQueryform" })
public class RetrieveUserAppDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sourceClass = "RetrieveUserAppDetailsServlet";
	// private Logger logger;
	ServletContext context = null;

	@Override
	public void init(ServletConfig config) {
		context = config.getServletContext();

		// logger = Logger
		// .getLogger(RetrieveUserAppDetailsServlet.class.getName());
		// logger.addHandler(new ConsoleHandler());
		// logger.log(Level.INFO, " logger initialized in init() " );

	}

	public void goGet(HttpServletRequest request, HttpServletResponse response) {
		this.doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String sourceMethod = "doPost";
		String username = request.getParameter("username");
		response.setContentType("text/html");

		
		RegistrationGapUserDTO dto = new RegistrationGapUserDTO();

		RetrieveAppDetailsResponseType waasResponse = null;
		try {
			/*
			 * if(logger == null){ this.init();
			 * 
			 * logger.log(Level.INFO, " initialized logger in doPost() " ); }
			 * logger.log(Level.INFO, methodName + " username = " + username);
			 */

			waasResponse = getAppDetails(username);
		} catch (IOException e) {

			System.out.println("82 IOException Thrown " + e.getMessage());
		} catch (RetrieveAppDetailsFault e) {
			System.out.println("84 RetrieveAppDetailsFault Thrown "
					+ e.getMessage());
		} catch (RegistrationGapException e) {
			System.out.println("86 RegistrationGapException Thrown "
					+ e.getMessage());
		}
		try {
			if (waasResponse == null) {

				dto.setUsername(username);

				dto.setResponseWasNull(true);

				dto.setErrorMessage("WaasApplicationInfoV6 response was null ");
				this.populateDTOwithTestData(dto);

				System.out
						.println("WaasApplicationInfoV6 response was null for user "
								+ dto.getUsername());
				request.setAttribute("userDTO", dto);

				System.out.println(" Getting request dispatcher");

				RequestDispatcher rd = context
						.getRequestDispatcher("/view.jsp");

				System.out.println(" Forwarding data to view ");

				rd.forward(request, response);
			} else {
				dto = this.parseAppDetailsResponseType(waasResponse);

				request.setAttribute("userDTO", dto);

				System.out.println(" Getting request dispatcher");

				RequestDispatcher rd = context
						.getRequestDispatcher("/view.jsp");

				System.out.println(" Forwarding data to view ");

				rd.forward(request, response);

				System.out.println(" Completed Forward");
			}

		} catch (ServletException e) {
			System.out.println("118 ServletException Thrown " + e.getMessage());

		} catch (ParseException e) {
			System.out.println("122 ParseException thrown " + e.getMessage());
		} catch (Exception e) {
			System.out.println("124 Exception thrown " + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Maks Web Service call to WaaSApplicationV6
	 * 
	 * @param username
	 * @return
	 * @throws RegistrationGapException
	 * @throws RetrieveAppDetailsFault
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	private RetrieveAppDetailsResponseType getAppDetails(String username)
			throws RegistrationGapException, MalformedURLException,
			RetrieveAppDetailsFault {
		String methodName = "getAppDetails";
		WaaSApplicationService service = new WaaSApplicationService();

		RetrieveAppDetailsResponseType response = null;

		if (username != null) {

			/*
			 * WEB SERVICE CALL
			 */

			// logger.entering(username, methodName, username);
			response = service.retrieveApplicationDetails(username);
			if (response == null) {
				System.out
						.println(sourceClass + " WaaSAppV6 Response is null ");
			} else {
				System.out.println(sourceClass
						+ "WaaSAppV6 Response is not null, userid = "
						+ response.getUserProfile().getUserInfo().getUserId() 
			+ ", first name = "
						+ response.getUserProfile().getUserInfo()
								.getFirstName()
				+", last name = "
								+ response.getUserProfile().getUserInfo()
										.getLastName()+", GUID "
						+ response.getUserProfile().getUserInfo().getGuid()+"\n, Account Create Date = "
								+ response.getUserProfile().getUserInfo()
										.getAccountCreateDate() + ", LOA = " +response.getUserProfile().getUserInfo().getLoa());
			}

		} else {
			throw new RegistrationGapException("Exception username is null");
		}
		return response;
	}
	/** Create DTO, set userName, firstname, lastname guid, account create date
	 *  call "getUserRoleInfo" 
	 * 
	 * @param response
	 * @return
	 * @throws RegistrationGapException
	 * @throws ParseException
	 */
	private RegistrationGapUserDTO parseAppDetailsResponseType(

	RetrieveAppDetailsResponseType response) throws RegistrationGapException,
			ParseException {
		String sourceMethod = "parseAppDetailsResponseType";
		System.out.println(sourceClass + " " + sourceMethod);
		String userId = null;
		String firstName = null;
		String lastName = null;
		int loa = -1;
		Date accountCreateDate = null;
		XMLGregorianCalendar xgCal = null;
		String guid = null;

		RegistrationGapUserDTO dto = new RegistrationGapUserDTO();

		UserProfileV6Type userProfile = getUserProfileV6Type(response);

		UserInfoV6Type userInfo = this.getUserInfoV6Type(userProfile);

		if (userInfo != null) {
			userId = userInfo.getUserId();
			if (userId != null) {
				dto.setUsername(userId);
			}
			firstName = userInfo.getFirstName();
			if (firstName != null) {
				dto.setFirstName(firstName);
			}
			lastName = userInfo.getLastName();
			if (lastName != null) {
				dto.setLastName(lastName);
			}
			guid = userInfo.getGuid();
			if (guid != null) {
				dto.setGuid(guid);
			}
			xgCal = userInfo.getAccountCreateDate();
			if (xgCal != null) {
				accountCreateDate = xgCal.toGregorianCalendar().getTime();
				if (accountCreateDate != null) {
					dto.setAccountCreateDate(accountCreateDate);
				}
			}
			dto.setLoa(userInfo.getLoa());
			

		} else {
			System.out.println(" User Info was null");
		}

		/*
		 * populate role data
		 */
		System.out.println(sourceClass + " Getting User Role Info");

		return getUserRoleInfo(dto, response);
	}
	/**
	 * Check if FFM Training Access role is assigned, then set historical role
	 * grant date check role attribute data
	 * 
	 * @param dto
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws RegistrationGapException
	 */
	private RegistrationGapUserDTO getUserRoleInfo(RegistrationGapUserDTO dto,
			RetrieveAppDetailsResponseType response)
			throws RegistrationGapException, ParseException {
		String sourceMethod = "getUserRoleInfo";
		System.out.println(sourceClass + " " + sourceMethod);
		
		RegistrationGapUserDTO returnDto = dto;
		
		RolesType rolesType = null;
		
		RoleInfoType roleInfoType = null;
		List<RoleInfoType> roleList = null;
		Iterator<RoleInfoType> it = null;

		rolesType = this.getRolesType(response);
		if (rolesType != null) {
			roleList = rolesType.getRoleInfo();
			returnDto.setRoleList(roleList);
			System.out.println(sourceClass + " " + sourceMethod
					+ " length of role list " + roleList.size());
			it = roleList.iterator();

			while (it.hasNext()) {
				roleInfoType = it.next();
				System.out.println(sourceClass + " " + sourceMethod
						+ " Role Name " + roleInfoType.getRoleName()
						+ ", role grant date "
						+ roleInfoType.getRoleGrantDate());

				if (roleInfoType != null
						&& isFFMTrainingAccess(roleInfoType)) {
					returnDto.setFfmTrainingAccess(true);
					//returnDto = this.setHistoricalRoleGrantDate(returnDto,
					//		roleInfoType);

				}
				if (roleInfoType != null
						&& this.isFFMAgentBroker(roleInfoType)) {
					returnDto.setFfmAgentBroker(true);
					returnDto = this.setHistoricalRoleGrantDate(returnDto,
							roleInfoType);

				}
				if(roleInfoType.getRoleName().equalsIgnoreCase(Constants.FFM_AGENT_BROKER)){
					/*
					 * check role attributes
					 */
					returnDto = this.getRoleAndAttributeData(returnDto,
							roleInfoType);
				}

			}
		} else {
			System.out.println(sourceClass + " " + sourceMethod
					+ " RolesType is null");
		}// rolesType

		return returnDto;
	}
	/**
	 * 
	 * @param response
	 * @return
	 * @throws RegistrationGapException
	 */
	private UserProfileV6Type getUserProfileV6Type(
			RetrieveAppDetailsResponseType response)
			throws RegistrationGapException {
		if (response != null) {
			return response.getUserProfile();
		} else {
			throw new RegistrationGapException(
					"WaaSAppInfoType Response is null");
		}

	}

	/**
	 * 
	 * @param userProfileV6Type
	 * @return
	 * @throws RegistrationGapException
	 */
	private UserInfoV6Type getUserInfoV6Type(UserProfileV6Type userProfileV6Type)
			throws RegistrationGapException {
		if (userProfileV6Type != null) {
			return userProfileV6Type.getUserInfo();
		} else {
			throw new RegistrationGapException("UserProfileV6Type is null");
		}

	}

	/**
	 * 
	 */
	private RolesType getRolesType(RetrieveAppDetailsResponseType response) {

		return response.getRolesInfo();
	}

	

	/**
	 * 
	 * @param roleInfoType
	 * @return
	 */
	private Boolean isFFMTrainingAccess(RoleInfoType roleInfoType) {
		String sourceMethod = "isFFMTrainingAccess";
		Boolean retVal = false;

		if (roleInfoType != null) {
			String roleName = roleInfoType.getRoleName().toLowerCase();

			if (roleName != null
					&& roleName
							.equalsIgnoreCase(gov.cms.cciio.common.util.Constants.FFM_TRAINING_ACCESS)
					|| (roleName
							.indexOf(gov.cms.cciio.common.util.Constants.FFM_TRAINING_ACCESS) > -1)) {
				System.out.println(sourceClass + " " + sourceMethod
						+ " Role FFM_TRAINING_ACCESS " + roleName);
				retVal = true;
			}
		}
		return retVal;
	}
	/*
	 * 
	 */
	private Boolean isFFMAgentBroker(RoleInfoType roleInfoType) {
		String sourceMethod = "isFFMAgentBroker";
		Boolean retVal = false;

		if (roleInfoType != null) {
			String roleName = roleInfoType.getRoleName().toLowerCase();

			if (roleName != null
					&& roleName
							.equalsIgnoreCase(Constants.FFM_AGENT_BROKER)
					|| (roleName.toLowerCase()
							.indexOf(Constants.FFM_AGENT_BROKER) > -1)) {
				System.out.println(sourceClass + " " + sourceMethod
						+ " Role  " + Constants.FFM_AGENT_BROKER + " "  + roleName);
				retVal = true;
			}
		}
		return retVal;
	}

	/**
	 * 
	 * @param dto
	 * @param roleInfoType
	 * @return
	 */
	private RegistrationGapUserDTO setHistoricalRoleGrantDate(
			RegistrationGapUserDTO dto, RoleInfoType roleInfoType) {
		String sourceMethod = "setHistoricalRoleGrantDate";
		RegistrationGapUserDTO returnDto = dto;
		if (roleInfoType.getRoleGrantDate() != null) {
			returnDto.setHistoricalRoleGrantDate(roleInfoType
					.getRoleGrantDate().toGregorianCalendar().getTime());
			System.out.println(sourceClass
					+ " "
					+ sourceMethod
					+ " Historical Role Grant Date  :"
					+ roleInfoType.getRoleGrantDate().toGregorianCalendar()
							.getTime());
			System.out.println(sourceClass + " " + sourceMethod
					+ " Historical Role Grant Date day-month-year "
					+ roleInfoType.getRoleGrantDate().getDay() + "-"
					+ roleInfoType.getRoleGrantDate().getMonth() + "-"
					+ roleInfoType.getRoleGrantDate().getYear());
					

		}
		return returnDto;
	}

	/**
	 * 
	 * @param roleInfotype
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	private RegistrationGapUserDTO getRoleAndAttributeData(
			RegistrationGapUserDTO dto, RoleInfoType roleInfoType)
			throws RegistrationGapException, ParseException {
		String sourceMethod = "getRoleAndAttributeData";
		
		RegistrationGapUserDTO returnDto = dto;
		Date shopTrainingExpDate = null;
		Date userTrainingExpDate = null;
		RoleAttributesType roleAttributesType = null;
		List<RoleAttributeType> roleAttributeList = null;
		Iterator<RoleAttributeType> it = null;
		
		RoleAttributeType roleAttributeType = null;
		String rollAttributeName = null;
		String rollAttributeValue = null;
		
		if (roleInfoType != null) {

			 roleAttributesType = roleInfoType
					.getRoleAttributes();
			 
			 roleAttributeList = roleAttributesType
					.getAttribute();
			 returnDto.setRoleAttributeList(roleAttributeList);
			 it = roleAttributeList.iterator();
			
			while (it.hasNext()) {
				roleAttributeType = it.next();

				rollAttributeName = roleAttributeType.getName().trim();
				rollAttributeValue = roleAttributeType.getValue();
				System.out.println(sourceClass + " " + sourceMethod
						+ " Role Attribute Name " + rollAttributeName);
				System.out.println(sourceClass + " " + sourceMethod
						+ " Role Attribute value " + rollAttributeValue);

				/*
				 * check if FFM_Training role is present
				 */
				if (rollAttributeName.equalsIgnoreCase(Constants.FFM_TRAINING)) {

					userTrainingExpDate = this
							.getFormattedDate(roleAttributeType.getValue());
					System.out.println(sourceClass + " " + sourceMethod
							+ "\n FFMTraining attribute found, expiration date =  " + userTrainingExpDate + ", current system expiration date " + this.getCurrentTrainingDate());
					returnDto.setFFMExpirationDate(userTrainingExpDate);
					if (this.isTrainingCurrent(this.getCurrentTrainingDate(),
							userTrainingExpDate)) {
						returnDto.setFFMComplete(true);
						
						System.out.println(sourceClass + " " + sourceMethod
								+ " DTO FFMTraining complete "
								+ returnDto.isFFMComplete()
								+ " expiration date "
								+ returnDto.getFFMExpirationDate());

					}
				}
				if (rollAttributeName.equalsIgnoreCase(Constants.SHOP_TRAINING)) {
					shopTrainingExpDate = this
							.getFormattedDate(roleAttributeType.getValue());
					System.out.println(sourceClass + " " + sourceMethod
							+ "\n SHOPTraining attribute found, expiration date =  " +shopTrainingExpDate+ ", current system expiration date " + this.getCurrentTrainingDate());
					returnDto.setShopExpirationdate(shopTrainingExpDate);
					if (this.isTrainingCurrent(this.getCurrentTrainingDate(),
							shopTrainingExpDate)) {
						returnDto.setShopComplete(true);
						
						System.out.println(sourceClass + " " + sourceMethod
								+ " DTO SHOPTraining complete "
								+ returnDto.isShopComplete()
								+ " expiration date "
								+ returnDto.getShopExpirationdate());

					}
				}
				if (rollAttributeName
						.equalsIgnoreCase(Constants.AB_ROLE_EFFECTIVE_DATE)) {
					Date agentBrokerEffectiveDate = this
							.getMMddYYYYFormattedDate(roleAttributeType.getValue());
					
					returnDto
							.setAgentBrokerEffectiveDate(agentBrokerEffectiveDate);
					returnDto.setRawAgentBrokerEffectiveDate(roleAttributeType.getValue());
					System.out.println(sourceClass + " " + sourceMethod
							+ " AgentBrokerEffectiveDate found "
							+ rollAttributeName
							+ " expiration date "
							+ returnDto.getShopExpirationdate());
				}
			}
		}

		return returnDto;
	}
	/**
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getMMddYYYYFormattedDate(String str) throws ParseException {
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
			currDate = getFormattedDate(currDateStr);
			// retVal = dateFormat.format(currDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new ParseException(
					RegistrationGapClientSession.class.getSimpleName()
							+ " MLMS Exception, format of mlms.ffm.access.date should be "
							+ dateFormatStr + " received " + currDateStr, 394);
		}

		return currDate;
		// return dateStr;
	}

	/**
	 * 
	 * @param mlmsDefinedExpirationDate
	 * @param assignedTrainingExpirationDate
	 * @return
	 * @throws Exception
	 */
	private boolean isTrainingCurrent(Date mlmsDefinedExpirationDate,
			Date assignedTrainingExpirationDate)
			throws RegistrationGapException {
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
			return retVal;
		}
		throw new RegistrationGapException("Exception "
				+ RetrieveUserAppDetailsServlet.class.getSimpleName() + " "
				+ " null value passed ");

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * for testing purposes only
	 */
	private RegistrationGapUserDTO populateDTOwithTestData(
			RegistrationGapUserDTO dto) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date hDate = format.parse("2014-10-31");
		Date eDate = format.parse("2016-07-24");
		Date aDate = format.parse("2014-10-25");
		Date xDate = format.parse("2016-10-31");

		dto.setFirstName("TestData");
		dto.setLastName("Tester");
		dto.setHistoricalRoleGrantDate(hDate);
		dto.setAgentBrokerEffectiveDate(eDate);
		dto.setAccountCreateDate(aDate);
		dto.setGuid("[Global Unique Identifier] ");
		dto.setFFMComplete(false);
		dto.setFFMExpirationDate(xDate);
		return dto;
	}

}

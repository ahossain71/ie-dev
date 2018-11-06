package util.mlms.cciio.cms.mlmsshopuserprofile.dao;

import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.AddressType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.AgencyInfoType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.DayofWeekAvailabilityType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.DaysOfWeekType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.LanguageType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.SearchIndicatorType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.TimeType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UsStateType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserPreferencesType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.WorkingDaysType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.WorkingHoursType;
import gov.mlms.cciio.cms.shopuserprofile.exception.ShopUserProfileConnectionException;
import gov.mlms.cciio.cms.shopuserprofile.exception.ShopUserProfileException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class MLMSShopUserProfileDAO {
	boolean isDebug = System.getProperty("mlms.debug", "true").equalsIgnoreCase("true");
	/**
	 * <complexType name="UserType"> <attribute name="UserID"> <simpleType>
	 * <restriction base="string"> <minLength value="1"></minLength> <maxLength
	 * value="36"></maxLength> </restriction> </simpleType> </attribute>
	 * <attribute name="FirstName"> <simpleType> <restriction base="string">
	 * <minLength value="1"></minLength> <maxLength value="20"></maxLength>
	 * </restriction> </simpleType> </attribute> <attribute name="ActiveStatus">
	 * <simpleType> <restriction base="string"> <enumeration
	 * value="active"></enumeration> <enumeration
	 * value="inactive"></enumeration> </restriction> </simpleType> </attribute>
	 * <attribute name="LastName"> <simpleType> <restriction base="string">
	 * <minLength value="1"></minLength> <maxLength value="25"></maxLength>
	 * </restriction> </simpleType> </attribute>
	 * 
	 * 
	 * <attribute name="npn"> <simpleType> <restriction base="string">
	 * <minLength value="1"></minLength> <maxLength value="10"></maxLength>
	 * </restriction> </simpleType> </attribute> </complexType>
	 * 
	 * 
	 * @throws ShopUserProfileException
	 * @throws ShopUserProfileConnectionException 
	 * @throws ParseException 
	 */
	

	public UserType getUserTypeData(String userName)
			throws ShopUserProfileException, ShopUserProfileConnectionException, ParseException {
		String methodName = "getUserTypeData";
		this.getClass().getName();
		
		
		
		System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " "
				+ methodName);
		UserType userType = new UserType();
		userType.setUserID(userName.toUpperCase());

		String sql = "select custom1 as \"npn\",  fname as \"FirstName\", lname as \"LastName\", 'active' as \"ActiveStatus\""
				+ " from cmt_person where username = ?";

		Connection con = this.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		con = this.getConnection();
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, userName.toUpperCase());

			rs = pstmt.executeQuery();
			
			int count = 0;
			
			while (rs.next()) {
				count++;
				String activeStatus = rs.getString("ActiveStatus");
				String firstName = rs.getString("FirstName");
				String lastName = rs.getString("LastName");
				String npn = rs.getString("npn");
				if(isDebug){
					System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " " + methodName +
							"\nActive status " + activeStatus +
							"\nfirst name " + firstName +
							"\nlast name " + lastName +
							"\nnpn " + npn);
				}

				if (activeStatus == null ) {
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for ActiveStatus found for "
									+ userName);
				} else {
					
					userType.setActiveStatus(activeStatus);
				}
				/**
				 * firstName and lastName cannot be null in Saba
				 */
				userType.setFirstName(firstName);

				userType.setLastName(lastName);
				
				if (npn == null ) {
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for npn found for " + userName);
				} else {
					userType.setNpn(npn);
				}

			}
			if(count==0){
				throw new ShopUserProfileException(ErrorCodeType.ME_800);
			}
		} catch (SQLException e) {

			System.out.println(MLMSShopUserProfileDAO.class.getSimpleName()
					+ " " + methodName + "Exception thrown " + e.getMessage());
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new ShopUserProfileException(
						"Datasource resources closing error!", e);
			}
		}

		return getActiveStatus(userType);
	}
	/**
	 * 
	 * @param userType
	 * @return
	 * @throws ParseException
	 * @throws ShopUserProfileException
	 * @throws ShopUserProfileConnectionException
	 */
	public UserType getActiveStatus(UserType userType) throws ParseException, ShopUserProfileException, ShopUserProfileConnectionException{
		String methodName = "getActiveStatus";
		userType.getUserID();
		String currDateStr = System.getProperty("mlms.ffm.access.date", "2016-10-31");
		
		Date expDate = this.getFormattedDate(currDateStr );
		
		String sql = "select count(stud.owner_id) from tpt_ce_stud_certification stud " +
					 " inner join TPT_EXT_CE_CERTIFICATION c on c.id = stud.certification_id " +
					 " where c.custom3 = 'SHOP Marketplace' and c.inherited = '0'  and stud.status = 100" + 
					 " and stud.expired_on >= ? and stud.owner_id = (select id from cmt_person where username = upper(?))";
		 Connection con = this.getConnection();
			PreparedStatement pstmt = null;
			ResultSet rs = null;
		    int recordcount = 0;
		 try {
			
				pstmt = con.prepareStatement(sql);
				pstmt.setDate(1, new java.sql.Date(expDate.getTime()));
				pstmt.setString(2, userType.getUserID().toUpperCase());
				rs = pstmt.executeQuery();
				
				while(rs.next()){
					recordcount = rs.getInt(1);
					
				}
				if(recordcount > 0){
					userType.setActiveStatus("active");
				} else {
					userType.setActiveStatus("inactive");
				}
				if(isDebug){
					System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " " + methodName +
							"\nActive record count " + recordcount +
							"\nCurrent System Date " + currDateStr +
							"\nActive status " + userType.getActiveStatus() +
							"\nUsername " + userType.getUserID() );
				}
		 } catch (SQLException e) {
				throw new ShopUserProfileException(methodName + e.getMessage(), e);
			} finally {

				try {
					if (rs != null)
						rs.close();
					if (pstmt != null)
						pstmt.close();
					if (con != null)
						con.close();
				} catch (SQLException e) {
					System.out
							.println("SQLException: Datasource resources closing error!");
					throw new ShopUserProfileException(
							"Datasource resources closing error!", e);
				}
			}
					
		return userType;
	}

	public AddressType getAddressTypeData(String userName)
			throws ShopUserProfileException, ShopUserProfileConnectionException {
		String methodName = "getAddressTypeData";
		AddressType addressType = new AddressType();
		UsStateType usStateType = null;

		String sql = "select ind_org_addr, ind_org_city, ind_org_state, ind_org_zip, ind_org_email, ind_org_phone from AD_MLMS_PERSON_AGENT_INFO i "
				+ " inner join cmt_person p on p.id = i.person_id where p.username = ? ";
		Connection con = this.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, userName.toUpperCase());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String streetAddress = rs.getString("ind_org_addr");
				String city = rs.getString("ind_org_city");
				String state = rs.getString("ind_org_state");
				String zip = rs.getString("ind_org_zip");
				String email = rs.getString("ind_org_email");
				String phone = rs.getString("ind_org_phone");
				
				if(isDebug){
					System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " " + methodName +
							"\nStreet Address " + streetAddress +
							"\nCity " + city +
							"\nState " + state +
							"\nZip " + zip +
							"\nEmail " + email +
							"\nPhone " + phone);
							
				}
				
				if(streetAddress == null) {
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for Street Address found for " + userName);
				} else {
					addressType.setStreetAddress(streetAddress);
				}
				
				if(city == null){
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for City found for " + userName);
				} else {
					addressType.setCity(city);
				}

				if(state == null){
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for State found for " + userName);
				} else {
					usStateType = UsStateType.fromValue(state);
					addressType.setState(usStateType);
				}

				
				if(zip ==  null){
					throw new ShopUserProfileException(
							ErrorCodeType.ME_840.value()+" No database value for Zip Code found for " + userName);
				} else {
					addressType.setZipCode(zip);
				}
				
				if(email == null){
					throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for Email found for " + userName);
				} else {
					addressType.setEmailAddress(email);
				}
				
			    if(phone == null){
			    	throw new ShopUserProfileException(ErrorCodeType.ME_840.value()+
							" No database value for Phone found for " + userName);
			    } else {
			    	addressType.setPhoneNumber(phone);
			    }
				

			}
		} catch (SQLException e) {
			throw new ShopUserProfileException(methodName + e.getMessage(), e);
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new ShopUserProfileException(
						"Datasource resources closing error!", e);
			}
		}

		return addressType;
	}

	public AgencyInfoType getAgencyInfoType(String userName)
			throws ShopUserProfileException, ShopUserProfileConnectionException {
		String methodName = "getAgencyInfoType";

		AgencyInfoType agencyInfoType = new AgencyInfoType();

		String sql = "select SHOP_NAME as \"AgencyName\", SHOP_URL as \"AgencyUrl\" from AD_MLMS_PERSON_AGENT_INFO AI "
				+ "inner join cmt_person p on p.id = ai.person_id "
				+ "where p.username = ? ";

		Connection con = this.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, userName.toUpperCase());
			rs = pstmt.executeQuery();

			String agencyName = null;
			String agencyUrl = null;
			
			

			while (rs.next()) {
				agencyName = rs.getString("AgencyName");
				agencyUrl = rs.getString("AgencyUrl");
				
				if(isDebug){
					System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " " + methodName +
							"\nAgency Name " + agencyName +
							"\nAgency URL " + agencyUrl );
							
				}

				if (agencyName == null ) {
					throw new ShopUserProfileException(
							" No database value for the Agency Name found for "
									+ userName);
				} else {
					agencyInfoType.setAgencyName(agencyName);
				}
				
				if (agencyUrl == null || agencyUrl.isEmpty()) {
					throw new ShopUserProfileException(
							" No database value for the Agency URL found for "
									+ userName);
				} else {
					agencyInfoType.setAgencyUrl(agencyUrl);
				}
				
				
			}

		} catch (SQLException e) {
			throw new ShopUserProfileException(methodName + e.getMessage(), e);
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new ShopUserProfileException(
						"Datasource resources closing error!", e);
			}
		}
		return agencyInfoType;
	}

	public UserPreferencesType getUserPreferencesTypeData(String userName)
			throws ShopUserProfileException, ParseException, ShopUserProfileConnectionException {
		String methodName = "getUserPreferencesTypeData";

		UserPreferencesType userPreferencesType = new UserPreferencesType();

		String sql = "select p.username, ai.person_id, ai.WORKING_HOURS, ai.WORKING_DAYS, "
				+ "ai.PREFERRED_LANG, "
				+ "ai.METHOD_CONTACT, ai.DISP_SHOP_CONTACT from AD_MLMS_PERSON_AGENT_INFO AI "
				+ "inner join cmt_person p on p.id = ai.person_id "
				+ "where p.username = ? ";

		Connection con = this.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, userName.toUpperCase());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String workingHoursStr = rs.getString("WORKING_HOURS");
				String workingDaysStr = rs.getString("WORKING_DAYS");
				String preferredLang = rs.getString("PREFERRED_LANG");
				String contactMethod = rs.getString("METHOD_CONTACT");
				if(isDebug){
					System.out.println(MLMSShopUserProfileDAO.class.getSimpleName() + " " + methodName +
							"\nWorking hours string " + workingHoursStr +
							"\nWorking days string " + workingDaysStr +
							"\nPreferred Language " + preferredLang +
							"\nContactMethod " + contactMethod );
							
				}

				/*
				 * set working hours
				 */
				if (workingHoursStr != null) {
					userPreferencesType.setWorkingHours(this
							.getWorkingHoursType(workingHoursStr));
				} else {
					throw new ShopUserProfileException(
							" No database value for workingHours found for "
									+ userName);
				}
				/*
				 * set working days selection
				 */
				if (workingDaysStr != null) {
					userPreferencesType.setWorkingDays(this
							.getDayOfWeekType(workingDaysStr));
				} else {
					throw new ShopUserProfileException(
							" No database value for workingDays found for "
									+ userName);
				}
				/*
				 * set preferred language type, DB stores Email or Mailing, SHOP
				 * expects ' address'
				 */
				if (preferredLang == null || preferredLang.isEmpty()) {
					throw new ShopUserProfileException(
							" No database value for preferred language found for "
									+ userName);
				} else {
					userPreferencesType
							.setPreferredLanguage(getLanguageType(preferredLang));
				}
				if (contactMethod == null || contactMethod.isEmpty()) {
					throw new ShopUserProfileException(
							" No database value for contact method found for "
									+ userName);
				} else {
					StringBuffer contactMethodBuff = new StringBuffer(
							contactMethod);
					contactMethodBuff.append(" address");
					userPreferencesType
							.setPreferredContactMethod(contactMethodBuff
									.toString());
				}
				/*
				 * set shop display preferences, database holds DoNotAuthorize
				 * or Authorize SHOP expects yes or no
				 */
				userPreferencesType.setSearchIndicator(this
						.getSearchIndicatorType(rs
								.getString("DISP_SHOP_CONTACT")));

			}

		} catch (SQLException e) {
			throw new ShopUserProfileException(methodName + e.getMessage(), e);
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new ShopUserProfileException(
						"Datasource resources closing error!", e);
			}
		}
		return userPreferencesType;
	}

	/**
	 * 
	 * @param indicatorStr
	 * @return
	 */
	private SearchIndicatorType getSearchIndicatorType(String indicatorStr) {
		SearchIndicatorType searchIndicatorType = SearchIndicatorType.YES;
		String DO_NOT_AUTHORIZE = new String("donotauthorize");
		if (indicatorStr != null && !indicatorStr.isEmpty()) {
			if (indicatorStr.equalsIgnoreCase(DO_NOT_AUTHORIZE)) {
				searchIndicatorType = SearchIndicatorType.NO;
			}
		}
		return searchIndicatorType;
	}

	/**
	 * returns English by default
	 * 
	 * @param languageStr
	 * @return
	 */
	private LanguageType getLanguageType(String languageStr) {
		String es_ES = "es_es";
		String English = "English";
		String Spanish = "Spanish";
		LanguageType lang = LanguageType.fromValue(English);
		if (languageStr != null & !languageStr.isEmpty()) {

			if (languageStr.equalsIgnoreCase(es_ES)) {
				lang = LanguageType.fromValue(Spanish);
			}
		}

		return lang;
	}

	/**
	 * 
	 * @param workingHoursStr
	 * @return
	 * @throws ParseException
	 */
	private WorkingHoursType getWorkingHoursType(String workingHoursStr)
			throws ParseException {
		WorkingHoursType workingHoursType = new WorkingHoursType();
		TimeType fromTime = new TimeType();
		TimeType toTime = new TimeType();
		if (workingHoursStr != null && !workingHoursStr.isEmpty()) {
			String fromStr = workingHoursStr.substring(0, 7);
			String toStr = workingHoursStr.substring(8, 15);
			fromTime.setTime(formatTimeStr(fromStr));

			toTime.setTime(formatTimeStr(toStr));
		}

		workingHoursType.setFrom(fromTime);
		workingHoursType.setTo(toTime);
		return workingHoursType;
	}

	private String formatTimeStr(String timeStr) throws ParseException {
		String methodName = "formatTimeStr";
		SimpleDateFormat inFormat = new SimpleDateFormat("hh:mma");
		SimpleDateFormat outFormat = new SimpleDateFormat("hh:mm a");
		String formattedTimeStr = null;

		if (timeStr != null) {

			Date time = inFormat.parse(timeStr);
			formattedTimeStr = outFormat.format(time);
		} else {
			throw new ParseException(
					MLMSShopUserProfileDAO.class.getSimpleName() + methodName
							+ " null time string passed", -1);
		}

		return formattedTimeStr;
	}

	
	/**
	 * 
	 * @param workingDaysStr
	 * @return
	 */
	private DaysOfWeekType getDayOfWeekType(String workingDaysStr) {
		DaysOfWeekType dayOfWeekType = new DaysOfWeekType();
		String lwrWorkingDaysStr = workingDaysStr.toLowerCase();
		System.out.println(" working days string " + lwrWorkingDaysStr);
		if (lwrWorkingDaysStr.indexOf("mon") > -1) {
			dayOfWeekType.setMonday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setMonday(DayofWeekAvailabilityType.UNAVAILABLE);
		}
		if (lwrWorkingDaysStr.indexOf("tue") > -1) {
			dayOfWeekType.setTuesday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setTuesday(DayofWeekAvailabilityType.UNAVAILABLE);

		}
		if (lwrWorkingDaysStr.indexOf("wed") > -1) {
			dayOfWeekType.setWednesday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setWednesday(DayofWeekAvailabilityType.UNAVAILABLE);
		}
		if (lwrWorkingDaysStr.indexOf("thu") > -1) {
			dayOfWeekType.setThursday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setThursday(DayofWeekAvailabilityType.UNAVAILABLE);
		}
		if (lwrWorkingDaysStr.indexOf("fri") > -1) {
			dayOfWeekType.setFriday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setFriday(DayofWeekAvailabilityType.UNAVAILABLE);
		}
		if (lwrWorkingDaysStr.indexOf("sat") > -1) {
			dayOfWeekType.setSaturday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setSaturday(DayofWeekAvailabilityType.UNAVAILABLE);
		}
		if (lwrWorkingDaysStr.indexOf("sun") > -1) {
			dayOfWeekType.setSunday(DayofWeekAvailabilityType.AVAILABLE);
		} else {
			dayOfWeekType.setSunday(DayofWeekAvailabilityType.UNAVAILABLE);
		}

		return dayOfWeekType;
	}
	/**
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Date getFormattedDate(String str) throws ParseException{
		SimpleDateFormat dateSlashFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dateHyphenFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date retVal;
		if(str.indexOf("/")>-1){
			retVal = dateSlashFormat.parse(str);
		} else if(str.indexOf("-")>-1){
			retVal = dateHyphenFormat.parse(str);
		} else {
			throw new ParseException("MLMS Exception, format of string " + str + " not recognized ", 371);
		}
	    return retVal;
	}

	private Connection getConnection() throws ShopUserProfileException, ShopUserProfileConnectionException {

		Connection con = null;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
			con = ds.getConnection();

		} catch (NamingException e) {
			System.out.println("JNDI NamingException! Check output console");
			throw new ShopUserProfileConnectionException("DB_CONNECT_EXCEPTION", e);
		} catch (SQLException e) {
			System.out.println("SQLException! Check output console");
			throw new ShopUserProfileException("DB_CONNECT_EXCEPTION", e);
		}
		return con;
	}
}

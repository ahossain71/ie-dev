package util.mlms.cciio.cms.mlmsshopuserprofile.dto;

import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.AddressType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.AgencyInfoType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.DayofWeekAvailabilityType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.DaysOfWeekType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserPreferencesType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserType;
import gov.mlms.cciio.cms.shopuserprofile.exception.ShopUserProfileConnectionException;
import gov.mlms.cciio.cms.shopuserprofile.exception.ShopUserProfileException;
import gov.mlms.cciio.cms.shopuserprofileresponsetype.ShopUserProfileResponseType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import util.mlms.cciio.cms.mlmsshopuserprofile.dao.MLMSShopUserProfileDAO;
/**
 * Data Transfer object makes calls to DAO layer and assembles the type components of the response.
 * 
 * @author sjmeyer
 *
 */
public class MLMSShopUserProfileDTO {
 
	/**
	 * 
	 * @param username
	 * @return ShopUserProfileResponseType
	 * <complexType name="ShopUserProfileResponseType">
    	<sequence>
    		<element name="UserAddress" type="tns:AddressType"
    			maxOccurs="1" minOccurs="1">
    		</element>
    		<element name="UserInfo" type="tns:UserType" maxOccurs="1"
    			minOccurs="1">
    		</element>
    		<element name="UserPreferences"
    			type="tns:UserPreferencesType">
    		</element>
    	</sequence>
    </complexType>
	 * @throws ShopUserProfileException 
	 * @throws ParseException 
	 */
	public ShopUserProfileResponseType getShopUserProfileResponseType(String username) {
		String methodName = "getShopUserProfileResponseType";
		ShopUserProfileResponseType shopUserProfileResponseType = new ShopUserProfileResponseType();
		String userId = username.toUpperCase();
		System.out.println(methodName + " Retrieve user profile info for " + userId);
		
		
		try {
			shopUserProfileResponseType.setUserInfo(this.getUserType(userId));
			AddressType addressType = this.getAddressType(userId);
			if(addressType.getCity() == null){
				throw new ShopUserProfileException("Exception, Address, city is null");
			} else if(addressType.getStreetAddress() == null){
				throw new ShopUserProfileException("Exception, Address, street is null");
			} else if(addressType.getEmailAddress() == null){
				throw new ShopUserProfileException("Exception, Email Address is null");
			} else if(addressType.getZipCode() == null){
				throw new ShopUserProfileException("Exception, Zip code is null");
			} 
			shopUserProfileResponseType.setUserAddress(this.getAddressType(userId));
			
			
			UserPreferencesType  userPreferencesType =  this.getUserPreferencesType(userId);
			if(userPreferencesType.getPreferredContactMethod() == null || userPreferencesType.getPreferredContactMethod().isEmpty() ){
				throw new ShopUserProfileException("Exception, Preferred Contact Method is null");
			} else if (userPreferencesType.getPreferredLanguage() == null ){
				throw new ShopUserProfileException("Exception, Preferred Language is null");
			} 
			
			userPreferencesType.setAgencyInfo(this.getAgenecyInfoType(userId));
			
			if (userPreferencesType.getAgencyInfo()==null){
				throw new ShopUserProfileException("Exception, Agency Info Object is null");
			} else if (userPreferencesType.getAgencyInfo()!= null && userPreferencesType.getAgencyInfo().getAgencyName() == null){
				throw new ShopUserProfileException("Exception, Agency Name is null");
			}else if (userPreferencesType.getAgencyInfo()!= null && userPreferencesType.getAgencyInfo().getAgencyUrl() == null){
				throw new ShopUserProfileException("Exception, Agency Url is null");
			}
			System.out.println(MLMSShopUserProfileDTO.class.getSimpleName() + " " + methodName+ "\t PreferredLanaguage " + userPreferencesType.getPreferredLanguage().name());
			shopUserProfileResponseType.setUserPreferences(userPreferencesType);
			
			StatusType statusType = new StatusType();
			
			statusType.setStatusCode(StatusCodeType.MS_200);
			statusType.setStatusMessage("Request processed Successfully");
			shopUserProfileResponseType.setStatusInfo(statusType);
			statusType.setErrorCode("");
			statusType.setErrorMessage("");
			statusType.setErrorKey("");
			
		} catch (ShopUserProfileException e) {
			System.out.println(MLMSShopUserProfileDTO.class.getSimpleName() + " " + methodName+"\t ShopUserProfileCaught " + e.getMessage());
			
			StatusType statusType = new StatusType();
			statusType.setErrorCode(ErrorCodeType.ME_820.value());
			System.out.println("ShopUserProfileExpception " + e.getMessage());
			if(e.getMessage().indexOf("800")>-1){
				statusType.setErrorCode(ErrorCodeType.ME_800.value());
				statusType.setErrorMessage("ME_800_USER_ID_DOES_NOT_EXIST_IN_MLMS_DATABASE");
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("Request failed");
				
			} else if(e.getMessage().indexOf("Exception")>-1){
				statusType.setErrorCode(ErrorCodeType.ME_840.value());
				statusType.setErrorMessage("ME_840_REQUIRED_DATA_NOT_FOUND");
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("Request failed");
			} else if(e.getMessage().indexOf("820")>-1){
				statusType.setErrorCode(ErrorCodeType.ME_820.value());
				statusType.setErrorMessage("ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR");
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("Request failed");
			} else {
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("System Unavaiable, see error type");
				statusType.setErrorCode(ErrorCodeType.ME_840.value());
				
				statusType.setErrorMessage(e.getMessage());
			}
			
			shopUserProfileResponseType.setStatusInfo(statusType);
		} catch (ShopUserProfileConnectionException e){
			StatusType statusType = new StatusType();
			statusType.setErrorCode(ErrorCodeType.ME_830.value());
			//ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
			statusType.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE.value());
			statusType.setStatusCode(StatusCodeType.MS_500);
			statusType.setStatusMessage("Request failed");
			shopUserProfileResponseType.setStatusInfo(statusType);
		} catch (ParseException e) {
			StatusType statusType = new StatusType();
			statusType.setErrorCode(ErrorCodeType.ME_820.value());
			//ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
			statusType.setErrorMessage(ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR.value());
			statusType.setStatusCode(StatusCodeType.MS_500);
			statusType.setStatusMessage("Request failed");
			shopUserProfileResponseType.setStatusInfo(statusType);
			e.printStackTrace();
		}
		
		
		
		//need to add AgencyInfoType
		
		
		return shopUserProfileResponseType;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 * @throws ShopUserProfileException 
	 * @throws ShopUserProfileConnectionException 
	 * @throws ParseException 
	 */
	protected UserType getUserType(String userName) throws ShopUserProfileException, ShopUserProfileConnectionException, ParseException{
		String methodName = "getuserType";
		System.out.println(MLMSShopUserProfileDTO.class.getSimpleName() + " " +methodName);
		MLMSShopUserProfileDAO upDAO = new MLMSShopUserProfileDAO();
		 UserType userType = upDAO.getUserTypeData(userName);
		 
		return userType;
	}
   /** <complexType name="AddressType">
    	<sequence>
    		<element name="State" type="Q1:usStateType"></element>
    		<element name="City" type="tns:CityType"></element>
    	</sequence>
    	<attribute name="StreetAddress">
    		<simpleType>
    			<restriction base="string">
    				<minLength value="0"></minLength>
    				<maxLength value="255"></maxLength>
    			</restriction>
    		</simpleType>
    	</attribute>
    	
    	<attribute name="ZipCode">
    		<simpleType>
    			<restriction base="string">
    				<minLength value="0"></minLength>
    				<maxLength value="5"></maxLength>
    			</restriction>
    		</simpleType>
    	</attribute>
    	<attribute name="EmailAddress">
    		<simpleType>
    			<restriction base="string">
    				<minLength value="0"></minLength>
    				<maxLength value="255"></maxLength>
    			</restriction>
    		</simpleType>
    	</attribute>
    	<attribute name="PhoneNumber">
    		<simpleType>
    			<restriction base="string">
    				<minLength value="0"></minLength>
    				<maxLength value="10"></maxLength>
    			</restriction>
    		</simpleType>
    	</attribute>
    </complexType>
    <simpleType name="CityType">
    	<restriction base="string">
    		<minLength value="0"></minLength>
    		<maxLength value="30"></maxLength>
    	</restriction>
    </simpleType>
  */
   /**
    * 
    * @param username
    * @return
 * @throws ShopUserProfileException 
 * @throws ShopUserProfileConnectionException 
    */
   protected AddressType getAddressType(String userName) throws ShopUserProfileException, ShopUserProfileConnectionException{
	   MLMSShopUserProfileDAO upDAO = new MLMSShopUserProfileDAO();
	   AddressType addressType = upDAO.getAddressTypeData(userName);
	   return addressType;
   }
  /** <complexType name="UserPreferencesType">
	<sequence>
		<element name="WorkingHours" type="tns:WorkingHoursType"></element>
		<element name="WorkingDays" type="tns:DaysOfWeekType"></element>
		<element name="PreferredLanguage" type="tns:languageType"></element>
		<element name="PreferredContactMethod">
			<simpleType>
				<restriction base="string">
					<enumeration value="Email address"></enumeration>
					<enumeration value="Mailing address"></enumeration>
				</restriction>
			</simpleType>
		</element>
		<element name="SearchIndicator"
			type="tns:SearchIndicatorType">
		</element>

		<element name="AgencyInfo" type="tns:AgencyInfoType"></element>
	</sequence>
</complexType> **/
   /**
    * 
    * @param username
    * @return UserPreferencesType
 * @throws ShopUserProfileException 
 * @throws ParseException 
 * @throws ShopUserProfileConnectionException 
    */
    protected UserPreferencesType getUserPreferencesType(String username) throws ShopUserProfileException, ParseException, ShopUserProfileConnectionException{
    	 MLMSShopUserProfileDAO upDAO = new MLMSShopUserProfileDAO();
    	 
    	UserPreferencesType userPreferencesType = upDAO.getUserPreferencesTypeData(username);
    	
        return userPreferencesType;
    }
   
    

   
    /**
     * <complexType name="DaysOfWeekType">
    	<sequence>
    		<element name="Monday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Tuesday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Wednesday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Thursday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Friday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Saturday" type="tns:DayofWeekAvailabilityType"></element>
    		<element name="Sunday" type="tns:DayofWeekAvailabilityType"></element>
    	</sequence>
    </complexType>
     * @param String array of days 
     * @return populated DayOfWeekAvailabilityType
     */
    protected List<DaysOfWeekType> getDaysOfWeekType(String[] days){
    	ArrayList<DaysOfWeekType> workingDays = new ArrayList<DaysOfWeekType>(1);
    	DaysOfWeekType daysOfWeekType = new DaysOfWeekType();
    	daysOfWeekType.setMonday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setTuesday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setWednesday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setThursday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setFriday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setSaturday(DayofWeekAvailabilityType.UNAVAILABLE);
    	daysOfWeekType.setSunday(DayofWeekAvailabilityType.UNAVAILABLE);
    	for(String s : days){
    		switch(s.toLowerCase()){
    		case "monday" : daysOfWeekType.setMonday(DayofWeekAvailabilityType.AVAILABLE);
    			break;
    		case "tuesday" : daysOfWeekType.setTuesday(DayofWeekAvailabilityType.AVAILABLE);
    			break;
    		case "wednesday" : daysOfWeekType.setTuesday(DayofWeekAvailabilityType.AVAILABLE);
    			break;
    		case "thursday" : daysOfWeekType.setThursday(DayofWeekAvailabilityType.AVAILABLE);
    		 	break;
    		case "friday" : daysOfWeekType.setFriday(DayofWeekAvailabilityType.AVAILABLE);
    			break;
    		case "saturday" : daysOfWeekType.setSaturday(DayofWeekAvailabilityType.AVAILABLE);
    		 	break;
    		case "sunday" : daysOfWeekType.setSunday(DayofWeekAvailabilityType.AVAILABLE);
    			break;
    		}
    	}
    	
    	workingDays.add(daysOfWeekType);
    	return workingDays;
    }
    /**
     * 
     * @param username
     * @return
     * @throws ShopUserProfileException 
     * @throws ShopUserProfileConnectionException 
     */
    protected AgencyInfoType getAgenecyInfoType(String username) throws ShopUserProfileException, ShopUserProfileConnectionException{
    	 MLMSShopUserProfileDAO upDAO = new MLMSShopUserProfileDAO();
    	 return upDAO.getAgencyInfoType(username);
    	
    }
    	
    
}

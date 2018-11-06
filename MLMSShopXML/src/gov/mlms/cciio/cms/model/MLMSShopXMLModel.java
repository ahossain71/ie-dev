package gov.mlms.cciio.cms.model;

//import java.math.BigInteger;
import gov.mlms.cciio.cms.exception.ShopXMLException;
import gov.mlms.cciio.cms.mlmsshopincrementalfiletype.CompletionInfoType;
import gov.mlms.cciio.cms.mlmsshopincrementalfiletype.FileDescriptionType;
import gov.mlms.cciio.cms.mlmsshopincrementalfiletype.FileTypeInfoType;
import gov.mlms.cciio.cms.mlmsshopincrementalfiletype.ShopRecordType;
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
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.WorkingHoursType;
import gov.mlms.cciio.cms.util.ShopDBUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLMSShopXMLModel{
	public MLMSShopXMLModel(){
		
	}
	
	public FileDescriptionType getShopXMLFile() throws ShopXMLException{
		
		Connection conn = null;
		CallableStatement cstmt = null;
		ResultSet rs = null;
		FileDescriptionType fileDescType = new FileDescriptionType();
		
		Logger log = Logger.getLogger("MLMSShopXMLModel.getShopXMLFile");
		log.log(Level.ALL,"MLMSShopXMLModel.getShopXMLFile Before getting connection");
		try {
			conn = ShopDBUtil.getConnection("MLMSShopXMLModel.getShopXMLFile");
			log.log(Level.ALL,"MLMSShopXMLModel.getShopXMLFile After getting connection");
			/*
			 * AP_MLMS_SHOP_XML(
				  iDt in Date, --1
				  iFileIdentifier out varchar2, --2
				  iFileDateTime out varchar2, --3
				  iFileRecordCount out number, --4
				  iFileTypeInfo out varchar2, --5
				  qCursor out Type_cursor.q_cursor --6
				)
			 */
			log.log(Level.ALL,"MLMSShopXMLModel.getShopXMLFile Before calling procedure");
			cstmt = conn.prepareCall("{ call AP_MLMS_SHOP_XML(?,?,?,?,?,?) }");
			cstmt.setDate(1, null);
			cstmt.registerOutParameter(2, oracle.jdbc.OracleTypes.VARCHAR);
			cstmt.registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR);
			cstmt.registerOutParameter(4, oracle.jdbc.OracleTypes.NUMBER);
			cstmt.registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR);
			cstmt.registerOutParameter(6, oracle.jdbc.OracleTypes.CURSOR);
			cstmt.execute();
			log.log(Level.ALL,"MLMSShopXMLModel.getShopXMLFile After calling procedure");
			
			FileTypeInfoType fileTypeInfo = new FileTypeInfoType();
			fileTypeInfo.setFileType((String) cstmt.getObject(5));
			
			fileDescType.setFileIdentifier((String) cstmt.getObject(2));
			fileDescType.setFileDateTime((String) cstmt.getObject(3));
			fileDescType.setFileRecordCount(BigInteger.valueOf(cstmt.getInt(4)));
			fileDescType.setFileTypeInfo(fileTypeInfo);
			
			rs = (ResultSet) cstmt.getObject(6);
			UserType userTy = null;
			
			AddressType addressTy = null;
			
			UserPreferencesType userPrefTy = null;
			AgencyInfoType agencyTy= null;
			DaysOfWeekType daysOfWeekTy = null;
			TimeType fromTimeTy = null;
			TimeType toTimeTy = null;
			WorkingHoursType workHoursTy = null;
			
			/**
			 * decode(p.status,'Active','active','inactive') as "ActiveStatus", 
                        substr(fname,0,20) as "FirstName",
                        substr(lname,0,25) as "LastName",
                        substr(username,0,36) as "UserID", 
       substr(ind_org_email,0,255) as "EmailAddress",
                      substr(ind_org_phone,0,10) as "PhoneNumber",
                      substr(ind_org_addr,0,255) as "StreetAddress",
                      substr(ind_org_zip,0,5) as "ZipCode",
          ind_org_state,ind_org_city,
        trim(replace(replace(working_hours,substr(working_hours,InStr(working_hours,'-'),length(working_hours) + 1),''),'AM',' AM')) as FromTime,
            trim(replace(replace(working_hours,substr(working_hours,0,InStr(working_hours,'-')),''),'PM', ' PM') ) as ToTime,
          case when InStr(working_days,'Mon') > 0 then 'available' else 'unavailable'  end as "Monday",
            case when InStr(working_days,'Tues') > 0  then 'available' else 'unavailable'   end as "Tuesday",
            case when InStr(working_days,'Wed') > 0  then 'available' else 'unavailable'  end as "Wednesday",
            case when InStr(working_days,'Thu') > 0  then 'available' else 'unavailable'   end as "Thursday",
            case when InStr(working_days,'Fri') > 0  then 'available' else 'unavailable'   end as "Friday",
            case when InStr(working_days,'Sat') > 0  then 'available' else 'unavailable'   end as "Saturday",
          case when InStr(working_days,'Sun') > 0 then 'available' else 'unavailable'  end as "Sunday",
       decode(preferred_lang,'en_US','English','es_ES','Spanish','English') as "PreferredLanguage",
        method_contact,
        'yes' as SearchIndicator,
        lpad(p.custom1,10,'0') as "NPN",
        
       substr(a.bus_org_name,0,60) as "AgencyName",a.IND_ORG_URL as "AgencyURL",
     c.custom3 as ShopAgreementName,
          '100' as  ShopAgreementStatus,
         to_char(s.EXPIRED_ON,'DD-MM-YYYY') as ShopAgreementExpirationDate,
         to_char(s.acquired_on,'DD-MM-YYYY') as ShopAgreemenAcquiredDate
			 */
			String uId = "";
			CompletionInfoType compInfoTy = null;
			ShopRecordType shopRecordTy = null;
			
			while(rs.next()){
				if(!uId.equals((String)rs.getString("UserID"))){
					if(shopRecordTy != null){
						fileDescType.getIncrementalRecord().add(shopRecordTy);
					}
					shopRecordTy = new ShopRecordType();
				}
				
				userTy = new UserType();
				userTy.setActiveStatus(rs.getString("ActiveStatus"));
				userTy.setFirstName(rs.getString("FirstName"));
				userTy.setLastName(rs.getString("LastName"));
				userTy.setUserID(rs.getString("UserID"));
				userTy.setNpn(rs.getString("NPN"));
				
				addressTy = new AddressType();
				addressTy.setCity(rs.getString("ind_org_city"));
				addressTy.setEmailAddress(rs.getString("EmailAddress"));
				addressTy.setPhoneNumber(rs.getString("PhoneNumber"));
				addressTy.setStreetAddress(rs.getString("StreetAddress"));
				addressTy.setState(UsStateType.fromValue(rs.getString("ind_org_state")));
				addressTy.setZipCode(rs.getString("ZipCode"));
				
				userPrefTy = new UserPreferencesType();
				
				agencyTy = new AgencyInfoType();
				agencyTy.setAgencyName(rs.getString("AgencyName"));
				agencyTy.setAgencyUrl(rs.getString("AgencyURL"));
				
				userPrefTy.setAgencyInfo(agencyTy);
				userPrefTy.setPreferredContactMethod(rs.getString("method_contact")+ " address");
				userPrefTy.setPreferredLanguage(LanguageType.fromValue(rs.getString("PreferredLanguage")));
				userPrefTy.setSearchIndicator(SearchIndicatorType.fromValue(rs.getString("SearchIndicator")));
				
				daysOfWeekTy = new DaysOfWeekType();
				daysOfWeekTy.setMonday(DayofWeekAvailabilityType.fromValue(rs.getString("Monday")));
				daysOfWeekTy.setTuesday(DayofWeekAvailabilityType.fromValue(rs.getString("Tuesday")));
				daysOfWeekTy.setWednesday(DayofWeekAvailabilityType.fromValue(rs.getString("Wednesday")));
				daysOfWeekTy.setThursday(DayofWeekAvailabilityType.fromValue(rs.getString("Thursday")));
				daysOfWeekTy.setFriday(DayofWeekAvailabilityType.fromValue(rs.getString("Friday")));
				daysOfWeekTy.setSaturday(DayofWeekAvailabilityType.fromValue(rs.getString("Saturday")));
				daysOfWeekTy.setSunday(DayofWeekAvailabilityType.fromValue(rs.getString("Sunday")));
				
				userPrefTy.setWorkingDays(daysOfWeekTy);
				
				fromTimeTy = new TimeType();
				fromTimeTy.setTime(rs.getString("fromTime"));
				
				toTimeTy = new TimeType();
				toTimeTy.setTime(rs.getString("toTime"));
				
				workHoursTy = new WorkingHoursType();
				workHoursTy.setFrom(fromTimeTy);
				workHoursTy.setTo(toTimeTy);
				
				userPrefTy.setWorkingHours(workHoursTy);
				
				compInfoTy = new CompletionInfoType();
				compInfoTy.setShopAgreementAcquiredDate(rs.getString("ShopAgreemenAcquiredDate"));
				compInfoTy.setShopAgreementExpirationDate(rs.getString("ShopAgreementExpirationDate"));
				compInfoTy.setShopAgreementName(rs.getString("ShopAgreementName"));
				compInfoTy.setShopAgreementStatus(rs.getString("ShopAgreementStatus"));
				
				
				
				shopRecordTy.setAddressInfo(addressTy);
				shopRecordTy.setPreferenceInfo(userPrefTy);
				shopRecordTy.setUserInfo(userTy);
				shopRecordTy.getCompletionInfo().add(compInfoTy);
				
				uId = (String)rs.getString("UserID");
				
				log.log(Level.ALL,shopRecordTy.toString());
			}
			//add the last record to the file.  This could be the last or the only one.
			if(shopRecordTy != null){
				fileDescType.getIncrementalRecord().add(shopRecordTy);
			}
		} catch (SQLException e) {
			ShopXMLException e1 = new ShopXMLException(
					"SQLException encountered when executing the stored procedure AP_MLMS_SHOP_XML" + "in method MLMSShopXMLModel.getShopXMLFile() : "
							+ e.getMessage());
			log.log(Level.SEVERE, "MLMSShopXMLModel.getShopXMLFile caught SQLException: " + e.getMessage());
			throw e1;
		} catch (Exception e2) {
			ShopXMLException e1 = new ShopXMLException("Exception encountered " + "in method MLMSShopXMLModel.getShopXMLFile() : "
					+ e2.getMessage());
			log.log(Level.SEVERE, "MLMSShopXMLModel.getShopXMLFile caught Exception: " + e2.getMessage());
			throw e1;
		} finally {
			try {
				ShopDBUtil.freeDBResources(rs, cstmt, conn, "MLMSShopXMLModel.getShopXMLFile");
			} catch (Exception e) {
				log.log(Level.SEVERE, "MLMSShopXMLModel.getShopXMLFile caught Exception while closing connections: " + e.getMessage());
			}
		}
		
		return fileDescType;
		
	}
	/**
	 * 
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getFormattedDate(String str) {
		SimpleDateFormat oldFormat = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date oldDate;
		try {
			oldDate = oldFormat.parse(str);
			return newFormat.format(oldDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
		
		
		
	}
		
}
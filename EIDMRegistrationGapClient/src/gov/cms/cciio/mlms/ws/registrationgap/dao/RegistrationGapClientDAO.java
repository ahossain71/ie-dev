package gov.cms.cciio.mlms.ws.registrationgap.dao;

/*
 * Written by Steve Meyer
 * change log
 * 
 * 10/7/2016 -
 * 10/21/2016 - updated to only have batch jobs write "Complete" 
 * 10/26/2016 - updated to retrieve search for gap users with Individual Marketplace completions and SHOP Marketplace completions separately
 * 
 */

import gov.cms.cciio.common.util.Constants;
import gov.hhs.cms.registrationgap.bean.UserIdBean;
import gov.hhs.cms.registrationgap.dto.RegistrationGapUserDTO;
import gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.UserTrainingRecordResponseType;
import gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.UserTrainingResponseType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import cms.cciio.ws.registrationgap.exception.RegistrationGapAuditException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUserTrainingSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersConnectionSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersListSQLException;
import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.exception.WebServiceTransactionLogException;

public class RegistrationGapClientDAO {
	/*
	 * Must return a list of users that have completed a select p.username,
	 * ce.name, v.status_desc, p.custom0 from tpt_ce_stud_certification stud
	 * inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id
	 * inner join cmt_person p on p.id = stud.owner_id inner join
	 * tpv_pub_i18n_cert_status v on stud.status = v.code where v.LOCALE_ID =
	 * 'local000000000000001' and stud.status = '100' and p.custom0 =
	 * 'Incomplete'
	 */
	String sourceClass = RegistrationGapClientDAO.class.getSimpleName();
	String currentOfferingyear = RegistrationGapClientDAO
			.getCurrentOfferingYear();
	boolean isDebug = System.getProperty("mlms.debug", "true")
			.equalsIgnoreCase("true");

	final DateFormat dbDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.S");
	final DateFormat wsDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	Logger logger = Logger.getLogger("RegistrationGapClientDAO");

	/**
	 * 
	 * @param maxReturnLength
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws ParseException
	 * @throws RegistrationGapUserTrainingSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 */
	public List<UserIdBean> getMLMSRegistrationGAPList(int maxReturnLength) throws RegistrationGapUsersListSQLException,
			ParseException, RegistrationGapUserTrainingSQLException,
			RegistrationGapUsersConnectionSQLException {
		String sourceMethod = "getMLMSRegistrationGAPListv1.5";
		if(sourceMethod.matches("^1-9")){
			//logger.logp(Level.INFO,sourceClass, sourceMethod, "REGEX Match Version " + sourceMethod.substring(sourceMethod.indexOf('v')),sourceMethod.length());
			System.out.println("RegistrationGapClientDAO getMLMSRegistrationGapListv1.5");
		}
		
		ArrayList<UserIdBean> userList = this.getRegistrationGapUserIdList(
				maxReturnLength, "IM", null);
		logger.logp(Level.INFO, sourceClass, sourceMethod, " count of users in registration gap, IM  " + userList.size());
		userList = this.getRegistrationGapUserIdList(maxReturnLength, "SHOP",
				userList);
		logger.logp(Level.INFO, sourceClass, sourceMethod, " count of users in registration gap, IM, SHOP  " + userList.size());
		userList = this.getCustom9NullUserIdList(maxReturnLength, userList);
		logger.logp(Level.INFO, sourceClass, sourceMethod, " count of users in registration gap, IM, SHOP, nulls  " + userList.size());
		return userList;
	}

	

	/**
	 * 
	 * @param maxReturnLength
	 * @param curriculum
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 */
	private ArrayList<UserIdBean> getRegistrationGapUserIdList(
			int maxReturnLength, String curriculum,
			ArrayList<UserIdBean> userList)
			throws RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException {
		String sourceMethod = "getRegistrationGapUserIdList";
		ArrayList<UserIdBean> list;
		String sql = null;
		String imSql = "select p.username as username, p.id as id, ce.id as certId from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+ "where stud.status = '100' and ce.inherited = '0' and p.custom0 = 'Incomplete' "
				+ "and ce.custom3 = ? "
				+ "and rownum <= ? and  length(p.username) > 5 "
				+ " and extract(year from stud.expired_on) = ? "
				+ "and stud.owner_id not in (select login_id from at_mlms_login_log where login_date < sysdate-1)"
				+ " and stud.acquired_on <= sysdate-1 "
				+ " order by p.username";
		
		String shopSql = "select p.username as username, p.id as id, ce.id as certId from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+  "inner join AT_MLMS_AB_MISC_ATTRIBUTES m on m.person_id = stud.owner_id "
				+ "where stud.status = '100' and ce.inherited = '0' and  m.SHOP_COMPLETION_STATUS = 'Incomplete' "
				+ "and ce.custom3 = ? "
				+ "and rownum <= ? and  length(p.username) > 5 "
				+ " and extract(year from stud.expired_on) = ? "
				+ "and stud.owner_id not in (select login_id from at_mlms_login_log where login_date < sysdate-1)"
				+ " and stud.acquired_on < sysdate-1 "
				+ " order by p.username";
		
		
		if (userList == null) {
			list = new ArrayList<UserIdBean>(maxReturnLength / 2);
		} else {
			list = userList;
		}
		//logger.logp(Level.INFO, sourceClass, sourceMethod, "pre-query, the registration gap list length is " + list.size());
		System.out.println("pre-query, the registration gap list length is " + list.size());
		String curriculumStr = null;

		String username = null;
		String personId = null;
		Connection userListCon = null;
		UserIdBean userBean = null;

		ResultSet rs = null;
		// CallableStatement cstmt = null;
		PreparedStatement pstmt = null;

		if (curriculum != null && curriculum.equalsIgnoreCase("IM")) {
			curriculumStr = "Individual Marketplace";
			sql = imSql;
		} else if (curriculum != null && curriculum.equalsIgnoreCase("SHOP")) {
			curriculumStr = "SHOP Marketplace";
			sql = shopSql;
		} 

		try {
			userListCon = this.getConnection();

			pstmt = userListCon.prepareCall(sql);
			pstmt.setString(1, curriculumStr);
			pstmt.setInt(2, maxReturnLength/2);
			pstmt.setString(3, currentOfferingyear);
			rs = pstmt.executeQuery();

			int count = 0;

			while (rs.next()) {
				count++;
				userBean = new UserIdBean();
				username = rs.getString("username");
				personId = rs.getString("id");

				userBean.setUsername(username);
				userBean.setPersonId(personId);
				userBean.setCurriculum(curriculum);
				if (username != null && personId != null) {
					list.add(userBean);
					logger.logp(Level.INFO,sourceClass,sourceMethod,"Registration Gap username " + username + " Curriculum " + curriculum);
					//System.out.println("Registration Gap username " + username + " Curriculum " + curriculum);
				}

			}

		} catch (SQLException e) {
			// if ME820 unexpected database error
			// System.out.println(e.getMessage());
			e.printStackTrace();
			throw new RegistrationGapUsersListSQLException(sourceMethod + " "
					+ ErrorCodeType.ME_830.value() + " " + e.getMessage());

		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (userListCon != null)
					userListCon.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new RegistrationGapUsersListSQLException(sourceMethod
						+ " " + "Datasource resources closing error!", e);
			}
		}
		return list;
	}
	/**
	 * 
	 * @param userList
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 */
	private ArrayList<UserIdBean> getCustom9NullUserIdList(int maxReturnLength,
					ArrayList<UserIdBean> userList)
			throws RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException {
		String sourceMethod = "getCustom9NullUserIdList";
		ArrayList<UserIdBean> list;
		/* Anu - 04/27/2017 
		 * Change to move all effective dates to at_mlms_ab_misc_attributes table
		 * String sql = "select  p.username, p.id as id, ce.id as certId from tpt_ce_stud_certification sc "
				+ "inner join AD_MLMS_PERSON_AGENT_INFO ai on ai.person_id = sc.owner_id "
				+ "inner join cmt_person p on sc.owner_id = p.id "
				+ "inner join tpt_ext_ce_certification ce on ce.id = sc.certification_id "
				+ "where p.custom0 is not null and p.custom9 is null "
				+ "and ce.custom3 in ('Individual Marketplace','SHOP Marketplace') "
				+ "and rownum <= ? and ce.INHERITED = 0 and sc.status = 100"
				+ "and sc.owner_id not in (select login_id from at_mlms_login_log where login_date < sysdate-1)"
				+ "ce.custom1 = '2017' ";*/
		
		String sql = "select  p.username, p.id as id, ce.id as certId from tpt_ce_stud_certification sc "
				+ "inner join AD_MLMS_PERSON_AGENT_INFO ai on ai.person_id = sc.owner_id "
				+ "inner join cmt_person p on sc.owner_id = p.id "
				+ "inner join AT_MLMS_AB_MISC_ATTRIBUTES m on m.person_id =  p.id "
				+ "inner join tpt_ext_ce_certification ce on ce.id = sc.certification_id "
				+ "where p.custom0 is not null and m.raw_effective_dt is null "
				+ "and ce.custom3 in ('Individual Marketplace','SHOP Marketplace') "
				+ "and rownum <= ? and ce.INHERITED = 0 and sc.status = 100 "
				+ "and sc.owner_id not in (select login_id from at_mlms_login_log where login_date < sysdate-1) "
				+ "and ce.custom1 in (select current_year from at_mlms_current_year) ";
			
		
		
		
		if (userList == null) {
			list = new ArrayList<UserIdBean>(maxReturnLength);
		} else {
			list = userList;
		}
		
		
		logger.logp(Level.INFO, sourceClass, sourceMethod, "pre-query, the list length is " + list.size());
		
		String username = null;
		String personId = null;
		String certificationId = null;
		Connection userListCon = null;
		UserIdBean userBean = null;

		ResultSet rs = null;
		// CallableStatement cstmt = null;
		PreparedStatement pstmt = null;

		

		try {
			userListCon = this.getConnection();

			pstmt = userListCon.prepareCall(sql);
			pstmt.setInt(1, maxReturnLength/2);
			rs = pstmt.executeQuery();

			int count = 0;

			while (rs.next()) {
				count++;
				userBean = new UserIdBean();
				username = rs.getString("username");
				personId = rs.getString("id");
				certificationId = rs.getString("certId");

				userBean.setUsername(username);
				userBean.setPersonId(personId);
				userBean.setCertificationId(certificationId);
				
				
				if (username != null && personId != null) {
					list.add(userBean);
					logger.logp(Level.INFO,sourceClass,sourceMethod,"Registration Gap username " + username + " custom9 is null");
					
				}

			}
			
		} catch (SQLException e) {
			// if ME820 unexpected database error
			// System.out.println(e.getMessage());
			e.printStackTrace();
			throw new RegistrationGapUsersListSQLException(sourceMethod + " "
					+ ErrorCodeType.ME_830.value() + " " + e.getMessage());

		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (userListCon != null)
					userListCon.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new RegistrationGapUsersListSQLException(sourceMethod
						+ " " + "Datasource resources closing error!", e);
			}
		}
		return list;
	}

	/**
	 * returns a UserTrainingRecordResponseType for a given user id
	 * 
	 * @XmlElement(name = "CertificationID", required = true) protected String
	 *                  certificationID;
	 * @XmlElement(name = "CertificationName", required = true) protected String
	 *                  certificationName;
	 * @XmlElement(name = "CertificationStatus", required = true) protected
	 *                  String certificationStatus;
	 * @XmlElement(name = "CertificationExpirationDate", required = true)
	 *                  protected String certificationExpirationDate;
	 * @XmlElement(name = "CertificationYear", required = true) protected String
	 *                  certificationYear;
	 * 
	 * @param userId
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws ParseException
	 * @throws RegistrationGapUserTrainingSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 */
	private UserTrainingResponseType getAgentBrokerTrainingRecords(
			String username) throws RegistrationGapUsersListSQLException,
			ParseException, RegistrationGapUserTrainingSQLException,
			RegistrationGapUsersConnectionSQLException {
		String methodName = "getAgentBrokerTrainingRecords";
		/*
		 * user name to retrieve training records
		 */
		String userName = username;

		/*
		 * UserTrainingRecordResponseType holds training records
		 */
		UserTrainingRecordResponseType userTrainingRecordResponseType = new UserTrainingRecordResponseType();

		/*
		 * User Training response type holds user training
		 */
		UserTrainingResponseType userTrainingResponseType = new UserTrainingResponseType();

		userTrainingResponseType.setUsername(username);

		String sql = "select c.id as certificationId, C.custom3 as certificationName, stud.status as certificationStatus, "
				+ "stud.expired_on as certificationExpirationDate,c.custom2 as certificationYear from tpt_ext_ce_certification c "
				+ "inner join tpt_ce_stud_certification stud on stud.certification_id = c.id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+ "where p.username = ? and stud.status = '100'"
				+ " and c.custom3 in ('Individual Marketplace','SHOP Marketplace') "
				+ " and extract(year from stud.expired_on) = ? "
				+ " and c.inherited = '0'";
		// String sql = "{call AP_MLMS_GET_AB_TRAINING_RECS(?,?)}";

		Connection trainingListCon = null;
		// private Connection trainingListCon = null;
		// CallableStatement cstmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		if (username != null)
			try {
				trainingListCon = this.getConnection();
				// pstmt = trainingListCon.prepareCall(sql);
				pstmt = trainingListCon.prepareStatement(sql);
				pstmt.setString(1, userName);
				pstmt.setString(2, currentOfferingyear);
				rs = pstmt.executeQuery();

				// Boolean hadResults = cstmt.execute();
				// System.out.println("call AP_MLMS_GET_AB_TRAINING_RECS(?,?) had results "
				// + hadResults);

				// rs = (ResultSet)cstmt.getResultSet();
				// rs = (ResultSet)cstmt.getObject(2);
				// rs = (ResultSet)cstmt.getResultSet();
				/*
				 * get list of user training records
				 */
				int count = 0;
				while (rs.next()) {

					/**
					 * get value check for null set in object set object in list
					 */
					count++;
					String certificationId = rs.getString("certificationId");
					if (null == certificationId)
						certificationId = "";
					userTrainingRecordResponseType
							.setCertificationID(certificationId);
					// System.out.println(" Certification id for user " +
					// username + "; " +
					// userTrainingRecordResponseType.getCertificationID());

					String certificationName = rs
							.getString("certificationName");
					if (null == certificationName)
						certificationName = "";
					userTrainingRecordResponseType
							.setCertificationName(certificationName);
					// System.out.println(" Certification Name for user " +
					// username + "; " +
					// userTrainingRecordResponseType.getCertificationName());

					String certificationStatus = rs
							.getString("certificationStatus");
					if (null == certificationStatus)
						certificationStatus = "";
					userTrainingRecordResponseType
							.setCertificationStatus(certificationStatus);
					// System.out.println(" Certification Status for user " +
					// username + "; " +
					// userTrainingRecordResponseType.getCertificationStatus());

					String certificationExpirationDate = rs
							.getString("certificationExpirationDate");
					if (null == certificationExpirationDate) {
						certificationExpirationDate = "";
					} else {
						Date dbDate = dbDateFormat
								.parse(certificationExpirationDate);
						certificationExpirationDate = wsDateFormat.format(
								dbDate).toString();
					}
					// convert date to "yyyy-MM-dd"

					// System.out.println(" dbDateFormat " + dbDate.toString());

					// System.out.println(" wsDateFormat " +
					// certificationExpirationDate);
					userTrainingRecordResponseType
							.setCertificationExpirationDate(certificationExpirationDate);
					// System.out.println(" Certification Expiration Date for user "
					// + username + "; " +
					// userTrainingRecordResponseType.getCertificationExpirationDate());

					String certificationYear = rs
							.getString("certificationYear");
					if (null == certificationYear)
						certificationYear = "";
					userTrainingRecordResponseType
							.setCertificationYear(certificationYear);
					// System.out.println(" Certification Year for user " +
					// username + "; " +
					// userTrainingRecordResponseType.getCertificationYear());
				}

				/*
				 * put list of training records into new
				 * UserTrainingResponseType
				 */
				if (count > 0) {
					// System.out.println("Adding " + username +"'s "+count+
					// "training records to response type");
					userTrainingResponseType.getTrainingRecord().add(
							userTrainingRecordResponseType);
				}
			} catch (SQLException e) {

				// if ME820 unexpected database error

				e.printStackTrace();
				throw new RegistrationGapUserTrainingSQLException(methodName
						+ " " + ErrorCodeType.ME_830.value() + " "
						+ e.getMessage());

			} finally {

				try {
					if (rs != null)
						rs.close();
					if (pstmt != null)
						pstmt.close();
					if (trainingListCon != null)
						trainingListCon.close();
				} catch (SQLException e) {
					System.out
							.println("SQLException: Datasource resources closing error!");
					throw new RegistrationGapUserTrainingSQLException(
							sourceClass + " " + methodName + " "
									+ "Datasource resources closing error!", e);
				}
			}
		return userTrainingResponseType;
	}

	public int[] setFFMRegistrationGapDataBatch(
			List<RegistrationGapUserDTO> list) throws RegistrationGapException {
		String sourceMethod = "setFFMRegistrationGapDataBatch";
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				Constants.DD_MMM_YYYY);
		String custom0Value = "Incomplete";
		//String custom9Value = null;

		String custom0sql = "update cmt_person set custom0 = ? where id = ?";
		//String custom9sql = "update cmt_person set custom9 = ? where id = ?";
		
		String effectiveDtSql = "update at_mlms_ab_misc_attributes set raw_effective_dt = ?, role_Effective_date = ? where id = ?";

		System.out.println(sourceClass + " " + sourceMethod + " update list length : " + list.size());
		Iterator<RegistrationGapUserDTO> it = list.iterator();
		RegistrationGapUserDTO dto = null;
		Connection con = null;
		PreparedStatement pstmt0 = null;
		PreparedStatement pstmt9 = null;
		int[] updateCount1 = null;
		int[] updateCount2 = null;

		try {

			con = this.getConnection();

			pstmt0 = con.prepareStatement(custom0sql);
			//pstmt9 = con.prepareStatement(custom9sql);
			pstmt9 = con.prepareStatement(effectiveDtSql);

			while (it.hasNext()) {
				dto = it.next();
				if (dto != null) {
					custom0Value = dto.isFFMComplete() ? "Complete"
							: "Incomplete";

					if (custom0Value.equalsIgnoreCase("Complete")) {

						pstmt0.setString(1, custom0Value);
						pstmt0.setString(2, dto.getPersonId());
						pstmt0.addBatch();
					}

					logger.logp(
							Level.INFO,
							sourceClass,
							sourceMethod,
							" user " + dto.getUsername() + " id "
									+ dto.getPersonId() + " ffm status "
									+ custom0Value);

					if (dto.getAgentBrokerEffectiveDate() != null && dto.getRawAgentBrokerEffectiveDate() != null) {
						/*
						 * custom9Value = this.convertToSQLDate(dto
						 * .getAgentBrokerEffectiveDate());
						 */			
						pstmt9.setString(1, getDDMMMYYYYFormattedDateString((dto.getAgentBrokerEffectiveDate())));
						pstmt9.setString(2, dto.getRawAgentBrokerEffectiveDate());
						pstmt9.setString(3, dto.getPersonId());
						pstmt9.addBatch();
					}
				}

			}
			updateCount1 = pstmt0.executeBatch();
			updateCount2 = pstmt9.executeBatch();

			logger.logp(Level.INFO,sourceClass,sourceMethod ," updated  "
					+ updateCount1.length + " rows successfully");

		} catch (SQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {
			logger.logp(Level.SEVERE,sourceClass,sourceMethod ,"SQLException: Datasource resources closing error! "
					+ e.getMessage());
		} finally {

			try {

				if (pstmt0 != null)
					pstmt0.close();
				if (pstmt9 != null)
					pstmt9.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				logger.logp(Level.SEVERE,sourceClass,sourceMethod ,"SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}

		return updateCount1;
	}

	/**
	 * 
	 * @param list
	 * @return
	 * @throws RegistrationGapException
	 */
	public int[] setABShopCompletionStatusBatch(
			List<RegistrationGapUserDTO> list) throws RegistrationGapException {
		String sourceMethod = "setABShopCompletionStatus";
		String updateShopSql = "update AT_MLMS_AB_MISC_ATTRIBUTES SET SHOP_COMPLETION_STATUS = ?, UPDATED_ON = sysdate where person_id = ?";
		/*
		 * String insertShopSql =
		 * "insert into AT_MLMS_AB_MISC_ATTRIBUTES (ID, PERSON_ID,GUID, SHOP_COMPLETION_STATUS, UPDATED_ON)  "
		 * + "values  ('ABDATA'||AT_MLMS_AB_MISC_SEQ.NEXTVAL,?,?,?,?,sysdate)";
		 */

		Iterator<RegistrationGapUserDTO> it = list.iterator();

		PreparedStatement pstmt = null;
		int[] updateCount = null;

		Connection con = null;
		String shopStatus = null;

		if (isDebug) {
			logger.logp(Level.FINER,sourceClass,sourceMethod , " update list length : " + list.size());
		}
		try {
			con = this.getConnection();
			pstmt = con.prepareStatement(updateShopSql);
			while (it.hasNext()) {
				RegistrationGapUserDTO dto = it.next();

				shopStatus = dto.isShopComplete() ? "Complete" : "Incomplete";

				if (shopStatus.equalsIgnoreCase("Complete")) {
					pstmt.setString(1, shopStatus);
					pstmt.setString(2, dto.getPersonId());
					pstmt.addBatch();

				}

				logger.logp(Level.FINER, sourceClass, sourceMethod, " user "
						+ dto.getUsername() + "\t id " + dto.getPersonId()
						+ " shop status " + shopStatus);

			}
			updateCount = pstmt.executeBatch();
			if (isDebug) {
				System.out.println(sourceClass + " " + sourceMethod
						+ " updated  " + updateCount.length
						+ " rows successfully");
			}
		} catch (RegistrationGapUsersConnectionSQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getClass() + " " + e.getMessage());

		} catch (SQLException e) {
			System.out.println(sourceClass + " " + sourceMethod
					+ "SQLException: Datasource resources closing error! "
					+ e.getMessage());
		} finally {

			try {

				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out.println(sourceClass + " " + sourceMethod
						+ "SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}
		return updateCount;
	}

	/**
	 * 
	 * @param list
	 * @return
	 * @throws RegistrationGapException
	 */
	public int[] setABMiscDataHistDateBatch(List<RegistrationGapUserDTO> list)
			throws RegistrationGapException {
		String sourceMethod = "setABMiscDataHistDateBatch";

		String histEffectiveDateSql = "update AT_MLMS_AB_MISC_ATTRIBUTES SET HIST_EFFECTIVE_DATE = ?, UPDATED_ON = sysdate where person_id = ?";
		String guidSql = "update AT_MLMS_AB_MISC_ATTRIBUTES SET EIDM_GUID = ?, UPDATED_ON = sysdate where person_id = ?";
		if (isDebug) {
			System.out.println(sourceClass + " " + sourceMethod
					+ " update list length : " + list.size());
		}
		Iterator<RegistrationGapUserDTO> it = list.iterator();

		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;

		int[] updateCount2 = null;
		int[] updateCount3 = null;
		;

		Connection con = null;

		try {
			con = this.getConnection();

			pstmt2 = con.prepareStatement(histEffectiveDateSql);
			pstmt3 = con.prepareStatement(guidSql);

			while (it.hasNext()) {
				RegistrationGapUserDTO dto = it.next();

				if (dto.getHistoricalRoleGrantDate() != null) {
					
					pstmt2.setString(1, this.getDDMMMYYYYFormattedDateString(dto.getHistoricalRoleGrantDate()));
					pstmt2.setString(2, dto.getPersonId());
					pstmt2.addBatch();
				}
				if (dto.getGuid() != null) {
					pstmt3.setString(1, dto.getGuid().toUpperCase());
					pstmt3.setString(2, dto.getPersonId());
					pstmt3.addBatch();
				}

			}
			/*
			 * update this separate into separate methods
			 */

			updateCount2 = pstmt2.executeBatch();
			updateCount3 = pstmt3.executeBatch();

			logger.logp(Level.INFO,sourceClass,sourceMethod ," updated  "
					+ updateCount2.length + " rows successfully");

		} catch (SQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getClass() + " " + e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {
			logger.logp(Level.SEVERE,sourceClass,sourceMethod , "SQLException: Datasource resources closing error! "
					+ e.getMessage());
		} finally {

			try {

				if (pstmt2 != null)
					pstmt2.close();
				if (pstmt3 != null)
					pstmt3.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out.println(sourceClass + " " + sourceMethod
						+ "SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}
		return updateCount2;
	}

	public int[] setABMiscDataGUIDBatch(List<RegistrationGapUserDTO> list)
			throws RegistrationGapException {
		String sourceMethod = "setABMiscDataGUIDBatch";

		String guidSql = "update AT_MLMS_AB_MISC_ATTRIBUTES SET EIDM_GUID = ?, UPDATED_ON = sysdate where person_id = ?";
		if (isDebug) {
			System.out.println(sourceClass + " " + sourceMethod
					+ " update list length : " + list.size());
		}
		Iterator<RegistrationGapUserDTO> it = list.iterator();

		PreparedStatement pstmt1 = null;

		int[] updateCount = null;

		Connection con = null;

		try {
			con = this.getConnection();

			pstmt1 = con.prepareStatement(guidSql);

			while (it.hasNext()) {
				RegistrationGapUserDTO dto = it.next();

				if (dto.getGuid() != null) {
					pstmt1.setString(1, dto.getGuid().toUpperCase());
					pstmt1.setString(2, dto.getPersonId());
					pstmt1.addBatch();
				}

			}
			/*
			 * update this separate into separate methods
			 */

			updateCount = pstmt1.executeBatch();

			System.out.println(sourceClass + " " + sourceMethod + " updated  "
					+ updateCount.length + " rows successfully");

		} catch (SQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getClass() + " " + e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {
			System.out.println(sourceClass + " " + sourceMethod
					+ "SQLException: Datasource resources closing error! "
					+ e.getMessage());
		} finally {

			try {

				if (pstmt1 != null)
					pstmt1.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out.println(sourceClass + " " + sourceMethod
						+ "SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}
		return updateCount;
	}

	/**
	 * 
	 * @param list
	 * @return
	 * @throws RegistrationGapException
	 */
	public int[] setABMiscDataAccountCreateBatch(
			List<RegistrationGapUserDTO> list) throws RegistrationGapException {
		String sourceMethod = "setABMiscDataAccountCreateBatch";
		String acctCreateDateSql = "update AT_MLMS_AB_MISC_ATTRIBUTES SET EIDM_ACCT_CREATE_DATE = ?, UPDATED_ON = sysdate where person_id = ?";

		if (isDebug) {
			System.out.println(sourceClass + " " + sourceMethod
					+ " update list length : " + list.size());
		}
		Iterator<RegistrationGapUserDTO> it = list.iterator();

		PreparedStatement pstmt = null;

		int[] updateCount = null;

		Connection con = null;

		try {
			con = this.getConnection();
			pstmt = con.prepareStatement(acctCreateDateSql);

			while (it.hasNext()) {
				RegistrationGapUserDTO dto = it.next();
				if (dto.getAccountCreateDate() != null) {
					pstmt.setDate(1,
							this.convertToSQLDate(dto.getAccountCreateDate()));
					pstmt.setString(2, dto.getPersonId());
					pstmt.addBatch();
				}

			}
			/*
			 * update this separate into separate methods
			 */
			updateCount = pstmt.executeBatch();

			System.out.println(sourceClass + " " + sourceMethod + " updated  "
					+ updateCount.length + " rows successfully");

		} catch (SQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getClass() + " " + e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {
			System.out.println(sourceClass + " " + sourceMethod
					+ "SQLException: Datasource resources closing error! "
					+ e.getMessage());
		} finally {

			try {

				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				System.out.println(sourceClass + " " + sourceMethod
						+ "SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}
		return updateCount;
	}

	/**
	 * 
	 * @param person_id
	 * @param status
	 * @param userName
	 *            TODO
	 * @param description
	 *            TODO
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 * @throws RegistrationGapAuditException
	 */
	/*
	 * private int auditRegGap(String person_id, String status, String userName,
	 * String description) throws RegistrationGapUsersListSQLException,
	 * RegistrationGapUsersConnectionSQLException, RegistrationGapAuditException
	 * { String sourceMethod = "auditRegGap"; String insertSql =
	 * "insert into at_mlms_reg_gap_audit (id, lot, person_id, status, created_on) values "
	 * + "('regaud'|| AT_MLMS_REG_GAP_AUDIT_SEQ.nextval,?,?,?,sysdate) ";
	 * 
	 * //* insert sql 1 = lot 2 = person id 3 = status
	 * 
	 * String updateSql = "update AT_MLMS_REG_GAP_AUDIT set STATUS = ?," +
	 * " UPDATED_ON = sysdate where PERSON_ID = ?"; String sql = null; if
	 * (isDebug) { System.out.println(sourceClass + " " + sourceMethod +
	 * " Update audit record for " + userName + ", id " + person_id +
	 * " status  " + status); } // update sql 1 = status 2 = person_d
	 * 
	 * String methodName = "auditRegGap";
	 * 
	 * // lot # is String yyyyMMdd
	 * 
	 * SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd"); String lot =
	 * format.format(new Date());
	 * 
	 * // * get current date
	 * 
	 * 
	 * if (status == null) { status = "I"; } if (person_id == null) { throw new
	 * RegistrationGapAuditException(
	 * "person id null cannot insert/update audit"); }
	 * 
	 * Connection con = this.getConnection(); PreparedStatement updatePstmt =
	 * null; PreparedStatement insertPstmt = null; ResultSet rs = null; int
	 * sqlResult = -1;
	 * 
	 * try {
	 * 
	 * // logger.log(Level.FINEST, " methodName " + sql); updatePstmt =
	 * con.prepareStatement(updateSql); sql = updateSql;
	 * updatePstmt.setString(1, status); updatePstmt.setString(2, person_id);
	 * sqlResult = updatePstmt.executeUpdate();
	 * 
	 * 
	 * 
	 * } catch (SQLException e) {
	 * System.out.println("RegistrationGapAuditException Thrown"); throw new
	 * RegistrationGapAuditException(methodName + " query  " + sql + "\n " +
	 * e.getMessage()); } finally {
	 * 
	 * try { if (rs != null) rs.close(); if (insertPstmt != null)
	 * insertPstmt.close(); if (updatePstmt != null) updatePstmt.close(); if
	 * (con != null) con.close(); } catch (SQLException e) { System.out
	 * .println("SQLException: Datasource resources closing error!"); throw new
	 * RegistrationGapUsersConnectionSQLException(
	 * "Datasource resources closing error!", e); } }
	 * 
	 * return sqlResult; }
	 * 
	 * /**
	 * 
	 * @param list
	 * 
	 * @return
	 * 
	 * @throws RegistrationGapUsersConnectionSQLException
	 * 
	 * @throws RegistrationGapException
	 */
	public int[] auditRegGapBatch(List<RegistrationGapUserDTO> list)
			throws RegistrationGapException {
		String sourceMethod = "auditRegGapBatch";
		String sql = "update AT_MLMS_REG_GAP_AUDIT set STATUS = ?,"
				+ " UPDATED_ON = sysdate where PERSON_ID = ?";
		System.out.println(sourceClass + " " + sourceMethod
				+ " update list length : " + list.size());
		Iterator<RegistrationGapUserDTO> it = list.iterator();

		Connection con = null;
		PreparedStatement pstmt = null;
		int[] updateCount = null;

		try {
			con = this.getConnection();

			pstmt = con.prepareStatement(sql);

			RegistrationGapUserDTO dto = null;

			while (it.hasNext()) {
				dto = it.next();
				pstmt.setString(1, "R");
				pstmt.setString(2, dto.getPersonId());
				pstmt.addBatch();

			}
			updateCount = pstmt.executeBatch();
			System.out.println(sourceClass + " " + sourceMethod + " updated  "
					+ updateCount.length + " rows successfully");

		} catch (SQLException e) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " " + e.getClass() + " " + e.getMessage());
		} catch (RegistrationGapUsersConnectionSQLException e) {
			// TODO Auto-generated catch block
			logger.logp(Level.SEVERE, sourceClass, sourceMethod, e.getMessage());
		} finally {

			try {

				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				logger.logp(Level.SEVERE,sourceClass,sourceMethod
						,"SQLException: Datasource resources closing error! "
						+ e.getMessage());

			}
		}

		return updateCount;

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
	private String getDDMMMYYYYFormattedDateString(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		String retVal = null;
		if (date != null) {
			retVal = dateFormat.format(date);
		}

		return retVal;
	}

	private java.sql.Date convertToSQLDate(Date date) {
		java.sql.Date retVal = new java.sql.Date(date.getTime());
		return retVal;
	}

	/*
	 * 
	 */
	private Connection getConnection()
			throws RegistrationGapUsersConnectionSQLException {
		int count = 0;
		Connection con = null;
		if (con == null && count == 0) {
			try {
				count++;
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
				// System.out.println(" Getting Connection ");
				con = ds.getConnection();

			} catch (NamingException ne) {
				System.out
						.println("JNDI NamingException! Check output console");
				throw new RegistrationGapUsersConnectionSQLException(
						ErrorMessageType.ME_850_NAMING_CONTEXT_ERROR.value()
								+ " Exception follows" + ne.getMessage());
			} catch (SQLException se) {
				System.out.println("SQLException! Check output console");
				throw new RegistrationGapUsersConnectionSQLException(
						ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
								.value()
								+ ". SQLState: "
								+ se.getSQLState().toString());

			}
		}
		return con;
	}

	/**public void logTransaction(String webserviceId, String transactionCode,
			String partnerId, String statusCode)
			throws WebServiceTransactionLogException {
		TransactionLogAPI xlogAPI = new TransactionLogAPI();
		xlogAPI.insertWSTransaction(webserviceId, transactionCode, partnerId,
				statusCode);
	}*/

	private static String getCurrentOfferingYear() {
		String retVal = null;
		retVal = System.getProperty("mlms.current.offering.year", "2017");
		return retVal;
	}

	private Date getDefaultCreateDate() throws ParseException {
		String sourceMethod = "getDefaultCreateDate";
		String inputStr = "10-31-2014";
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date inputDate = dateFormat.parse(inputStr);
		return inputDate;
	}

}

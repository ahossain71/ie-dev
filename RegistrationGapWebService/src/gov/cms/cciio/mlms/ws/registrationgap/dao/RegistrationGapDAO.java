package gov.cms.cciio.mlms.ws.registrationgap.dao;

import gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.UserTrainingRecordResponseType;
import gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.UserTrainingResponseType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.mlmsregistrationgap.bean.UserIdBean;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import javax.xml.bind.Marshaller;

import cms.cciio.ws.registrationgap.exception.RegistrationGapAuditException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUserTrainingSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersConnectionSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersCountSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersListSQLException;
import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.exception.WebServiceTransactionLogException;

public class RegistrationGapDAO {
	final String sourceClass = RegistrationGapDAO.class.getSimpleName();
	Logger logger = Logger.getLogger("RegistrationGapDAO");
	/*
	 * Must return a list of users that have completed a select distinct
	 * p.username, ce.name, v.status_desc, p.custom0 from
	 * tpt_ce_stud_certification stud inner join tpt_ext_ce_certification ce on
	 * ce.id = stud.certification_id inner join cmt_person p on p.id =
	 * stud.owner_id inner join tpv_pub_i18n_cert_status v on stud.status =
	 * v.code where v.LOCALE_ID = 'local000000000000001' and stud.status = '100'
	 * and p.custom0 = 'Incomplete'
	 */

	final SimpleDateFormat dbDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.S");
	final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
	// final DateFormat wsDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	final DateFormat wsDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	String currentOfferingyear = RegistrationGapDAO.getCurrentOfferingYear();

	 

	public RegistrationGapDAO() {
		String sourceMethod = "RegistrationGapDAO";
		 logger.addHandler(new ConsoleHandler());
		 //logger.setLevel(Level.INFO);
		 logger.logp(Level.FINER, sourceClass ,sourceMethod ," source Console Handler created " );
		 
	}

	

	/**
	 * 
	 * @param maxReturnLength
	 * @param userList
	 * @param curriculum
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws ParseException
	 * @throws RegistrationGapUserTrainingSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 * @throws RegistrationGapException
	 */
	public List<UserTrainingResponseType> getMLMSRegistrationGAPList(
			int maxReturnLength, String string)
			throws RegistrationGapUsersListSQLException, ParseException,
			RegistrationGapUserTrainingSQLException,
			RegistrationGapUsersConnectionSQLException {
		String sourceMethod = "getMLMSRegistrationGAPList";

		ArrayList<UserIdBean> userList = null;
		//
		// make call to populate list with users with Individual Marketplace completions
		// 
		userList = this.getRegistrationGapUserIdList(maxReturnLength, "IM",
				userList);
		
		logger.logp(Level.INFO, sourceClass ,sourceMethod , " fetching registration gap list, with IM's "
				+ userList.size());
		//
		// make call to populate list with users with SHOP Marketplace completions
		// 
		userList = this.getRegistrationGapUserIdList(maxReturnLength, "SHOP",
				userList);
		
		logger.logp(Level.INFO, sourceClass ,sourceMethod , " fetching registration gap list, with IM's and SHOP "
				+ userList.size());
		
		ArrayList<UserTrainingResponseType> registrationGAPList = new ArrayList<UserTrainingResponseType>(
				userList.size() + 1);

		Iterator<UserIdBean> it = userList.iterator();

		while (it.hasNext()) {
			UserIdBean userIdBean = null;
			try {
				userIdBean = (UserIdBean) it.next();
				registrationGAPList.add(this
						.getAgentBrokerTrainingRecords(userIdBean));
				logger.logp(Level.FINER, sourceClass ,sourceMethod ," requesting training for user " + userIdBean.getUsername());
			} catch (RegistrationGapException ex) {
				logger.logp(Level.SEVERE, sourceClass ,sourceMethod ," user with incomplete response data "
						+ userIdBean.getUsername() + ex.getMessage());
			}
		}
		return registrationGAPList;

	}

	/**
	 * Gets the user ids in the registration gap list
	 * 
	 * @param maxReturnLength
	 *            TODO
	 * 
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 * @throws RegistrationGapAuditException
	 */
	

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
		ArrayList<UserIdBean> list = userList;
		String sql = null;
		String imSql = "select p.username as username, p.id as id, ce.id as certId from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+ "where stud.status = '100' and ce.inherited = '0' and p.custom0 = 'Incomplete' "
				+ "and ce.custom3 = ? "
				+ "and rownum <= ? and  length(p.username) > 5 "
				+ " and extract(year from stud.expired_on) = ? "
				+ " and stud.id not in (select login_id from at_mlms_login_log where login_date < sysdate-1)"
				+ " order by p.username";
		String shopSql = "select p.username as username, p.id as id, ce.id as certId from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+  "inner join AT_MLMS_AB_MISC_ATTRIBUTES m on m.person_id = stud.owner_id "
				+ "where stud.status = '100' and ce.inherited = '0' and  m.SHOP_COMPLETION_STATUS = 'Incomplete' "
				+ "and ce.custom3 = ? "
				+ "and stud.id not in (select login_id from at_mlms_login_log where login_date < sysdate-1)"
				+ "and rownum <= ? and  length(p.username) > 5 "
				+ " and extract(year from stud.expired_on) = ? "
				+ " order by p.username";
		if (list == null) {
			list = new ArrayList<UserIdBean>(maxReturnLength * 2);
		}
		logger.logp(Level.FINER, sourceClass, sourceMethod,
				" populating response with training data ");
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
		logger.logp(Level.FINER, sourceClass ,sourceMethod ," making database call to get list, input parameters \n curriculumStr "
						+ curriculumStr + "\n maxReturnLength "
						+ maxReturnLength + "\n currentOfferingYear "
						+ currentOfferingyear);

		try {
			userListCon = this.getConnection();

			pstmt = userListCon.prepareCall(sql);
			pstmt.setString(1, curriculumStr);
			pstmt.setInt(2, maxReturnLength);
			pstmt.setString(3, currentOfferingyear);
			rs = pstmt.executeQuery();

			int count = 0;
			
			while (rs.next()) {
				count++;
				userBean = new UserIdBean();
				userBean.setUsername(rs.getString("username"));
				userBean.setPersonId(rs.getString("id"));
				userBean.setCurriculum(curriculum);
				userBean.setCertificationId(rs.getString("certId"));
				if (userBean.getUsername() != null && userBean.getPersonId() != null) {
					list.add(userBean);
					logger.logp(Level.FINER, sourceClass ,sourceMethod , " username " + userBean.getUsername());
					
				}

			}//
			logger.logp(Level.INFO, sourceClass ,sourceMethod ,curriculum + " records returned from registation gap query, " +
			" size of response list is " + list.size());

		} catch (SQLException e) {
			// if ME820 unexpected database error
			
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
				logger.logp(Level.SEVERE, sourceClass ,sourceMethod ,"SQLException: Datasource resources closing error! " + e.getMessage());
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
	 * @throws RegistrationGapException
	 */
	private UserTrainingResponseType getAgentBrokerTrainingRecords(
			UserIdBean userIdBean) throws RegistrationGapUsersListSQLException,
			ParseException, RegistrationGapUserTrainingSQLException,
			RegistrationGapUsersConnectionSQLException,
			RegistrationGapException {
		String sourceMethod = "getAgentBrokerTrainingRecords";
		logger.logp(Level.FINER, sourceClass ,sourceMethod ," getting training record for  " + userIdBean.getUsername() + " for cert id " + userIdBean.getCertificationId());
		/*
		 * user name to retrieve training records
		 */
		if (userIdBean.getUsername() == null) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " null user id found in UserIdBean ");
		}
		if (userIdBean.getCertificationId() == null) {
			throw new RegistrationGapException(sourceClass + " " + sourceMethod
					+ " null certification id found in UserIdBean ");
		}
		// String userName = userIdBean.getUsername();
		// String certId = userIdBean.getCertificationId();

		/*
		 * UserTrainingRecordResponseType holds training records
		 */
		UserTrainingRecordResponseType userTrainingRecordResponseType = new UserTrainingRecordResponseType();

		/*
		 * User Training response type holds user training
		 */
		UserTrainingResponseType userTrainingResponseType = new UserTrainingResponseType();

		userTrainingResponseType.setUsername(userIdBean.getUsername());
		String sql = "select ce.id as certificationId, Ce.custom3 as certificationName, stud.status as certificationStatus, "
				+ "stud.expired_on as certificationExpirationDate,ce.custom2 as certificationYear from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ "inner join cmt_person p on p.id = stud.owner_id "
				+ " where p.username = upper(?) and stud.status = '100' and ce.inherited = '0' "
				+ " and ce.id = ? "
				+ " and extract(year from stud.expired_on) = ? ";

		/*
		 * String sql =
		 * "select c.id as certificationId, C.custom3 as certificationName, stud.status as certificationStatus, "
		 * +
		 * "stud.expired_on as certificationExpirationDate,c.custom2 as certificationYear from tpt_ext_ce_certification c "
		 * +
		 * "inner join tpt_ce_stud_certification stud on stud.certification_id = c.id "
		 * + "inner join cmt_person p on p.id = stud.owner_id " +
		 * "where p.username = upper(?) and stud.status = '100'" +
		 * " and c.custom3 in ('Individual Marketplace','SHOP Marketplace') " +
		 * " and extract(year from stud.expired_on) >= extract(year from sysdate) -1 "
		 * + " and c.inherited = 0";
		 */
		// String sql = "{call AP_MLMS_GET_AB_TRAINING_RECS(?,?)}";

		Connection trainingListCon = null;
		// private Connection trainingListCon = null;
		// CallableStatement cstmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			trainingListCon = this.getConnection();
			pstmt = trainingListCon.prepareCall(sql);
			pstmt = trainingListCon.prepareStatement(sql);
			pstmt.setString(1, userIdBean.getUsername());
			pstmt.setString(2, userIdBean.getCertificationId());
			pstmt.setString(3, currentOfferingyear);

			rs = pstmt.executeQuery();

			/*
			 * get list of user training records
			 */
			int count = 0;
			Date dbDate = null;
			String certificationYear = null;
			String certificationExpirationDate = null;
			String certificationStatus = null;
			String certificationName = null;
			String certificationId = null;
			while (rs.next()) {

				/**
				 * get value check for null set in object set object in list
				 */
				count++;
				certificationId = rs.getString("certificationId");
				if (null == certificationId) {
					throw new RegistrationGapException(sourceClass + " "
							+ sourceMethod
							+ " Exception certificationId is null");

				} else {
					userTrainingRecordResponseType
							.setCertificationID(certificationId);
				}
				certificationName = rs.getString("certificationName");
				if (null == certificationName) {
					throw new RegistrationGapException(sourceClass + " "
							+ sourceMethod
							+ " Exception certificationName is null");

				} else {
					userTrainingRecordResponseType
							.setCertificationName(certificationName);
				}

				certificationStatus = rs
						.getString("certificationStatus");
				if (null == certificationStatus) {
					throw new RegistrationGapException(sourceClass + " "
							+ sourceMethod
							+ " Exception certificationStatus is null");

				} else {
					userTrainingRecordResponseType
							.setCertificationStatus(certificationStatus);
				}

				certificationExpirationDate = rs
						.getString("certificationExpirationDate");
				if (null == certificationExpirationDate) {
					throw new RegistrationGapException(sourceClass + " "
							+ sourceMethod
							+ " Exception certificationExpirationDate is null");

				} else {
					dbDate = dbDateFormat
							.parse(certificationExpirationDate);
					certificationExpirationDate = wsDateFormat.format(dbDate)
							.toString();
				}
				userTrainingRecordResponseType
						.setCertificationExpirationDate(certificationExpirationDate);

				certificationYear = rs.getString("certificationYear");
				if (null == certificationYear && dbDate !=null) {
				   Calendar cal = new GregorianCalendar();
				   cal.setTime(dbDate);
				
				   userTrainingRecordResponseType
					.setCertificationYear(String.valueOf(cal.get(Calendar.YEAR)));

				} else {
					userTrainingRecordResponseType
							.setCertificationYear(certificationYear);
				}

			}

			/*
			 * put list of training records into new UserTrainingResponseType
			 */
			if (count > 0) {
				userTrainingResponseType.getTrainingRecord().add(
						userTrainingRecordResponseType);
			}
		} catch (SQLException e) {

			// if ME820 unexpected database error
			e.printStackTrace(System.out);
			throw new RegistrationGapUserTrainingSQLException(sourceClass + " "
					+ sourceMethod + " " + ErrorCodeType.ME_830.value() + " "
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
				logger.logp(Level.SEVERE, sourceClass ,sourceMethod ,"SQLException: Datasource resources closing error! " + e.getMessage());
				throw new RegistrationGapUserTrainingSQLException(
						"Datasource resources closing error!", e);
			}
		}
		return userTrainingResponseType;
	}

	/**
	 * used to notify requester if the resultset is larger than 300
	 * 
	 * @param maxsize
	 *            TODO
	 * @return
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapUserTrainingSQLException
	 * @throws RegistrationGapUsersCountSQLException
	 * @throws RegistrationGapUsersConnectionSQLException
	 */
	public Boolean isRegistrationGapBig(int maxsize)
			throws RegistrationGapUsersCountSQLException,
			RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException {
		String sourceMethod = "isRegistrationGapBig";
		int maxSize = maxsize;

		String shopSql = " select count(p.id) as usrCount from tpt_ce_stud_certification stud "
				+ " inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ " inner join cmt_person p on p.id = stud.owner_id "
				+ " left outer join at_mlms_ab_misc_attributes m "
				+ " on p.id = m.person_id "
				+ " where stud.status = '100' and ce.inherited = '0' "
				+ " AND (ce.custom3 = 'SHOP Marketplace' and m.shop_completion_status = 'Incomplete')"
				+ " and LENGTH(p.username) > 5"
				+ " and extract(year from stud.expired_on) = ? ";
		String ffmSql = " select count(p.id) as usrCount from tpt_ce_stud_certification stud "
				+ " inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ " inner join cmt_person p on p.id = stud.owner_id "
				+ " where stud.status = '100' and ce.inherited = '0' "
				+ " AND (ce.custom3 = 'Individual Marketplace' and p.custom0 = 'Incomplete')"
				+ " and LENGTH(p.username) > 5"
				+ " and extract(year from stud.expired_on) = ? ";

		Connection shopCountCon = this.getConnection();
		Connection ffmCountCon = this.getConnection();
		PreparedStatement shopPstmt = null;
		PreparedStatement ffmPstmt = null;
		ResultSet shopRS = null;
		ResultSet ffmRS = null;
		int count = -1;
		try {

			shopPstmt = shopCountCon.prepareStatement(shopSql);
			ffmPstmt = ffmCountCon.prepareStatement(ffmSql);

			shopPstmt.setString(1, currentOfferingyear);
			ffmPstmt.setString(1, currentOfferingyear);
			shopRS = shopPstmt.executeQuery();
			ffmRS = ffmPstmt.executeQuery();
			while (shopRS.next()) {
				count = shopRS.getInt("usrCount");
			}
			
			while (shopRS.next()) {
				count += ffmRS.getInt("usrCount");
			}
		} catch (SQLException e) {

			// if ME820 unexpected database error

			e.printStackTrace();
			throw new RegistrationGapUsersCountSQLException(
					ErrorCodeType.ME_830.value() + " " + e.getMessage());

		} finally {

			try {
				if (shopRS != null)
					shopRS.close();
				if (shopPstmt != null)
					shopPstmt.close();
				if (shopCountCon != null)
					shopCountCon.close();
			} catch (SQLException e) {
				System.out
						.println("SQLException: Datasource resources closing error!");
				throw new RegistrationGapUsersCountSQLException(
						"Datasource resources closing error!", e);
			}
		}
		 logger.logp(Level.INFO, sourceClass ,sourceMethod ," Max return limit " + maxSize + " gap size  "
				+ count);
		if (count > maxSize) {
			return true;
		} else {

			return false;
		}
	}

	/**
	 * 
	 * @param person_id
	 * @param status
	 * @param list
	 *            TODO
	 * @param updated_on
	 * @param created_on
	 * @return
	 * @throws RegistrationGapUsersConnectionSQLException
	 * @throws RegistrationGapUsersListSQLException
	 * @throws RegistrationGapAuditException
	 */
	private int auditRegGap(String person_id, String status,
			List<UserIdBean> list) throws RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException,
			RegistrationGapAuditException {
		String sourceMethod = "auditRegGap";
		String insertSql = "insert into at_mlms_reg_gap_audit (id, lot, person_id, status, created_on) values "
				+ "('regaud'|| AT_MLMS_REG_GAP_AUDIT_SEQ.nextval,?,?,?,sysdate) ";
		/*
		 * insert sql 1 = lot 2 = person id 3 = status
		 */
		String updateSql = "update at_mlms_reg_gap_audit set status = ?,"
				+ " updated_on = sysdate where person_id = ?";
		/*
		 * update sql 1 = status 2 = person_d
		 */

		/*
		 * lot # is String yyyyMMdd
		 */
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String lot = format.format(new Date());
		/*
		 * get current date
		 */

		if (status == null) {
			status = "I";
		}
		if (person_id == null) {
			throw new RegistrationGapAuditException(
					"person id null cannot insert/update audit");
		}

		Connection con = this.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int sqlResult = 0;

		try {
			String sql = updateSql;

			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setString(2, person_id);
			logger.logp(Level.FINER, sourceClass ,sourceMethod , " updating..."
					+ person_id);
			sqlResult = pstmt.executeUpdate();

			if (sqlResult == 0) {
				 logger.logp(Level.FINER, sourceClass ,sourceMethod ," user not found inserting...");

				pstmt.clearParameters();
				sql = insertSql;
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, lot);
				pstmt.setString(2, person_id);
				pstmt.setString(3, status);
				sqlResult = pstmt.executeUpdate();

				logger.logp(Level.FINER, sourceClass ,sourceMethod ," insert result " + sqlResult);

			}

		} catch (SQLException e) {
			
			throw new RegistrationGapAuditException(sourceClass + " "
					+ sourceMethod + " " + e.getMessage());
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				
				throw new RegistrationGapUsersConnectionSQLException(
						"Datasource resources closing error!", e);
			}
		}

		return sqlResult;
	}

	private static String getCurrentOfferingYear() {
		String retVal = null;
		retVal = System.getProperty("mlms.current.offering.year", "2017");
		return retVal;
	}

	private Connection getConnection()
			throws RegistrationGapUsersListSQLException,
			RegistrationGapUsersConnectionSQLException {
		int count = 0;
		Connection con = null;
		if (con == null && count == 0) {
			try {
				count++;
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
				con = ds.getConnection();

			} catch (NamingException ne) {
				
				throw new RegistrationGapUsersConnectionSQLException(
						ErrorMessageType.ME_850_NAMING_CONTEXT_ERROR.value()
								+ " Exception follows" + ne.getMessage());
			} catch (SQLException se) {
				
				throw new RegistrationGapUsersConnectionSQLException(
						ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
								.value() + ". SQLState: " + se.getMessage());

			}
		}
		return con;
	}
	public void logTransaction(String webserviceId, String transactionCode, String partnerId, String statusCode,RegistrationGapResponseType regGapResType ) throws WebServiceTransactionLogException{
		TransactionLogAPI xlogAPI = new TransactionLogAPI();
		
		try{
			JAXBContext context = JAXBContext.newInstance(RegistrationGapResponseType.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    try{
		    m.marshal(regGapResType, sw);
		    }catch(JAXBException me){
		    	sw.write("cannot Marshal XML " + regGapResType.getStatusCode().getStatusMessage());
		    }
		    String result = sw.toString();
		    xlogAPI.insertWSTransaction(webserviceId, 
				transactionCode, 
				partnerId, 
				statusCode,result,"RegistrationGapWebServiceReponse");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("RegistrationGapDAO MLMSWS Transaction logging exception --- " + e.getStackTrace());
			throw(new WebServiceTransactionLogException(e));
		}
	}

}

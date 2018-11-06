package util.mlms.cciio.cms.mlmsutil.dao;

import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.mlmsutil.MLMSStaticUtils;
import gov.mlms.cciio.cms.usertrainingcompletionstatus.UserTrainingRecordResponseType;
import gov.mlms.cciio.cms.usertrainingcompletionstatus.UserTrainingResponseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import util.mlms.cciio.cms.mlmsutil.exception.MLMSUtilDAOException;

public class MLMSUtilDAO {

	String lnameSeed = null;
	String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 
	 * @return
	 * @throws MLMSUtilDAOException
	 */
	public ArrayList<String> getMLMSRandomUserlist()
			throws MLMSUtilDAOException {
		ArrayList<String> userList = new ArrayList<String>(100);
		lnameSeed = MLMSStaticUtils.getRandomString().toLowerCase();

		String sql = "select username as \"USERNAME\" from cmt_person where lname like ? ";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			pstmt = con.prepareStatement(sql);
			System.out
					.println(" getMLMSRandomUserList, lnameSeed " + lnameSeed);
			pstmt.setString(1, "%" + lnameSeed + "%");
			System.out.println(" getMLMSRandomUserList, executequery ");
			rs = pstmt.executeQuery();
			String username = null;

			while (rs.next()) {
				username = rs.getString(1);
				// System.out.println(" ResultSet username " + username);
				if (username != null && !username.isEmpty()) {
					userList.add(username);
				}

			}
			if (userList.size() == 0) {
				System.out.println("user list = zero");
				throw new MLMSUtilDAOException(ErrorCodeType.ME_800.value());
			}
		} catch (SQLException se) {
			System.out.println(" SQLException thrown message "
					+ se.getMessage());
			System.out.println(" SQLException thrown sqlstate"
					+ se.getSQLState());
			System.out.println(" SQLException thrown errorcode"
					+ se.getErrorCode());
			throw new MLMSUtilDAOException(ErrorCodeType.ME_830.value()
					+ se.getMessage());
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
				throw new MLMSUtilDAOException(
						"Datasource resources closing error!", e);
			}
		}

		return userList;
	}

	/**
	 * @throws MLMSUtilDAOException
	 * 
	 * 
	 */
	public UserTrainingResponseType getEnrollmentCompletionData(String username)
			throws MLMSUtilDAOException {
		/*
		 * Must return a list of users that have completed a select p.username,
		 * ce.name, v.status_desc, p.custom0 from tpt_ce_stud_certification stud
		 * inner join tpt_ext_ce_certification ce on ce.id =
		 * stud.certification_id inner join cmt_person p on p.id = stud.owner_id
		 * inner join tpv_pub_i18n_cert_status v on stud.status = v.code where
		 * v.LOCALE_ID = 'local000000000000001' and stud.status = '100' and
		 * p.custom0 = 'Incomplete'
		 */

		String sql = "select ce.id as \"certId\", ce.name as \"certName\", v.status_desc "
				+ "from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ " inner join cmt_person p on p.id = stud.owner_id "
				+ " inner join tpv_pub_i18n_cert_status v on stud.status = v.code "
				+ " where v.LOCALE_ID = 'local000000000000001' and ce.inherited = 0 "
				+ " and p.username = ?";
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		UserTrainingResponseType trainingResponseType = new UserTrainingResponseType();
		UserTrainingRecordResponseType trainingRecordType = null;
		try {

			con = this.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, username);

			rs = pstmt.executeQuery();

			trainingResponseType.setUsername(username);
			while (rs.next()) {

				trainingRecordType = new UserTrainingRecordResponseType();
				String certId = rs.getString("certId");
				System.out.println("Certification id " + certId);
				if (certId == null) {

					certId = "";
				}
				trainingRecordType.setCertificationID(certId);

				String certName = rs.getString("certName");
				System.out.println("Certification Name " + certName);
				if (certName == null) {

					certName = "";
				}
				trainingRecordType.setCertificationName(certName);

				String statusDesc = rs.getString("status_desc");
				System.out.println("Status Description " + statusDesc);
				if (statusDesc == null) {

					statusDesc = "";
				}
				trainingRecordType.setCertificationStatus(statusDesc);

				trainingResponseType.setUsername(username);
				trainingResponseType.getTrainingRecord()
						.add(trainingRecordType);
				trainingResponseType.setStatusCode(StatusCodeType.MS_200);

			}
			if (trainingResponseType.getStatusCode() == null) {
				trainingResponseType.setStatusCode(StatusCodeType.MS_200);
				trainingResponseType.setErrorCode(ErrorCodeType.ME_810.value());
			}
			/**
			 * no errors
			 */

		} catch (SQLException se) {

			System.out.println(" SQLException thrown message "
					+ se.getMessage());
			System.out.println(" SQLException thrown sqlstate"
					+ se.getSQLState());
			System.out.println(" SQLException thrown errorcode"
					+ se.getErrorCode());
			throw new MLMSUtilDAOException(ErrorCodeType.ME_830.value()
					+ se.getMessage());

		} catch (Exception e) {
			System.out.println(" Exception thrown message " + e.getMessage());
			trainingResponseType.setStatusCode(StatusCodeType.MS_500);
			trainingResponseType.setErrorCode(ErrorCodeType.ME_820.value());
		} finally {

			/*
			 * try{ if(rs != null) rs.close(); if(pstmt != null) pstmt.close();
			 * if(con != null) con.close(); }catch (SQLException e){
			 * System.out.println
			 * ("SQLException: Datasource resources closing error!"); throw new
			 * MLMSUtilDAOException("Datasource resources closing error!", e); }
			 */
		}
		return trainingResponseType;
	}

	public ArrayList<UserTrainingResponseType> getEnrollmentCompletionData(
			ArrayList<String> usernames) throws MLMSUtilDAOException {
		/*
		 * Must return a list of users that have completed a select p.username,
		 * ce.name, v.status_desc, p.custom0 from tpt_ce_stud_certification stud
		 * inner join tpt_ext_ce_certification ce on ce.id =
		 * stud.certification_id inner join cmt_person p on p.id = stud.owner_id
		 * inner join tpv_pub_i18n_cert_status v on stud.status = v.code where
		 * v.LOCALE_ID = 'local000000000000001' and stud.status = '100' and
		 * p.custom0 = 'Incomplete'
		 */
		ArrayList<String> usernameList = usernames;
		ArrayList<UserTrainingResponseType> userTrainingResponses = new ArrayList<UserTrainingResponseType>();

		Iterator<String> it = null;
		UserTrainingResponseType trainingResponseType = new UserTrainingResponseType();
		UserTrainingRecordResponseType trainingRecordType = null;

		String sql = "select ce.id as \"certId\", ce.name as \"certName\", v.status_desc "
				+ "from tpt_ce_stud_certification stud "
				+ "inner join tpt_ext_ce_certification ce on ce.id = stud.certification_id "
				+ " inner join cmt_person p on p.id = stud.owner_id "
				+ " inner join tpv_pub_i18n_cert_status v on stud.status = v.code "
				+ " where v.LOCALE_ID = 'local000000000000001' and ce.inherited = 0 "
				+ " and p.username = ?";
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String username = null;
		int recordCount = 0;
		if (usernameList != null && !usernameList.isEmpty()) {
			it = usernameList.iterator();
			while (it.hasNext()) {
				username = it.next();
				try {

					con = this.getConnection();
					pstmt = con.prepareStatement(sql);
					pstmt.setString(1, username);

					rs = pstmt.executeQuery();

					trainingResponseType.setUsername(username);
					while (rs.next()) {

						trainingRecordType = new UserTrainingRecordResponseType();
						String certId = rs.getString("certId");
						System.out.println("Certification id " + certId);
						if (certId == null) {

							certId = "";
						}
						trainingRecordType.setCertificationID(certId);

						String certName = rs.getString("certName");
						System.out.println("Certification Name " + certName);
						if (certName == null) {

							certName = "";
						}
						trainingRecordType.setCertificationName(certName);

						String statusDesc = rs.getString("status_desc");
						System.out.println("Status Description " + statusDesc);
						if (statusDesc == null) {

							statusDesc = "";
						}
						trainingRecordType.setCertificationStatus(statusDesc);

						trainingResponseType.setUsername(username);
						trainingResponseType.getTrainingRecord().add(
								trainingRecordType);
						trainingResponseType
								.setStatusCode(StatusCodeType.MS_200);

					}
					if (trainingResponseType.getStatusCode() == null && recordCount == 0) {
						trainingResponseType
								.setStatusCode(StatusCodeType.MS_200);
						trainingResponseType.setErrorCode(ErrorCodeType.ME_810
								.value());
					} else if (trainingResponseType.getStatusCode() == null && recordCount > 0){
						trainingResponseType
						.setStatusCode(StatusCodeType.MS_200);
					}
					/**
					 * no errors
					 */

				} catch (SQLException se) {

					System.out.println(" SQLException thrown message "
							+ se.getMessage());
					System.out.println(" SQLException thrown sqlstate"
							+ se.getSQLState());
					System.out.println(" SQLException thrown errorcode"
							+ se.getErrorCode());
					throw new MLMSUtilDAOException(ErrorCodeType.ME_830.value()
							+ se.getMessage());

				} catch (Exception e) {
					System.out.println(" Exception thrown message "
							+ e.getMessage());
					trainingResponseType.setStatusCode(StatusCodeType.MS_500);
					trainingResponseType.setErrorCode(ErrorCodeType.ME_820
							.value());
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
						throw new MLMSUtilDAOException(
								"Datasource resources closing error!", e);
					}

				}// finally
				userTrainingResponses.add(trainingResponseType);
			}// while
			
		}// if
		return userTrainingResponses;
	}

	private Connection getConnection() throws MLMSUtilDAOException {
		int count = 0;
		Connection con = null;
		if (con == null && count == 0) {
			try {
				count++;
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
				System.out.println(" Getting Connection ");
				con = ds.getConnection();

			} catch (NamingException ne) {
				System.out
						.println("JNDI NamingException! Check output console");
				throw new MLMSUtilDAOException(
						ErrorMessageType.DATABASE_UNAVAILABLE.value()
								+ " Exception follows" + ne.getMessage());
			} catch (SQLException se) {
				System.out.println("SQLException! Check output console");
				throw new MLMSUtilDAOException(
						ErrorMessageType.DATABASE_UNAVAILABLE.value()
								+ ". SQLState: " + se.getSQLState().toString());

			}
		}
		return con;
	}
}

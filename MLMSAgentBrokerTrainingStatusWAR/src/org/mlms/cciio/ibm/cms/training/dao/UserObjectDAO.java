package org.mlms.cciio.ibm.cms.training.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class UserObjectDAO {
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public boolean validateUserId(String userId) {
		String sql = null;
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		boolean isValidUserId = false;
		
		sql = "select count(*) from cmt_person where username = ?";

		try {

			con = this.getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, userId.toUpperCase());
			rs = pstmt.executeQuery();
			while(rs.next()){
				if(rs.getInt(1)>0){
					isValidUserId = true;
				}
			}
			
			
		} catch (SQLException sqle) {
			System.out.println(UserObjectDAO.class.getName() + " Exception " + sqle.getMessage());
		} catch (CciioDaoException e) {
			
			e.printStackTrace();
		}
		return isValidUserId;
	}
	/**
	 * 
	 * @return
	 * @throws CciioDaoException
	 */
	private Connection getConnection() throws CciioDaoException {

		Connection con = null;
		Context ctx = null;
		DataSource ds = null;
		try {
			ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("jdbc/lms");
			con = ds.getConnection();

		} catch (NamingException e) {
			System.out.println("JNDI NamingException! Check output console");
			throw new CciioDaoException("DB_CONNECT_EXCEPTION", e);
		} catch (SQLException e) {
			System.out.println("SQLException! Check output console");
			throw new CciioDaoException("DB_CONNECT_EXCEPTION", e);
		}
		return con;
	}

}

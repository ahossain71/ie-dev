package gov.mlms.cciio.cms.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import java.util.logging.*;

public class ShopDBUtil {

	private static Logger logger = Logger.getLogger("ShopDBUtil");

	public static Connection getConnection(String source) throws Exception {
		Connection con = null;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
			if (ds != null) {

				con = ds.getConnection();
			}
			// DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
		} catch (SQLException se) {
			throw se;
		} catch (Exception e) {
			throw e;
		}
		return con;
	}

	public static void freeDBResources(ResultSet rs, Statement statement, Connection conn, String source) throws SQLException {
		try {
			if (rs != null) {
				rs.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE,"SQLException: Datasource resources closing error!");
			throw e;
		}
	}

}

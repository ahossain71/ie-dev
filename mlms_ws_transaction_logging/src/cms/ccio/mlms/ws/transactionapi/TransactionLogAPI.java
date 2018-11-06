package cms.ccio.mlms.ws.transactionapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Clob;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;


import cms.ccio.mlms.ws.transactionapi.exception.WebServiceTransactionLogException;

public class TransactionLogAPI {
	private final static String DATASOURCE_JNDI = "jdbc/lms";
	final DateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public void insertWSTransaction(String webserviceId,
			String transactionCode, String partnerId, String statusCode,
			String xml,String description) throws WebServiceTransactionLogException {
		Connection conn = getConnection();
		
		InetAddress ip;
		
		String web_service_id = webserviceId;
		if(web_service_id==null || web_service_id.isEmpty()){
			throw new WebServiceTransactionLogException("web_service_id cannot be null or an empty string");
		}
		String transaction_code = transactionCode;
		if(transaction_code==null || transaction_code.isEmpty()){
			throw new WebServiceTransactionLogException("transaction_code cannot be null or an empty string");
		}
		String action_date = DATEFORMAT.format(new Date());
		
		String partner_id = partnerId;
		if(partner_id==null || partner_id.isEmpty()){
			throw new WebServiceTransactionLogException("partner_id cannot be null or an empty string");
		}
		String status_code = statusCode;
		if(status_code==null || status_code.isEmpty()){
			throw new WebServiceTransactionLogException("status_code cannot be null or an empty string");
		}
		
		String xml_string = xml;
		Clob xmlGT4000 = null; 
		String xmlLT4000 = "";
		
		
		
		String sql = "insert into AT_MLMS_WS_TRANS_LOG (id,web_service_id,transaction_code," +
				"action_date,partner_id,status_code,description,xml,xml_clob,mlms_host) " +
				"values ('WSTRANS'||at_mlms_ws_trans_seq.nextval,?,?,to_date(?,'DD/MM/YYYY HH24:MI:SS'),?,?,?,?,?,?)";
		try{
			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());
			String strHostAddr = ip.getHostName();
			
			if(xml_string != null){
				if(xml_string.length() > 4000){
					xmlGT4000 = conn.createClob();
					xmlGT4000.setString(0, xml_string);
					xmlLT4000 = "";
				}else{
					//xmlGT4000 = "";
					xmlLT4000 = xml_string;
				}
			}
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, web_service_id);
			pstmt.setString(2, transaction_code);
			pstmt.setString(3, action_date);
			pstmt.setString(4, partner_id);
			pstmt.setString(5, status_code);
			pstmt.setString(6,description);
			pstmt.setString(7,xmlLT4000);
			pstmt.setClob(8, xmlGT4000);
			pstmt.setString(9,strHostAddr);
			System.out.println("Tx insert return value"+pstmt.executeUpdate());
		} catch (SQLException e){
			System.out.println("MLMSWS TransactionLogAPI SQL Exception--- " + e.getMessage());
			e.printStackTrace();
			throw new WebServiceTransactionLogException(e.getMessage());
		} catch(Exception e){
			System.out.println("MLMSWS TransactionLogAPI --- " + e.getMessage());
			e.printStackTrace();
			throw new WebServiceTransactionLogException(e.getMessage());
		}finally{
		
			try{
				if(xmlGT4000 != null){
					xmlGT4000.free();
				}
			}catch(Exception e){
				//not able to free the blob
			}
		}
		
	}

	private Connection getConnection() throws WebServiceTransactionLogException {

		Connection con = null;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(DATASOURCE_JNDI);
			con = ds.getConnection();

			/*
			 * if(logger.isDebugEnabled()){
			 * System.out.println("Successfully connected to datasource: " +
			 * DATASOURCE_JNDI); }
			 */

		} catch (NamingException e) {
			System.out
					.println("JNDI NamingException! Check output console" + e);
			throw new WebServiceTransactionLogException("DB_CONNECT_EXCEPTION",
					e);
		} catch (SQLException e) {
			System.out.println("SQLException! Check output console" + e);
			throw new WebServiceTransactionLogException("DB_CONNECT_EXCEPTION",
					e);
		}
		return con;
	}
}

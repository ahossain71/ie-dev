package org.mlms.cciio.ibm.cms.training.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.mlms.cciio.ibm.cms.retrievetrainingcompletionstatustype.CertificationType;

public class TrainingCertificationDao {
	
	public List<CertificationType> getCertificationInfo(String userId) throws CciioDaoException{
		
		final DateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy");		
		
		List<CertificationType> certList = new ArrayList<CertificationType>();
		String sql = null;
		Connection con = null;
		 ResultSet rs = null;
		 PreparedStatement pstmt = null;
		 
		 
		try {
			
			
			
			
				
			 con = this.getConnection();
			 
			
			DriverManager.registerDriver (new oracle.jdbc.OracleDriver());

			sql = "select p.username as userId, sc.certification_id as certificationId,  " 
							+ "c.custom3 as certificationName, "
							+ "sc.status as certificationStatus, "
							+ "c.custom2 as certificationYear, sc.expired_on as certificationExperationDt " 
							+ "from tpt_ce_stud_certification sc "
							+ "inner join tpt_ext_ce_certification c on sc.certification_id = c.id "
							+ "inner join cmt_person p on p.id = sc.owner_id " 
							+ "where sc.status = 100 and c.inherited = 0 and c.custom3 in ('Individual Marketplace','SHOP Marketplace') and p.username = ? ";
			
		   pstmt = con.prepareStatement(sql);
		    
		    pstmt.setString(1, userId.toUpperCase());
		   rs = pstmt.executeQuery();

		    while (rs.next()) {
		    	CertificationType cert = new CertificationType();
		        //String userIdout = rs.getString("userId");
		        
		        String certificationId = rs.getString("certificationId");
		        if(null == certificationId) certificationId = "";		        
		        cert.setCertificationId(certificationId);
		        
		        String certificationName = rs.getString("certificationName");
		        if(null == certificationName) certificationName = "";
		        cert.setCertificationName(certificationName);
		        
		        String certificationStatus = rs.getString("certificationStatus");
		        if(null == certificationStatus) certificationStatus = "";
		        cert.setCertificationStatus(certificationStatus);
		        
		        String certificationYear = rs.getString("certificationYear");
		        if(null == certificationYear) certificationYear = "";
		        cert.setCertificationYear(certificationYear);
		        
		        Date certificationExperationDt = rs.getDate("certificationExperationDt");
		        if(certificationExperationDt == null) 
		        	certificationExperationDt = new Date(); //today
		        cert.setCertificationExperationDate(DATEFORMAT.format(certificationExperationDt));
				certList.add(cert);
				System.out.println("AGENTBROKER SOAP RESPONSE- User: " + userId.toUpperCase()
						+ " Certification Id: " + certificationId
						+ " Certification Name: " + certificationName
						+ " Certification Status: " + certificationStatus
						+ " Certification Year: " + certificationYear
						+ " Certification Expiration date: " + certificationExperationDt);
	
		    }
 
		} catch (SQLException e) {
 
			System.out.println("SQLException! Check output console: " + sql);
			throw new CciioDaoException("CCIIO_SQLException", e);
 
		} catch (Exception e){
			throw new CciioDaoException(e);
		} finally {
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			}catch (SQLException e){
				System.out.println("SQLException: Datasource resources closing error!");
				throw new CciioDaoException("Datasource resources closing error!", e);
			}
		}
		
		
		
		return certList;
	}
	
	private Connection getConnection() throws CciioDaoException{

		Connection con = null;
		Context ctx = null;
		DataSource ds = null;
			try {
				 ctx = new InitialContext();
				 ds = (DataSource) ctx.lookup("jdbc/lms");
				con = ds.getConnection();
				
			}catch (NamingException e) {	 
				System.out.println("JNDI NamingException! Check output console");
				throw new CciioDaoException("DB_CONNECT_EXCEPTION", e);	 
			}catch (SQLException e) {	 
				System.out.println("SQLException! Check output console");
				throw new CciioDaoException("DB_CONNECT_EXCEPTION", e);	 
			} 
		return con;
	}

}

/**
 * 
 */
package gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.dao;

import gov.hios.mlms.cciio.cms.assisteragreementstatusresponse.AssisterCertificationStatusType;
import gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.exception.HiosAssisterAgreementStatusException;
import static gov.hios.mlms.cciio.cms.hiosassisteragreement.service.HiosAssisterAgreementService.DATEFORMAT;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.logging.Logger;


/**
 * @author xnieibm
 *
 */
public class HiosAssisterAgreementStatusDao {
	
//final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Logger logger = Logger.getLogger("HiosAssisterAgreementStatusDao.class");
	private Connection con = null;
	
	public List<AssisterCertificationStatusType> getAssisterAgreementStatus(java.sql.Date dateTime)
		throws HiosAssisterAgreementStatusException{
		
		List<AssisterCertificationStatusType> assisterAgreements = new ArrayList<AssisterCertificationStatusType>();
		
		String sql = "select p.custom6 as assisterId, c.name as certificationName, " +
				"sc.status as certificationStatus, sc.acquired_on as certificationAcquiredDt, " +
				"ai.ATYPE as assisterType " +
				"from tpt_ce_stud_certification sc " +
				"inner join tpt_ext_ce_certification c on sc.certification_id = c.id " +
				"inner join cmt_person p on p.id = sc.owner_id  " +
				"inner join AD_MLMS_PERSON_ASSISTER_INFO AI on AI.person_id = sc.owner_id " +
				"where sc.status = 100 and ai.atype in ('Navigator','FederalIPA') " +
				"and sc.acquired_on >= ?";
		
		try{
			Connection con = this.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);
			
//			Date dt = DATEFORMAT.parse(dateTime);
//			pstmt.setDate(1, new java.sql.Date(dt.getDate()));
			pstmt.setDate(1,dateTime);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()){
				AssisterCertificationStatusType aast = new AssisterCertificationStatusType();
				
				String assisterId = rs.getString("assisterId");
				if(null == assisterId) assisterId = "";
				aast.setAssisterId(assisterId);
				
				String certificationName = rs.getString("certificationName");
				if(certificationName == null) certificationName = "";
				aast.setCertificationName(certificationName);
				
				String certificationStatus = rs.getString("certificationStatus");
				if (certificationStatus == null) certificationStatus = "";
				aast.setCertificationStatus(certificationStatus);
				
				Date cadt = rs.getDate("certificationAcquiredDt");
				if(cadt == null){
					aast.setCertificationAcquiredDate("1980-01-01");
				}
				aast.setCertificationAcquiredDate(DATEFORMAT.format(cadt));
				
				aast.setRequestDatetime(DATEFORMAT.format(dateTime));
				aast.setQueryDatetime(DATEFORMAT.format(new Date()));
				
				assisterAgreements.add(aast);
			}
		}		
		catch (Exception e){
			throw new HiosAssisterAgreementStatusException("HiosAssisterAgreementStatusDao error!", e);
		}
		
		return assisterAgreements;
	}
	
	
	private Connection getConnection() throws HiosAssisterAgreementStatusException{
		int count = 0;
		if(con == null && count == 0){
			try {
				count++;
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
				con = ds.getConnection();
				
			}catch (NamingException e) {	 
				logger.severe("JNDI NamingException! Check output console: " + e.getMessage());
				throw new HiosAssisterAgreementStatusException("DB_CONNECT_EXCEPTION", e);	 
			}catch (SQLException e) {	 
				logger.severe("SQLException! Check output console " + e.getMessage());
				throw new HiosAssisterAgreementStatusException("DB_CONNECT_EXCEPTION", e);	 
			}
		}
		return con;
	}

}

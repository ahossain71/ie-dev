/**
 * 
 */
package gov.mlms.cciio.cms.smallbusinesshealthoption.dao;

import gov.mlms.cciio.cms.cciiowebservicecommontype.UsStateType;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.NpnEntityType;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.ShopAgreementType;
import gov.mlms.cciio.cms.smallbusinesshealthoption.exception.ShopAgreementException;

import java.sql.Connection;
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

/**
 * @author xnieibm
 *
 */
public class ShopAgreementStatusDao {
	
	final DateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy");	
	
	public List<NpnEntityType> getNpnInfo(String userId) throws ShopAgreementException {
		
		List<NpnEntityType> npnEntities = new ArrayList<NpnEntityType>();
		
		try {

			//Connection con = this.getConnection();
			
			//TODO: retrieve NpnEntityInfo
			
			npnEntities.add(this.buildEmpltyNpnEntity());
 
		} catch (Exception e){
			throw new ShopAgreementException(e);
		}
		
		return npnEntities;
	}
	
	public List<ShopAgreementType> getShopAgreementInfo(String userId) throws ShopAgreementException{
		boolean debug = System.getProperty("mlms.debug", "true").equalsIgnoreCase("true");
		List<ShopAgreementType> shopAgreementInfo = new ArrayList<ShopAgreementType>();
		
		String sql = "select  p.username, c.custom3 as shopAgreementName, " +
				"sc.status as shopAgreementStatus, " +
				"sc.expired_on as shopAgreementExpirationDate, " +
				"sc.acquired_on as shopAgreemenAcquiredDate " +
				"from tpt_ce_stud_certification sc " +
				"inner join tpt_ext_ce_certification c on sc.certification_id = c.id " +
				"inner join cmt_person p on p.id = sc.owner_id " +
				"where sc.status = 100 and c.inherited = 0 and c.custom3 = 'SHOP Marketplace'" +
				" and extract(year from sc.expired_on) >= extract(year from sysdate) -1 " +
				" and p.username = ? and ROWNUM <= 3 order by sc.acquired_on DESC";
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			con = this.getConnection();
		    pstmt = con.prepareStatement(sql);
		    
		    pstmt.setString(1, userId.toUpperCase());
		    if(debug){
		    	System.out.println( ShopAgreementStatusDao.class.getSimpleName() + " query user id value: " + userId.toUpperCase() + "\n");
		    	System.out.println( ShopAgreementStatusDao.class.getSimpleName() + " query\n\t " + sql + "\n");
		    }
		   
		    rs = pstmt.executeQuery();
		  
		    if(debug && rs.isBeforeFirst()){
		    	System.out.println(this.getClass() + " Resultset returned results ");
		    	System.out.println(this.getClass() + " Prepared Statement, get metat data column count " +pstmt.getMetaData().getColumnCount());
		    }
			
		    while (rs.next()) {
		    	ShopAgreementType sat = new ShopAgreementType();
		    	
		        
		        String shopAgreementName = rs.getString("shopAgreementName");
		        if(debug){
		        	System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " shopAgreementName " + shopAgreementName );
		        }
		        if(null == shopAgreementName) shopAgreementName = "";		        
		        sat.setShopAgreementName(shopAgreementName);
		        
		        String shopAgreementStatus = rs.getString("shopAgreementStatus");
		        if(debug){
		        	System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " shopAgreementStatus " + shopAgreementStatus);
		        }
		        if(null == shopAgreementStatus) shopAgreementStatus = "";
		        sat.setShopAgreementStatus(shopAgreementStatus);
		        
		        Date shopAgreementExperationDate = rs.getDate("shopAgreementExpirationDate");
		        if(debug){
		        	System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " shopAgreementExpirationDate  " + shopAgreementExperationDate );
		        }
		        if(shopAgreementExperationDate == null) 
		        	shopAgreementExperationDate = new Date(); //today
		        sat.setShopAgreementExperationDate(DATEFORMAT.format(shopAgreementExperationDate));
		        
		        Date shopAgreementAcquiredDate = rs.getDate("shopAgreemenAcquiredDate");
		        if(debug){
		        	System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " shopAgreementAcquiredDate  " + shopAgreementAcquiredDate );
		        }
		        if(shopAgreementAcquiredDate == null)
		        	sat.setShopAgreemenAcquiredDate("");
		        else sat.setShopAgreemenAcquiredDate(DATEFORMAT.format(shopAgreementAcquiredDate));
		        shopAgreementInfo.add(sat);
		        if(debug){
		        System.out.println("user id " + userId + " shop agreement name " + shopAgreementName +
		        		" shop agreement status " + shopAgreementStatus +
		        		" shop agreement exp date " + sat.getShopAgreementExperationDate() +
		        		" shop agreement acq date " + sat.getShopAgreemenAcquiredDate()); 
		        }
		    }

 
		
		}catch (Exception e){
			System.out.println(ShopAgreementStatusDao.class.getSimpleName() + "\n exception message: " + e.getMessage()); 
			throw new ShopAgreementException(e);
		}
		finally {
			if(debug){
				System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " and exception was thrown, return list length " + shopAgreementInfo.size() );
				 
			}
			try{
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(con != null) con.close();
			}catch (SQLException e){
				System.out.println("SQLException: Datasource resources closing error!");
				throw new ShopAgreementException("Datasource resources closing error!", e);
			}
		}
		if(debug){
			System.out.println(ShopAgreementStatusDao.class.getSimpleName() + " return list length = " + shopAgreementInfo.size());
		}
		return shopAgreementInfo;
	}
	
	private Connection getConnection() throws ShopAgreementException{

		Connection con = null;
			try {
				Context ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("jdbc/lms");
				con = ds.getConnection();
				
			}catch (NamingException e) {	 
				System.out.println("JNDI NamingException! Check output console");
				throw new ShopAgreementException("DB_CONNECT_EXCEPTION", e);	 
			}catch (SQLException e) {	 
				System.out.println("SQLException! Check output console");
				throw new ShopAgreementException("DB_CONNECT_EXCEPTION", e);	 
			}
		return con;
	}
	
	private NpnEntityType buildEmpltyNpnEntity(){
		
		NpnEntityType npn = new NpnEntityType();
		npn.setNpnEntityName("EmptyNpnName");
		npn.setNpnEntityType("Individual NPN");
		npn.setNpnId("0123456789");
		npn.getNpnState().add(UsStateType.MD);
		
		return npn;
	}
	
}

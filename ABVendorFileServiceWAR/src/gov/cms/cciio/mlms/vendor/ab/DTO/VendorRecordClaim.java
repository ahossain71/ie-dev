package gov.cms.cciio.mlms.vendor.ab.DTO;

import java.sql.Date;

public class VendorRecordClaim {

	private String raw_feed_id;
	private String username;
	private Date created_date;
	private String status_code;
	
	//getter
	public String getRawFeedId(){
		return raw_feed_id;
	}
	
	public String getUserName(){
		return username;
	}
	
	public Date getCreatedDate(){
		return created_date;
	}
	
	public String getStatusCode(){
		return status_code;
	}
	
	//setter
	public void  setRawFeedId(String id){
		this.raw_feed_id = id;
	}
	
	public void  setUserName(String uName){
		this.username = uName;
	}
	
	public void  setCreatedDate(Date dt){
		this.created_date = dt;
	}
	
	public void  setStatusCode(String code){
		this.status_code = code;
	}
}

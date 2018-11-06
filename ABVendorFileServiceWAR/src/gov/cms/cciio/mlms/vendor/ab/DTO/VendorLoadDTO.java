package gov.cms.cciio.mlms.vendor.ab.DTO;

import java.sql.Date;

/**
 * 
 * @author anu
 *
 *
 */
public class VendorLoadDTO {
	private String id ;
	private int	file_record_count;
	private Date created_date; 
	private String vendor_name;
	private String file_identifier; 
	private String file_date;
	private String file_time;
	
	//getter
	public String getId(){
		return id;
	}
	
	public int getFileRecordCount(){
		return file_record_count;
	}
	
	public Date getCreatedDate(){
		return created_date;
	}
	
	public String getVendorName(){
		return vendor_name;
	}
	
	public String getFileIdentifier(){
		return file_identifier;
	}
	
	public String getFileDate(){
		return file_date;
	}
	
	public String getFileTime(){
		return file_time;
	}
	
	//setter
	public void setId(String id){
		this.id = id;
	}
	
	public void setFileRecordCount(int cnt){
		this.file_record_count = cnt;
	}
	
	public void setCreatedDate(Date dt){
		this.created_date = dt;
	}
	
	public  void setVendorName(String name){
		this.vendor_name = name;
	}
	
	public void setFileIdentifier(String fileIdentifier){
		this.file_identifier = fileIdentifier;
	}
	
	public void setFileDate(String fileDate){
		this.file_date = fileDate;
	}
	
	public void setFileTime(String fileTime){
		this.file_time = fileTime;
	}
}

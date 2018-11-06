package gov.cms.cciio.mlms.vendor.ab.DTO;

import java.sql.Date;

public class VendorRawFeedDTO {
	private String id;
	private String language_id;
	private String language;
	private Date created_date;
	private String claim_token;
	private String curriculum_name;
	private String curriculum_year;
	private String vendor_load_id;
	
	public String getId(){
		return id;
	}
	
	public String getLanguageId(){
		return language_id;
	}
	
	public String getLanguage(){
		return language;
	}
	
	public Date getCreatedDate(){
		return created_date;
	}
	
	public String getClaimToken(){
		return claim_token;
	}
	
	public String getCurriculumName(){
		return curriculum_name;
	}
	
	public String getCurriculumYear(){
		return curriculum_year;
	}
	
	public String getVendorLoadId(){
		return vendor_load_id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void  setLanguageId(String langId){
		this.language_id = langId;
	}
	
	public void  setLanguage(String lang){
		this.language = lang;
	}
	
	public void  setCreatedDate(Date dt){
		this.created_date = dt;
	}
	
	public void  setClaimToken(String claimToken){
		this.claim_token = claimToken;
	}
	
	public void  setCurriculumName(String currName){
		this.curriculum_name = currName;
	}
	
	public void  setCurriculumYear(String currYear){
		this.curriculum_year = currYear;
	}
	
	public void  setVendorLoadId(String vendorLoadId){
		this.vendor_load_id = vendorLoadId;
	}
}

package gov.cms.cciio.mlms.vendor.ab.DTO;

public class CurriculumRefDTO {
	private String id;
	private String curriculum_code;
	private String curriculum_name;
	
	public String getId(){
		return id;
	}
	
	public String getCurriculumCode(){
		return curriculum_code;
	}
	
	public String getCurriculumName(){
		return curriculum_name;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void  setCurriculumCode(String curriculumCode){
		this.curriculum_code = curriculumCode;
	}
	
	public void setCurriculumName(String curriculumName){
		this.curriculum_name = curriculumName;
	}
}

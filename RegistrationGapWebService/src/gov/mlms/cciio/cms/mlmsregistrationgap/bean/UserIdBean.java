package gov.mlms.cciio.cms.mlmsregistrationgap.bean;

public class UserIdBean {
	
	private String username;
	private String personId;
	private String certificationId;
	private String curriculum;
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the person_id
	 */
	public String getPersonId() {
		return personId;
	}
	/**
	 * @param person_id the person_id to set
	 */
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	/**
	 * @return the certificationId
	 */
	public String getCertificationId() {
		return certificationId;
	}
	/**
	 * @param certificationId the certificationId to set
	 */
	public void setCertificationId(String certificationId) {
		this.certificationId = certificationId;
	}
	/**
	 * @return the curriculum
	 */
	public String getCurriculum() {
		return curriculum;
	}
	/**
	 * @param curriculum the curriculum to set
	 */
	public void setCurriculum(String curriculum) {
		this.curriculum = curriculum;
	}
	

}

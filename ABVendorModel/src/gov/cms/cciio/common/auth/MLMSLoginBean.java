package gov.cms.cciio.common.auth;

import java.util.Date;

/**
 * To avoid using hidden fields (visible by viewing page source),
 * this bean is used by MLMS custom login page to hold user data in session
 * e.g. 
 * @author Steve M
 * updated Jul 18, 2016 added EIDM Account Create Date
 * updtaed Sept 6, 2016 added isSHOPComplete and SHOPTrainingDate
 *
 */
public class MLMSLoginBean implements java.io.Serializable{
    /**
	 * + userProfile.getAddress().getStreetAddress() + "\n\t\t"
						+ userProfile.getAddress().getStreetAddress2()+ "\n\t\t"
						+ userProfile.getAddress().getCity() + ", " 
						+ userProfile.getAddress().getState() + " " 
						+ userProfile.getAddress().getZipcode() + "-" 
						+ userProfile.getAddress().getZipcodeExtension());
						+ userProfile.getEmail().getEmailId());
						+ userProfile.getPhone().getCountryCode() + " " 
						+ userProfile.getPhone().getPrimaryPhone());
						roleInfoType.getRoleName() );
						rollAttribute.getName()
	 */
	private static final long serialVersionUID = 1L;
	private String uid = null;
    private String fname = null;
    private String lname = null;
    private String role = null;
    private String group = null;
    private boolean isComplete = false;
    private boolean isSHOPComplete = false;
    private String password = null;
    private String streetAddress = null;
    private String streetAddress2 = null;
    private String city = null;
    private String state = null;
    private String zipcode = null;
    private String zipcodeExtension = null;
    private String email = null;
    private String guid = null;
    private String countryCode = null;
    private String primaryPhone = null;
    private String roleAttribute = null;
    private String roleAttributeExpDt = null;
    private String encryptedUsername = null;
    private boolean isDev = false;
    private boolean isEIDM = false;
    private Date agentBrokerRoleEffectiveDate = null;
    private Date agentBrokerRoleHistoricalEffectiveDate = null;
    private Date eidmAccountCreateDate = null;
    private String rawRoleEffectiveDate = null;
    
    

	private Date ffmTrainingDate = null;
	private Date SHOPTrainingDate = null;
	
    private String sessionId = null;
    private boolean certReq = false;
    
    
    private String loginUrl = "/Saba/Web/Main";
   
    public String getRawRoleEffectiveDate(){
    	return rawRoleEffectiveDate;
    }
    
    public void setRawRoleEffectiveDate(String rawEffDate){
    	this.rawRoleEffectiveDate = rawEffDate;
    }

    public String getUsername() {
        return uid;
    }
    
    public void setUsername(String username) {
    	this.uid= null;
        this.uid = username;
    }

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = null;
		this.group = group;
		
	}

	/**
	 * @return the roles
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRole(String role) {
		this.role = null;
		this.role = role;
	}

	/**
	 * @return the lname
	 */
	public String getLname() {
		return lname;
	}

	/**
	 * @param lname the lname to set
	 */
	public void setLname(String lname) {
		this.lname = null;
		this.lname = lname;
	}

	/**
	 * @return the fname
	 */
	public String getFname() {
		return fname;
	}

	/**
	 * @param fname the fname to set
	 */
	public void setFname(String fname) {
		this.fname = null;
		this.fname = fname;
	}

	/**
	 * @return the loginUrl
	 */
	public String getLoginUrl() {
		return loginUrl;
	}

	/**
	 * @param loginUrl the loginUrl to set
	 */
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = null;
		this.loginUrl = loginUrl;
	}

	/**
	 * @return the isComplete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * @param isComplete the isComplete to set
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = null;
		this.password = password;
	}
/**
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}**/

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = null;
		this.streetAddress = streetAddress;
	}

	/**
	 * @return the streetAddress2
	 */
	public String getStreetAddress2() {
		return streetAddress2;
	}

	/**
	 * @param streetAddress2 the streetAddress2 to set
	 */
	public void setStreetAddress2(String streetAddress2) {
		this.streetAddress2 = null;
		this.streetAddress2 = streetAddress2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = null;
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = null;
		this.state = state;
	}

	public String getZipcode() {
		this.zipcode = null;
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = null;
		this.zipcode = zipcode;
	}

	public String getZipcodeExtension() {
		return zipcodeExtension;
	}

	public void setZipcodeExtension(String zipcodeExtension) {
		this.zipcodeExtension = zipcodeExtension;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getPrimaryPhone() {
		return primaryPhone;
	}

	public void setPrimaryPhone(String primaryPhone) {
		this.primaryPhone = primaryPhone;
	}

	/**
	 * @return the roleAttribute
	 */
	public String getRoleAttribute() {
		return roleAttribute;
	}

	/**
	 * @param roleAttribute the roleAttribute to set
	 */
	public void setRoleAttribute(String roleAttribute) {
		this.roleAttribute = null;
		this.roleAttribute = roleAttribute;
	}

	/**
	 * @return the roleAttributeExpDt
	 */
	public String getRoleAttributeExpDt() {
		return roleAttributeExpDt;
	}

	/**
	 * @param roleAttributeExpDt the roleAttributeExpDt to set
	 */
	public void setRoleAttributeExpDt(String roleAttributeExpDt) {
		this.roleAttributeExpDt = roleAttributeExpDt;
	}

	/**
	 * @return the encryptedUsername
	 */
	public String getEncryptedUsername() {
		return encryptedUsername;
	}

	/**
	 * @param encryptedUsername the encryptedUsername to set
	 */
	public void setEncryptedUsername(String encryptedUsername) {
		this.encryptedUsername = encryptedUsername;
	}

	/**
	 * @return the isDev
	 */
	public boolean isDev() {
		return isDev;
	}

	/**
	 * @param isDev the isDev to set
	 */
	public void setDev(boolean isDev) {
		this.isDev = isDev;
	}

	/**
	 * @return the isEIDM
	 */
	public boolean isEIDM() {
		return isEIDM;
	}

	/**
	 * @param isEIDM the isEIDM to set
	 */
	public void setEIDM(boolean isEIDM) {
		this.isEIDM = isEIDM;
	}

	/**
	 * @return the agentBrokerRoleEffectiveDate
	 */
	public Date getAgentBrokerRoleEffectiveDate() {
		return agentBrokerRoleEffectiveDate;
	}

	/**
	 * @param agentBrokerRoleEffectiveDate the agentBrokerRoleEffectiveDate to set
	 */
	public void setAgentBrokerRoleEffectiveDate(
			Date agentBrokerRoleEffectiveDate) {
		this.agentBrokerRoleEffectiveDate = agentBrokerRoleEffectiveDate;
	}

	/**
	 * @return the ffmTrainingDate
	 */
	public Date getFfmTrainingDate() {
		return ffmTrainingDate;
	}

	/**
	 * @param ffmTrainingDate the ffmTrainingDate to set
	 */
	public void setFfmTrainingDate(Date ffmTrainingDate) {
		this.ffmTrainingDate = ffmTrainingDate;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public boolean isCertReq() {
		return certReq;
	}

	public void setCertReq(boolean certReq) {
		this.certReq = certReq;
	}
	public Date getAgentBrokerRoleHistoricalEffectiveDate() {
		return agentBrokerRoleHistoricalEffectiveDate;
	}

	public void setAgentBrokerRoleHistoricalEffectiveDate(
			Date agentBrokerRoleHistoricalEffectiveDate) {
		this.agentBrokerRoleHistoricalEffectiveDate = agentBrokerRoleHistoricalEffectiveDate;
	}

	/**
	 * @return the eidmAccountCreateDate
	 */
	public Date getEidmAccountCreateDate() {
		return eidmAccountCreateDate;
	}

	/**
	 * @param eidmAccountCreateDate the eidmAccountCreateDate to set
	 */
	public void setEidmAccountCreateDate(Date eidmAccountCreateDate) {
		this.eidmAccountCreateDate = eidmAccountCreateDate;
	}

	/**
	 * @return the isSHOPComplete
	 */
	public boolean isSHOPComplete() {
		return isSHOPComplete;
	}

	/**
	 * @param isSHOPComplete the isSHOPComplete to set
	 */
	public void setSHOPComplete(boolean isSHOPComplete) {
		this.isSHOPComplete = isSHOPComplete;
	}

	/**
	 * @return the sHOPTrainingDate
	 */
	public Date getSHOPTrainingDate() {
		return SHOPTrainingDate;
	}

	/**
	 * @param sHOPTrainingDate the sHOPTrainingDate to set
	 */
	public void setSHOPTrainingDate(Date sHOPTrainingDate) {
		SHOPTrainingDate = sHOPTrainingDate;
	}
	

	
    
    
}

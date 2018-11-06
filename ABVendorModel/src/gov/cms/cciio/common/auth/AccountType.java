package gov.cms.cciio.common.auth;

import gov.cms.cciio.common.util.CommonUtil;

import java.util.Date;


/**
 * Author Feilung Wong
 * 
 * Modified by Steve Meyer for MLMS
 */
public class AccountType {
    private String system;
    private String code;
    private String defaultDomain;
    private String domainID;
    private String securityList;
    private String roleID;
    private String audienceType;
    private String audienceTypeID;
    private String personType;
    private String weight;
    private String ffmRoleStatus;
    private String shopRoleStatus;
    private Date ffmEffectiveRoleGrantDate;
   
    
    /**
     * @return the system (e.g. AKO, ATRRS)
     */
    public String getSystem() {
        return system;
    }
    /**
     * @param system the system to set (e.g. AKO, ATRRS)
     */
    public void setSystem(String system) {
        this.system = system;
    }
    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
    /**
     * @return the default domain
     */
    public String getDefaultDomain() {
        return defaultDomain;
    }
    /**
     * @param defaultDomain the default domain to set
     */
    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }
    /**
     * @return the domain ID
     */
    public String getDomainID() {
        return domainID;
    }
    /**
     * @param domainID the domain ID to set
     */
    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }
    /**
     * @return the security list
     */
    public String getSecurityList() {
        return securityList;
    }
    /**
     * @param securityList the security list to set
     */
    public void setSecurityList(String securityList) {
        this.securityList = securityList;
    }
    /**
     * @return the role ID
     */
    public String getRoleID() {
        return roleID;
    }
    /**
     * @param roleID the role ID to set
     */
    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }
    /**
     * @return the audience type
     */
    public String getAudienceType() {
        return audienceType;
    }
    /**
     * @param audienceType the audience type to set
     */
    public void setAudienceType(String audienceType) {
        this.audienceType = audienceType;
    }
    /**
     * @return the audience type ID
     */
    public String getAudienceTypeID() {
        return audienceTypeID;
    }
    /**
     * @param audienceTypeID the audience type ID to set
     */
    public void setAudienceTypeID(String audienceTypeID) {
        this.audienceTypeID = audienceTypeID;
    }
    /**
     * @return the person type
     */
    public String getPersonType() {
        return personType;
    }
    /**
     * @param personType the person type to set
     */
    public void setPersonType(String personType) {
        this.personType = personType;
    }
    /**
     * @return the weight
     */
    public String getWeight() {
        return weight;
    }
    /**
     * @param weight the weight to set
     */
    public void setWeight(String weight) {
        this.weight = weight;
    }
 
    /**
	 * @return the ffmRoleStatus
	 */
	public String getFfmRoleStatus() {
		return ffmRoleStatus;
	}
	/**
	 * @param ffmRoleStatus the ffmRoleStatus to set
	 */
	public void setFfmRoleStatus(String ffmRoleStatus) {
		this.ffmRoleStatus = ffmRoleStatus;
	}
	/**
     * @return a String that contains all attributes defined in <i>AccountType</i>
     */
    public String toString() {
        String eol = CommonUtil.EOL;
        StringBuilder sb = new StringBuilder();

        sb.append("System        : ");
        sb.append(system==null?"":system);
        sb.append(eol);
        sb.append("Code          : ");
        sb.append(code==null?"":code);
        sb.append(eol);
        sb.append("Default Domain: ");
        sb.append(defaultDomain==null?"":defaultDomain);
        sb.append(eol);
        sb.append("Domain ID     : ");
        sb.append(domainID==null?"":domainID);
        sb.append(eol);
        sb.append("Security List : ");
        sb.append(securityList==null?"":securityList);
        sb.append(eol);
        sb.append("Role ID       : ");
        sb.append(roleID==null?"":roleID);
        sb.append(eol);
        sb.append("Aud. Type     : ");
        sb.append(audienceType==null?"":audienceType);
        sb.append(eol);
        sb.append("Aud. Type ID  : ");
        sb.append(audienceTypeID==null?"":audienceTypeID);
        sb.append(eol);
        sb.append("Person Type   : ");
        sb.append(personType==null?"":personType);
        sb.append(eol);
        sb.append("Weight        : ");
        sb.append(weight==null?"":weight);
        return sb.toString();
    }
	public Date getFfmEffectiveRoleGrantDate() {
		return ffmEffectiveRoleGrantDate;
	}
	public void setFfmEffectiveRoleGrantDate(Date ffmRoleGrantDate) {
		this.ffmEffectiveRoleGrantDate = ffmRoleGrantDate;
	}
	/**
	 * @return the shopRoleStatus
	 */
	public String getShopRoleStatus() {
		return shopRoleStatus;
	}
	/**
	 * @param shopRoleStatus the shopRoleStatus to set
	 */
	public void setShopRoleStatus(String shopRoleStatus) {
		this.shopRoleStatus = shopRoleStatus;
	}

}

package gov.hhs.cms.registrationgap.dto;

import gov.hhs.cms.eidm.ws.waas.domain.role.RoleAttributeType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RoleInfoType;

import java.util.Date;
import java.util.List;

public class RegistrationGapUserDTO {
  private String userName;
  private String guid;
  private String firstName;
  private String lastName;
  private String errorMessage;
  private String personId;
  
  private Boolean ffmComplete = false;
  private Boolean shopComplete = false;
  private Boolean responseWasNull = false;
  private Boolean ffmAgentBroker = false;
  private Boolean ffmTrainingAccess = false;
  
  private Date ffmExpirationDate;
  private Date agentBrokerEffectiveDate;
  private String rawAgentBrokerEffectiveDate;
  private Date accountCreateDate;
  private Date historicalRoleGrantDate;
  private Date shopExpirationdate;
  
  private List<RoleInfoType> roleList = null;
  private List<RoleAttributeType> roleAttributeList = null;
  
  private int loa = -1;
 
  public RegistrationGapUserDTO(){
	  super();
  }
  
  public String getRawAgentBrokerEffectiveDate(){
	  return rawAgentBrokerEffectiveDate;
  }
  
  public void setRawAgentBrokerEffectiveDate(String rawEffDt){
	  this.rawAgentBrokerEffectiveDate = rawEffDt;
  }
  /**
 * @return the firstName
 */
public String getFirstName() {
	return firstName;
}
/**
 * @param firstName the firstName to set
 */
public void setFirstName(String firstName) {
	this.firstName = firstName;
}
/**
 * @return the lastName
 */
public String getLastName() {
	return lastName;
}
/**
 * @param lastName the lastName to set
 */
public void setLastName(String lastName) {
	this.lastName = lastName;
}
/**
 * @return the ffmComplete
 */
public Boolean getRegistrationComplete() {
	return ffmComplete;
}
/**
 * @param ffmComplete the ffmComplete to set
 */
public void setRegistrationComplete(Boolean registrationComplete) {
	this.ffmComplete = registrationComplete;
}
/**
 * @return the accountCreateDate
 */
public Date getAccountCreateDate() {
	return accountCreateDate;
}
/**
 * @param accountCreateDate the accountCreateDate to set
 */
public void setAccountCreateDate(Date accountCreateDate) {
	this.accountCreateDate = accountCreateDate;
}
  
  
/**
 * @return the userName
 */
public String getUsername() {
	return userName;
}
/**
 * @param userName the userName to set
 */
public void setUsername(String username) {
	this.userName = username;
}
/**
 * @return the guid
 */
public String getGuid() {
	return guid;
}
/**
 * @param guid the guid to set
 */
public void setGuid(String guid) {
	this.guid = guid;
}
/**
 * @return the accountCreatedate
 */

/**
 * @return the ffmComplete
 */
public boolean isFFMComplete() {
	return ffmComplete;
}
/**
 * @return the userName
 */
public String getUserName() {
	return userName;
}
/**
 * @param userName the userName to set
 */
public void setUserName(String userName) {
	this.userName = userName;
}
/**
 * @return the shopComplete
 */
public boolean isShopComplete() {
	return shopComplete;
}
/**
 * @param shopComplete the shopComplete to set
 */
public void setShopComplete(boolean shopComplete) {
	this.shopComplete = shopComplete;
}
/**
 * @param ffmComplete the ffmComplete to set
 */
public void setFFMComplete(boolean complete) {
	this.ffmComplete = complete;
}
/**
 * @return the agentBrokerEffectiveDate
 */
public Date getAgentBrokerEffectiveDate() {
	return agentBrokerEffectiveDate;
}
/**
 * @param agentBrokerEffectiveDate the agentBrokerEffectiveDate to set
 */
public void setAgentBrokerEffectiveDate(Date agentBrokerEffectiveDate) {
	this.agentBrokerEffectiveDate = agentBrokerEffectiveDate;
}
/**
 * @return the ffmExpirationDate
 */
public Date getFFMExpirationDate() {
	return ffmExpirationDate;
}
/**
 * @param ffmExpirationDate the ffmExpirationDate to set
 */
public void setFFMExpirationDate(Date ffmExpirationDate) {
	this.ffmExpirationDate = ffmExpirationDate;
}
/**
 * @return the historicalRoleGrantDate
 */
public Date getHistoricalRoleGrantDate() {
	return historicalRoleGrantDate;
}
/**
 * @param historicalRoleGrantDate the historicalRoleGrantDate to set
 */
public void setHistoricalRoleGrantDate(Date historicalRoleGrantDate) {
	this.historicalRoleGrantDate = historicalRoleGrantDate;
}
/**
 * @return the errorMessage
 */
public String getErrorMessage() {
	return errorMessage;
}
/**
 * @param errorMessage the errorMessage to set
 */
public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
}
/**
 * @return the responseWasNull
 */
public Boolean getResponseWasNull() {
	return responseWasNull;
}
/**
 * @param responseWasNull the responseWasNull to set
 */
public void setResponseWasNull(Boolean responseWasNull) {
	this.responseWasNull = responseWasNull;
}
/**
 * @return the personId
 */
public String getPersonId() {
	return personId;
}
/**
 * @param personId the personId to set
 */
public void setPersonId(String personId) {
	this.personId = personId;
}
/**
 * @return the shopExpirationdate
 */
public Date getShopExpirationdate() {
	return shopExpirationdate;
}
/**
 * @param shopExpirationdate the shopExpirationdate to set
 */
public void setShopExpirationdate(Date shopExpirationdate) {
	this.shopExpirationdate = shopExpirationdate;
}
/**
 * @return the loa
 */
public int getLoa() {
	return loa;
}
/**
 * @param loa the loa to set
 */
public void setLoa(int loa) {
	this.loa = loa;
}
/**
 * @return the ffmAgentBroker
 */
public Boolean getFfmAgentBroker() {
	return ffmAgentBroker;
}
/**
 * @param ffmAgentBroker the ffmAgentBroker to set
 */
public void setFfmAgentBroker(Boolean ffmAgentBroker) {
	this.ffmAgentBroker = ffmAgentBroker;
}
/**
 * @return the ffmTrainingAccess
 */
public Boolean getFfmTrainingAccess() {
	return ffmTrainingAccess;
}
/**
 * @param ffmTrainingAccess the ffmTrainingAccess to set
 */
public void setFfmTrainingAccess(Boolean ffmTrainingAccess) {
	this.ffmTrainingAccess = ffmTrainingAccess;
}
/**
 * @return the roleList
 */
public List<RoleInfoType> getRoleList() {
	return roleList;
}
/**
 * @param roleList the roleList to set
 */
public void setRoleList(List<RoleInfoType> roleList) {
	this.roleList = roleList;
}
/**
 * @return the roleAttributeList
 */
public List<RoleAttributeType> getRoleAttributeList() {
	return roleAttributeList;
}
/**
 * @param roleAttributeList the roleAttributeList to set
 */
public void setRoleAttributeList(List<RoleAttributeType> roleAttributeList) {
	this.roleAttributeList = roleAttributeList;
}
  
}

package gov.cms.cciio.mlms.ws.registrationgap;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserData {
	private String userName;
	private String firstName;
	private String lastName;
	private String middleName;
	private String businessName;
	private String businessAddress;
	private String businesssAddress2;
	private String businessCity;
	private String businessZip;
	private String businessState;
	private String businessPhone;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getBusinessAddress() {
		return businessAddress;
	}
	public void setBusinessAddress(String businessAddress) {
		this.businessAddress = businessAddress;
	}
	public String getBusinesssAddress2() {
		return businesssAddress2;
	}
	public void setBusinesssAddress2(String businesssAddress2) {
		this.businesssAddress2 = businesssAddress2;
	}
	public String getBusinessCity() {
		return businessCity;
	}
	public void setBusinessCity(String businessCity) {
		this.businessCity = businessCity;
	}
	public String getBusinessZip() {
		return businessZip;
	}
	public void setBusinessZip(String businessZip) {
		this.businessZip = businessZip;
	}
	public String getBusinessState() {
		return businessState;
	}
	public void setBusinessState(String businessState) {
		this.businessState = businessState;
	}
	public String getBusinessPhone() {
		return businessPhone;
	}
	public void setBusinessPhone(String businessPhone) {
		this.businessPhone = businessPhone;
	}
	public String getBusinessExtension() {
		return businessExtension;
	}
	public void setBusinessExtension(String businessExtension) {
		this.businessExtension = businessExtension;
	}
	private String businessExtension;
}

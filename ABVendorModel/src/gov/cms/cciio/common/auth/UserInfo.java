package gov.cms.cciio.common.auth;

import java.util.ArrayList;

public class UserInfo {
	protected String userName;
	protected String fName;
	protected String lName;
	protected String mName;
	protected String email;
	protected String localeId;
	protected String password;
	protected String rate;
	protected String maxDiscount;
	protected String flags;
	protected String timezone;
	protected String quota;
	protected String territoryId;
	protected String companyId;
	protected String title;
	protected String suffix;
	protected String workPhone;
	protected String fax;
	protected String addr1;
	protected String city;
	protected String state;
	protected String zip;
	protected String empType;
	protected String homePhone;
	protected Character gender;
	protected String country;
	

	public String getEmpType() {
		return empType;
	}



	public void setEmpType(String empType) {
		this.empType = empType;
	}



	public String getCity() {
		return city;
	}



	public void setCity(String city) {
		this.city = city;
	}



	public String getState() {
		return state;
	}



	public void setState(String state) {
		this.state = state;
	}



	public String getZip() {
		return zip;
	}



	public void setZip(String zip) {
		this.zip = zip;
	}

	protected String addr2;
	protected String addr3;
	
	public String getFax() {
		return fax;
	}



	public void setFax(String fax) {
		this.fax = fax;
	}



	public String getWorkPhone() {
		return workPhone;
	}



	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}



	public String getSuffix() {
		return suffix;
	}



	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public ArrayList<String> getAccountType() {
		return m_arrListAccountType;
	}



	public void setListAccountType(ArrayList<String> m_arrListAccountType) {
		this.m_arrListAccountType = m_arrListAccountType;
	}

	protected String status;
	
	protected ArrayList<String> m_arrListAccountType;   // Stored in CUSTOM8. No accountType2 according to AKO Documentation
	 
	
	public ArrayList<String> getAccountTypes() {
		return m_arrListAccountType;
	}

	
	
	public void addAccountType(String sAccountType) {
        if (sAccountType == null || (sAccountType = sAccountType.trim()).length() < 1) {
            return;
        }
        m_arrListAccountType.add(sAccountType);
    }

	public Boolean getAccountActive() {
		return accountActive;
	}

	protected Boolean accountActive;
	
	public Boolean isAccountActive() {
		return accountActive;
	}

	public void setAccountActive(Boolean accountActive) {
		this.accountActive = accountActive;
	}

	

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getTerritoryId() {
		return territoryId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getFName() {
		return fName;
	}

	public void setFName(String firstName) {
		this.fName = firstName;
	}

	public String getLName() {
		return lName;
	}

	public void setLName(String lastName) {
		this.lName = lastName;
	}

	public String getMName() {
		return mName;
	}

	public void setMName(String middleName) {
		this.mName = middleName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTimeZone() {
		return timezone;
	}

	public void setTimeZone(String timeZone) {
		this.timezone = timeZone;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getMaxDiscount() {
		return maxDiscount;
	}

	public void setMaxDiscount(String maxDiscount) {
		this.maxDiscount = maxDiscount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLocaleId() {
		return localeId;
	}

	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getAddr1() {
		return addr1;
	}



	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}



	public String getAddr2() {
		return addr2;
	}



	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}



	public String getAddr3() {
		return addr3;
	}



	public void setAddr3(String addr3) {
		this.addr3 = addr3;
	}



	public String getHomePhone() {
		return homePhone;
	}



	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}



	public char getGender() {
		return gender;
	}



	public void setGender(Character gender) {
		this.gender = gender;
	}



	public String getCountry() {
		return country;
	}



	public void setCountry(String country) {
		this.country = country;
	}

}

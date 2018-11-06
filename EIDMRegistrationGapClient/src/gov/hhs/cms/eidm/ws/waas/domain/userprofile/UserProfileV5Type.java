
package gov.hhs.cms.eidm.ws.waas.domain.userprofile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserProfileV5Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserProfileV5Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserInfo" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}UserInfoV5Type"/>
 *         &lt;element name="Address" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}AddressV5Type" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}EmailType" minOccurs="0"/>
 *         &lt;element name="Phone" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}PhoneType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserProfileV5Type", propOrder = {
    "userInfo",
    "address",
    "email",
    "phone"
})
public class UserProfileV5Type {

    @XmlElement(name = "UserInfo", required = true)
    protected UserInfoV5Type userInfo;
    @XmlElement(name = "Address")
    protected AddressV5Type address;
    @XmlElement(name = "Email")
    protected EmailType email;
    @XmlElement(name = "Phone")
    protected PhoneType phone;

    /**
     * Gets the value of the userInfo property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoV5Type }
     *     
     */
    public UserInfoV5Type getUserInfo() {
        return userInfo;
    }

    /**
     * Sets the value of the userInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoV5Type }
     *     
     */
    public void setUserInfo(UserInfoV5Type value) {
        this.userInfo = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link AddressV5Type }
     *     
     */
    public AddressV5Type getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressV5Type }
     *     
     */
    public void setAddress(AddressV5Type value) {
        this.address = value;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link EmailType }
     *     
     */
    public EmailType getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailType }
     *     
     */
    public void setEmail(EmailType value) {
        this.email = value;
    }

    /**
     * Gets the value of the phone property.
     * 
     * @return
     *     possible object is
     *     {@link PhoneType }
     *     
     */
    public PhoneType getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     * 
     * @param value
     *     allowed object is
     *     {@link PhoneType }
     *     
     */
    public void setPhone(PhoneType value) {
        this.phone = value;
    }

}

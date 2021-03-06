//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.shopuserprofileresponsetype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.AddressType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserPreferencesType;
import gov.mlms.cciio.cms.mlmsshopuserprofilecommontype.UserType;


/**
 * <p>Java class for ShopUserProfileResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ShopUserProfileResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserAddress" type="{http://cms.cciio.mlms.gov/MLMSShopUserProfileCommonType}AddressType"/>
 *         &lt;element name="UserInfo" type="{http://cms.cciio.mlms.gov/MLMSShopUserProfileCommonType}UserType"/>
 *         &lt;element name="UserPreferences" type="{http://cms.cciio.mlms.gov/MLMSShopUserProfileCommonType}UserPreferencesType"/>
 *         &lt;element name="StatusInfo" type="{http://cms.cciio.mlms.gov/CCIIOWebServiceCommonType}StatusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShopUserProfileResponseType", propOrder = {
    "userAddress",
    "userInfo",
    "userPreferences",
    "statusInfo"
})
public class ShopUserProfileResponseType {

    @XmlElement(name = "UserAddress", required = true)
    protected AddressType userAddress;
    @XmlElement(name = "UserInfo", required = true)
    protected UserType userInfo;
    @XmlElement(name = "UserPreferences", required = true)
    protected UserPreferencesType userPreferences;
    @XmlElement(name = "StatusInfo", required = true)
    protected StatusType statusInfo;

    /**
     * Gets the value of the userAddress property.
     * 
     * @return
     *     possible object is
     *     {@link AddressType }
     *     
     */
    public AddressType getUserAddress() {
        return userAddress;
    }

    /**
     * Sets the value of the userAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressType }
     *     
     */
    public void setUserAddress(AddressType value) {
        this.userAddress = value;
    }

    /**
     * Gets the value of the userInfo property.
     * 
     * @return
     *     possible object is
     *     {@link UserType }
     *     
     */
    public UserType getUserInfo() {
        return userInfo;
    }

    /**
     * Sets the value of the userInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserType }
     *     
     */
    public void setUserInfo(UserType value) {
        this.userInfo = value;
    }

    /**
     * Gets the value of the userPreferences property.
     * 
     * @return
     *     possible object is
     *     {@link UserPreferencesType }
     *     
     */
    public UserPreferencesType getUserPreferences() {
        return userPreferences;
    }

    /**
     * Sets the value of the userPreferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserPreferencesType }
     *     
     */
    public void setUserPreferences(UserPreferencesType value) {
        this.userPreferences = value;
    }

    /**
     * Gets the value of the statusInfo property.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatusInfo() {
        return statusInfo;
    }

    /**
     * Sets the value of the statusInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatusInfo(StatusType value) {
        this.statusInfo = value;
    }

}

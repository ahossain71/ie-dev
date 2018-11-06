//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import gov.hhs.cms.eidm.ws.waas.domain.common.StatusType;
import gov.hhs.cms.eidm.ws.waas.domain.role.AppsInfoType;
import gov.hhs.cms.eidm.ws.waas.domain.role.RolesType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.BusinContInfoV6Type;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserProfileV6Type;


/**
 * <p>Java class for RetrieveAppDetailsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RetrieveAppDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserProfile" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}UserProfileV6Type" minOccurs="0"/>
 *         &lt;element name="BusinessInfo" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}BusinContInfoV6Type" minOccurs="0"/>
 *         &lt;element name="AppsInfo" type="{http://role.domain.waas.ws.eidm.cms.hhs.gov}AppsInfoType" minOccurs="0"/>
 *         &lt;element name="RolesInfo" type="{http://role.domain.waas.ws.eidm.cms.hhs.gov}RolesType" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://common.domain.waas.ws.eidm.cms.hhs.gov}StatusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveAppDetailsResponseType", propOrder = {
    "userProfile",
    "businessInfo",
    "appsInfo",
    "rolesInfo",
    "status"
})
public class RetrieveAppDetailsResponseType {

    @XmlElement(name = "UserProfile")
    protected UserProfileV6Type userProfile;
    @XmlElement(name = "BusinessInfo")
    protected BusinContInfoV6Type businessInfo;
    @XmlElement(name = "AppsInfo")
    protected AppsInfoType appsInfo;
    @XmlElement(name = "RolesInfo")
    protected RolesType rolesInfo;
    @XmlElement(name = "Status", required = true)
    protected StatusType status;

    /**
     * Gets the value of the userProfile property.
     * 
     * @return
     *     possible object is
     *     {@link UserProfileV6Type }
     *     
     */
    public UserProfileV6Type getUserProfile() {
        return userProfile;
    }

    /**
     * Sets the value of the userProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserProfileV6Type }
     *     
     */
    public void setUserProfile(UserProfileV6Type value) {
        this.userProfile = value;
    }

    /**
     * Gets the value of the businessInfo property.
     * 
     * @return
     *     possible object is
     *     {@link BusinContInfoV6Type }
     *     
     */
    public BusinContInfoV6Type getBusinessInfo() {
        return businessInfo;
    }

    /**
     * Sets the value of the businessInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinContInfoV6Type }
     *     
     */
    public void setBusinessInfo(BusinContInfoV6Type value) {
        this.businessInfo = value;
    }

    /**
     * Gets the value of the appsInfo property.
     * 
     * @return
     *     possible object is
     *     {@link AppsInfoType }
     *     
     */
    public AppsInfoType getAppsInfo() {
        return appsInfo;
    }

    /**
     * Sets the value of the appsInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppsInfoType }
     *     
     */
    public void setAppsInfo(AppsInfoType value) {
        this.appsInfo = value;
    }

    /**
     * Gets the value of the rolesInfo property.
     * 
     * @return
     *     possible object is
     *     {@link RolesType }
     *     
     */
    public RolesType getRolesInfo() {
        return rolesInfo;
    }

    /**
     * Sets the value of the rolesInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link RolesType }
     *     
     */
    public void setRolesInfo(RolesType value) {
        this.rolesInfo = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

}


package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.WaaSApplicationInfoType;


/**
 * <p>Java class for RetrieveAppDetailsRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RetrieveAppDetailsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Credentials" type="{http://userprofile.domain.waas.ws.eidm.cms.hhs.gov}WaaSApplicationInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveAppDetailsRequestType", propOrder = {
    "credentials"
})
public class RetrieveAppDetailsRequestType {

    @XmlElement(name = "Credentials", required = true)
    protected WaaSApplicationInfoType credentials;

    /**
     * Gets the value of the credentials property.
     * 
     * @return
     *     possible object is
     *     {@link WaaSApplicationInfoType }
     *     
     */
    public WaaSApplicationInfoType getCredentials() {
        return credentials;
    }

    /**
     * Sets the value of the credentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link WaaSApplicationInfoType }
     *     
     */
    public void setCredentials(WaaSApplicationInfoType value) {
        this.credentials = value;
    }

}

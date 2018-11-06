
package gov.hhs.cms.eidm.ws.waas.domain.role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AppInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AppInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="appAttributeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="appAttributeText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppInfoType", propOrder = {
    "appAttributeName",
    "appAttributeText"
})
public class AppInfoType {

    protected String appAttributeName;
    protected String appAttributeText;

    /**
     * Gets the value of the appAttributeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppAttributeName() {
        return appAttributeName;
    }

    /**
     * Sets the value of the appAttributeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppAttributeName(String value) {
        this.appAttributeName = value;
    }

    /**
     * Gets the value of the appAttributeText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppAttributeText() {
        return appAttributeText;
    }

    /**
     * Sets the value of the appAttributeText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppAttributeText(String value) {
        this.appAttributeText = value;
    }

}
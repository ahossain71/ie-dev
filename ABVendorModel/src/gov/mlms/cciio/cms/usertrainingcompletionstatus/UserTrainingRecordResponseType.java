//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vIBM 2.2.3-07/07/2014 12:56 PM(foreman)- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.15 at 07:08:55 PM EDT 
//


package gov.mlms.cciio.cms.usertrainingcompletionstatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserTrainingRecordResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserTrainingRecordResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="CertificationID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificationName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificationStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificationExpirationDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CertificationYear" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserTrainingRecordResponseType", propOrder = {

})
public class UserTrainingRecordResponseType {

    @XmlElement(name = "CertificationID", required = true)
    protected String certificationID;
    @XmlElement(name = "CertificationName", required = true)
    protected String certificationName;
    @XmlElement(name = "CertificationStatus", required = true)
    protected String certificationStatus;
    @XmlElement(name = "CertificationExpirationDate", required = true)
    protected String certificationExpirationDate;
    @XmlElement(name = "CertificationYear", required = true)
    protected String certificationYear;

    /**
     * Gets the value of the certificationID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificationID() {
        return certificationID;
    }

    /**
     * Sets the value of the certificationID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificationID(String value) {
        this.certificationID = value;
    }

    /**
     * Gets the value of the certificationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificationName() {
        return certificationName;
    }

    /**
     * Sets the value of the certificationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificationName(String value) {
        this.certificationName = value;
    }

    /**
     * Gets the value of the certificationStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificationStatus() {
        return certificationStatus;
    }

    /**
     * Sets the value of the certificationStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificationStatus(String value) {
        this.certificationStatus = value;
    }

    /**
     * Gets the value of the certificationExpirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificationExpirationDate() {
        return certificationExpirationDate;
    }

    /**
     * Sets the value of the certificationExpirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificationExpirationDate(String value) {
        this.certificationExpirationDate = value;
    }

    /**
     * Gets the value of the certificationYear property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCertificationYear() {
        return certificationYear;
    }

    /**
     * Sets the value of the certificationYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCertificationYear(String value) {
        this.certificationYear = value;
    }

}

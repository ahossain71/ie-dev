//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vIBM 2.2.3-07/07/2014 12:56 PM(foreman)- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.06 at 01:47:13 PM EDT 
//


package gov.mlms.cciio.cms.mlmsshopincrementalfiletype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompletionInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompletionInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shopAgreementName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shopAgreementStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="shopAgreementExpirationDate">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-3][0-9]/[0-1][0-9]/[2][0-9][0-9][0-9]"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="shopAgreementAcquiredDate">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-3][0-9]/[0-1][0-9]/[2][0-9][0-9][0-9]"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompletionInfoType", propOrder = {
    "shopAgreementName",
    "shopAgreementStatus",
    "shopAgreementExpirationDate",
    "shopAgreementAcquiredDate"
})
public class CompletionInfoType {

    @XmlElement(required = true)
    protected String shopAgreementName;
    @XmlElement(required = true)
    protected String shopAgreementStatus;
    @XmlElement(required = true)
    protected String shopAgreementExpirationDate;
    @XmlElement(required = true)
    protected String shopAgreementAcquiredDate;

    /**
     * Gets the value of the shopAgreementName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShopAgreementName() {
        return shopAgreementName;
    }

    /**
     * Sets the value of the shopAgreementName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShopAgreementName(String value) {
        this.shopAgreementName = value;
    }

    /**
     * Gets the value of the shopAgreementStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShopAgreementStatus() {
        return shopAgreementStatus;
    }

    /**
     * Sets the value of the shopAgreementStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShopAgreementStatus(String value) {
        this.shopAgreementStatus = value;
    }

    /**
     * Gets the value of the shopAgreementExpirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShopAgreementExpirationDate() {
        return shopAgreementExpirationDate;
    }

    /**
     * Sets the value of the shopAgreementExpirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShopAgreementExpirationDate(String value) {
        this.shopAgreementExpirationDate = value;
    }

    /**
     * Gets the value of the shopAgreementAcquiredDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShopAgreementAcquiredDate() {
        return shopAgreementAcquiredDate;
    }

    /**
     * Sets the value of the shopAgreementAcquiredDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShopAgreementAcquiredDate(String value) {
        this.shopAgreementAcquiredDate = value;
    }

}

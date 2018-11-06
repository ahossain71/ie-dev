//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vIBM 2.2.3-07/07/2014 12:56 PM(foreman)- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.04.14 at 04:24:19 PM EDT 
//


package gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;


/**
 * <p>Java class for UserTrainingResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserTrainingResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="trainingRecord" type="{http://cms.cciio.mlms.gov/AgentBrokerTrainingCompletionStatus}UserTrainingRecordResponseType" maxOccurs="unbounded"/>
 *         &lt;element name="StatusCode" type="{http://cms.cciio.mlms.gov/CCIIOWebServiceCommonType}statusCodeType"/>
 *         &lt;element name="ErrorCode" type="{http://cms.cciio.mlms.gov/CCIIOWebServiceCommonType}errorCodeType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserTrainingResponseType", propOrder = {
    "username",
    "trainingRecord",
    "statusCode",
    "errorCode"
})
public class UserTrainingResponseType {

    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected List<UserTrainingRecordResponseType> trainingRecord;
    @XmlElement(name = "StatusCode", required = true)
    protected StatusCodeType statusCode;
    @XmlElement(name = "ErrorCode", required = true)
    protected ErrorCodeType errorCode;

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the trainingRecord property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trainingRecord property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrainingRecord().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserTrainingRecordResponseType }
     * 
     * 
     */
    public List<UserTrainingRecordResponseType> getTrainingRecord() {
        if (trainingRecord == null) {
            trainingRecord = new ArrayList<UserTrainingRecordResponseType>();
        }
        return this.trainingRecord;
    }

    /**
     * Gets the value of the statusCode property.
     * 
     * @return
     *     possible object is
     *     {@link StatusCodeType }
     *     
     */
    public StatusCodeType getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the value of the statusCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusCodeType }
     *     
     */
    public void setStatusCode(StatusCodeType value) {
        this.statusCode = value;
    }

    /**
     * Gets the value of the errorCode property.
     * 
     * @return
     *     possible object is
     *     {@link ErrorCodeType }
     *     
     */
    public ErrorCodeType getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorCodeType }
     *     
     */
    public void setErrorCode(ErrorCodeType value) {
        this.errorCode = value;
    }

}

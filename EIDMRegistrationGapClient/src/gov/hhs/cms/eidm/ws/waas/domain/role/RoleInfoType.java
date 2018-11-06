
package gov.hhs.cms.eidm.ws.waas.domain.role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for RoleInfoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RoleInfoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="roleName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="roleGrantDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="roleAttributes" type="{http://role.domain.waas.ws.eidm.cms.hhs.gov}RoleAttributesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleInfoType", propOrder = {
    "roleName",
    "roleGrantDate",
    "roleAttributes"
})
public class RoleInfoType {

    protected String roleName;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar roleGrantDate;
    protected RoleAttributesType roleAttributes;

    /**
     * Gets the value of the roleName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Sets the value of the roleName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoleName(String value) {
        this.roleName = value;
    }

    /**
     * Gets the value of the roleGrantDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRoleGrantDate() {
        return roleGrantDate;
    }

    /**
     * Sets the value of the roleGrantDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRoleGrantDate(XMLGregorianCalendar value) {
        this.roleGrantDate = value;
    }

    /**
     * Gets the value of the roleAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link RoleAttributesType }
     *     
     */
    public RoleAttributesType getRoleAttributes() {
        return roleAttributes;
    }

    /**
     * Sets the value of the roleAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleAttributesType }
     *     
     */
    public void setRoleAttributes(RoleAttributesType value) {
        this.roleAttributes = value;
    }

}

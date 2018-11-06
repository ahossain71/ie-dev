
package gov.hhs.cms.eidm.ws.waas.domain.role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RoleAndAttrInfoMultipleRoleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RoleAndAttrInfoMultipleRoleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="roleName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attrInfo" type="{http://role.domain.waas.ws.eidm.cms.hhs.gov}AttributeInfoType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoleAndAttrInfoMultipleRoleType", propOrder = {
    "roleName",
    "attrInfo"
})
public class RoleAndAttrInfoMultipleRoleType {

    protected String roleName;
    protected AttributeInfoType attrInfo;

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
     * Gets the value of the attrInfo property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeInfoType }
     *     
     */
    public AttributeInfoType getAttrInfo() {
        return attrInfo;
    }

    /**
     * Sets the value of the attrInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeInfoType }
     *     
     */
    public void setAttrInfo(AttributeInfoType value) {
        this.attrInfo = value;
    }

}

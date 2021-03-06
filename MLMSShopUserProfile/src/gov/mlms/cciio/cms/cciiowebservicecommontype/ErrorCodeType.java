//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vIBM 2.2.3-07/07/2014 12:56 PM(foreman)- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.07.01 at 06:28:46 PM EDT 
//


package gov.mlms.cciio.cms.cciiowebservicecommontype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorCodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ME800"/>
 *     &lt;enumeration value="ME810"/>
 *     &lt;enumeration value="ME820"/>
 *     &lt;enumeration value="ME830"/>
 *     &lt;enumeration value="ME840"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorCodeType")
@XmlEnum
public enum ErrorCodeType {

    @XmlEnumValue("ME800")
    ME_800("ME800"),
    @XmlEnumValue("ME810")
    ME_810("ME810"),
    @XmlEnumValue("ME820")
    ME_820("ME820"),
    @XmlEnumValue("ME830")
    ME_830("ME830"),
    @XmlEnumValue("ME840")
    ME_840("ME840");
    private final String value;

    ErrorCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ErrorCodeType fromValue(String v) {
        for (ErrorCodeType c: ErrorCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

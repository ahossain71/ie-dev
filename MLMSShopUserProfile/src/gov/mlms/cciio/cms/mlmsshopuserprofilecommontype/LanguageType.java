//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.mlmsshopuserprofilecommontype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for languageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="languageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="English"/>
 *     &lt;enumeration value="Spanish"/>
 *     &lt;minLength value="3"/>
 *     &lt;maxLength value="24"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "languageType")
@XmlEnum
public enum LanguageType {

    @XmlEnumValue("English")
    ENGLISH("English"),
    @XmlEnumValue("Spanish")
    SPANISH("Spanish");
    private final String value;

    LanguageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LanguageType fromValue(String v) {
        for (LanguageType c: LanguageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
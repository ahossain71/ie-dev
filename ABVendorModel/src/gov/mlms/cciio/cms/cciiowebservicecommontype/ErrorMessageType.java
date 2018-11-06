//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.cciiowebservicecommontype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorMessageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorMessageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="user id does not exist in MLMS database"/>
 *     &lt;enumeration value="Query returned no data"/>
 *     &lt;enumeration value="The system encountered an unexpected error"/>
 *     &lt;enumeration value="Database unavailable"/>
 *     &lt;enumeration value="System down for planned maintenance"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorMessageType")
@XmlEnum
public enum ErrorMessageType {

    @XmlEnumValue("user id does not exist in MLMS database")
    USER_ID_DOES_NOT_EXIST_IN_MLMS_DATABASE("user id does not exist in MLMS database"),
    @XmlEnumValue("No data exists for user. User Id is valid, no data exists for data")
    NO_DATA_EXISTS_FOR_USER_USER_ID_IS_VALID_NO_DATA_EXISTS_FOR_DATA("No data exists for user. User Id is valid, no data exists for data"),
    @XmlEnumValue("Query returned no data")
    QUERY_RETURNED_NO_DATA("Query returned no data"),
    @XmlEnumValue("The system encountered an unexpected error")
    THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR("The system encountered an unexpected error"),
    @XmlEnumValue("Database unavailable")
    DATABASE_UNAVAILABLE("Database unavailable"),
    @XmlEnumValue("System down for planned maintenance")
    SYSTEM_DOWN_FOR_PLANNED_MAINTENANCE("System down for planned maintenance");
    private final String value;

    ErrorMessageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ErrorMessageType fromValue(String v) {
        for (ErrorMessageType c: ErrorMessageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
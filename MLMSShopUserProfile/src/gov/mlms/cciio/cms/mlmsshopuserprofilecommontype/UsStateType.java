//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.mlmsshopuserprofilecommontype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for usStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="usStateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AL"/>
 *     &lt;enumeration value="AK"/>
 *     &lt;enumeration value="AZ"/>
 *     &lt;enumeration value="AR"/>
 *     &lt;enumeration value="CA"/>
 *     &lt;enumeration value="CO"/>
 *     &lt;enumeration value="CT"/>
 *     &lt;enumeration value="DE"/>
 *     &lt;enumeration value="FL"/>
 *     &lt;enumeration value="GA"/>
 *     &lt;enumeration value="HI"/>
 *     &lt;enumeration value="ID"/>
 *     &lt;enumeration value="IL"/>
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="IA"/>
 *     &lt;enumeration value="KS"/>
 *     &lt;enumeration value="KY"/>
 *     &lt;enumeration value="LA"/>
 *     &lt;enumeration value="ME"/>
 *     &lt;enumeration value="MD"/>
 *     &lt;enumeration value="MA"/>
 *     &lt;enumeration value="MI"/>
 *     &lt;enumeration value="MN"/>
 *     &lt;enumeration value="MS"/>
 *     &lt;enumeration value="MO"/>
 *     &lt;enumeration value="MT"/>
 *     &lt;enumeration value="NE"/>
 *     &lt;enumeration value="NV"/>
 *     &lt;enumeration value="NH"/>
 *     &lt;enumeration value="NJ"/>
 *     &lt;enumeration value="NM"/>
 *     &lt;enumeration value="NY"/>
 *     &lt;enumeration value="NC"/>
 *     &lt;enumeration value="ND"/>
 *     &lt;enumeration value="OH"/>
 *     &lt;enumeration value="OK"/>
 *     &lt;enumeration value="OR"/>
 *     &lt;enumeration value="PA"/>
 *     &lt;enumeration value="RI"/>
 *     &lt;enumeration value="SC"/>
 *     &lt;enumeration value="SD"/>
 *     &lt;enumeration value="TN"/>
 *     &lt;enumeration value="TX"/>
 *     &lt;enumeration value="UT"/>
 *     &lt;enumeration value="VT"/>
 *     &lt;enumeration value="VA"/>
 *     &lt;enumeration value="WA"/>
 *     &lt;enumeration value="WV"/>
 *     &lt;enumeration value="WI"/>
 *     &lt;enumeration value="WY"/>
 *     &lt;enumeration value="DC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "usStateType")
@XmlEnum
public enum UsStateType {

    AL,
    AK,
    AZ,
    AR,
    CA,
    CO,
    CT,
    DE,
    FL,
    GA,
    HI,
    ID,
    IL,
    IN,
    IA,
    KS,
    KY,
    LA,
    ME,
    MD,
    MA,
    MI,
    MN,
    MS,
    MO,
    MT,
    NE,
    NV,
    NH,
    NJ,
    NM,
    NY,
    NC,
    ND,
    OH,
    OK,
    OR,
    PA,
    RI,
    SC,
    SD,
    TN,
    TX,
    UT,
    VT,
    VA,
    WA,
    WV,
    WI,
    WY,
    DC;

    public String value() {
        return name();
    }

    public static UsStateType fromValue(String v) {
        return valueOf(v);
    }

}

//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.mlmsshopuserprofilecommontype;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WorkingDaysType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorkingDaysType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="DayofWeek" type="{http://cms.cciio.mlms.gov/MLMSShopUserProfileCommonType}DaysOfWeekType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkingDaysType", propOrder = {
    "dayofWeek"
})
public class WorkingDaysType {

    @XmlElement(name = "DayofWeek")
    protected DaysOfWeekType dayofWeek;

    /**
     * Gets the value of the dayofWeek property.
     * 
     * @return
     *     possible object is
     *     {@link DaysOfWeekType }
     *     
     */
    public DaysOfWeekType getDayofWeek() {
        return dayofWeek;
    }

    /**
     * Sets the value of the dayofWeek property.
     * 
     * @param value
     *     allowed object is
     *     {@link DaysOfWeekType }
     *     
     */
    public void setDayofWeek(DaysOfWeekType value) {
        this.dayofWeek = value;
    }

}

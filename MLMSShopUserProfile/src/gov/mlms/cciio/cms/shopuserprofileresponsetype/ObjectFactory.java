//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.shopuserprofileresponsetype;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.mlms.cciio.cms.shopuserprofileresponsetype package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ShopUserProfile_QNAME = new QName("http://cms.cciio.mlms.gov/ShopUserProfileResponseType", "ShopUserProfile");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.mlms.cciio.cms.shopuserprofileresponsetype
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ShopUserProfileResponseType }
     * 
     */
    public ShopUserProfileResponseType createShopUserProfileResponseType() {
        return new ShopUserProfileResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ShopUserProfileResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://cms.cciio.mlms.gov/ShopUserProfileResponseType", name = "ShopUserProfile")
    public JAXBElement<ShopUserProfileResponseType> createShopUserProfile(ShopUserProfileResponseType value) {
        return new JAXBElement<ShopUserProfileResponseType>(_ShopUserProfile_QNAME, ShopUserProfileResponseType.class, null, value);
    }

}

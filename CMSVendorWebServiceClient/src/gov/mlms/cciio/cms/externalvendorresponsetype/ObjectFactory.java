//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.externalvendorresponsetype;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.mlms.cciio.cms.externalvendorresponsetype package. 
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

    private final static QName _ExternalVendorResponse_QNAME = new QName("http://cms.cciio.mlms.gov/ExternalVendorResponseType", "ExternalVendorResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.mlms.cciio.cms.externalvendorresponsetype
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExternalVendorResponseType }
     * 
     */
    public ExternalVendorResponseType createExternalVendorResponseType() {
        return new ExternalVendorResponseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExternalVendorResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://cms.cciio.mlms.gov/ExternalVendorResponseType", name = "ExternalVendorResponse")
    public JAXBElement<ExternalVendorResponseType> createExternalVendorResponse(ExternalVendorResponseType value) {
        return new JAXBElement<ExternalVendorResponseType>(_ExternalVendorResponse_QNAME, ExternalVendorResponseType.class, null, value);
    }

}

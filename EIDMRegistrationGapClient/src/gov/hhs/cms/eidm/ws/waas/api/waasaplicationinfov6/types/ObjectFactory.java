
package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import gov.hhs.cms.eidm.ws.waas.domain.common.FaultType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types package. 
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

    private final static QName _RetrieveAppDetailsResponse_QNAME = new QName("http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "RetrieveAppDetailsResponse");
    private final static QName _RetrieveAppDetailsFault_QNAME = new QName("http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "RetrieveAppDetailsFault");
    private final static QName _RetrieveAppDetailsRequest_QNAME = new QName("http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "RetrieveAppDetailsRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RetrieveAppDetailsResponseType }
     * 
     */
    public RetrieveAppDetailsResponseType createRetrieveAppDetailsResponseType() {
        return new RetrieveAppDetailsResponseType();
    }

    /**
     * Create an instance of {@link RetrieveAppDetailsRequestType }
     * 
     */
    public RetrieveAppDetailsRequestType createRetrieveAppDetailsRequestType() {
        return new RetrieveAppDetailsRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveAppDetailsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", name = "RetrieveAppDetailsResponse")
    public JAXBElement<RetrieveAppDetailsResponseType> createRetrieveAppDetailsResponse(RetrieveAppDetailsResponseType value) {
        return new JAXBElement<RetrieveAppDetailsResponseType>(_RetrieveAppDetailsResponse_QNAME, RetrieveAppDetailsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FaultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", name = "RetrieveAppDetailsFault")
    public JAXBElement<FaultType> createRetrieveAppDetailsFault(FaultType value) {
        return new JAXBElement<FaultType>(_RetrieveAppDetailsFault_QNAME, FaultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetrieveAppDetailsRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", name = "RetrieveAppDetailsRequest")
    public JAXBElement<RetrieveAppDetailsRequestType> createRetrieveAppDetailsRequest(RetrieveAppDetailsRequestType value) {
        return new JAXBElement<RetrieveAppDetailsRequestType>(_RetrieveAppDetailsRequest_QNAME, RetrieveAppDetailsRequestType.class, null, value);
    }

}
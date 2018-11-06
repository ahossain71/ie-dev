//
// Generated By:JAX-WS RI IBM 2.2.1-11/28/2011 08:28 AM(foreman)- (JAXB RI IBM 2.2.3-11/28/2011 06:21 AM(foreman)-)
//


package gov.cms.hios.soa.assister;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "RetrieveAssisterService", targetNamespace = "http://soa.jboss.org/Assister", wsdlLocation = "WEB-INF/wsdl/wsdl/RetrieveAssister.wsdl")
public class RetrieveAssisterService
    extends Service
{

    private final static URL RETRIEVEASSISTERSERVICE_WSDL_LOCATION;
    private final static WebServiceException RETRIEVEASSISTERSERVICE_EXCEPTION;
    private final static QName RETRIEVEASSISTERSERVICE_QNAME = new QName("http://soa.jboss.org/Assister", "RetrieveAssisterService");

    static {
            RETRIEVEASSISTERSERVICE_WSDL_LOCATION = gov.cms.hios.soa.assister.RetrieveAssisterService.class.getResource("/WEB-INF/wsdl/wsdl/RetrieveAssister.wsdl");
        WebServiceException e = null;
        if (RETRIEVEASSISTERSERVICE_WSDL_LOCATION == null) {
            e = new WebServiceException("Cannot find 'WEB-INF/wsdl/wsdl/RetrieveAssister.wsdl' wsdl. Place the resource correctly in the classpath.");
        }
        RETRIEVEASSISTERSERVICE_EXCEPTION = e;
    }

    public RetrieveAssisterService() {
        super(__getWsdlLocation(), RETRIEVEASSISTERSERVICE_QNAME);
    }

    public RetrieveAssisterService(WebServiceFeature... features) {
        super(__getWsdlLocation(), RETRIEVEASSISTERSERVICE_QNAME, features);
    }

    public RetrieveAssisterService(URL wsdlLocation) {
        super(wsdlLocation, RETRIEVEASSISTERSERVICE_QNAME);
    }

    public RetrieveAssisterService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, RETRIEVEASSISTERSERVICE_QNAME, features);
    }

    public RetrieveAssisterService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RetrieveAssisterService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns RetrieveAssisterPortType
     */
    @WebEndpoint(name = "RetrieveAssisterPortType")
    public RetrieveAssisterPortType getRetrieveAssisterPortType() {
        return super.getPort(new QName("http://soa.jboss.org/Assister", "RetrieveAssisterPortType"), RetrieveAssisterPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RetrieveAssisterPortType
     */
    @WebEndpoint(name = "RetrieveAssisterPortType")
    public RetrieveAssisterPortType getRetrieveAssisterPortType(WebServiceFeature... features) {
        return super.getPort(new QName("http://soa.jboss.org/Assister", "RetrieveAssisterPortType"), RetrieveAssisterPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (RETRIEVEASSISTERSERVICE_EXCEPTION!= null) {
            throw RETRIEVEASSISTERSERVICE_EXCEPTION;
        }
        return RETRIEVEASSISTERSERVICE_WSDL_LOCATION;
    }

}

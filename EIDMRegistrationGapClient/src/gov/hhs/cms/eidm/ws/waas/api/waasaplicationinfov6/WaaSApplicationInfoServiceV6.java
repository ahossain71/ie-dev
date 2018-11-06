
package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "WaaSApplicationInfoServiceV6", targetNamespace = "urn:waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", wsdlLocation = "file:/home/sjmeyer/IBM/rationalsdp/workspace/EIDMRegistrationGapClient/WebContent/WEB-INF/wsdl/WaaSApplicationInfoV6.wsdl")
public class WaaSApplicationInfoServiceV6
    extends Service
{

    private final static URL WAASAPPLICATIONINFOSERVICEV6_WSDL_LOCATION;
    private final static WebServiceException WAASAPPLICATIONINFOSERVICEV6_EXCEPTION;
    private final static QName WAASAPPLICATIONINFOSERVICEV6_QNAME = new QName("urn:waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "WaaSApplicationInfoServiceV6");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/home/sjmeyer/IBM/rationalsdp/workspace/EIDMRegistrationGapClient/WebContent/WEB-INF/wsdl/WaaSApplicationInfoV6.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        WAASAPPLICATIONINFOSERVICEV6_WSDL_LOCATION = url;
        WAASAPPLICATIONINFOSERVICEV6_EXCEPTION = e;
    }

    public WaaSApplicationInfoServiceV6() {
        super(__getWsdlLocation(), WAASAPPLICATIONINFOSERVICEV6_QNAME);
    }

    public WaaSApplicationInfoServiceV6(WebServiceFeature... features) {
        super(__getWsdlLocation(), WAASAPPLICATIONINFOSERVICEV6_QNAME, features);
    }

    public WaaSApplicationInfoServiceV6(URL wsdlLocation) {
        super(wsdlLocation, WAASAPPLICATIONINFOSERVICEV6_QNAME);
    }

    public WaaSApplicationInfoServiceV6(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, WAASAPPLICATIONINFOSERVICEV6_QNAME, features);
    }

    public WaaSApplicationInfoServiceV6(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WaaSApplicationInfoServiceV6(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns WaaSApplicationInfoV6
     */
    @WebEndpoint(name = "WaaSApplicationInfoServiceV6")
    public WaaSApplicationInfoV6 getWaaSApplicationInfoServiceV6() {
        return super.getPort(new QName("urn:waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "WaaSApplicationInfoServiceV6"), WaaSApplicationInfoV6.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns WaaSApplicationInfoV6
     */
    @WebEndpoint(name = "WaaSApplicationInfoServiceV6")
    public WaaSApplicationInfoV6 getWaaSApplicationInfoServiceV6(WebServiceFeature... features) {
        return super.getPort(new QName("urn:waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", "WaaSApplicationInfoServiceV6"), WaaSApplicationInfoV6.class, features);
    }

    private static URL __getWsdlLocation() {
        if (WAASAPPLICATIONINFOSERVICEV6_EXCEPTION!= null) {
            throw WAASAPPLICATIONINFOSERVICEV6_EXCEPTION;
        }
        return WAASAPPLICATIONINFOSERVICEV6_WSDL_LOCATION;
    }

}

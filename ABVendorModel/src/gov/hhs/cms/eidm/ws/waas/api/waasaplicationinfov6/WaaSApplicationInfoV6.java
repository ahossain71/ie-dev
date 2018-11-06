//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsRequestType;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;

@WebService(name = "WaaSApplicationInfoV6", targetNamespace = "urn:waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.ObjectFactory.class,
    gov.hhs.cms.eidm.ws.waas.domain.common.ObjectFactory.class,
    gov.hhs.cms.eidm.ws.waas.domain.role.ObjectFactory.class,
    gov.hhs.cms.eidm.ws.waas.domain.userprofile.ObjectFactory.class
})
public interface WaaSApplicationInfoV6 {


    /**
     * 
     * @param appDtlsInfoReq
     * @return
     *     returns gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType
     * @throws RetrieveAppDetailsFault
     */
    @WebMethod(operationName = "RetrieveAppDetails", action = "wai:WaaSApplicationInfo/RetrieveAppDetails")
    @WebResult(name = "RetrieveAppDetailsResponse", targetNamespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", partName = "appDtlsInfoRes")
    public RetrieveAppDetailsResponseType retrieveAppDetails(
        @WebParam(name = "RetrieveAppDetailsRequest", targetNamespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov", partName = "appDtlsInfoReq")
        RetrieveAppDetailsRequestType appDtlsInfoReq)
        throws RetrieveAppDetailsFault
    ;

}

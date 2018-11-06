//
// Generated By:JAX-WS RI IBM 2.2.1-07/09/2014 01:53 PM(foreman)- (JAXB RI IBM 2.2.3-07/07/2014 12:56 PM(foreman)-)
//


package gov.mlms.cciio.cms.mlmsregistrationgap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import gov.mlms.cciio.cms.registrationgaprequest.RegistrationGapRequestType;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;

@WebService(name = "MLMSRegistrationGap", targetNamespace = "http://cms.cciio.mlms.gov/MLMSRegistrationGap/")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.ObjectFactory.class,
    gov.mlms.cciio.cms.cciiowebservicecommontype.ObjectFactory.class,
    gov.mlms.cciio.cms.registrationgaprequest.ObjectFactory.class,
    gov.mlms.cciio.cms.registrationgapresponse.ObjectFactory.class
})
public interface MLMSRegistrationGap {


    /**
     * 
     * @param registrationGapRequest
     * @return
     *     returns gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType
     */
    @WebMethod(operationName = "RetrieveMLMSRegistrationGAP", action = "http://cms.cciio.mlms.gov/MLMSRegistrationGap/RetrieveRegistrationGAP")
    @WebResult(name = "RegistrationGapResponseElement", targetNamespace = "http://cms.cciio.mlms.gov/RegistrationGapResponse", partName = "RegistrationGapResponse")
    public RegistrationGapResponseType retrieveMLMSRegistrationGAP(
        @WebParam(name = "RegistrationGapRequestElement", targetNamespace = "http://cms.cciio.mlms.gov/RegistrationGapRequest", partName = "RegistrationGapRequest")
        RegistrationGapRequestType registrationGapRequest);

}

package gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6;

import javax.xml.ws.WebFault;
import gov.hhs.cms.eidm.ws.waas.domain.common.FaultType;

@WebFault(name = "RetrieveAppDetailsFault", targetNamespace = "http://types.waasaplicationinfov6.api.waas.ws.eidm.cms.hhs.gov")
public class RetrieveAppDetailsFault
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private FaultType faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public RetrieveAppDetailsFault(String message, FaultType faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public RetrieveAppDetailsFault(String message, FaultType faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: gov.hhs.cms.eidm.ws.waas.domain.common.FaultType
     */
    public FaultType getFaultInfo() {
        return faultInfo;
    }

}

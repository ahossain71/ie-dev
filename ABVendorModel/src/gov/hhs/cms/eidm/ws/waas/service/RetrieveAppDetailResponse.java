/**
 * 
 */
package gov.hhs.cms.eidm.ws.waas.service;


import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
/**
 * @author xnieibm
 * 4/26 updated to reference Version 6 of the EIDM user profile service
 */
public class RetrieveAppDetailResponse {
	
	private RetrieveAppDetailsResponseType responseType;
	private RetrieveAppDetailsFault faultType;
	public RetrieveAppDetailsResponseType getResponseType() {
		return responseType;
	}
	public void setResponseType(RetrieveAppDetailsResponseType responseType) {
		this.responseType = responseType;
	}
	public RetrieveAppDetailsFault getFaultType() {
		return faultType;
	}
	public void setFaultType(RetrieveAppDetailsFault faultType) {
		this.faultType = faultType;
	}

}

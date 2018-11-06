package gov.mlms.cciio.cms.mlmsabvendorcompletion.sessionbean.view;

import javax.ejb.Local;

import gov.mlms.cciio.cms.externalvendorrequesttype.ExternalVendorRequestType;
import gov.mlms.cciio.cms.externalvendorresponsetype.ExternalVendorResponseType;

@Local
public interface MLMSABVendorCompletionSessionBeanLocal {
	public ExternalVendorResponseType receiveABVendorCompletion(
			ExternalVendorRequestType completionRecord);
}

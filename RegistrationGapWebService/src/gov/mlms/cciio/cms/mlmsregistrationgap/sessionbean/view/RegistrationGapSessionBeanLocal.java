package gov.mlms.cciio.cms.mlmsregistrationgap.sessionbean.view;

import gov.mlms.cciio.cms.registrationgaprequest.RegistrationGapRequestType;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;
import javax.ejb.Local;

import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersConnectionSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersCountSQLException;


@Local
public interface RegistrationGapSessionBeanLocal {
	public RegistrationGapResponseType retrieveMLMSRegistrationGAP(RegistrationGapRequestType registrationGapRequest);
}

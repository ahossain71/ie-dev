package gov.mlms.cciio.cms.mlmsregistrationgap.sessionbean;

import gov.cms.cciio.mlms.ws.registrationgap.dao.RegistrationGapDAO;
import gov.mlms.cciio.cms.agentbrokertrainingcompletionstatus.UserTrainingResponseType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.mlmsregistrationgap.sessionbean.view.RegistrationGapSessionBeanLocal;
import gov.mlms.cciio.cms.registrationgaprequest.RegistrationGapRequestType;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;

import cms.cciio.ws.registrationgap.exception.RegistrationGapAuditException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUserTrainingSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersConnectionSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersCountSQLException;
import cms.cciio.ws.registrationgap.exception.RegistrationGapUsersListSQLException;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;
import cms.ccio.mlms.ws.transactionapi.exception.WebServiceTransactionLogException;

@Stateful
public class RegistrationGapSessionBean implements
		RegistrationGapSessionBeanLocal {
	//private static final Logger logger = Logger.getLogger(RegistrationGapDAO.class.getName());
	private static final String sourceClass = RegistrationGapSessionBean.class.getSimpleName();
	Logger logger = Logger.getLogger("RegistrationGapClientSession");
	/**
	 * The requirement is for the web service to return training records for all agents and brokers
	 * that have completed the Individual Marketplace or SHOP Marketplace certification for the current 
	 * year and the MLMS does not have "Complete" in the custom0 field in the cmt_person table.
	 * 
	 A RegistrationGapResponseType contains
	 	ME800 User ID was not found error.invalid.userid
		ME810 No Data Found no.data.for.userid
		ME820 Unexpected Error unexpected.error
		ME830 Database Unavailable database.unavailable
		ME840 System Maintenance: Contact MLMS Administration for expected uptime system.maintenance.window

	  **/
	public RegistrationGapSessionBean(){
		String sourceMethod = "RegistrationGapSessionBean";
		 logger.addHandler(new ConsoleHandler());
		// logger.setLevel(Level.INFO);
		 logger.logp(Level.FINER, sourceClass ,sourceMethod ," source Console Handler created " );
	}
	
	@Override
	public RegistrationGapResponseType retrieveMLMSRegistrationGAP(
			RegistrationGapRequestType registrationGapRequest)  {
		
		String sourceMethod = "retrieveMLMSRegistrationGAP";
		String requestSystemName = registrationGapRequest.getRequestSystemName();

		String strMaxSize = System.getProperty("registrationgap.maxsize", "300"); //"MS200";
		int maxReturnLength = Integer.valueOf(strMaxSize);
		
		RegistrationGapResponseType registrationGapResponseType = new RegistrationGapResponseType();
		// get list of user training response types
		RegistrationGapDAO registrationGapDAO = new RegistrationGapDAO();
		StatusType status = new StatusType();

		List<UserTrainingResponseType> list;
		try {
			list = registrationGapDAO.getMLMSRegistrationGAPList(maxReturnLength, null);
			// add list items to response type
			logger.logp(Level.FINER, sourceClass , sourceMethod, " Registation gap list size " + list.size());
			
			ListIterator<UserTrainingResponseType> it = list.listIterator();
			
			while (it.hasNext()) {
				
				UserTrainingResponseType userType = it.next();
				
				registrationGapResponseType.getRegistrationGapUserType().add(userType
						);
				
				 logger.logp(Level.FINER, sourceClass ,sourceMethod , "Entering registrationGapDAO.auditRegGap "  + userType.getUsername());
				/*
				 * need to get cmt_person.id for user, whether guid was sent
				 */
				//registrationGapDAO.auditRegGap(userType.getUsername(), "I");

			}
			
		if(registrationGapDAO.isRegistrationGapBig(maxReturnLength)){
				status.setStatusCode(StatusCodeType.MS_210);
				
			} else {
				status.setStatusCode(StatusCodeType.MS_200);
			} 
			status.setStatusMessage("Success");
		} catch (RegistrationGapUsersListSQLException e) {
			
			 logger.logp(Level.SEVERE, sourceClass ,sourceMethod , e.getMessage());
			if (e.getMessage().equalsIgnoreCase(ErrorCodeType.ME_810.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_810);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorCode(ErrorCodeType.ME_810);
				status.setErrorMessage(ErrorMessageType.ME_810_QUERY_RETURNED_NO_DATA
						.value());
			} else if (e.getMessage().equalsIgnoreCase(
					ErrorCodeType.ME_820.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_820
						);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
						.value());
			} else {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830
						);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
						.value());
			}
		} catch (RegistrationGapUserTrainingSQLException e) {
			 logger.logp(Level.SEVERE, sourceClass ,sourceMethod ," RegistrationGapUserTrainingSQLException "+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase(ErrorCodeType.ME_810.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_810);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorCode(ErrorCodeType.ME_810);
				status.setErrorMessage(ErrorMessageType.ME_810_QUERY_RETURNED_NO_DATA
						.value());
			} else if (e.getMessage().equalsIgnoreCase(
					ErrorCodeType.ME_820.value())) {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_820
						);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_820_THE_SYSTEM_ENCOUNTERED_AN_UNEXPECTED_ERROR
						.value());
			} else {
				registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830
						);
				status.setStatusCode(StatusCodeType.MS_500);
				status.setStatusMessage("Unsuccessful");
				status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE.value()
						);
			}
		} catch (ParseException e) {
			
			e.printStackTrace();
		} catch (RegistrationGapUsersConnectionSQLException e) {
			
			System.out.println(sourceClass + " " +  sourceMethod + " " + e.getClass() + " "+ e.getMessage());
			registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830
					);
			
			status.setStatusCode(StatusCodeType.MS_500);
			status.setStatusMessage("Unsuccessful");
			status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
					.value());
		} catch (RegistrationGapUsersCountSQLException e) {
			
			logger.logp(Level.SEVERE, sourceClass ,sourceMethod , e.getMessage());
			registrationGapResponseType.setErrorCode(ErrorCodeType.ME_830
					);
			status.setStatusCode(StatusCodeType.MS_500);
			status.setStatusMessage("Unsuccessful");
			status.setErrorMessage(ErrorMessageType.ME_830_DATABASE_UNAVAILABLE
					.value());
		}
		

		registrationGapResponseType.setStatusCode(status);
		String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
		String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
		
		if(!status.equals(System.getProperty("operation.successful", "MS200"))){
			mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
			mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
		}
		try {
			//registrationGapDAO.logTransaction(webserviceId, transactionCode, partnerId, statusCode)
			registrationGapDAO.logTransaction(MLMSWSCONSTANTS.REG_GAP_PROVIDER, mlmsConstantsResSuccess
					, MLMSWSCONSTANTS.PARTNER_CODE_EIDM, mlmsConstantsSuccess,registrationGapResponseType);
		} catch (WebServiceTransactionLogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return registrationGapResponseType;
	}
	

}

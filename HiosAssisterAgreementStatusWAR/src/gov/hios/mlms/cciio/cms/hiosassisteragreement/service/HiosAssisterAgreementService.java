/**
 * 
 */
package gov.hios.mlms.cciio.cms.hiosassisteragreement.service;

import gov.hios.mlms.cciio.cms.assisteragreementstatusresponse.AssisterAgreementStatusResponse;
import gov.hios.mlms.cciio.cms.assisteragreementstatusresponse.AssisterCertificationStatusType;
import gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.dao.HiosAssisterAgreementStatusDao;
import gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.exception.HiosAssisterAgreementStatusException;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;


/**
 * @author xnieibm
 * Changes:
 * Added Transaction Logging to DB
 */
public class HiosAssisterAgreementService {
	
	Logger logger = Logger.getLogger("HiosAssisterAgreementService.class");
	
	public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public AssisterAgreementStatusResponse retrieveAssisterAgreementStatus(String requestDateTime)
		throws HiosAssisterAgreementStatusException{
		
		
		AssisterAgreementStatusResponse response = new AssisterAgreementStatusResponse();
		
		String statusCode = System.getProperty("operation.successful", "MS200"); //"MS200";
		String statusMsg = System.getProperty("operation.successful.message", "Operation Successful");
		String errorCode = "";
		String errorMessage = "";
		String errorKey = "";
		
		try{
			Date dt = DATEFORMAT.parse(requestDateTime);
			java.sql.Date datetime = new java.sql.Date(dt.getTime()); 
			
			HiosAssisterAgreementStatusDao haaDao = new HiosAssisterAgreementStatusDao();
			List<AssisterCertificationStatusType> statusList = haaDao.getAssisterAgreementStatus(datetime);
			
			response.getAssisterAgreementStatus().addAll(statusList);
			
		}catch(ParseException pe){
			throw new HiosAssisterAgreementStatusException("Please format the datetime in \"yyyy-MM-dd HH:mm:ss\"", pe);
		}catch(Exception e){
    		
			errorKey = "unexpected.error";
			errorCode = System.getProperty(errorKey, "");
			errorMessage = System.getProperty(errorKey + ".message", "");
			statusCode = System.getProperty("operation.unsuccessful", "MS500");
			statusMsg = System.getProperty("operation.unsuccessful.message", "Operation unsuccessful");
    		
    		logger.severe("Unexpected error: " + e.getMessage());
    	}   
		
		StatusType statusType = new StatusType();
		statusType.setErrorCode(errorCode);
		statusType.setErrorKey(errorKey);
		statusType.setErrorMessage(errorMessage);
		StatusCodeType statusCodeType = StatusCodeType.fromValue(statusCode);
		statusType.setStatusCode(statusCodeType);
		statusType.setStatusMessage(statusMsg);
		
		response.setOperationStatus(statusType);
		TransactionLogAPI transLog= new TransactionLogAPI();
		String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
		String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
		
		if(!statusCode.equals(System.getProperty("operation.successful", "MS200"))){
			mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
			mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
		}
		
	    
		try{
			JAXBContext context = JAXBContext.newInstance(AssisterAgreementStatusResponse.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    m.marshal(response, sw);

		    String result = sw.toString();
		transLog.insertWSTransaction(MLMSWSCONSTANTS.HIOS_VERIFICATION_CLIENT, 
				mlmsConstantsResSuccess, 
				MLMSWSCONSTANTS.PARTNER_CODE_HIOS, 
				mlmsConstantsSuccess,result,"HiosAssisterAgreementReponse");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("HiosAssisterAgreementService MLMSWS Transaction logging exception --- " + e.getStackTrace());
		}
		return response;
	}

}

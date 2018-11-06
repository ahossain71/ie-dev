package gov.hios.mlms.cciio.cms.hiosassisteragreementstatus;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
//import org.slf4j.LoggerFactory;

import gov.hios.mlms.cciio.cms.assisteragreementstatusrequest.AcquiredDatetime;
import gov.hios.mlms.cciio.cms.assisteragreementstatusresponse.AssisterAgreementStatusResponse;
import gov.hios.mlms.cciio.cms.hiosassisteragreement.service.HiosAssisterAgreementService;
import gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.exception.HiosAssisterAgreementStatusException;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;




@javax.jws.WebService (endpointInterface="gov.hios.mlms.cciio.cms.hiosassisteragreementstatus.HiosAssisterAgreementStatus", targetNamespace="http://cms.cciio.mlms.hios.gov/HiosAssisterAgreementStatus/", serviceName="HiosAssisterAgreementStatus", portName="HiosAssisterAgreementStatusSOAP", wsdlLocation="HiosAssisterAgreementStatusDefinition/HiosAssisterAgreementStatus.wsdl")
public class HiosAssisterAgreementStatusSOAPImpl{
	
	Logger logger = Logger.getLogger("HiosAssisterAgreementStatusSOAPImpl.class");

    public AssisterAgreementStatusResponse retrieveAssisterAgreementStatus(AcquiredDatetime certificationDateTime) {
String requestDateTime = certificationDateTime.getValue();
    	
		TransactionLogAPI transLog= new TransactionLogAPI();
		try{
			JAXBContext context = JAXBContext.newInstance(AcquiredDatetime.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    m.marshal(certificationDateTime, sw);

		    String result = sw.toString();
		transLog.insertWSTransaction(MLMSWSCONSTANTS.HIOS_VERIFICATION_CLIENT, 
				MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
				MLMSWSCONSTANTS.PARTNER_CODE_HIOS, 
				MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"HiosAssisterAgreementRequest");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("HiosAssisterAgreementStatusSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
		}
    	AssisterAgreementStatusResponse response = null;
    	
    	try{
    		HiosAssisterAgreementService service = new HiosAssisterAgreementService();
    		response = service.retrieveAssisterAgreementStatus(requestDateTime);
    	}
    	catch(HiosAssisterAgreementStatusException haasE){
    		
			String errorKey = "unexpected.error";
			String errorCode = System.getProperty(errorKey, "");
			String errorMessage = System.getProperty(errorKey + ".message", "");
			String statusCode = System.getProperty("operation.unsuccessful", "MS500");
			String statusMsg = System.getProperty("operation.unsuccessful.message", "Operation unsuccessful");    		
	
			StatusType statusType = new StatusType();
			statusType.setErrorCode(errorCode);
			statusType.setErrorKey(errorKey);
			statusType.setErrorMessage(errorMessage);
			StatusCodeType statusCodeType = StatusCodeType.fromValue(statusCode);
			statusType.setStatusCode(statusCodeType);
			statusType.setStatusMessage(statusMsg);
			
			if(null == response) response = new AssisterAgreementStatusResponse();
		
			response.setOperationStatus(statusType);

    		logger.severe("Unexpected Error: " + haasE.getMessage());
    	}
        return response;
    }

}
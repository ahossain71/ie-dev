package org.mlms.cciio.ibm.cms.mlmstrainingstatusservice;

import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;

import java.io.StringWriter;
import java.util.List;

import org.mlms.cciio.ibm.cms.retrievetrainingcompletionstatusrequest.UserId;
import org.mlms.cciio.ibm.cms.retrievetrainingcompletionstatustype.CertificationType;
import org.mlms.cciio.ibm.cms.retrievetrainingcompletionstatustype.RetrieveTrainingCompletionStatusResponse;
import org.mlms.cciio.ibm.cms.training.dao.TrainingCertificationDao;
import org.mlms.cciio.ibm.cms.training.dao.UserObjectDAO;
import java.util.logging.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;


@javax.jws.WebService (endpointInterface="org.mlms.cciio.ibm.cms.mlmstrainingstatusservice.MLMSTrainingStatusService", targetNamespace="http://cms.ibm.cciio.mlms.org/MLMSTrainingStatusService/", serviceName="MLMSTrainingStatusService", portName="MLMSTrainingStatusServiceSOAP", wsdlLocation="WEB-INF/wsdl/MLMSAgentBrokerTrainingStatusDefinition/MLMSTrainingStatusService.wsdl")
public class MLMSTrainingStatusServiceSOAPImpl{
	private java.util.logging.Logger logger = Logger.getLogger("MLMSTrainingStatusServiceSOAPImpl.class");

	 public RetrieveTrainingCompletionStatusResponse retrieveTrainingCompletionStatus(UserId userId) {
	        
    	 String statusCode = System.getProperty("operation.successful", "MS200"); //"MS200";
    	 String statusMsg = System.getProperty("operation.successful.message", "Operation Successful");
    	 String errorCode = "";
    	 String errorMessage = "";
    	 String errorKey = "";
    	 
    	 
    	 String userIdStr = userId.getValue();
    	 
    	 TransactionLogAPI transLog= new TransactionLogAPI();
			try{
				JAXBContext context = JAXBContext.newInstance(UserId.class);
			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			    StringWriter sw = new StringWriter();
			    m.marshal(userId, sw);

			    String result = sw.toString();
			transLog.insertWSTransaction(MLMSWSCONSTANTS.AGENT_BROKER_COMPLETION_PROVIDER, 
					MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
					MLMSWSCONSTANTS.PARTNER_CODE_EIDM, 
					MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"MLMSAgentBrokerTrainingStatusRequest");
			}catch(Exception e){
				//don't do anything for now
				System.out.println("MLMSAgentBrokerTrainingStatusRequest MLMSTrainingStatusServiceSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
			}
    	 
			boolean bNoNullErr = true;
			boolean bValidUser = true;
			boolean bCertNotEmpty = true;
			
    	 RetrieveTrainingCompletionStatusResponse response = new RetrieveTrainingCompletionStatusResponse();
    	 
     	if(null == userIdStr || "".equals(userIdStr)){
    		errorKey = "error.invalid.userid";
    		errorCode = System.getProperty(errorKey, "");
    		errorMessage = System.getProperty(errorKey + ".message", "");
    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
        	setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg);  
        	//System.out.println("SOAP RESPONSE: "+errorKey+" for " + userIdStr);
            //return response;
        	bNoNullErr = false;
    	}   
     	UserObjectDAO userDAO = new UserObjectDAO();
     	
     	if(bNoNullErr){
	     	if(!userDAO.validateUserId(userIdStr)){
	     		errorKey = "error.invalid.userid";
	    		errorCode = System.getProperty(errorKey, "");
	    		errorMessage = System.getProperty(errorKey + ".message", "");
	    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
	    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
	        	//setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg);  
	        	System.out.println("SOAP RESPONSE: "+errorKey+" for " + userIdStr);
	            //return response;
	        	bValidUser = false;
	     	}
     	}
    	TrainingCertificationDao dao = new TrainingCertificationDao();
    	if(bNoNullErr && bValidUser){
	    	try{
	    		List<CertificationType> certList= dao.getCertificationInfo(userIdStr);
	    	
		    	if(certList.isEmpty()){
		    		createEmptyCertification(response);
		    		errorKey = "no.data.for.userid";
		    		errorCode = System.getProperty(errorKey, "");
		    		errorMessage = System.getProperty(errorKey + ".message", "");
		    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
		    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
		        	//setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg); 
		        	System.out.println("AGENTBROKER SOAP RESPONSE: "+errorKey+" for " + userIdStr);
		        	bCertNotEmpty = false;
		            //return response;
		    	}
		    	if(bCertNotEmpty){
			    	for(CertificationType cert : certList){
			    		response.getCertification().add(cert);
			    	}
		    	}
	    	}    	
	    	catch(Exception e){
	    		
	    		if( "CCIIO_SQLException".equals(e.getMessage())){
	        		errorKey = "database.unavailable";
	    		} else    		
	    			errorKey = "unexpected.error";
	    		errorCode = System.getProperty(errorKey, "");
	    		errorMessage = System.getProperty(errorKey + ".message", "");
	    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
	    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
	    		System.out.println("SOAP RESPONSE: "+errorKey+" for " + userIdStr);
	    		logger.severe("Unexpected Error " + e.getMessage());
	    	}
	    	
	    	 
    	}
    	setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg);
    	
    	TransactionLogAPI transLog1= new TransactionLogAPI();
		String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
		String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
		
		if(!response.getStatusCode().value().equals(System.getProperty("operation.successful", "MS200"))){
			mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
			mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
		}
		
	    
		try{
			JAXBContext context = JAXBContext.newInstance(RetrieveTrainingCompletionStatusResponse.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    m.marshal(response, sw);

		    String result = sw.toString();
		transLog1.insertWSTransaction(MLMSWSCONSTANTS.AGENT_BROKER_COMPLETION_PROVIDER, 
				mlmsConstantsResSuccess, 
				MLMSWSCONSTANTS.PARTNER_CODE_EIDM, 
				mlmsConstantsSuccess,result,"MLMSAgentBrokerTrainingStatusResponse");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("MLMSAgentBrokerTrainingStatusResponse MLMSTrainingStatusServiceSOAPImpl MLMSWS Transaction logging exception --- " + e.getMessage());
			 e.printStackTrace();
		}
		
        return response;
    }

	private void createEmptyCertification(RetrieveTrainingCompletionStatusResponse response) throws Exception{
		
		CertificationType cert = new CertificationType();
		cert.setCertificationId("");
		cert.setCertificationName("");
		cert.setCertificationStatus("");
		cert.setCertificationExperationDate("");		
	}
	
	private void setResponseStatus(RetrieveTrainingCompletionStatusResponse response, String userIdStr, 
			String errorKey, String errorCode, String errorMessage, String statusCode, String statusMsg){
		
			StatusCodeType statusType = StatusCodeType.fromValue(statusCode);
		
	    	response.setUserId(userIdStr);
	    	response.setErrorKey(errorKey);
	       	response.setErrorCode(errorCode);
	    	response.setErrorMessage(errorMessage);
	    	response.setStatusCode(statusType); 
	    	response.setStatusMessage(statusMsg);
	}


}
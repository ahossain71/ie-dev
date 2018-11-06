package gov.mlms.cciio.cms.smallbusinesshealthoption.sessionbean;

import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.shopagreementstatusrequesttype.UserId;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.NpnEntityType;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.ShopAgreementStatusResponse;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.ShopAgreementType;
import gov.mlms.cciio.cms.smallbusinesshealthoption.dao.ShopAgreementStatusDao;
import gov.mlms.cciio.cms.smallbusinesshealthoption.exception.ShopAgreementException;
import gov.mlms.cciio.cms.smallbusinesshealthoption.sessionbean.view.ShopAgreementStatusSessionBeanLocal;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Session Bean implementation class ShopAgreementStatusSessionBean
 */
@Stateless
@Local(ShopAgreementStatusSessionBeanLocal.class)
@LocalBean
public class ShopAgreementStatusSessionBean implements ShopAgreementStatusSessionBeanLocal {

    /**
     * Default constructor. 
     */
    public ShopAgreementStatusSessionBean() {
    }
    
    public ShopAgreementStatusResponse retrieveShopAgreementStatus(UserId userId){    	
        
		String statusCode = System.getProperty("operation.successful", "MS200"); //"MS200";
		String statusMsg = System.getProperty("operation.successful.message", "Operation Successful");
		String errorCode = "";
		String errorMessage = "";
		String errorKey = "";
    	
		String userIdStr = userId.getValue();
		
    	ShopAgreementStatusResponse response = new ShopAgreementStatusResponse();
    	
     	if(null == userIdStr || "".equals(userIdStr)){
    		errorKey = "error.invalid.userid";
    		errorCode = System.getProperty(errorKey, "");
    		errorMessage = System.getProperty(errorKey + ".message", "");
    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
        	setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg);    	   	
            return response;
    	}   	
    	
    	ShopAgreementStatusDao saDao = new ShopAgreementStatusDao();
    	
		List<NpnEntityType> npnEntities = new ArrayList<NpnEntityType>();
		List<ShopAgreementType> agreements = new ArrayList<ShopAgreementType>();
    	
    	try{
    		npnEntities = saDao.getNpnInfo(userIdStr);
    		agreements = saDao.getShopAgreementInfo(userIdStr);
    		
    		System.out.println("user id: " + userIdStr + "Agreements empty: " + agreements.isEmpty());
    		
    	
    		if(agreements.isEmpty() || npnEntities.isEmpty() ){
    			errorKey = "no.data.for.userid";
	    		errorCode = System.getProperty(errorKey, "");
	    		errorMessage = System.getProperty(errorKey + ".message", "");
	    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
	    		statusMsg = System.getProperty("operation.unsuccessful.message", "An Error Occured");
	    		
    		}     		
    	}
    	catch(ShopAgreementException e){    		
    		
    		if( "DB_CONNECT_EXCEPTION".equals(e.getMessage())){
        		errorKey = "database.unavailable";
    		} else    		
    			errorKey = "unexpected.error";
    		errorCode = System.getProperty(errorKey, "");
    		errorMessage = System.getProperty(errorKey + ".message", "");
    		statusCode = System.getProperty("operation.unsuccessful", "MS500");
    		
    		e.printStackTrace();
    		
    	}
    	catch(Exception e){
    		
			errorKey = "unexpected.error";
			errorCode = System.getProperty(errorKey, "");
			errorMessage = System.getProperty(errorKey + ".message", "");
			statusCode = System.getProperty("operation.unsuccessful", "MS500");
    		
    		e.printStackTrace();
    	}    	
        if(!agreements.isEmpty()) {response.getNpnEntity().addAll(npnEntities);}
        
		response.getShopAgreement().addAll(agreements);
		this.setResponseStatus(response, userIdStr, errorKey, errorCode, errorMessage, statusCode, statusMsg);
		return response;
    }
    
	private void setResponseStatus(ShopAgreementStatusResponse response, String userIdStr, 
			String errorKey, String errorCode, String errorMessage, String statusCode, String statusMsg){
		
			StatusCodeType statusCodeType = StatusCodeType.fromValue(statusCode);
		
	    	response.setUserId(userIdStr);
	    	
	    	StatusType statusType = new StatusType();
	    	statusType.setErrorCode(errorCode);
	    	statusType.setErrorKey(errorKey);
	    	statusType.setErrorMessage(errorMessage);
	    	statusType.setStatusCode(statusCodeType);
	    	statusType.setStatusMessage(statusMsg);

	    	response.setOperationStatus(statusType);
	}

}

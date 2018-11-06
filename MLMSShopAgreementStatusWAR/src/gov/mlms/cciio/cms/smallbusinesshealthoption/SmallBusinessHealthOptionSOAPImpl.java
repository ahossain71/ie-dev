package gov.mlms.cciio.cms.smallbusinesshealthoption;

import java.io.StringWriter;

import gov.mlms.cciio.cms.shopagreementstatusrequesttype.UserId;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.ShopAgreementStatusResponse;
import gov.mlms.cciio.cms.smallbusinesshealthoption.sessionbean.view.ShopAgreementStatusSessionBeanLocal;

import javax.ejb.EJB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;


@javax.jws.WebService (endpointInterface="gov.mlms.cciio.cms.smallbusinesshealthoption.SmallBusinessHealthOption", targetNamespace="http://cms.cciio.mlms.gov/SmallBusinessHealthOption/", serviceName="SmallBusinessHealthOption", portName="SmallBusinessHealthOptionSOAP", wsdlLocation="SmallBusinessHealthOption/SmallBusinessHealthOption.wsdl")
public class SmallBusinessHealthOptionSOAPImpl{

	 @EJB
	 private ShopAgreementStatusSessionBeanLocal sasSessionBean;

	 public ShopAgreementStatusResponse retrieveShopAgreementStatus(UserId userId) {
		 
		 TransactionLogAPI transLog= new TransactionLogAPI();
			try{
				JAXBContext context = JAXBContext.newInstance(UserId.class);
			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			    StringWriter sw = new StringWriter();
			    m.marshal(userId, sw);

			    String result = sw.toString();
			transLog.insertWSTransaction(MLMSWSCONSTANTS.SHOP_COMPLETION_PROVIDER, 
					MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
					MLMSWSCONSTANTS.PARTNER_CODE_SHOP, 
					MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"MLMSShopAgreementStatusRequest");
			}catch(Exception e){
				//don't do anything for now
				System.out.println("MLMSShopAgreementStatus SmallBusinessHealthOptionSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
			}
       
		 	ShopAgreementStatusResponse response = sasSessionBean.retrieveShopAgreementStatus(userId);
		 	
		 	TransactionLogAPI transLog1= new TransactionLogAPI();
			String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
			String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
			
			if(!response.getOperationStatus().getStatusCode().value().equals(System.getProperty("operation.successful", "MS200"))){
				mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
				mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
			}
			
		    
			try{
				JAXBContext context = JAXBContext.newInstance(ShopAgreementStatusResponse.class);
			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			    StringWriter sw = new StringWriter();
			    m.marshal(response, sw);

			    String result = sw.toString();
			transLog1.insertWSTransaction(MLMSWSCONSTANTS.SHOP_COMPLETION_PROVIDER, 
					mlmsConstantsResSuccess, 
					MLMSWSCONSTANTS.PARTNER_CODE_SHOP, 
					mlmsConstantsSuccess,result,"MLMSShopAgreementStatusReponse");
			}catch(Exception e){
				//don't do anything for now
				System.out.println("MLMSShopAgreementStatus SmallBusinessHealthOptionSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
			}
  	 		return response;
   }

}
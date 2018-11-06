package gov.hhs.cms.eidm.ws.waas.service;


import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.WaaSApplicationInfoServiceV6;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.WaaSApplicationInfoV6;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsRequestType;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.WaaSApplicationInfoType;

import java.net.MalformedURLException;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;

	public class WaaSApplicationService {
		
		/**
		 * 
		 * @param userId
		 * @return
		 * @throws RetrieveAppDetailsFault 
		 * @throws MalformedURLException 
		 */
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(String  userId) throws MalformedURLException, RetrieveAppDetailsFault{
			
			UserType userType = new UserType(); 
			
			userType.setUserid(userId);
			
			return this.retrieveApplicationDetails(userType);
		}
		/**
		 * 
		 * @param usertype
		 * @return
		 * @throws RetrieveAppDetailsFault 
		 * @throws MalformedURLException 
		 */
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(UserType  usertype) throws MalformedURLException, RetrieveAppDetailsFault {
			
			TransactionLogAPI transLog= new TransactionLogAPI();
			try{
				/*JAXBContext context = JAXBContext.newInstance(UserType.class);
			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);*/
			    //System.out.println("Before marshalling retrieveApplicationDetails " + usertype);
			    //StringWriter sw = new StringWriter();
			    String result = "";
			    
			    if(usertype != null){
			    	//m.marshal(usertype, sw);
			    	//m.marshal(usertype, sw);
			    	result = usertype.getUserid();
			    }
			    //result = sw.toString();
			    //System.out.println("Before transaction logging retrieveApplicationDetails Result = " + result);
			transLog.insertWSTransaction(MLMSWSCONSTANTS.REG_GAP_CLIENT, 
					MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
					MLMSWSCONSTANTS.PARTNER_CODE_EIDM, 
					MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"EIDMRegistrationGapClientRequest");
			}catch(Exception e){
				//don't do anything for now
				
				System.out.println("WaaSApplicationService Request MLMSWS Transaction logging exception ");
				e.printStackTrace();
			}
			WaaSApplicationInfoType wsait = new WaaSApplicationInfoType();
			wsait.setUser(usertype);
			
			RetrieveAppDetailsRequestType retrieveAppDetailsRequestType = new RetrieveAppDetailsRequestType();
			retrieveAppDetailsRequestType.setCredentials(wsait);
			
			RetrieveAppDetailsResponseType appResponse = this.retrieveApplicationDetails(retrieveAppDetailsRequestType);
			
			TransactionLogAPI transLog1= new TransactionLogAPI();
			
			String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
			String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
			
			if(appResponse.getStatus().getErrors().size() > 0){
				mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
				mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
			}
			try{
				/*JAXBContext context = JAXBContext.newInstance(RetrieveAppDetailsResponseType.class);
			    Marshaller m = context.createMarshaller();
			    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			    StringWriter sw = new StringWriter();
			    m.marshal(appResponse, sw);*/
				String result = "";
			    result = appResponse.getUserProfile().getUserInfo().getUserId();
			    
			transLog1.insertWSTransaction(MLMSWSCONSTANTS.REG_GAP_CLIENT, 
					mlmsConstantsResSuccess, 
					MLMSWSCONSTANTS.PARTNER_CODE_EIDM, 
					mlmsConstantsSuccess,result,"EIDMRegistrationGapClientResponse");
			}catch(Exception e){
				//don't do anything for now
				System.out.println("WaaSApplicationService Response MLMSWS Transaction logging exception --- ");
				e.printStackTrace();
			}
			return appResponse;
		}
		/**
		 * 
		 * @param appRequest
		 * @return
		 * @throws RetrieveAppDetailsFault, MalformedURLException 
		 */
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(RetrieveAppDetailsRequestType appRequest) throws RetrieveAppDetailsFault, MalformedURLException{
			
			RetrieveAppDetailsResponseType response = null;
			/*
			 * 
			 */
			
				/*
				 * class that has the WebServiceClient Annotation
				 */
				WaaSApplicationInfoServiceV6 waaSApplicationInfoV6 = ServiceFactory.getWaaSApplicationInfoServiceV6();
				/*
				 * class that has the WebService Annotation
				 */
				WaaSApplicationInfoV6 proxy = waaSApplicationInfoV6.getPort(WaaSApplicationInfoV6.class);
				response = proxy.retrieveAppDetails(appRequest);
				
			 
			
			
			
			return response;
		}

}

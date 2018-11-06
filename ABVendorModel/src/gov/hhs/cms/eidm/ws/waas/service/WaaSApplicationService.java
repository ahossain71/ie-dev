package gov.hhs.cms.eidm.ws.waas.service;

import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.RetrieveAppDetailsFault;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.WaaSApplicationInfoServiceV6;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.WaaSApplicationInfoV6;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsRequestType;
import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.types.RetrieveAppDetailsResponseType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.UserType;
import gov.hhs.cms.eidm.ws.waas.domain.userprofile.WaaSApplicationInfoType;

import java.net.MalformedURLException;


	public class WaaSApplicationService {
		
		/**
		 * 
		 * @param userId
		 * @return
		 * @throws RetrieveAppDetailsFault 
		 * @throws MalformedURLException 
		 */
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(String  userId) throws RetrieveAppDetailsFault, MalformedURLException{
			
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
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(UserType  usertype) throws RetrieveAppDetailsFault, MalformedURLException{
			
			WaaSApplicationInfoType wsait = new WaaSApplicationInfoType();
			wsait.setUser(usertype);
			
			RetrieveAppDetailsRequestType retrieveAppDetailsRequestType = new RetrieveAppDetailsRequestType();
			retrieveAppDetailsRequestType.setCredentials(wsait);
			
			return this.retrieveApplicationDetails(retrieveAppDetailsRequestType);
		}
		/**
		 * 
		 * @param appRequest
		 * @return
		 * @throws RetrieveAppDetailsFault 
		 * @throws MalformedURLException 
		 */
		public RetrieveAppDetailsResponseType retrieveApplicationDetails(RetrieveAppDetailsRequestType appRequest) throws RetrieveAppDetailsFault, MalformedURLException{
			
			RetrieveAppDetailsResponseType response = null;
			/*
			 * 
			 */
			
			
				WaaSApplicationInfoServiceV6 waaSApplicationInfoV6 = ServiceFactory.getWaaSApplicationInfoServiceV6();
				WaaSApplicationInfoV6 proxy = waaSApplicationInfoV6.getPort(WaaSApplicationInfoV6.class);
				response = proxy.retrieveAppDetails(appRequest);
				
			
			
			return response;
		}

}

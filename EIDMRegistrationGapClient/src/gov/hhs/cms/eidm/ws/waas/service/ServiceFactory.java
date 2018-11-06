package gov.hhs.cms.eidm.ws.waas.service;

import gov.hhs.cms.eidm.ws.waas.api.waasaplicationinfov6.WaaSApplicationInfoServiceV6;

import java.net.MalformedURLException;
import java.net.URL;

public class ServiceFactory {
	public static final String WAAS_APP_INFO__V6_SERVICE_URL_KEY = "MLMS.waas.app.v6.info.service.url";
	private static WaaSApplicationInfoServiceV6 serviceV6 = null;
	
	public static WaaSApplicationInfoServiceV6 getWaaSApplicationInfoServiceV6() throws MalformedURLException{
		
		if(serviceV6 == null){
			createWaaSApplicationInfoServiceV6();
		}
		return serviceV6;
	}
	
	private static void createWaaSApplicationInfoServiceV6()throws MalformedURLException{
		String serviceUrl = System.getProperty(WAAS_APP_INFO__V6_SERVICE_URL_KEY);
		URL wsdlUrl = new URL(serviceUrl+"?wsdl");
		System.out.println(ServiceFactory.class.getCanonicalName() + " created service WSDL URL = " + wsdlUrl);
		/*
		 * instance of class that has @WebServiceClient annotation
		 */
		serviceV6 = new WaaSApplicationInfoServiceV6(wsdlUrl);
	}
}

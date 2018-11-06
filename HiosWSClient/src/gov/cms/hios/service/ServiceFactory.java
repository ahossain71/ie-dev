/**
 * 
 */
package gov.cms.hios.service;

import gov.cms.hios.service.exception.RetrieveAssisterException;
import gov.cms.hios.soa.assister.RetrieveAssisterService;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.Service;

/**
 * @author xnieibm
 *
 */
public class ServiceFactory {
	
	public static final String HIOS_SERVICE_URL_KEY = "hios.service.url";
	
	public static Service assisterService = null;
	
	public static Service getAssisterService()throws RetrieveAssisterException {
		try{
			if (assisterService == null){
				createAssisterService();
			}
		}
		catch (MalformedURLException mue){
			throw new RetrieveAssisterException("RetrieveAssisterService URL error. " + mue);
		}
		return assisterService;
	}
	
	private static void createAssisterService() throws MalformedURLException{
		String serviceUrl = System.getProperty(HIOS_SERVICE_URL_KEY);
		URL wsdlUrl = new URL(serviceUrl+"?wsdl");
		assisterService = new RetrieveAssisterService(wsdlUrl);
	}

}

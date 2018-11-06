package gov.mlms.cciio.cms.mlmsabvendorcompletion.service;

import gov.mlms.cciio.cms.mlmsabvendorcompletion.MLMSABVendorCompletion_Service;

import java.net.MalformedURLException;
import java.net.URL;

public class ServiceFactory {
	/*
	 * Reference to hold the web service URL
	 */
	public static final String MLMS_VENDOR_COMPLETION_SERVICE_URL = "mlms.vendor.completion.service.url";
	/*
	 * reference to the web service client implementation class
	 */
	private static MLMSABVendorCompletion_Service service = null;
	
	public static MLMSABVendorCompletion_Service getService() throws Exception{
		if(service == null){
			createService();
		}
		return service;
	}
	private static void createService() throws MalformedURLException{
		//String serviceUrl = System.getProperty(MLMS_VENDOR_COMPLETION_SERVICE_URL,"http://localhost:9080/ExternalABVendor/MLMSABVendorCompletion");
		String serviceUrl = System.getProperty(MLMS_VENDOR_COMPLETION_SERVICE_URL,"https://testi.mlms.cms.gov/MLMSABVendorCompletion");
		URL wsdlUrl = new URL(serviceUrl+"?wsdl");
		/*
		 * create service from class that has @WebServiceClientAnnotation
		 */
		service = new MLMSABVendorCompletion_Service(wsdlUrl);
	}
	
}

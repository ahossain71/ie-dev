package gov.mlms.cciio.cms.mlmsregistrationgap;

import java.io.StringWriter;

import gov.mlms.cciio.cms.mlmsregistrationgap.sessionbean.view.RegistrationGapSessionBeanLocal;
import gov.mlms.cciio.cms.registrationgaprequest.RegistrationGapRequestType;
import gov.mlms.cciio.cms.registrationgapresponse.RegistrationGapResponseType;

import javax.ejb.EJB;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


@javax.jws.WebService (endpointInterface="gov.mlms.cciio.cms.mlmsregistrationgap.MLMSRegistrationGap", targetNamespace="http://cms.cciio.mlms.gov/MLMSRegistrationGap/", serviceName="MLMSRegistrationGap", portName="MLMSRegistrationGapSOAP", wsdlLocation="WEB-INF/wsdl/MLMSRegistrationGap.wsdl")
public class MLMSRegistrationGapSOAPImpl{
	
	@EJB
	private RegistrationGapSessionBeanLocal rgStatefulSessionBean;

	/**
	 * The requirement is for the web service to return training records for all agents and brokers
	 * that have completed the Individual Marketplace or SHOP Marketplace certification for the current 
	 * year and the MLMS does not have "Complete" in the custom0 field in the cmt_person table.
	 * 
	 A RegistrationGapResponseType contains
	 	ME800 User ID was not found error.invalid.userid
		ME810 No Data Found no.data.for.userid
		ME820 Unexpected Error unexpected.error
		ME830 Database Unavailable database.unavailable
		ME840 System Maintenance: Contact MLMS Administration for expected uptime system.maintenance.window

	  **/
	/**
	 * 
	 * @param registrationGapRequest
	 * @return
	 */
	
    public RegistrationGapResponseType retrieveMLMSRegistrationGAP(RegistrationGapRequestType registrationGapRequest){
    	
    	cms.ccio.mlms.ws.transactionapi.TransactionLogAPI transLog= new TransactionLogAPI();
		try{
			JAXBContext context = JAXBContext.newInstance(RegistrationGapRequestType.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    try{
		    m.marshal(registrationGapRequest, sw);
		    }catch(JAXBException me){
		    	sw.write("Cannot Marshal XML " + registrationGapRequest.getRequestSystemName());
		    }
		    String result = sw.toString();
		    transLog.insertWSTransaction(MLMSWSCONSTANTS.HIOS_VERIFICATION_CLIENT, 
				MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
				MLMSWSCONSTANTS.PARTNER_CODE_HIOS, 
				MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"RegistrationGapServiceRequest");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("RegistrationGapSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
		}
    	
    	return rgStatefulSessionBean.retrieveMLMSRegistrationGAP(registrationGapRequest);
    }
  
	
}
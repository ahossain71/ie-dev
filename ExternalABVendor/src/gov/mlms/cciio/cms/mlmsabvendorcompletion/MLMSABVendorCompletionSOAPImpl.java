package gov.mlms.cciio.cms.mlmsabvendorcompletion;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
//import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;
import gov.mlms.cciio.cms.externalvendorrequesttype.ExternalVendorRequestType;
import gov.mlms.cciio.cms.externalvendorresponsetype.ExternalVendorResponseType;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.sessionbean.MLMSABVendorCompletionSessionBean;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.sessionbean.view.MLMSABVendorCompletionSessionBeanLocal;


@javax.jws.WebService (endpointInterface="gov.mlms.cciio.cms.mlmsabvendorcompletion.MLMSABVendorCompletion", targetNamespace="http://cms.cciio.mlms.gov/MLMSABVendorCompletion/", serviceName="MLMSABVendorCompletion", portName="MLMSABVendorCompletionSOAP")
public class MLMSABVendorCompletionSOAPImpl{

	private MLMSABVendorCompletionSessionBeanLocal vendorCompletionSessionBean;
	
    public ExternalVendorResponseType receiveABVendorCompletion(ExternalVendorRequestType completionRecord) {
    	
    	cms.ccio.mlms.ws.transactionapi.TransactionLogAPI transLog= new TransactionLogAPI();
		try{
			JAXBContext context = JAXBContext.newInstance(ExternalVendorRequestType.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    try{
		    	m.marshal(completionRecord, sw);
		    }catch(JAXBException me){
		    	sw.write(" cannot Marshal XML learnerId = " + completionRecord.getLearnerID() );
		    }
		    String strVendorName = "";
		    
		    switch(completionRecord.getVendorName()){
		    case "NAHU": 
		    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_NAHU;
		    	break;
		    case "AHIP":
		    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_AHIP;
		    	break;
		    case "LITMOS":
		    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_LITMOS;
		    	break;
		    default:
		    	strVendorName = "";
		    
		    }	
		    String result = sw.toString();
		    transLog.insertWSTransaction(MLMSWSCONSTANTS.AB_VENDOR_PROVIDER, 
				MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_REQ_SUCCESS, 
				strVendorName, 
				MLMSWSCONSTANTS.WS_STATUS_SUCCESS,result,"ExternalABVendorCompletionRequest");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("MLMSABVendorCompletionSOAPImpl MLMSWS Transaction logging exception --- " + e.getStackTrace());
		}
    	vendorCompletionSessionBean = new MLMSABVendorCompletionSessionBean();
		return vendorCompletionSessionBean.receiveABVendorCompletion(completionRecord);
    }

}
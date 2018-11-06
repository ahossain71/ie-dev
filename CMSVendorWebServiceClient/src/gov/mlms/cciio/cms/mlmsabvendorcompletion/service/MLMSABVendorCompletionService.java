package gov.mlms.cciio.cms.mlmsabvendorcompletion.service;

import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.externalvendorrequesttype.CurriculumCodeType;
import gov.mlms.cciio.cms.externalvendorrequesttype.CurriculumLanguageType;
import gov.mlms.cciio.cms.externalvendorrequesttype.CurriculumYearType;
import gov.mlms.cciio.cms.externalvendorrequesttype.ExternalVendorRequestType;
import gov.mlms.cciio.cms.externalvendorresponsetype.ExternalVendorResponseType;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.MLMSABVendorCompletion;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.MLMSABVendorCompletion_Service;

public class MLMSABVendorCompletionService {
	/**
	 * 
	 * @param VendorCode
	 * @param CurriculumCode
	 * @param year
	 * @param GUID
	 */
	public ExternalVendorResponseType submitCompletion(String vendorCode,
			String curriculumCode, String year, String guid, String language) {
		String methodName = "submitCompletion";
		/*
		 * create response object
		 */
		ExternalVendorResponseType response = null;
		System.out.println(MLMSABVendorCompletionService.class.getName() + " " +methodName +
		"\nVendor Code " + vendorCode +
		"\nCurriculum Code " + curriculumCode +
		"\nCurriculum Year " + year +
		"\nGUID " + guid +
		"\nLanguage " + language);

		try {
			
			/*
			 * class that has WebServiceClient annotation
			 */
			MLMSABVendorCompletion_Service mlmsABVendorCompletion_Service = ServiceFactory.getService();
			/*
			 * interface that has the WebService annotation
			 */
			MLMSABVendorCompletion proxy = mlmsABVendorCompletion_Service.getPort(MLMSABVendorCompletion.class );
		
			/*
			 * create request
			 */
			ExternalVendorRequestType request = new ExternalVendorRequestType();
			/*
			 * populate request
			 */
			request.setCurriculumCode(getCurriculumCodeType(curriculumCode));
			
			request.setVendorName(vendorCode);
			
			request.setLearnerID(guid);
			
			request.setCurriculumLanguage(getCurriculumLanguageType(language));
			
			request.setCurriculumYear(getCurriculumYearType(year));
			/*
			call web service and get the response
			 */
			response = proxy.receiveABVendorCompletion(request);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(response == null){
			response = new ExternalVendorResponseType();
			StatusType status = new StatusType();
			StatusCodeType statusCode =  StatusCodeType.MS_500;
			
			status.setStatusCode(statusCode);
			status.setStatusMessage("Error Occurred, null received from web service");
			response.setStatusCode(status);
		}

		return response;
	}
	/**
	 * 
	 * @param curriculumCode
	 * @return
	 */
	private CurriculumCodeType getCurriculumCodeType(String curriculumCode){
		CurriculumCodeType type = new CurriculumCodeType();
		type.setCurriculumCode(curriculumCode);
		return type;
	}
	/**
	 * 
	 * @param language
	 * @return
	 */
	private CurriculumLanguageType getCurriculumLanguageType(String language){
		CurriculumLanguageType type = new CurriculumLanguageType();
		if(language == null){
			language = "English";
		}
		type.setCurriculumLanguage(language);
		return type;
	}
	/**
	 * 
	 * @param year
	 * @return
	 */
	private CurriculumYearType getCurriculumYearType(String year){
		CurriculumYearType type = new CurriculumYearType();
		if(year == null){
			year = "2017";
		}
		type.setCurriculumYear(year);
		return type;
	}
}

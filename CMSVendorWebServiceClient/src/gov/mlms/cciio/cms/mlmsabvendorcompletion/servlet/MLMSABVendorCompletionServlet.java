package gov.mlms.cciio.cms.mlmsabvendorcompletion.servlet;

import gov.mlms.cciio.cms.externalvendorresponsetype.ExternalVendorResponseType;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.service.MLMSABVendorCompletionService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MLMSABVendorCompletionServlet extends HttpServlet {

	private static final String VENDORNAME = "vendorName";
	private static final String CURRICULUMCODE = "curriculumCode";
	private static final String CURRICULUMYEAR = "curriculumYear";
	private static final String GUID = "guid";
	private static final String CURRICULUMLANGUAGE = "curriculumLanguage";
	private static final long serialVersionUID = 1L;
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		String methodName = "doGet";
		//Map<String, String[]> map = request.getParameterMap();
		//Set set = map.entrySet();
		String vendorCode = request.getParameter(VENDORNAME);
		String curriculumCode = request.getParameter(CURRICULUMCODE);
		String year = request.getParameter(CURRICULUMYEAR);
		String guid = request.getParameter(GUID);
		String language = request.getParameter(CURRICULUMLANGUAGE);
		System.out.println(MLMSABVendorCompletionServlet.class.getName() + " " + methodName +
				"\n Vendor Code " + vendorCode +
				"\n Curriculum Code " + curriculumCode +
				"\n Year " + year + 
				"\n guid " + guid +
				"\n language " + language);
		
		MLMSABVendorCompletionService service = new MLMSABVendorCompletionService();
		
		ExternalVendorResponseType evResponse = service.submitCompletion(vendorCode, curriculumCode, year, guid, language);
		
		System.out.println(MLMSABVendorCompletionServlet.class.getName() + " " + methodName +
				"\nResponse status code " + evResponse.getStatusCode().getStatusCode() +
				"\nResponse status message " + evResponse.getStatusCode().getStatusMessage());
		request.setAttribute("MLMSABCompletionResponse", evResponse);
		
		
		try {
			getServletContext().getRequestDispatcher("result/appDetails.jsp").forward(request, response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

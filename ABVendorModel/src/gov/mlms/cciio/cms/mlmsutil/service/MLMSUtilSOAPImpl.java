package gov.mlms.cciio.cms.mlmsutil.service;

import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorMessageType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.checkmlmsdbstatusrequest.CheckMLMSDBStatusRequestType;
import gov.mlms.cciio.cms.usertrainingcompletionstatus.UserTrainingResponseType;

import java.util.ArrayList;

import util.mlms.cciio.cms.mlmsutil.dao.MLMSUtilDAO;
import util.mlms.cciio.cms.mlmsutil.exception.MLMSUtilDAOException;

@javax.jws.WebService(endpointInterface = "gov.mlms.cciio.cms.mlmsutil.service.MLMSUtil", targetNamespace = "http://cms.cciio.mlms.util/MLMSUtil/", serviceName = "MLMSUtil", portName = "MLMSUtilSOAP")
public class MLMSUtilSOAPImpl {

	public StatusType checkMLMSDBStatus(
			CheckMLMSDBStatusRequestType checkMLMSDBStatusRequest) {
		UserTrainingResponseType userTrainingResponseType = new UserTrainingResponseType();
		MLMSUtilDAO dao = new MLMSUtilDAO();
		StatusType statusType = new StatusType();
		try {
			ArrayList<String> usernames = dao.getMLMSRandomUserlist();
			if (usernames != null) {
				System.out.println(" user list length " + usernames.size());
				 dao.getEnrollmentCompletionData(usernames);

				 statusType.setStatusCode(StatusCodeType.MS_200);
					statusType.setStatusMessage("operation successful");
			} else {
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("no data returned");
				statusType.setErrorCode(ErrorCodeType.ME_810.value());
				statusType.setErrorMessage(ErrorMessageType.QUERY_RETURNED_NO_DATA.value());
			}

		} catch (MLMSUtilDAOException e) {
			userTrainingResponseType.setErrorCode(e.toString());

		}

		return statusType;
	}

}
package gov.cms.cciio.interfaces.abmodel;

import gov.cms.cciio.common.response.MLMSResponse;

/**
 * Explicitly calling super method since I am not sure how the WebService/WSDL generator handles empty class.
 * @author feilung
 *
 */
public class Response extends MLMSResponse {

    protected String statusCode;
    protected String statusMessage;
    protected String errorCode;
    protected String errorKey;
    protected String errorMessage;
    
    public Response(String statusCode, String statusMessage) {
        this(statusCode, statusMessage, null, null, null);
    }
    
    public Response(String statusCode, String statusMessage, String errorCode, String errorKey, String errorMessage) {
    	super(statusCode, statusMessage, errorCode, errorKey, errorMessage);
    }
    
    public Response(MLMSResponse response) {
    	super(response.getStatusCode(), response.getStatusMessage(), response.getErrorCode(), response.getErrorKey(), response.getErrorMessage());
    }
    
    public String getStatusCode() {
        return super.getStatusCode();
    }

    public void setStatusCode(String statusCode) {
    	super.setStatusCode(statusCode);
    }

    public String getStatusMessage() {
        return super.getStatusMessage();
    }

    public void setStatusMessage(String statusMessage) {
    	super.setStatusMessage(statusMessage);
    }

    public String getErrorCode() {
        return super.getErrorCode();
    }

    public void setErrorCode(String errorCode) {
    	super.setErrorCode(errorCode);
    }

    public String getErrorKey() {
        return super.getErrorKey();
    }

    public void setErrorKey(String errorKey) {
    	super.setErrorKey(errorKey);
    }

    public String getErrorMessage() {
        return super.getErrorMessage();
    }

    public void setErrorMessage(String errorMessage) {
    	super.setErrorMessage(errorMessage);
    }

    public String toString() {
    	return super.toString();
    }
}

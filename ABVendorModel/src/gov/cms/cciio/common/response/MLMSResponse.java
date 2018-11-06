package gov.cms.cciio.common.response;

import gov.cms.cciio.common.util.CommonUtil;

public class MLMSResponse {

    protected String statusCode;
    protected String statusMessage;
    protected String errorCode;
    protected String errorKey;
    protected String errorMessage;
    
    public MLMSResponse(String statusCode, String statusMessage) {
        this(statusCode, statusMessage, null, null, null);
    }
    
    public MLMSResponse(String statusCode, String statusMessage, String errorCode, String errorKey, String errorMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.errorCode = errorCode;
        this.errorKey = errorKey;
        this.errorMessage = errorMessage;
    }
    
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Status Code : ");
        sb.append(statusCode);
        sb.append(CommonUtil.EOL);
        sb.append("status Message : ");
        sb.append(statusMessage);
        sb.append(CommonUtil.EOL);
        sb.append("Error Code : ");
        sb.append(errorCode);
        sb.append(CommonUtil.EOL);
        sb.append("Error Key : ");
        sb.append(errorKey);
        sb.append(CommonUtil.EOL);
        sb.append("Error Message : ");
        sb.append(errorMessage);
        return sb.toString();
    }
}

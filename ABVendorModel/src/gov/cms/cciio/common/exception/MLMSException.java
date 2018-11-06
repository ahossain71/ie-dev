package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    New for Saba 7
 */
/**
 * Custom Exception class for raising general processing exceptions
 * @author Feilung Wong
 */

public class MLMSException extends Exception implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -2151952456377681822L;
    protected int errorCode;
    protected int retrySeconds;
    
    public MLMSException(int errorCode) {
        super();
        this.errorCode = errorCode;
        this.retrySeconds = -1;
    }

    public MLMSException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.retrySeconds = -1;
    }
    
    public MLMSException(int errorCode, String message, Throwable exception){
        super(message, exception);
        this.errorCode = errorCode;
        this.retrySeconds = -1;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public int getRetrySeconds() {
        return retrySeconds;
    }
    
    public void setRetrySeconds(int retrySeconds) {
        this.retrySeconds = retrySeconds;
    }
    
    public boolean shouldRetry() {
        return retrySeconds > 0;
    }
}

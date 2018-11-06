package gov.mlms.cciio.cms.exception;

import java.io.Serializable;

/**
 * Custom Exception class for raising general processing exceptions
 * @author Feilung Wong
 */

public class MLMSException extends Exception implements Serializable{

    private static final long serialVersionUID = -2151952456377681822L;
    protected int errorCode;
    
    public MLMSException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public MLMSException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MLMSException(int errorCode, String message, Throwable exception){
        super(message, exception);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}

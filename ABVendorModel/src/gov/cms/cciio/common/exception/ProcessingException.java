package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used for raise Registration exceptions
 * @author Feilung Wong
 */
public class ProcessingException extends MLMSException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -3074356986490805385L;

    public ProcessingException(int errorCode) {
        super(errorCode);
    }

    public ProcessingException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public ProcessingException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

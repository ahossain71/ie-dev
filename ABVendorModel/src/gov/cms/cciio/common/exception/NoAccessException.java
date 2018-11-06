package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used for raise No Access exceptions
 * @author Feilung Wong
 */
public class NoAccessException extends MLMSException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -8066477279047722035L;

    public NoAccessException(int errorCode) {
        super(errorCode);
    }

    public NoAccessException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public NoAccessException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used for raise User exceptions
 * @author Feilung Wong
 */
public class UserException extends MLMSException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 5680859719290364350L;

    public UserException(int errorCode) {
        super(errorCode);
    }

    public UserException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public UserException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

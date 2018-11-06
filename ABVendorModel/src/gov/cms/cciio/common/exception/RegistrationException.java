package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Generic processing exceptions
 * @author Feilung Wong
 */
public class RegistrationException extends MLMSException implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -3074356986490805385L;

    public RegistrationException(int errorCode) {
        super(errorCode);
    }

    public RegistrationException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public RegistrationException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

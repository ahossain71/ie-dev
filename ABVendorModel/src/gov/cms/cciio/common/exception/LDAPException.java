package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used for raise User exceptions
 * @author Steve Meyer
 */
public class LDAPException extends MLMSException implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -6658307770684086568L;

    public LDAPException(int errorCode) {
        super(errorCode);
    }

    public LDAPException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public LDAPException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

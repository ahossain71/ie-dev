package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used for raise DB exceptions
 * @author Feilung Wong
 */
public class DBUtilException extends MLMSException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 662428983404651231L;

    public DBUtilException(int errorCode) {
        super(errorCode);
    }

    public DBUtilException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public DBUtilException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

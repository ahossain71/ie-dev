package gov.mlms.cciio.cms.exception;

import java.io.Serializable;

/**
 * Used for raise WS Logging exceptions
 * @author Feilung Wong
 */
public class WSLogException extends MLMSException implements Serializable{

    private static final long serialVersionUID = 662428983404651232L;

    public WSLogException(int errorCode) {
        super(errorCode);
    }

    public WSLogException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public WSLogException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

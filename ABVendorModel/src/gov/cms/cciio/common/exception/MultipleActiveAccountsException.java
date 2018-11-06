package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used when multiple accounts found for a user are active
 * @author Feilung Wong
 */
public class MultipleActiveAccountsException extends LDAPException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -867924559453902536L;

    public MultipleActiveAccountsException(int errorCode) {
        super(errorCode);
    }

    public MultipleActiveAccountsException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public MultipleActiveAccountsException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

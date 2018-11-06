package gov.cms.cciio.common.exception;

import java.io.Serializable;

/*
 * 2013-07-16   Feilung Wong    
 */
/**
 * Used when all accounts found for a user are inactive
 * @author Feilung Wong
 */
public class AllAccountsInactiveException extends LDAPException implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -8360346085325560409L;

    public AllAccountsInactiveException(int errorCode) {
        super(errorCode);
    }

    public AllAccountsInactiveException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    public AllAccountsInactiveException(int errorCode, String message, Throwable exception){
        super(errorCode, message, exception);
    }
}

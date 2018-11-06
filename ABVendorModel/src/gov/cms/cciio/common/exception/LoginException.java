package gov.cms.cciio.common.exception;

import java.io.Serializable;

public class LoginException extends MLMSException implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9207289832700307745L;
	public LoginException(int errorCode, String message, Throwable exception) {
		super(errorCode, message, exception);
		// TODO Auto-generated constructor stub
	}
	public LoginException(int errorCode) {
        super(errorCode);
    }
	

    public LoginException(int errorCode, String message) {
        super(errorCode, message);
    }
    
    

}

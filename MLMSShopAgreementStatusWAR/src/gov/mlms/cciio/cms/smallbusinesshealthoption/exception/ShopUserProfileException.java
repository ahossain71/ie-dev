package gov.mlms.cciio.cms.smallbusinesshealthoption.exception;

public class ShopUserProfileException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String message = null;
	public ShopUserProfileException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ShopUserProfileException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ShopUserProfileException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ShopUserProfileException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public String getException(){
		return message;
	}
}

package gov.mlms.cciio.cms.smallbusinesshealthoption.exception;

public class ShopUserProfileConnectionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String message = null;
	public ShopUserProfileConnectionException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ShopUserProfileConnectionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ShopUserProfileConnectionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ShopUserProfileConnectionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public String getException(){
		return message;
	}
}

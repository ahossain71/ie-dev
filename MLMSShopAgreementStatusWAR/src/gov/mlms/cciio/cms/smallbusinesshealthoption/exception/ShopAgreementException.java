/**
 * 
 */
package gov.mlms.cciio.cms.smallbusinesshealthoption.exception;

/**
 * @author xnieibm
 *
 */
public class ShopAgreementException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String message = null;
	/**
	 * 
	 */
	public ShopAgreementException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ShopAgreementException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ShopAgreementException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ShopAgreementException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public String getException(){
		return message;
	}

}

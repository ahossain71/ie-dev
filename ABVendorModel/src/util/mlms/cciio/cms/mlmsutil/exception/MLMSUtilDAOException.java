package util.mlms.cciio.cms.mlmsutil.exception;

public class MLMSUtilDAOException extends Exception{
	private static final long serialVersionUID = 1L;
	
	
	
	public MLMSUtilDAOException() {
		
	}

	/**
	 * @param message
	 */
	public MLMSUtilDAOException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	

	/**
	 * @param cause
	 */
	public MLMSUtilDAOException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MLMSUtilDAOException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
}

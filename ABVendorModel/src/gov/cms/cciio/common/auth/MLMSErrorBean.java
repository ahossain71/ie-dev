package gov.cms.cciio.common.auth;

public class MLMSErrorBean {
	boolean mlmsGroupError = false;
	boolean trainingGroupError = false;
	boolean generalError = false;
	boolean uidError = false;
	boolean eidmHeaderError = false;
	boolean loginError = false;
	private String errorMsg = "Error message is undefined";
	/**
	 * @return the mlmsGroupError
	 */
	public boolean isMlmsGroupError() {
		return mlmsGroupError;
	}
	/**
	 * @param mlmsGroupError the mlmsGroupError to set
	 */
	public void setMlmsGroupError(boolean mlmsGroupError) {
		this.mlmsGroupError = mlmsGroupError;
	}
	/**
	 * @return the trainingGroupError
	 */
	public boolean isTrainingGroupError() {
		return trainingGroupError;
	}
	/**
	 * @param trainingGroupError the trainingGroupError to set
	 */
	public void setTrainingGroupError(boolean trainingGroupError) {
		this.trainingGroupError = trainingGroupError;
	}
	/**
	 * @return the generalError
	 */
	public boolean isGeneralError() {
		return generalError;
	}
	/**
	 * @param generalError the generalError to set
	 */
	public void setGeneralError(boolean generalError) {
		this.generalError = generalError;
	}
	/**
	 * @return the uidError
	 */
	public boolean isUidError() {
		return uidError;
	}
	/**
	 * @param uidError the uidError to set
	 */
	public void setUidError(boolean uidError) {
		this.uidError = uidError;
	}
	/**
	 * @return the eidmHeaderError
	 */
	public boolean isEidmHeaderError() {
		return eidmHeaderError;
	}
	/**
	 * @param eidmHeaderError the eidmHeaderError to set
	 */
	public void setEidmHeaderError(boolean eidmHeaderError) {
		this.eidmHeaderError = eidmHeaderError;
	}
	/**
	 * @return the loginEror
	 */
	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	/**
	 * @param errorMsg the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	
	
	
}

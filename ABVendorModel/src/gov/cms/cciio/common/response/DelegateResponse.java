package gov.cms.cciio.common.response;

import gov.cms.cciio.common.util.CommonUtil;

import java.io.Serializable;

/*
 * 2013-07-30   Feilung Wong    Saba replaces EJB's with the Spring framework, no more stateful beans to remove
 */
/**
 * Response object for Delegate
 * @author Feilung Wong
 * @version 1.0
 * @since 2-25-10
 */

public class DelegateResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8938173482573545700L;
    
    protected int statusCode;
    protected String identifier;
    protected String status;
    protected String message;
    protected int retryMillis;
    protected String id1;
    protected String id2;
    protected String id3;
    protected String id4;
    
    public DelegateResponse(String identifier, String status, int statusCode) {
        this(identifier, status, statusCode, null, 0);
    }
    
    public DelegateResponse(String identifier, String status, int statusCode, String message) {
        this(identifier, status, statusCode, message, 0);
    }

    public DelegateResponse(String identifier, String status, int statusCode, String message, int retryMillis) {
        this.identifier = identifier;
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.retryMillis = retryMillis;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }    
    
    public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
    public int getRetryMillis() {
        return retryMillis;
    }

    public void setRetryMillis(int retryMillis) {
        this.retryMillis = retryMillis;
    }
    
    /**
     * Registration.createOrder saves REG_ID here
     * @return
     */
    public String getID1() {
        return id1;
    }
    
    /**
     * Registration.createOrder saves REG_ID here
     * @return
     */
    public void setID1(String id1) {
        this.id1 = id1;
    }
    
    /**
     * Registration.createOrder saves order ID here
     * @return
     */
    public String getID2() {
        return id2;
    }
    
    /**
     * Registration.createOrder saves order ID here
     * @return
     */
    public void setID2(String id2) {
        this.id2 = id2;
    }
    
    /**
     * Registration.createMandatoryOrder saves registration ID here
     * @return
     */
    public String getID3() {
        return id3;
    }
    
    /**
     * Registration.createMandatoryOrder saves registration ID here
     * @return
     */
    public void setID3(String id3) {
        this.id3 = id3;
    }
    
    /**
     * Registration.createMandatoryOrder saves offering ID here
     * @return
     */
    public String getID4() {
        return id4;
    }
    
    /**
     * Registration.createMandatoryOrder saves offering ID here
     * @return
     */
    public void setID4(String id4) {
        this.id4 = id4;
    }
    
	public String toString() {
		StringBuilder sb = new StringBuilder("ID : ");
		sb.append(identifier);
		sb.append(CommonUtil.EOL);
        sb.append("Code : ");
        sb.append(statusCode);
        sb.append(CommonUtil.EOL);
		sb.append("Status : ");
		sb.append(status);
		sb.append(CommonUtil.EOL);
        sb.append("Retry millis : ");
        sb.append(retryMillis);
        sb.append(CommonUtil.EOL);
        sb.append("ID 1 : ");
        sb.append(id1);
        sb.append(CommonUtil.EOL);
        sb.append("ID 2 : ");
        sb.append(id2);
        sb.append(CommonUtil.EOL);
        sb.append("ID 3 : ");
        sb.append(id3);
        sb.append(CommonUtil.EOL);
        sb.append("ID 4 : ");
        sb.append(id4);
        sb.append(CommonUtil.EOL);
		sb.append("Message: ");
		sb.append(message);
		return sb.toString();
	}

	/**
	 * Create an array of DelegateResponse with the same status and description, given a list of identifiers
	 * @param identifiers an array of identifiers for the DelegateResponse
	 * @param status the status for all DelegateResponse
	 * @param statusCode
	 * @param desc the description for all DelegateResponse
	 * @param retryMillis milliseconds to wait before retry
     * @param id1
     * @param id2
     * @param id3
     * @param id4
	 * @return
	 */
	public static DelegateResponse[] replicateResponse(String[] identifiers, String status, int statusCode, String desc, int retryMillis,
	        String id1, String id2, String id3, String id4) {
	    int size = identifiers.length;
	    DelegateResponse[] responses = new DelegateResponse[size];
	    
        for (int i = 0; i < size; i++) {
            responses[i] = new DelegateResponse(identifiers[i], status, statusCode, desc, retryMillis);
            responses[i].setID1(id1);
            responses[i].setID2(id2);
            responses[i].setID3(id3);
            responses[i].setID4(id4);
        }
	    return responses;
	}
}

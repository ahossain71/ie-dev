package gov.mlms.cciio.cms.mlmsabvendorcompletion.sessionbean;

import gov.mlms.cciio.cms.cciiowebservicecommontype.ErrorCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusCodeType;
import gov.mlms.cciio.cms.cciiowebservicecommontype.StatusType;
import gov.mlms.cciio.cms.externalvendorrequesttype.ExternalVendorRequestType;
import gov.mlms.cciio.cms.externalvendorresponsetype.ExternalVendorResponseType;
import gov.mlms.cciio.cms.mlmsabvendorcompletion.sessionbean.view.MLMSABVendorCompletionSessionBeanLocal;

import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import cms.ccio.mlms.ws.transactionapi.TransactionLogAPI;
import cms.ccio.mlms.ws.transactionapi.constants.MLMSWSCONSTANTS;

@Stateful
public class MLMSABVendorCompletionSessionBean implements
		MLMSABVendorCompletionSessionBeanLocal {

	 protected static final String EOL = System.getProperty("line.separator");
	 protected static final String JNDI_LMSDB = "jdbc/lms";
	@Override
	public ExternalVendorResponseType receiveABVendorCompletion(
			ExternalVendorRequestType completionRecord) {
		String methodName = "receiveABVendorCompletion";
		ExternalVendorResponseType response = new ExternalVendorResponseType();
		StatusType statusType = new StatusType();
		StringBuilder errorSB = new StringBuilder("MLMSABVendorCompletionSOAPImpl.receiveABVendorCompletion(");
		Connection conn = null;
		CallableStatement statement = null;
		String vendorName, learnerID, currCode, currYear, currLang;

		try {
			vendorName = completionRecord.getVendorName();
			learnerID = completionRecord.getLearnerID();

			currCode = completionRecord.getCurriculumCode()==null?null:completionRecord.getCurriculumCode().getCurriculumCode();
			currYear = completionRecord.getCurriculumYear()==null?null:completionRecord.getCurriculumYear().getCurriculumYear();
			currLang = completionRecord.getCurriculumLanguage()==null?null:completionRecord.getCurriculumLanguage().getCurriculumLanguage();
			System.out.println("MLMSABVendorCompletionSessionBean" + " " + methodName +
					"\n Vendor Name " + vendorName +
					"\n GUID 		" + learnerID +
					"\n CurrCode 	" + currCode +
					"\n CurrYear 	" + currYear +
					"\n CurrLang 	" + currLang);
			
			// for Logging
			errorSB.append(vendorName==null?"<null>":vendorName);
			errorSB.append(learnerID==null?"<null>":learnerID);
			errorSB.append(currCode==null?"<null>":currCode);
			errorSB.append(currYear==null?"<null>":currYear);
			errorSB.append(currLang==null?"<null>":currLang);
			errorSB.append(")\n");
			
			//TODO: Any validation or change to upper/lower case for consistency?
			
			conn = getDBConnection(JNDI_LMSDB, errorSB);
			if (conn == null) {
				statusType.setStatusCode(StatusCodeType.MS_500);
				statusType.setStatusMessage("Failure");
				statusType.setErrorCode(ErrorCodeType.ME_830);
				statusType.setErrorKey("ME830 Database unavailable");
				statusType.setErrorMessage("Error saving request to DB");
			} else {
				// Call Stored Procedure
				statement = conn.prepareCall("{call AP_MLMS_VENDOR_COMP_INS(?, ?, ?, ?, ?, ?, ?, ?)}");
				statement.setString(1, vendorName);
				statement.setString(2, learnerID);
				statement.setString(3, currCode);
				statement.setString(4, currYear);
				statement.setString(5, currLang);
				statement.registerOutParameter(6, Types.INTEGER);
				statement.registerOutParameter(7, Types.INTEGER);
				statement.registerOutParameter(8, Types.VARCHAR);
				statement.execute();
				int iError = statement.getInt(6);
				int iExist = statement.getInt(7);
				String errorDesc = statement.getString(8);
				
				if (iError > 0) {
					statusType.setStatusCode(StatusCodeType.MS_500);
					statusType.setStatusMessage("Failure");
					if (iError == 810) {
						statusType.setErrorCode(ErrorCodeType.ME_810);
						statusType.setErrorKey("ME810 Query returned no data");
						statusType.setErrorMessage(errorDesc);
					} else {
						statusType.setErrorCode(ErrorCodeType.ME_820);
						statusType.setErrorKey("ME820 The system encountered an unexpected error");
						statusType.setErrorMessage(errorDesc);
					}
					// Log the actual parameters
					errorSB.append("Failed: AP_LMS_VENDOR_COMP_INS (");
					errorSB.append(vendorName);
					errorSB.append(",");
					errorSB.append(learnerID);
					errorSB.append(",");
					errorSB.append(currCode);
					errorSB.append(",");
					errorSB.append(currYear);
					errorSB.append(",");
					errorSB.append(currLang);
					errorSB.append(",");
					errorSB.append(iError);
					errorSB.append(",");
					errorSB.append(iExist);
					errorSB.append(",");
					errorSB.append((errorDesc == null)?"<null>":errorDesc);
					errorSB.append(")\n");
		            System.out.println(errorSB.toString());
		            System.err.println(errorSB.toString());					
				} else if (iExist > 0) {
					/*
					statusType.setStatusCode(StatusCodeType.MS_210);
					statusType.setStatusMessage("Success");
					statusType.setErrorMessage("Received previously");
					*/
					statusType.setStatusCode(StatusCodeType.MS_200);
					statusType.setStatusMessage("Success");
				} else {
					statusType.setStatusCode(StatusCodeType.MS_200);
					statusType.setStatusMessage("Success");
				}
			}
		} catch (Exception ex) {
            errorSB.append(stackTraceToString(ex));

            statusType.setStatusCode(StatusCodeType.MS_500);
			statusType.setStatusMessage("Failure");
			statusType.setErrorCode(ErrorCodeType.ME_820);
			statusType.setErrorKey("ME820 The system encountered an unexpected error");
			statusType.setErrorMessage("The system encountered an unexpected error");
		} finally {
			freeDBResources(null, statement, conn, errorSB);
		}
		
		// If it failed for any reason, write to text log
		if (!StatusCodeType.MS_200.equals(statusType.getStatusCode())) {
			errorSB.append("\nStatusCode = ");
			errorSB.append(statusType.getStatusCode());
			errorSB.append("\nStatusMessage = ");
			errorSB.append(statusType.getStatusMessage());
			errorSB.append("\nErrorCode = ");
			errorSB.append(statusType.getErrorCode());
			errorSB.append("\nErrorKey = ");
			errorSB.append(statusType.getErrorKey());
			errorSB.append("\nErrorMessage = ");
			errorSB.append(statusType.getErrorMessage());

			System.out.println(errorSB.toString());
	        System.err.println(errorSB.toString());
		}		
		
		// Populate the response object
		response.setStatusCode(statusType);
		
		TransactionLogAPI transLog= new TransactionLogAPI();
		String mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_SUCCESS;
		String mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_SUCCESS;
		
		if(!statusType.equals(System.getProperty("operation.successful", "MS200"))){
			mlmsConstantsSuccess = MLMSWSCONSTANTS.WS_STATUS_FAIL;
			mlmsConstantsResSuccess = MLMSWSCONSTANTS.WS_TRANSACTIONC_CODE_RSP_FAIL;
		}
		
		String strVendorName = "";
	    
	    switch(completionRecord.getVendorName()){
	    case "NAHU": 
	    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_NAHU;
	    	break;
	    case "AHIP":
	    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_AHIP;
	    	break;
	    case "LITMOS":
	    	strVendorName = MLMSWSCONSTANTS.PARTNER_CODE_LITMOS;
	    	break;
	    default:
	    	strVendorName = "";
	    
	    }	
	    
		try{
			JAXBContext context = JAXBContext.newInstance(ExternalVendorResponseType.class);
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    StringWriter sw = new StringWriter();
		    try{
		    	m.marshal(response, sw);
		    }catch(JAXBException me){
		    	sw.write("cannot Marshal XML storing the status message " + response.getStatusCode().getStatusMessage());
		    }

		    String result = sw.toString();
		transLog.insertWSTransaction(MLMSWSCONSTANTS.AB_VENDOR_PROVIDER, 
				mlmsConstantsResSuccess, 
				strVendorName, 
				mlmsConstantsSuccess,result,"ExternalABVendorCompletionReponse");
		}catch(Exception e){
			//don't do anything for now
			System.out.println("MLMSABVendorCompletionSessionBean MLMSWS Transaction logging exception --- " + e.getStackTrace());
		}
		return response;
	}
	 /**
     * Obtain connection to DB defined in console
     * @param jndi the JNDI string to lookup data source
     * @param sb the StringBuilder that the error, if any, should be appended to. Can be null
     * @return a connection to the DB, null if unsuccessful
     */
    protected Connection getDBConnection(String jndi, StringBuilder sb) {
        InitialContext ctx = null;
        DataSource ds = null;
        Connection conn = null;
        
        try {
            ctx = new InitialContext();
            ds = (javax.sql.DataSource) ctx.lookup(jndi);
            conn = ds.getConnection();
        } catch (Exception ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** Exception caught ***\n");
                sb.append(stackTraceToString(ex));
            }
            System.out.println("MLMSABVendorCompletionSOAPImpl.getDBConnection(" + jndi + "): " + ex);
            ex.printStackTrace(System.err);
        }
        return conn;
    }

    /**
     * Close ResultSet, Statement, and DB Connection.
     * @param rs - the ResultSet to close, can be null
     * @param statement - java.sql.Statement object to close, can be null
     * @param conn - Connection to close, can be null
     * @param sb the StringBuilder that the error, if any, should be appended to. Can be null
     */
    private void freeDBResources(ResultSet rs, Statement statement, Connection conn, StringBuilder sb) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            System.out.println("MLMSABVendorCompletionSOAPImpl.freeDBResources: Error closing result set: " + ex);
            ex.printStackTrace(System.err);
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            System.out.println("MLMSABVendorCompletionSOAPImpl.freeDBResources: Error closing JDBC statement: " + ex);
            ex.printStackTrace(System.err);
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            if (sb != null && ex != null) {
                sb.append("\n*** SQLException caught ***\n");
                while (ex != null) {
                    sb.append("SQLState: " + ex.getSQLState() + "\n"
                        + "Message:  " + ex.getMessage() + "\n"
                        + "Vendor:   " + ex.getErrorCode()+ "\n \n");
                    ex = ex.getNextException();
                }
            }
            System.out.println("MLMSABVendorCompletionSOAPImpl.freeDBResources: Error freeing connection: " + ex);
            ex.printStackTrace(System.err);
        }
    }
    
    /**
     * Get stack trace of an exception in a String, default to system EOL
     * @param ex The Exception to be converted
     */
    private String stackTraceToString(Exception ex) {
        return stackTraceToString(ex, EOL);
    }
    
    /**
     * Get stack trace of an exception in a String
     * @param ex The Exception to be converted
     * @param eol End of Line string (e.g. \n in Unix, \r\n in Windows, <br/> for HTML etc
     */
    private String stackTraceToString(Exception ex, String eol) {
        StringBuilder sb = new StringBuilder();
        int i;

        sb.append(ex.toString());
        sb.append(eol);
        StackTraceElement[] st = ex.getStackTrace();
        for (i = 0; i < st.length; i++) {
            sb.append(st[i].toString());
            sb.append(eol);
        }
        Throwable cause = ex.getCause();
        if (cause != null) {
            sb.append("Caused by");
            sb.append(eol);
            sb.append(cause.toString());
            sb.append(eol);
            st = cause.getStackTrace();
            for (i = 0; i < st.length; i++) {
                sb.append(st[i].toString());
                sb.append(eol);
            }
        }
        return sb.toString();
    }

}

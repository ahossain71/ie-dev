package gov.cms.cciio.common.session;

import gov.cms.cciio.common.delegate.MLMSDelegate;
import gov.cms.cciio.common.exception.MLMSException;
import gov.cms.cciio.common.response.DelegateResponse;

import com.saba.locator.ServiceLocator;

public interface MLMSDynamicManager {

    /**
     * Use a delegate class to process a request. The commit check is used for single requests.
     * @param locator the ServiceLocator object that holds session information
     * @param delegate the initialized delegate instance
     * @param processID the ID of this process (Audit ID from IE)
     * @param transID the transaction ID
     * @param tableName (Optional) the database table name of the commit log, null to skip commit log logic
     * @return DelegateResponse
     * @throws ALMSException
     */
    public DelegateResponse processMessage(ServiceLocator locator, MLMSDelegate delegate, 
        String processID, String transID, String tableName) throws MLMSException;

    // The following was used to test transaction
//    public String work(ServiceLocator locator, String ID) throws ALMSException;
}

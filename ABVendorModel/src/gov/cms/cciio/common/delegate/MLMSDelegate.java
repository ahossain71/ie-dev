package gov.cms.cciio.common.delegate;

import gov.cms.cciio.common.exception.MLMSException;
import gov.cms.cciio.common.response.DelegateResponse;

import com.saba.locator.ServiceLocator;

/*
 * 2010-10-13   Feilung Wong  Initial
 */
/**
 * An abstract for delegates
 * @author Feilung Wong
 *
 */
public abstract class MLMSDelegate {
    protected ServiceLocator locator;
    protected boolean lockAcquired = false; // default to false

    public void setLocator(ServiceLocator locator) {
        this.locator = locator;
    }
    
    public boolean isLockAcquired() {
        return lockAcquired;
    }
    
    abstract public DelegateResponse processMessage() throws MLMSException;
    
    
    /**
     * Sub classes can override this method to release any lock. e.g. A5 lock.
     */
    public void releaseLock() {};
}

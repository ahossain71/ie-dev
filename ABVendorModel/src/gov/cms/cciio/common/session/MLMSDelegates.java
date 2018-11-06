package gov.cms.cciio.common.session;

import com.saba.locator.Delegates;

/**
 * An attempt to insert ALMS custom delegate into Saba's Delegates private Map.
 * @author Feilung
 *
 */
public class MLMSDelegates extends Delegates {
    /**
     * 
     */
    private static final long serialVersionUID = -4733170552311082690L;
    public static final Delegates kALMSDynamicManager = new MLMSDelegates("MLMSDynamicManager");
	public static final Delegates kMLMSDynamicManager = null;

    protected MLMSDelegates(String delegateName) {
        super(delegateName);
    }
}

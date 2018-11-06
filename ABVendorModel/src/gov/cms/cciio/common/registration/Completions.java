package gov.cms.cciio.common.registration;

public class Completions {
	private boolean isIMVendorComplete;
	private boolean isIMVendorRefresherComplete;
	private boolean isSHOPVendorComplete;

	public Completions(boolean isIMVendorComplete, boolean isIMVendorRefresherComplete, boolean isSHOPVendorComplete) {
		this.isIMVendorComplete = isIMVendorComplete;
		this.isIMVendorRefresherComplete = isIMVendorRefresherComplete;
		this.isSHOPVendorComplete = isSHOPVendorComplete;
	}

	public boolean isIMVendorComplete() {
		return isIMVendorComplete;
	}
	public boolean isIMVendorRefresherComplete() {
		return isIMVendorRefresherComplete;
	}
	public boolean isSHOPVendorComplete() {
		return isSHOPVendorComplete;
	}
}

package gov.cms.cciio.helpdesksupport;
//
public class HelpDeskSupportUser {
	String username = null;
	boolean controlAccess = false;
	boolean useTools = false;
	
	public String getUserName() {
		return this.username;
	}
	
	public void setUserName(String sUserName) {
		this.username = sUserName;
	}
	
	public boolean canControlAccess() {
		return this.controlAccess;
	}
	
	public void setControlAccess(boolean sControlAccess) {
		this.controlAccess = sControlAccess;
	}
	
	/**
	 * Whether the user can use custom tools
	 * @return
	 */
	public boolean canUseTools() {
		return this.useTools;
	}
	
	/**
	 * Set whether the user can use custom tools
	 * @param sUseTools
	 */
	public void setUseTools(boolean sUseTools) {
		this.useTools = sUseTools;
	} 
}
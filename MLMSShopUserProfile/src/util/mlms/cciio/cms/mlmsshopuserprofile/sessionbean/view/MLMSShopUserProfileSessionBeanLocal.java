package util.mlms.cciio.cms.mlmsshopuserprofile.sessionbean.view;

import gov.mlms.cciio.cms.shopuserprofileresponsetype.ShopUserProfileResponseType;

import javax.ejb.Local;

@Local
public interface MLMSShopUserProfileSessionBeanLocal {

	
	public ShopUserProfileResponseType retrieveShopUserProfile(String userId);
}

package util.mlms.cciio.cms.mlmsshopuserprofile.sessionbean;

import gov.mlms.cciio.cms.shopuserprofileresponsetype.ShopUserProfileResponseType;

import javax.ejb.Stateless;

import util.mlms.cciio.cms.mlmsshopuserprofile.dto.MLMSShopUserProfileDTO;
import util.mlms.cciio.cms.mlmsshopuserprofile.sessionbean.view.MLMSShopUserProfileSessionBeanLocal;
@Stateless
public class MLMSShopUserProfileSessionBean implements
		MLMSShopUserProfileSessionBeanLocal {
	/**
	 * Session bean instantiates the ShopUserProfileResponseType and DTO object. DTO object returns the assembled response
	 * session bean 
	 */
	public ShopUserProfileResponseType retrieveShopUserProfile(String userId) {
		ShopUserProfileResponseType response  = null;
		
		MLMSShopUserProfileDTO dto = new MLMSShopUserProfileDTO();
		
			response = dto.getShopUserProfileResponseType(userId);
		
		return response;
	}
	

}

package util.mlms.cciio.cms.mlmsshopuserprofile;

import gov.mlms.cciio.cms.shopuserprofileresponsetype.ShopUserProfileResponseType;
import util.mlms.cciio.cms.mlmsshopuserprofile.sessionbean.MLMSShopUserProfileSessionBean;


@javax.jws.WebService (endpointInterface="util.mlms.cciio.cms.mlmsshopuserprofile.MLMSShopUserProfile", targetNamespace="http://cms.cciio.mlms.util/MLMSShopUserProfile/", serviceName="MLMSShopUserProfile", portName="MLMSShopUserProfileSOAP")
public class MLMSShopUserProfileSOAPImpl{

    public ShopUserProfileResponseType retrieveShopUserProfile(String userId) {
       
    	// need the EJB interface with local
    	// need class that implements the EJB
    	MLMSShopUserProfileSessionBean bean = new MLMSShopUserProfileSessionBean();
    	ShopUserProfileResponseType response = bean.retrieveShopUserProfile(userId);
    	
        return response;
    }

}
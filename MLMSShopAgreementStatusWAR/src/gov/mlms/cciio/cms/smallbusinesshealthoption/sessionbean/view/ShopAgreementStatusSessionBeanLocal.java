package gov.mlms.cciio.cms.smallbusinesshealthoption.sessionbean.view;

import gov.mlms.cciio.cms.shopagreementstatusrequesttype.UserId;
import gov.mlms.cciio.cms.shopagreementstatusresponsetype.ShopAgreementStatusResponse;

public interface ShopAgreementStatusSessionBeanLocal {
	
	public ShopAgreementStatusResponse retrieveShopAgreementStatus(UserId userId);

}

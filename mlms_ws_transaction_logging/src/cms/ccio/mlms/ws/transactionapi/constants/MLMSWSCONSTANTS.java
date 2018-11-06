package cms.ccio.mlms.ws.transactionapi.constants;

public class MLMSWSCONSTANTS {
	/* WSPART0101	EIDM
	 WSPART0102	SHOP
	 WSPART0103	HIOS
	 WSPART0104	AHIP
	 WSPART0105	NAHU
	 WSPART0106	LITMOS */
	 public static final String PARTNER_CODE_EIDM = "WSPART0101";
	 public static final String PARTNER_CODE_SHOP = "WSPART0102";
	 public static final String PARTNER_CODE_HIOS = "WSPART0103";
	 public static final String PARTNER_CODE_AHIP = "WSPART0104";
	 public static final String PARTNER_CODE_NAHU = "WSPART0105";
	 public static final String PARTNER_CODE_LITMOS =  "WSPART0106";

	 
	/* WS0101	Registration Gap Provider
	 WS0102	Registration Gap Client
	 WS0103	Agent Broker Completion Provider
	 WS0104	EIDM User Profile Client
	 WS0105	SHOP Completion Provider
	 WS0106	HIOS ID lookup client
	 WS0107	Assister Completion Provider
	 WS0108	AB Vendor Provider
	  */
	 public static final String REG_GAP_PROVIDER = "WS0101";
	 public static final String REG_GAP_CLIENT 	 = "WS0102";
	 public static final String AGENT_BROKER_COMPLETION_PROVIDER = "WS0103";
	 public static final String EIDM_USER_PROFILE_CLIENT = "WS0104";
	 public static final String SHOP_COMPLETION_PROVIDER = "WS0105";
	 public static final String HIOS_VERIFICATION_CLIENT = "WS0106";
	 public static final String ASSISTER_COMPLETION_PROVIDER = "WS0107";
	 public static final String AB_VENDOR_PROVIDER = "WS0108";
	 
	 /*
	  * WSSTATUS4	WS200	Success
		WSSTATUS5	WS500	Failure
	  */
	 public static final String WS_STATUS_SUCCESS = "WS200";
	 public static final String WS_STATUS_FAIL    = "WS500";
	 /*
	  * Request	Request Operation
   		Response	Response Operation
	  */
	 public static final String WS_TRANSACTION_TYPE_REQ = "Request";
	 public static final String WS_TRANSACTION_TYPE_RSP = "Response";
	 /*
	  * WSREQ0200	Successful Request Operation
		WSRSPS0220	Successful Response Operation
		WSREQ0500	Failed Request Operation
		WSRSP0520	Failed Response Operation
	  */
	 public static final String WS_TRANSACTIONC_CODE_REQ_SUCCESS ="WSREQ0200";
	 public static final String WS_TRANSACTIONC_CODE_REQ_FAIL = "WSREQ0500";
	 public static final String WS_TRANSACTIONC_CODE_RSP_SUCCESS = "WSRSP0200";
	 public static final String WS_TRANSACTIONC_CODE_RSP_FAIL = "WSRSP0500";
}

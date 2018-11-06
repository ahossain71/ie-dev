package gov.mlms.cciio.cms.exception;


public class MLMSStatusCode {

    // Status
    public static final String SUCCESS = "S";
    public static final String PARENT = "P";
    public static final String FAILURE = "F";
    public static final String WARNING = "W"; //"SUCCESS WITH WARNING";
    public static final String NOT_EXIST = "N";
    public static final String USER_MISSING = "U";

    // Numeric status code
    public static final int NUM_SUCCESS = 100;
    public static final int NUM_SKIP    = 101; // Treat as success, used in IE
    public static final int NUM_PARENT  = 150; // Parent Error, used in IE
    public static final int NUM_WARNING = 199;

    // Registration errors
    public static final int NUM_REG_GRP_NO_DUMMY            = 600;
    public static final int NUM_REG_INVALID_STUD_CERT_STATE = 601;
    public static final int NUM_REG_OFFERING_PART_OF_CERT   = 602;
    public static final int NUM_REG_NOT_IN_CUSTOM_TABLE     = 603;
    public static final int NUM_REG_MULTIPLE_REG_PER_CERT   = 604; // Originated from AP_CMN_CI_REG_GET_INFO

    /** ATRRS related errors
    public static final int NUM_ATRRS_DOMAIN_SCHOOL_MAP   = 700;
    public static final int NUM_ATRRS_NO_DUMMY_TEMPLATE   = 701;
    public static final int NUM_ATRRS_SEATS_EXCEEDED      = 702;
    public static final int NUM_ATRRS_CRSITR_MULTI_CHOICE = 703;
    public static final int NUM_ATRRS_CRSITR_NON_OFFERING = 704;
    // Used in situations like student trying to cancel ATRRS course etc.
    public static final int NUM_ATRRS_INVALID_ACTION      = 705;**/
    
    // User/Account related errors
    public static final int NUM_USER_ACC_TYPE_UNKNOWN = 800;
    public static final int NUM_USER_NULL_SSN_FIN     = 801;
    public static final int NUM_USER_NO_ACCESS        = 803;
    public static final int NUM_USER_ACCOUNT_INACTIVE = 804;
    public static final int NUM_USER_MULTIPLE_ACC     = 805;
    public static final int NUM_USER_NOT_FOUND        = 806;

    // Content Completion
    public static final int NUM_CNT_COMP_COMPLETED_ALREADY    = 851;
    public static final int NUM_CNT_COMP_UNKNOWN_REG_MOD_ID   = 852;
    public static final int NUM_CNT_COMP_AKOUSERNAME_MISMATCH = 853;
    public static final int NUM_CNT_COMP_SECTION1_NOT_SKIPPED = 854;
    public static final int NUM_CNT_COMP_MAX_ATTEMPT          = 855;

    // General error code
    public static final int NUM_GENERIC_INTERNAL     = 900;
    public static final int NUM_GEN_LOGIN            = 901;
    public static final int NUM_GEN_VALIDATION_ERROR = 902;
    public static final int NUM_GEN_OBJ_NOT_FOUND    = 903;
    public static final int NUM_GEN_OBJ_NAME_IN_USE  = 903;
    public static final int NUM_SEC_ROLE_NOT_FOUND   = 904;
    public static final int NUM_DOMAIN_NOT_FOUND     = 905;
    public static final int NUM_COMPANY_NOT_FOUND 	 = 906;

    // DB error code
    public static final int NUM_DB_GENERAL           = 960;
    public static final int NUM_DB_NOWAIT            = 961;
    public static final int NUM_DB_PROCEDURE         = 962;

    public static final int NUM_UNKNOWN = 999;
    
    
}

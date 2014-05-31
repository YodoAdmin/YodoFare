package co.yodo.pos.fare.helper;

public class YodoGlobals {
	/*!< Preferences */
	public static final String PREFERENCES = "user_preferences";
	public static final String ID_LANGUAGE = "value_language";
	public static final String ID_CURRENCY = "value_currency";
	public static final String FIRST_USE   = "value_first_use";
	public static final CharSequence[] languages = {"English", "Español"};
	public static final int DEFAULT_LANGUAGE = 0;
	public static final int DEFAULT_CURRENCY = 0;
	public static final boolean DEFAULT_USE  = true;
	
	public static final String ID_OLD_FEE_1     = "value_old_fee_1";
	public static final String ID_OLD_FEE_2     = "value_old_fee_2";
	public static final String ID_OLD_FEE_3     = "value_old_fee_3";
	
	public static final String ID_ADULT_FEE_1   = "value_adult_fee_1";
	public static final String ID_ADULT_FEE_2   = "value_adult_fee_2";
	public static final String ID_ADULT_FEE_3   = "value_adult_fee_3";
	
	public static final String ID_CHILD_FEE_1   = "value_child_fee_1";
	public static final String ID_CHILD_FEE_2   = "value_child_fee_2";
	public static final String ID_CHILD_FEE_3   = "value_child_fee_3";
	
	public static final String ID_STUDENT_FEE_1 = "value_student_fee_1";
	public static final String ID_STUDENT_FEE_2 = "value_student_fee_2";
	public static final String ID_STUDENT_FEE_3 = "value_student_fee_3";
	
	public static final String DEFAULT_OLDFEE     = "2.50";
	public static final String DEFAULT_ADULTFEE   = "4.50";
	public static final String DEFAULT_CHILDFEE   = "2.50";
	public static final String DEFAULT_STUDENTFEE = "3.00";
	
	/*!< ID for messages */
	public static final String AUTHORIZED		       = "AU00";
	public static final String AUTHORIZED_REGISTRATION = "AU01";
	public static final String AUTHORIZED_ALTERNATE    = "AU69";
	public static final String AUTHORIZED_TRANSFER     = "AU88";
	
	/*!< ID for error response */
    public static final String ERROR_INTERNET      = "ERIN";
	public static final String ERROR_FAILED        = "ER00";
	public static final String ERROR_MAX_LIM       = "ER13";
	public static final String ERROR_INSUFF_FUNDS  = "ER25";
	public static final String ERROR_INCORRECT_PIP = "ER22";

    /*!< Id for Messages */
    public static final int NO_INTERNET   = 0;
    public static final int GENERAL_ERROR = 1;
    public static final int SUCCESS       = 3;
    
    /*!< Query Identifiers */
    public static final int QUERY_BALANCE       = 10;
    public static final int QUERY_TODAY_BALANCE = 12;
    
    /*!< Bluetooth Yodo POS name */
    public static final String YODO_POS = "Yodo-Merch";
}

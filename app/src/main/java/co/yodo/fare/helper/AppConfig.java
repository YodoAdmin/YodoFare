package co.yodo.fare.helper;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag: to print the logs in console */
    public static final boolean DEBUG = true;

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoFareSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Login status.
	 * type -- String
	 */
    static final String SPREF_HARDWARE_TOKEN = "SPHardwareToken";

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    static final String SPREF_FIRST_LOGIN = "SPFirstLogin";

    /* The current language.
	 * type -- Integer
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* The current bus route.
	 * type -- Integer
	 */
    public static final String SPREF_CURRENT_ROUTE = "SPBusRoute";

    /* The current beacon.
    * type -- String
    */
    public static final String SPREF_CURRENT_BEACON = "SPCurrentBeacon";

    /* The current currency.
    * type -- Integer
    */
    static final String SPREF_MERCHANT_CURRENCY = "SPMerchantCurrency";

    /* The current scanner position.
    * type -- Integer
    */
    static final String SPREF_CURRENT_SCANNER = "SPCurrentScanner";

    /* The current time to dismiss the response message.
    * type -- Integer
    */
    static final String SPREF_CURRENT_DISMISS_TIME = "SPDismissTime";

    /* Advertising service status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Service is running
	 * false -- Service not running
	 */
    public static final String SPREF_ADVERTISING_SERVICE = "SPAdvertisingService";

    /* Live Scan status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Live Scan On
	 * false -- Live Scan Off
	 */
    static final String SPREF_LIVE_SCAN = "SPLiveScan";

    /* The values to get the different zones */
    public static final int FEE_ZONE_1 = 1;
    public static final int FEE_ZONE_2 = 2;
    public static final int FEE_ZONE_3 = 3;

    /* The values for the different zones of the ic_elderly fare */
    public static final String SPREF_FEE_OLD_ZONE_1 = "SPOldZone1";
    public static final String SPREF_FEE_OLD_ZONE_2 = "SPOldZone2";
    public static final String SPREF_FEE_OLD_ZONE_3 = "SPOldZone3";

    /* The values for the different zones of the ic_elderly fare */
    public static final String SPREF_FEE_ADULT_ZONE_1 = "SPAdultZone1";
    public static final String SPREF_FEE_ADULT_ZONE_2 = "SPAdultZone2";
    public static final String SPREF_FEE_ADULT_ZONE_3 = "SPAdultZone3";

    /* The values for the different zones of the ic_elderly fare */
    public static final String SPREF_FEE_CHILD_ZONE_1 = "SPChildZone1";
    public static final String SPREF_FEE_CHILD_ZONE_2 = "SPChildZone2";
    public static final String SPREF_FEE_CHILD_ZONE_3 = "SPChildZone3";

    /* The values for the different zones of the ic_elderly fare */
    public static final String SPREF_FEE_STUDENT_ZONE_1 = "SPStudentZone1";
    public static final String SPREF_FEE_STUDENT_ZONE_2 = "SPStudentZone2";
    public static final String SPREF_FEE_STUDENT_ZONE_3 = "SPStudentZone3";

    /**
     * Default values
     * {{ ======================================================================
     */

    /*
	 * Default value position for the language
	 *
	 * Default: en (English)
	 */
    static final String DEFAULT_LANGUAGE = "en";

    /*
	 * Default value position for the scanner
	 *
	 * Default: position 0 (BarcodeScanner)
	 */
    static final Integer DEFAULT_SCANNER = 0;

    /*
	 * Default value for the time to dismiss the response message
	 *
	 * Default: 5 seconds
	 */
    static final Integer DEFAULT_DISMISS_TIME = 5;

    /* Defaults for the fares */
    static final String DEFAULT_OLD_FEE     = "2.50";
    static final String DEFAULT_ADULT_FEE   = "4.50";
    static final String DEFAULT_CHILD_FEE   = "2.50";
    static final String DEFAULT_STUDENT_FEE = "3.00";
}

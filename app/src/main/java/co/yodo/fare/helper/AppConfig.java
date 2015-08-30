package co.yodo.fare.helper;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag: to print the logs in console */
    public static final boolean DEBUG = false;

    /** FILE flag: to print the logs in a file */
    public static final boolean FDEBUG = false;

    /** Name of the log file */
    public static final String LOG_FILE = "log.file";

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoFareSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- Application registered and authorized
	 * false -- Application not registered or authorized
	 */
    public static final String SPREF_LOGIN_STATE = "SPLoginState";

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    public static final String SPREF_FIRST_LOGIN = "SPFirstLogin";

    /* The current language.
	 * type -- Integer
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* The timestamp of the latest currency update.
	 * type -- Integer
	 */
    public static final String SPREF_TIMESTAMP_CURRENCY = "SPTimestampCurrency";

    /* The current beacon.
    * type -- String
    */
    public static final String SPREF_CURRENT_BEACON = "SPCurrentBeacon";

    /* The current password, in case of remember option selected.
    * type -- String
    */
    public static final String SPREF_CURRENT_PASSWORD = "SPCurrentPassword";

    /* The current currency.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_CURRENCY = "SPCurrentCurrency";

    /* The current scanner position.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_SCANNER = "SPCurrentScanner";

    /* The current scanner engine.
    * type -- Integer
    */
    public static final String SPREF_SCANNER_ENGINE = "SPScannerEngine";

    /* The current time to dismiss the response message.
    * type -- Integer
    */
    public static final String SPREF_CURRENT_DISMISS_TIME = "SPDismissTime";

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
    public static final String SPREF_LIVE_SCAN = "SPLiveScan";

    /* The values to get the different zones */
    public static final int ZONE_1 = 1;
    public static final int ZONE_2 = 2;
    public static final int ZONE_3 = 3;

    /* The values for the different zones of the old fare */
    public static final String SPREF_FEE_OLD_ZONE_1 = "SPOldZone1";
    public static final String SPREF_FEE_OLD_ZONE_2 = "SPOldZone2";
    public static final String SPREF_FEE_OLD_ZONE_3 = "SPOldZone3";

    /* The values for the different zones of the old fare */
    public static final String SPREF_FEE_ADULT_ZONE_1 = "SPAdultZone1";
    public static final String SPREF_FEE_ADULT_ZONE_2 = "SPAdultZone2";
    public static final String SPREF_FEE_ADULT_ZONE_3 = "SPAdultZone3";

    /* The values for the different zones of the old fare */
    public static final String SPREF_FEE_CHILD_ZONE_1 = "SPChildZone1";
    public static final String SPREF_FEE_CHILD_ZONE_2 = "SPChildZone2";
    public static final String SPREF_FEE_CHILD_ZONE_3 = "SPChildZone3";

    /* The values for the different zones of the old fare */
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
	 * Default: position 0 (English)
	 */
    public static final Integer DEFAULT_LANGUAGE = 0;

    /*
	 * Default value position for the currency
	 *
	 * Default: position 1 (Canada Dollar)
	 */
    public static final Integer DEFAULT_CURRENCY = 1;

    /*
	 * Default value position for the scanner
	 *
	 * Default: position 0 (BarcodeScanner)
	 */
    public static final Integer DEFAULT_SCANNER = 0;

    /*
	 * Default value string of the engine
	 *
	 * Default: ZBarScanner (ZBar)
	 */
    public static final String DEFAULT_ENGINE = "ZBarScanner";

    /*
	 * Default value for the time to dismiss the response message
	 *
	 * Default: 5 seconds
	 */
    public static final Integer DEFAULT_DISMISS_TIME = 5;

    /* Bluetooth Yodo POS name */
    public static final String YODO_POS = "Yodo-Merch-";

    /* SKS Sizes */
    public static final int SKS_SIZE = 256;
    public static final int ALT_SIZE = 257;

    /* Defaults for the fares */
    public static final String DEFAULT_OLD_FEE     = "2.50";
    public static final String DEFAULT_ADULT_FEE   = "4.50";
    public static final String DEFAULT_CHILD_FEE   = "2.50";
    public static final String DEFAULT_STUDENT_FEE = "3.00";

    /* Type of transaction sounds  */
    public static final int ERROR      = 0;
    public static final int SUCCESSFUL = 1;
}

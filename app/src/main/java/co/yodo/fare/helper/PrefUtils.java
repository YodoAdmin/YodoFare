package co.yodo.fare.helper;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;

import com.orhanobut.hawk.Hawk;

import java.util.Arrays;
import java.util.Locale;

import co.yodo.fare.R;

/**
 * Created by luis on 15/12/14.
 * Utilities for the App, Mainly shared preferences
 */
public class PrefUtils {
    /**
     * A simple check to see if a string is a valid number before inserting
     * into the shared preferences.
     * @param s The number to be checked.
     * @return true  It is a number.
     *         false It is not a number.
     */
    @SuppressWarnings( "all" )
    public static Boolean isNumber( String s ) {
        try {
            Integer.parseInt( s );
        }
        catch( NumberFormatException e ) {
            return false;
        }
        return true;
    }

    /**
     * A helper class just o obtain the config file for the Shared Preferences
     * using the default values for this Shared Preferences app.
     * @param c The Context of the Android system.
     * @return Returns the shared preferences with the default values.
     */
    private static SharedPreferences getSPrefConfig( Context c ) {
        return c.getSharedPreferences( AppConfig.SHARED_PREF_FILE, Context.MODE_PRIVATE );
    }

    /**
     * Generates the mobile hardware identifier either
     * from the Phone (IMEI) or the Bluetooth (MAC)
     * @param c The Context of the Android system.
     */
    @SuppressLint( "HardwareIds" )
    public static String generateHardwareToken( Context c ) {
        String HARDWARE_TOKEN = null;

        TelephonyManager telephonyManager  = (TelephonyManager) c.getSystemService( Context.TELEPHONY_SERVICE );
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if( telephonyManager != null ) {
             String tempMAC = telephonyManager.getDeviceId();
            if( tempMAC != null )
                HARDWARE_TOKEN = tempMAC.replace( "/", "" );
        }

        if( HARDWARE_TOKEN == null && mBluetoothAdapter != null ) {
            if( mBluetoothAdapter.isEnabled() ) {
                String tempMAC = mBluetoothAdapter.getAddress();
                HARDWARE_TOKEN = tempMAC.replaceAll( ":", "" );
            }
        }

        return HARDWARE_TOKEN;
    }

    /**
     * Saves a hardware token to the secure preferences
     * @param hardwareToken The hardware token
     * @return The commit
     */
    public static Boolean saveHardwareToken( String hardwareToken ) {
        return Hawk.put( AppConfig.SPREF_HARDWARE_TOKEN, hardwareToken );
    }

    /**
     * Gets the hardware token
     * @return The hardware token if exists
     */
    public static String getHardwareToken() {
        String token = Hawk.get( AppConfig.SPREF_HARDWARE_TOKEN );
        return ( token == null || token.equals( "" ) ) ? null : token;
    }

    /**
     * Gets the current fare based in the selection of the fee and zone
     * @param context The application context
     * @param fee The fee view (e.g. ic_student, ic_adult)
     * @param zone The zone (e.g. one, two)
     * @return The fee for the selection
     */
    public static String getFare( Context context, View fee, int zone ) {
        switch( fee.getId() ) {
            case R.id.layout_adult_fee:
                return PrefUtils.getAdultFare( context, zone );

            case R.id.layout_child_fee:
                return PrefUtils.getChildFare( context, zone );

            case R.id.layout_student_fee:
                return PrefUtils.getStudentFare( context, zone );

            default:
                return PrefUtils.getElderlyFare( context, zone );
        }
    }

    /**
     * It gets the different Zones for the ic_elderly.
     *
     * @param c The Context of the Android system.
     * @param flag The choice between the three zones
     * @return String The value.
     *         null   If it was not possible to get the value.
     */
    public static String getElderlyFare( Context c, int flag ) {
        SharedPreferences config = getSPrefConfig( c );
        String s = null;

        switch( flag ) {
            /* Zone 1 value */
            case AppConfig.FEE_ZONE_1:
                s = config.getString( AppConfig.SPREF_FEE_OLD_ZONE_1, AppConfig.DEFAULT_OLD_FEE );
                break;

		    /* Zone 2 value */
            case AppConfig.FEE_ZONE_2:
                s = config.getString( AppConfig.SPREF_FEE_OLD_ZONE_2, AppConfig.DEFAULT_OLD_FEE );
                break;

		    /* Zone 3 value */
            case AppConfig.FEE_ZONE_3:
                s = config.getString( AppConfig.SPREF_FEE_OLD_ZONE_3, AppConfig.DEFAULT_OLD_FEE );
                break;
        }

        return s;
    }

    /**
     * It gets the different Zones for the ic_adult.
     *
     * @param c The Context of the Android system.
     * @param flag The choice between the three zones
     * @return String The value.
     *         null   If it was not possible to get the value.
     */
    public static String getAdultFare( Context c, int flag ) {
        SharedPreferences config = getSPrefConfig( c );
        String s = null;

        switch( flag ) {
            // Zone 1 value
            case AppConfig.FEE_ZONE_1:
                s = config.getString( AppConfig.SPREF_FEE_ADULT_ZONE_1, AppConfig.DEFAULT_ADULT_FEE );
                break;

            // Zone 2 value
            case AppConfig.FEE_ZONE_2:
                s = config.getString( AppConfig.SPREF_FEE_ADULT_ZONE_2, AppConfig.DEFAULT_ADULT_FEE );
                break;

            // Zone 3 value
            case AppConfig.FEE_ZONE_3:
                s = config.getString( AppConfig.SPREF_FEE_ADULT_ZONE_3, AppConfig.DEFAULT_ADULT_FEE );
                break;
        }

        return s;
    }

    /**
     * It gets the different Zones for the ic_child.
     *
     * @param c The Context of the Android system.
     * @param flag The choice between the three zones
     * @return String The value.
     *         null   If it was not possible to get the value.
     */
    public static String getChildFare(Context c, int flag) {
        SharedPreferences config = getSPrefConfig( c );
        String s = null;

        switch( flag ) {
            // Zone 1 value
            case AppConfig.FEE_ZONE_1:
                s = config.getString( AppConfig.SPREF_FEE_CHILD_ZONE_1, AppConfig.DEFAULT_CHILD_FEE );
                break;

            // Zone 2 value
            case AppConfig.FEE_ZONE_2:
                s = config.getString( AppConfig.SPREF_FEE_CHILD_ZONE_2, AppConfig.DEFAULT_CHILD_FEE );
                break;

            // Zone 3 value
            case AppConfig.FEE_ZONE_3:
                s = config.getString( AppConfig.SPREF_FEE_CHILD_ZONE_3, AppConfig.DEFAULT_CHILD_FEE );
                break;
        }

        return s;
    }

    /**
     * It gets the different Zones for the ic_student.
     *
     * @param c The Context of the Android system.
     * @param flag The choice between the three zones
     * @return String The value.
     *         null   If it was not possible to get the value.
     */
    public static String getStudentFare(Context c, int flag) {
        SharedPreferences config = getSPrefConfig( c );
        String s = null;

        switch( flag ) {
            /* Zone 1 value */
            case AppConfig.FEE_ZONE_1:
                s = config.getString( AppConfig.SPREF_FEE_STUDENT_ZONE_1, AppConfig.DEFAULT_STUDENT_FEE );
                break;

		    /* Zone 2 value */
            case AppConfig.FEE_ZONE_2:
                s = config.getString( AppConfig.SPREF_FEE_STUDENT_ZONE_2, AppConfig.DEFAULT_STUDENT_FEE );
                break;

		    /* Zone 3 value */
            case AppConfig.FEE_ZONE_3:
                s = config.getString( AppConfig.SPREF_FEE_STUDENT_ZONE_3, AppConfig.DEFAULT_STUDENT_FEE );
                break;
        }

        return s;
    }

    /**
     * It gets the language.
     * @param c The Context of the Android system.
     * @return String It returns the language.
     */
    private static String getLanguage( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_LANGUAGE, AppConfig.DEFAULT_LANGUAGE );
    }

    /**
     * It saves the merchant currency to the preferences.
     * @param c The Context of the Android system.
     * @param currency The currency of the merchant.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveMerchantCurrency( Context c, String currency ) {
        // Supported currencies
        final String[] currencies = c.getResources().getStringArray( R.array.currency_array );

        // Verify if currency exists in the array
        return Arrays.asList( currencies ).contains( currency ) &&
               Hawk.put( AppConfig.SPREF_MERCHANT_CURRENCY, currency );
    }

    /**
     * It gets the merchant currency.
     * @param c The Context of the Android system.
     * @return int It returns the currency.
     */
    public static String getMerchantCurrency( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_MERCHANT_CURRENCY, null );
    }

    /**
     * Returns if the device is advertising
     * @param c The Android application context
     * @return True if it is advertising
     *         False if it is not
     */
    public static Boolean isAdvertising( Context c ) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, false );
    }

    /**
     * It saves the scanner array position to the preferences.
     * @param c The Context of the Android system.
     * @param n The scanner position on the array.
     * @return true  If it was saved.
     *         false If it was not saved.
     */
    public static Boolean saveScanner(Context c, int n) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putInt( AppConfig.SPREF_CURRENT_SCANNER, n );
        return writer.commit();
    }

    /**
     * It gets the scanner position.
     * @param c The Context of the Android system.
     * @return int It returns the scanner position.
     */
    public static int getScanner(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_SCANNER, AppConfig.DEFAULT_SCANNER );
    }

    /**
     * It gets the beacon name.
     * @param c The Context of the Android system.
     * @return int It returns the beacon name.
     */
    public static String getBeaconName(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_BEACON, "" );
    }

    /**
     * It gets the current bus route.
     * @param c The Context of the Android system.
     * @return int It returns the bus route.
     */
    public static String getBusRoute(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getString( AppConfig.SPREF_CURRENT_ROUTE, "0" );
    }

    /**
     * It saves if it is the first login.
     * @param c The Context of the Android system.
     * @param flag If it is the first login or not.
     * @return true  The flag was saved successfully.
     *         false The flag was not saved successfully.
     */
    public static Boolean saveFirstLogin(Context c, Boolean flag) {
        SharedPreferences config = getSPrefConfig( c );
        SharedPreferences.Editor writer = config.edit();
        writer.putBoolean( AppConfig.SPREF_FIRST_LOGIN, flag );
        return writer.commit();
    }

    /**
     * It gets if it is the first login.
     * @param c The Context of the Android system.
     * @return true  It is logged in.
     *         false It is not logged in.
     */
    public static Boolean isFirstLogin(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getBoolean( AppConfig.SPREF_FIRST_LOGIN, true );
    }

    /**
     * It gets the status of the advertising service.
     * @param c The Context of the Android system.
     * @return true  Advertising service is on.
     *         false Advertising service is off.
     */
    public static Boolean isAdvertisingServiceRunning(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean( AppConfig.SPREF_ADVERTISING_SERVICE, false );
    }

    /**
     * It gets the status of the live scan.
     * @param c The Context of the Android system.
     * @return true  Live scan is on.
     *         false Live scan is off.
     */
    public static Boolean isLiveScan(Context c) {
        SharedPreferences config = getSPrefConfig(c);
        return config.getBoolean( AppConfig.SPREF_LIVE_SCAN, false );
    }

    /**
     * It gets the current time to dismiss the response message
     * @param c The Context of the Android system.
     * @return int It returns the time.
     */
    public static int getDismissTime(Context c) {
        SharedPreferences config = getSPrefConfig( c );
        return config.getInt( AppConfig.SPREF_CURRENT_DISMISS_TIME, AppConfig.DEFAULT_DISMISS_TIME );
    }

    /**
     * Gets the bluetooth adapter
     * @return The bluetooth adapter
     */
    private static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Check if the device possess bluetooth
     * @return true if it possess bluetooth otherwise false
     */
    public static boolean hasBluetooth() {
        return getBluetoothAdapter() != null;
    }

    public static void setLanguage( Context c ) {
        Locale appLoc = new Locale( getLanguage( c ) );

        Resources res = c.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        Locale.setDefault( appLoc );
        Configuration config = new Configuration( res.getConfiguration() );
        config.locale = appLoc;

        res.updateConfiguration( config, dm );
    }
}

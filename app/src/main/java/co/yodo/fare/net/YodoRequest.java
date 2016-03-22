package co.yodo.fare.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import co.yodo.fare.R;
import co.yodo.fare.component.Encrypter;
import co.yodo.fare.component.TransparentProgressDialog;
import co.yodo.fare.data.ServerResponse;
import co.yodo.fare.helper.AppUtils;
import co.yodo.fare.service.RESTService;

/**
 * Created by luis on 15/12/14.
 * Request to the Yodo Server
 */
public class YodoRequest extends ResultReceiver {
    /** DEBUG */
    private static final String TAG = YodoRequest.class.getSimpleName();

    public interface RESTListener {
        /**
         * Listener for the server responses
         * @param type Type of the request
         * @param response POJO for the response
         */
        void onResponse(RequestType type, ServerResponse response);
    }

    /** ID for each request */
    public enum RequestType {
        ERROR_NO_INTERNET ( "-1" ), // ERROR NO INTERNET
        ERROR_GENERAL     ( "00" ), // ERROR GENERAL
        AUTH_REQUEST      ( "01" ), // RT=0, ST=4
        QUERY_BAL_REQUEST ( "02" ), // RT=4, ST=3
        QUERY_DAY_REQUEST ( "03" ), // RT=4, ST=3
        QUERY_CUR_REQUEST ( "04" ), // RT=4, ST=3
        REG_MERCH_REQUEST ( "05 "), // RT=9, ST=1
        EXCH_MERCH_REQUEST( "06" ),	// RT=1, ST=1
        ALT_MERCH_REQUEST ( "07" );	// RT=7

        private final String name;

        RequestType(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return ( otherName != null ) && name.equals( otherName );
        }

        public String toString() {
            return name;
        }
    }

    /** ID for the types of progress dialog */
    public enum ProgressDialogType {
        NORMAL,
        TRANSPARENT
    }

    /** Object used to encrypt information */
    private Encrypter oEncrypter;

    /** Progress dialog */
    private ProgressDialog progressDialog;
    private TransparentProgressDialog transProgressDialog;

    /** Singleton instance */
    private static YodoRequest instance = null;

    /** the external listener to the service */
    private RESTListener externalListener;

    /** User's data separator */
    private static final String	REQ_SEP = ",";

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler Default
     */
    private YodoRequest(Handler handler) {
        super( handler );
        oEncrypter = new Encrypter();
    }

    /**
     * Gets the instance of the service
     * @return instance
     */
    public static YodoRequest getInstance() {
        if( instance == null )
            instance = new YodoRequest( new Handler() );
        return instance;
    }

    /**
     * Add a listener to the service
     * @param listener Listener for the requests to the server
     */
    public void setListener(RESTListener listener) {
        externalListener = listener ;
    }

    public void createProgressDialog(Activity activity, ProgressDialogType type) {
        switch( type ) {
            case NORMAL:
                progressDialog = new ProgressDialog( activity );
                progressDialog.setCancelable( false );
                progressDialog.show();
                progressDialog.setContentView( R.layout.custom_progressdialog );
                break;

            case TRANSPARENT:
                transProgressDialog = new TransparentProgressDialog( activity, R.drawable.spinner );
                transProgressDialog.show();
                break;
        }
    }

    public void destroyProgressDialog() {
        if( progressDialog != null ) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if( transProgressDialog != null ) {
            transProgressDialog.dismiss();
            transProgressDialog = null;
        }
    }

    public void requestAuthentication( Context context, String hardwareToken ) {
        String sEncryptedMerchData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( context );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createAuthenticationRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.AUTH_HW_MERCH_SUBREQ )
        );

        sendRequest( context, pRequest, RequestType.AUTH_REQUEST );
    }

    public void requestHistory(Activity activity, String hardwareToken, String pip) {
        String sEncryptedMerchData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_HISTORY_BALANCE
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_BAL_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestDailyHistory(Activity activity, String hardwareToken, String pip) {
        String sEncryptedMerchData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                pip + REQ_SEP +
                ServerRequest.QUERY_TODAY_BALANCE
        );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.QUERY_DAY_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestCurrency( Context context, String hardwareToken ) {
        String sEncryptedMerchData, pRequest;

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString(
                hardwareToken + REQ_SEP +
                ServerRequest.QUERY_MERCHANT_CURRENCY );
        oEncrypter.rsaEncrypt( context );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createQueryRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.QUERY_ACC_SUBREQ )
        );

        sendRequest( context, pRequest, RequestType.QUERY_CUR_REQUEST );
    }

    public void requestRegistration( Activity activity, String hardwareToken, String token ) {
        String sEncryptedMerchData, pRequest;
        StringBuilder sMerchData = new StringBuilder();

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ssZZZZ", Locale.US );
        String timeStamp = dateFormat.format( System.currentTimeMillis() );
        
        sMerchData.append( hardwareToken ).append(REQ_SEP);
        sMerchData.append( token ).append(REQ_SEP);
        sMerchData.append( timeStamp );

        // Encrypting to create request
        oEncrypter.setsUnEncryptedString( sMerchData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        pRequest = ServerRequest.createRegistrationRequest(
                sEncryptedMerchData,
                Integer.parseInt( ServerRequest.REG_MERCH_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.REG_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestExchange(Activity activity, String hardwareToken, String client,
                                String totalPurchase, String cashTender, String cashBack,
                                double latitude, double longitude, String currency) {
        String sEncryptedMerchData, sEncryptedExchangeUsrData, pRequest;
        StringBuilder sEncryptedUsrData = new StringBuilder();
        StringBuilder sExchangeUsrData = new StringBuilder();

        /// Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        sEncryptedUsrData.append( sEncryptedMerchData ).append( REQ_SEP );
        sEncryptedUsrData.append( client ).append( REQ_SEP );

        sExchangeUsrData.append( latitude ).append( REQ_SEP );
        sExchangeUsrData.append( longitude ).append(REQ_SEP);
        sExchangeUsrData.append( totalPurchase ).append(REQ_SEP);
        sExchangeUsrData.append( cashTender ).append( REQ_SEP );
        sExchangeUsrData.append( cashBack ).append(REQ_SEP);
        sExchangeUsrData.append( currency );

        oEncrypter.setsUnEncryptedString( sExchangeUsrData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedExchangeUsrData = oEncrypter.bytesToHex();
        sEncryptedUsrData.append( sEncryptedExchangeUsrData );

        pRequest = ServerRequest.createExchangeRequest(
                sEncryptedUsrData.toString(),
                Integer.parseInt( ServerRequest.REG_MERCH_SUBREQ )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.ALT_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    public void requestAlternate( Activity activity, String hardwareToken, String client,
                                String totalPurchase, String cashTender, String cashBack,
                                double latitude, double longitude, String currency ) {
        String sEncryptedMerchData, sEncryptedExchangeUsrData, pRequest;
        StringBuilder sEncryptedUsrData = new StringBuilder();
        StringBuilder sExchangeUsrData = new StringBuilder();

        // Get the client data and account type
        String clientData  = client.substring( 0, client.length() - 1 );
        String accountType = client.substring( client.length() - 1 );

        if( !AppUtils.isNumber(accountType) ) {
            instance.send( RESTService.STATUS_FAILED, null );
            return;
        }

        /// Encrypting to create request
        oEncrypter.setsUnEncryptedString( hardwareToken );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedMerchData = oEncrypter.bytesToHex();

        sEncryptedUsrData.append( sEncryptedMerchData ).append( REQ_SEP );
        sEncryptedUsrData.append( clientData ).append( REQ_SEP );

        sExchangeUsrData.append( latitude ).append( REQ_SEP );
        sExchangeUsrData.append( longitude ).append(REQ_SEP);
        sExchangeUsrData.append( totalPurchase ).append(REQ_SEP);
        sExchangeUsrData.append( cashTender ).append( REQ_SEP );
        sExchangeUsrData.append( cashBack ).append(REQ_SEP);
        sExchangeUsrData.append( currency );

        oEncrypter.setsUnEncryptedString( sExchangeUsrData.toString() );
        oEncrypter.rsaEncrypt( activity );
        sEncryptedExchangeUsrData = oEncrypter.bytesToHex();
        sEncryptedUsrData.append( sEncryptedExchangeUsrData );

        pRequest = ServerRequest.createAlternateRequest(
                sEncryptedUsrData.toString(),
                Integer.parseInt( accountType )
        );

        Intent intent = new Intent( activity, RESTService.class );
        intent.putExtra( RESTService.ACTION_RESULT, RequestType.EXCH_MERCH_REQUEST );
        intent.putExtra( RESTService.EXTRA_PARAMS, pRequest );
        intent.putExtra( RESTService.EXTRA_RESULT_RECEIVER, instance );
        activity.startService( intent );
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if( resultCode == RESTService.STATUS_FAILED ) {
            externalListener.onResponse(RequestType.ERROR_GENERAL, null);
        }
        else if( resultCode == RESTService.STATUS_NO_INTERNET ) {
            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
        }
        else {
            RequestType action      = (RequestType) resultData.getSerializable( RESTService.ACTION_RESULT );
            ServerResponse response = (ServerResponse) resultData.getSerializable( RESTService.EXTRA_RESULT );
            externalListener.onResponse( action , response );

            if( response != null )
                AppUtils.Logger( TAG, response.toString() );
        }
    }

    ///////////////////////////////////////////////////////
    // NEW WAY TO REQUEST THE SERVER (REST) USING VOLLEY //
    ///////////////////////////////////////////////////////

    /** Switch server IP address */
    //private static final String IP 	         = "http://50.56.180.133";  // Production
    private static final String IP 			 = "http://198.101.209.120";  // Development
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";

    /** Timeout 10 seconds */
    private final static int TIMEOUT = 10000;

    private RetryPolicy retryPolicy = new DefaultRetryPolicy(
            TIMEOUT,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    );

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue( Context context ) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue( context );
        }
        return mRequestQueue;
    }

    private void sendRequest( final Context activity, final String pRequest, final RequestType type ) {
        final StringRequest httpRequest = new StringRequest( Request.Method.GET, IP + YODO_ADDRESS + pRequest,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String xml ) {
                        try {
                            // Handling XML
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();

                            // Create handler to handle XML Tags ( extends DefaultHandler )
                            xr.setContentHandler( new XMLHandler() );
                            xr.parse( new InputSource( new StringReader( xml ) ) );

                            AppUtils.Logger( TAG, XMLHandler.response.toString() );
                            externalListener.onResponse( type, XMLHandler.response );
                        } catch( ParserConfigurationException | SAXException | IOException e ) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse( VolleyError error ) {
                        error.printStackTrace();

                        if( error instanceof TimeoutError || error instanceof NoConnectionError )
                            externalListener.onResponse( RequestType.ERROR_NO_INTERNET, null );
                        else
                            externalListener.onResponse( RequestType.ERROR_GENERAL, null );
                    }
                }
        );
        httpRequest.setTag( "GET" );
        httpRequest.setRetryPolicy( retryPolicy );
        getRequestQueue( activity ).add( httpRequest );
    }

    public static String getSwitch() {
        if( IP.equals( "http://50.56.180.133" ) )
            return "P";
        return "D";
    }
}

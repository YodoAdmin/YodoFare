package co.yodo.fare.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import javax.inject.Inject;

import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.helper.SystemUtils;
import co.yodo.fare.ui.notification.MessageHandler;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.AuthenticateRequest;
import co.yodo.restapi.network.request.QueryRequest;

public class SplashActivity extends Activity implements ApiClient.RequestsListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String mHardwareToken;

    /** Messages Handler */
    private MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

    /** Code for the error dialog */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    /** Response codes for the server requests */
    private static final int AUTH_REQ  = 0x00;
    private static final int QUERY_REQ = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestManager.setListener( this );
    }

    /**
     * Setup the main GUI components
     */
    private void setupGUI() {
        ac = SplashActivity.this;
        mHandlerMessages = new MessageHandler( this );

        // Injection
        YodoApplication.getComponent().inject( this );
        mRequestManager.setListener( this );
    }

    /**
     * It updates the basic data
     */
    private void updateData() {
        // Get the main booleans
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if( hasServices ) {
            mHardwareToken = PrefUtils.getHardwareToken( ac );
            if( mHardwareToken == null ) {
                setupPermissions();
            } else {
                mRequestManager.invoke(
                        new AuthenticateRequest(
                                AUTH_REQ,
                                mHardwareToken
                        )
                );
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
                SplashActivity.this,
                R.string.message_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission )
            authenticateUser();
    }

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    private void authenticateUser() {
        mHardwareToken = PrefUtils.generateHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            PrefUtils.saveHardwareToken( ac, mHardwareToken );
            mRequestManager.invoke(
                    new AuthenticateRequest(
                            AUTH_REQ,
                            mHardwareToken
                    )
            );
        }
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        // Get response values
        final String code    = response.getCode();
        final String message = response.getMessage();

        switch( responseCode ) {
            case AUTH_REQ:

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get the merchant currency
                    mRequestManager.invoke(
                            new QueryRequest(
                                    QUERY_REQ,
                                    mHardwareToken,
                                    QueryRequest.Record.MERCHANT_CURRENCY
                            )
                    );
                }
                else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( ac, RegistrationActivity.class );
                    startActivity( intent );
                    finish();
                }
                break;

            case QUERY_REQ:

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Set currencies
                    String currency = response.getParams().getCurrency();
                    PrefUtils.saveMerchantCurrency( ac, currency );

                    // Start the app
                    Intent intent = new Intent( ac, FareActivity.class );
                    startActivity( intent );
                } else {
                    MessageHandler.sendMessage( MessageHandler.INIT_ERROR,
                            mHandlerMessages,
                            code,
                            message
                    );
                }
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if( resultCode == RESULT_OK ) {
                    // Google play services installed
                    Intent iSplash = new Intent( this, SplashActivity.class );
                    startActivity( iSplash );
                } else if( resultCode == RESULT_CANCELED ) {
                    // Denied to install
                    Toast.makeText( ac, R.string.message_play_services, Toast.LENGTH_SHORT ).show();
                }
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    authenticateUser();
                } else {
                    // Permission Denied
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }
}

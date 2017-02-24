package co.yodo.fare.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class SplashActivity extends AppCompatActivity /*implements ApiClient.RequestsListener*/ {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Hardware Token */
    private String hardwareToken;

    /** Messages Handler */
    private MessageHandler messagesHandler;

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
        //updateData();
    }

    /*@Override
    public void onResume() {
        super.onResume();
        requestManager.setListener( this );
    }*/

    /**
     * Setup the main GUI components
     */
    private void setupGUI() {
        //messagesHandler = new MessageHandler( this );

        // Injection
        YodoApplication.getComponent().inject( this );
        //requestManager.setListener( this );
    }

    /**
     * It updates the basic data
     */
    /*private void updateData() {
        // Get the main booleans
        boolean hasServices = SystemUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if( hasServices ) {
            hardwareToken = PrefUtils.getHardwareToken( context );
            if( hardwareToken == null ) {
                setupPermissions();
            } else {
                requestManager.invoke(
                        new AuthenticateRequest(
                                AUTH_REQ,
                                hardwareToken
                        )
                );
            }
        }
    }*/

    /**
     * Request the necessary permissions for this activity
     */
    /*private void setupPermissions() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
                SplashActivity.this,
                R.string.message_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission )
            authenticateUser();
    }*/

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    /*private void authenticateUser() {
        hardwareToken = PrefUtils.generateHardwareToken( context );
        if( hardwareToken == null ) {
            ToastMaster.makeText( context, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        } else {
            PrefUtils.saveHardwareToken( context, hardwareToken );
            requestManager.invoke(
                    new AuthenticateRequest(
                            AUTH_REQ,
                            hardwareToken
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

        switch( responseCode ) {
            case AUTH_REQ:

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get the merchant currency
                    requestManager.invoke(
                            new QueryRequest(
                                    QUERY_REQ,
                                    hardwareToken,
                                    QueryRequest.Record.MERCHANT_CURRENCY
                            )
                    );
                }
                else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( context, RegistrationActivity.class );
                    startActivity( intent );
                    finish();
                }
                break;

            case QUERY_REQ:

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Set currencies
                    String currency = response.getParams().getCurrency();
                    final boolean savedMCurr= PrefUtils.saveMerchantCurrency( context, currency );

                    if( savedMCurr ) {
                        // Start the app
                        Intent intent = new Intent( context, FareActivity.class );
                        startActivity( intent );
                        finish();
                    } else {
                        MessageHandler.sendMessage( MessageHandler.INIT_ERROR,
                                messagesHandler,
                                ServerResponse.ERROR_FAILED,
                                "Currency not supported"
                        );
                    }
                }
                break;
        }
    }

    @Override
    public void onError( Throwable error, String message ) {
        MessageHandler.sendMessage( MessageHandler.INIT_ERROR,
                messagesHandler,
                null,
                message
        );
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
                    Toast.makeText( context, R.string.message_play_services, Toast.LENGTH_SHORT ).show();
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
    }*/
}

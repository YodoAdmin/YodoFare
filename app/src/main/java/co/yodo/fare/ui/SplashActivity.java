package co.yodo.fare.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import javax.inject.Inject;

import co.yodo.fare.R;
import co.yodo.fare.utils.ErrorUtils;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.helper.SystemUtils;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.AuthenticateRequest;
import co.yodo.restapi.network.request.QueryRequest;

public class SplashActivity extends AppCompatActivity {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** User token */
    private String hardwareToken;

    /** Code for the error dialog */
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setupGUI();
        updateData();
    }

    /**
     * Setup the main GUI components
     */
    private void setupGUI() {
        // Injection
        YodoApplication.getComponent().inject( this );
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
            hardwareToken = PrefUtils.getHardwareToken();
            if( hardwareToken != null ) {
                authenticateUser();
            } else {
                generateUserToken();
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     * and generates the hardware token
     */
    private void generateUserToken() {
        boolean phoneStatePermission = SystemUtils.requestPermission(
                SplashActivity.this,
                R.string.message_permission_read_phone_state,
                Manifest.permission.READ_PHONE_STATE,
                PERMISSIONS_REQUEST_READ_PHONE_STATE
        );

        if( phoneStatePermission ) {
            hardwareToken = PrefUtils.generateHardwareToken( context );
            if( hardwareToken == null ) {
                ToastMaster.makeText( context, R.string.error_hardware, Toast.LENGTH_LONG ).show();
                finish();
            } else {
                PrefUtils.saveHardwareToken( hardwareToken );
                authenticateUser();
            }
        }
    }

    /**
     * Generates the hardware token after we have the permission
     * and verifies if it is null or not. Null could be caused
     * if the bluetooth is off
     */
    private void authenticateUser() {
        requestManager.invoke(
            new AuthenticateRequest( hardwareToken ),
            new ApiClient.RequestCallback() {
                @Override
                public void onPrepare() {
                }

                @Override
                public void onResponse( ServerResponse response ) {
                    // Get response code
                    final String code = response.getCode();

                    // Do the correct action
                    if( code.equals( ServerResponse.AUTHORIZED ) ) {
                        getCurrency();
                    }
                    else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                        // We need to register first
                        Intent intent = new Intent( context, RegistrationActivity.class );
                        startActivity( intent );
                        finish();
                    }
                }

                @Override
                public void onError( Throwable error ) {
                    ErrorUtils.handleApiError(
                            SplashActivity.this,
                            error,
                            true
                    );
                }
            }
        );
    }

    /**
     * Gets the merchant currency from the server
     * and saves it to the preferences
     */
    private void getCurrency() {
        // Get the merchant currency
        requestManager.invoke(
            new QueryRequest(
                    hardwareToken,
                    QueryRequest.Record.MERCHANT_CURRENCY
            ),
            new ApiClient.RequestCallback() {
                @Override
                public void onPrepare() {
                }

                @Override
                public void onResponse( ServerResponse response ) {
                    // Get response code
                    final String code = response.getCode();
                    if( code.equals( ServerResponse.AUTHORIZED ) ) {
                        // Set currency
                        String currency = response.getParams().getCurrency();
                        if( PrefUtils.saveMerchantCurrency( context, currency ) ) {
                            // Start the fare app
                            Intent intent = new Intent( context, FareActivity.class );
                            startActivity( intent );
                            finish();
                        } else {
                            // Show an error message
                            ErrorUtils.handleError(
                                    SplashActivity.this,
                                    R.string.error_currency,
                                    true
                            );
                        }
                    }
                }

                @Override
                public void onError( Throwable error ) {
                    ErrorUtils.handleApiError(
                            SplashActivity.this,
                            error,
                            true
                    );
                }
            }
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
                    Toast.makeText( context, R.string.error_play_services, Toast.LENGTH_SHORT ).show();
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
                    generateUserToken();
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

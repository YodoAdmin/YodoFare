package co.yodo.fare.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.Toast;

import co.yodo.fare.R;
import co.yodo.fare.component.ToastMaster;
import co.yodo.fare.component.YodoHandler;
import co.yodo.fare.data.ServerResponse;
import co.yodo.fare.helper.AppUtils;
import co.yodo.fare.net.YodoRequest;

public class SplashActivity extends Activity implements YodoRequest.RESTListener {
    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String hardwareToken;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

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

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
    }

    /**
     * Setup the main GUI components
     */
    private void setupGUI() {
        ac = SplashActivity.this;
        handlerMessages = new YodoHandler( SplashActivity.this );
    }

    /**
     * It updates the basic data
     */
    private void updateData() {
        // Get the main booleans
        boolean hasServices = AppUtils.isGooglePlayServicesAvailable(
                SplashActivity.this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES
        );

        // Verify Google Play Services
        if( hasServices ) {
            hardwareToken = AppUtils.getHardwareToken( ac );
            if( hardwareToken == null ) {
                setupPermissions();
            } else {
                YodoRequest.getInstance().requestAuthentication( SplashActivity.this, hardwareToken );
            }
        }
    }

    /**
     * Request the necessary permissions for this activity
     */
    private void setupPermissions() {
        boolean phoneStatePermission = AppUtils.requestPermission(
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
        hardwareToken = AppUtils.generateHardwareToken( ac );
        if( hardwareToken != null ) {
            AppUtils.saveHardwareToken( ac, hardwareToken );
            YodoRequest.getInstance().requestAuthentication( ac, hardwareToken );
        } else {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    @Override
    public void onResponse( YodoRequest.RequestType type, ServerResponse response ) {
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                finish();
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                finish();
                break;

            case AUTH_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    if( AppUtils.getMerchantCurrency( ac ) == null ) {
                        // There is no currency saved
                        YodoRequest.getInstance().requestCurrency( ac, hardwareToken );
                    } else {
                        // There is already a currency, lets proceed
                        Intent intent = new Intent( ac, FareActivity.class );
                        startActivity( intent );
                        finish();
                    }
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( ac, RegistrationActivity.class );
                    startActivity( intent );
                    finish();
                }
                break;

            case QUERY_CUR_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    // Get Merchant Currency and save it
                    String currency = response.getParam( ServerResponse.CURRENCY );
                    AppUtils.saveMerchantCurrency( ac, currency );
                    // Start main activity
                    Intent intent = new Intent( ac, FareActivity.class );
                    startActivity( intent );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage();

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
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

package co.yodo.fare.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.helper.AppConfig;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.helper.SystemUtils;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.fare.utils.ErrorUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.Params;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.AuthMerchDeviceRequest;
import co.yodo.restapi.network.requests.QueryCurrencyRequest;
import co.yodo.restapi.network.requests.QueryFaresRequest;

public class SplashActivity extends AppCompatActivity {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = SplashActivity.class.getSimpleName();

    /** The context object */
    @Inject
    Context context;

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

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES) {
            if (resultCode == RESULT_OK) {
                // Google play services installed
                Intent iSplash = new Intent(this, SplashActivity.class);
                startActivity(iSplash);
            } else if (resultCode == RESULT_CANCELED) {
                // Denied to install
                Toast.makeText(context, R.string.error_play_services, Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                generateUserToken();
            } else {
                // Permission Denied
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        YodoApi.execute(
                new AuthMerchDeviceRequest(),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String code = response.getCode();
                        switch (code) {
                            case ServerResponse.AUTHORIZED:
                                getFees();
                                break;

                            case ServerResponse.ERROR_FAILED:
                                Intent intent = new Intent( context, RegistrationActivity.class );
                                startActivity( intent );
                                finish();
                                break;

                            default:
                                handleError( R.string.error_unknown );
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        ErrorUtils.handleApiError(
                                SplashActivity.this,
                                error,
                                true
                        );
                    }
                }
        );
    }

    private void getFees() {
        YodoApi.execute(
                new QueryFaresRequest(),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String code = response.getCode();
                        if (code.equals(ServerResponse.AUTHORIZED)) {
                            saveFares( response.getParams() );
                            // Start the currency process
                            getCurrency();
                        } else {
                            handleError( R.string.error_unknown );
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
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
     * Gets the fares from the params to save them
     * @param params The params of the request
     */
    private void saveFares( Params params ) {
        // Elderly
        PrefUtils.saveFare( context, AppConfig.SPREF_ELDERLY_ZONE_1, params.getElderlyZone1().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_ELDERLY_ZONE_2, params.getElderlyZone2().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_ELDERLY_ZONE_3, params.getElderlyZone3().getPrice() );

        // Adults
        PrefUtils.saveFare( context, AppConfig.SPREF_ADULT_ZONE_1, params.getAdultZone1().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_ADULT_ZONE_2, params.getAdultZone2().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_ADULT_ZONE_3, params.getAdultZone3().getPrice() );

        // Students
        PrefUtils.saveFare( context, AppConfig.SPREF_STUDENT_ZONE_1, params.getStudentZone1().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_STUDENT_ZONE_2, params.getStudentZone2().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_STUDENT_ZONE_3, params.getStudentZone3().getPrice() );

        // Child
        PrefUtils.saveFare( context, AppConfig.SPREF_CHILD_ZONE_1, params.getChildZone1().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_CHILD_ZONE_2, params.getChildZone2().getPrice() );
        PrefUtils.saveFare( context, AppConfig.SPREF_CHILD_ZONE_3, params.getChildZone3().getPrice() );
    }

    /**
     * Gets the merchant currency from the server
     * and saves it to the preferences
     */
    private void getCurrency() {
        YodoApi.execute(
                new QueryCurrencyRequest(),
                new RequestCallback() {
                    @Override
                    public void onPrepare() {
                    }

                    @Override
                    public void onResponse(ServerResponse response) {
                        final String code = response.getCode();
                        if (code.equals(ServerResponse.AUTHORIZED)) {
                            // Set currencies
                            String currency = response.getParams().getCurrency();
                            if( PrefUtils.saveMerchantCurrency( context, currency ) ) {
                                // Start the fare app
                                Intent intent = new Intent( context, FareActivity.class );
                                startActivity( intent );
                                finish();
                            } else {
                                // Show an error message
                                handleError( R.string.error_currency );
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
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
     * Handles any unknown error
     */
    private void handleError( int message ) {
        ErrorUtils.handleError(
                SplashActivity.this,
                message,
                true
        );
    }
}

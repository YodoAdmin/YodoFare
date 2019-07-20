package co.yodo.fare.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.helper.GUIUtils;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.fare.utils.ErrorUtils;
import co.yodo.restapi.YodoApi;
import co.yodo.restapi.network.contract.RequestCallback;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.requests.RegMerchDeviceRequest;

public class RegistrationActivity extends AppCompatActivity {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The application context */
    @Inject
    Context context;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    /** GUI Controllers */
    @BindView( R.id.layout_activation_code )
    TextInputLayout tilActivationCode;

    @BindView( R.id.text_activation_code )
    TextInputEditText tietActivationCode;

    /** Hardware Token */
    private String hardwareToken;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_registration );

        setupGUI();
        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_registration );
    }

    private void updateData() {
        // Gets the account identifier
        hardwareToken = PrefUtils.getHardwareToken();
        if( hardwareToken == null ) {
            ToastMaster.makeText( context, R.string.error_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void register( View v ) {
        String token = tietActivationCode.getText().toString();
        if( token.isEmpty() ) {
            ErrorUtils.handleFieldError(
                    context,
                    tietActivationCode,
                    R.string.error_required_field
            );
        } else {
            progressManager.create(
                    RegistrationActivity.this ,
                    ProgressDialogHelper.ProgressDialogType.NORMAL
            );

            YodoApi.execute(
                    new RegMerchDeviceRequest(token),
                    new RequestCallback() {
                        @Override
                        public void onPrepare() {
                        }

                        @Override
                        public void onResponse(ServerResponse response) {
                            progressManager.destroy();
                            final String code = response.getCode();
                            switch (code) {
                                case ServerResponse.AUTHORIZED_REGISTRATION:
                                    Intent intent = new Intent( context, SplashActivity.class );
                                    startActivity( intent );
                                    finish();
                                    break;

                                case ServerResponse.ERROR_DUP_AUTH:
                                    ErrorUtils.handleError(
                                            RegistrationActivity.this,
                                            R.string.error_20,
                                            false
                                    );
                                    break;

                                default:
                                    ErrorUtils.handleError(
                                            RegistrationActivity.this,
                                            R.string.error_server,
                                            false
                                    );
                                    break;
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            progressManager.destroy();
                            ErrorUtils.handleApiError(
                                    RegistrationActivity.this,
                                    error,
                                    false
                            );
                        }
                    }
            );
        }
    }

    /**
     * Restarts the application to authenticate the user
     * @param v The view of the button, not used
     */
    public void restart( View v ) {
        finish();
        Intent i = new Intent( context, SplashActivity.class );
        i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( i );
    }
}

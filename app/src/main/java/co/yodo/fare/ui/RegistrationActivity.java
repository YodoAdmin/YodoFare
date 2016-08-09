package co.yodo.fare.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.fare.helper.GUIUtils;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.ui.notification.MessageHandler;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.restapi.network.YodoRequest;
import co.yodo.restapi.network.model.ServerResponse;

public class RegistrationActivity extends AppCompatActivity implements YodoRequest.RESTListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String mHardwareToken;

    /** GUI Controllers */
    @BindView( R.id.etActivationCode )
    EditText etActivationCode;

    /** Messages Handler */
    private MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    YodoRequest mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** The shake animation for wrong inputs */
    private Animation aShake;

    /** Response codes for the queries */
    private static final int REG_REQ = 0x00;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_registration );

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register listener for requests
        mRequestManager.setListener( this );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        ac = RegistrationActivity.this;
        mHandlerMessages = new MessageHandler( this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Load the animation
        aShake = AnimationUtils.loadAnimation( this, R.anim.shake );

        // Setup the toolbar
        GUIUtils.setActionBar( this, R.string.title_activity_registration );
    }

    private void updateData() {
        // Gets the hardware token - account identifier
        mHardwareToken = PrefUtils.getHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Realize a registration request
     * @param v View of the button, not used
     */
    public void registrationClick( View v ) {
        String token = etActivationCode.getText().toString();
        if( token.isEmpty() ) {
            etActivationCode.startAnimation( aShake );
        } else {
            mProgressManager.createProgressDialog(
                    RegistrationActivity.this ,
                    ProgressDialogHelper.ProgressDialogType.NORMAL
            );

            mRequestManager.requestMerchReg(
                    REG_REQ,
                    mHardwareToken,
                    token
            );
        }
    }

    /**
     * Restarts the application to authenticate the user
     * @param v The view of the button, not used
     */
    public void restartClick( View v ) {
        finish();
        Intent i = new Intent( ac, SplashActivity.class );
        i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( i );
    }

    /**
     * Shows the activation code
     * @param v,The checkbox view
     */
    public void showPasswordClick( View v ) {
        GUIUtils.showPassword( (CheckBox) v, etActivationCode );
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();

        switch( responseCode ) {
             case REG_REQ:
                String code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED_REGISTRATION ) ) {
                    Intent intent = new Intent( ac, SplashActivity.class );
                    startActivity( intent );
                    finish();
                } else {
                    String message = response.getMessage();
                    MessageHandler.sendMessage( mHandlerMessages, code, message );
                }
                break;
        }
    }
}

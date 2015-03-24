package co.yodo.fare.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setupGUI();
        updateData();
    }

    private void setupGUI() {
        ac = SplashActivity.this;

        handlerMessages = new YodoHandler( SplashActivity.this );
        YodoRequest.getInstance().setListener( this );
    }

    private void updateData() {
        String hardwareToken = AppUtils.getHardwareToken( ac );
        if( hardwareToken == null ) {
            ToastMaster.makeText(ac, R.string.no_hardware, Toast.LENGTH_LONG).show();
            finish();
        } else {
            YodoRequest.getInstance().requestAuthentication( SplashActivity.this, hardwareToken );
        }
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        String code;
        finish();

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );

                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    Intent intent = new Intent( ac, FareActivity.class );
                    startActivity( intent );
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Intent intent = new Intent( ac, RegistrationActivity.class);
                    startActivity( intent );
                }

                break;
        }
    }
}

package co.yodo.pos.fare.main;

import java.lang.ref.WeakReference;

import co.yodo.pos.fare.R;
import co.yodo.pos.fare.helper.Encrypter;
import co.yodo.pos.fare.helper.HardwareToken;
import co.yodo.pos.fare.helper.Language;
import co.yodo.pos.fare.helper.ToastMaster;
import co.yodo.pos.fare.helper.YodoBase;
import co.yodo.pos.fare.helper.YodoGlobals;
import co.yodo.pos.fare.serverconnection.ServerResponse;
import co.yodo.pos.fare.serverconnection.SwitchServer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

public class YodoSplash extends Activity implements YodoBase {
	/*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;
    
    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;
	
	/**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoSplash> wMain;

        public MainHandler(YodoSplash main) {
            super();
            this.wMain = new WeakReference<YodoSplash>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoSplash main = wMain.get();

            // message arrived after activity death
            if(main == null)
                return;

            if(msg.what == YodoGlobals.NO_INTERNET) {
                ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.GENERAL_ERROR) {
                ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static MainHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Language.changeLanguage(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_yodo_splash);
        
        updateData();
        
        if(HARDWARE_TOKEN != null)
        	requestHardwareAuthorization();
    }
    
    /**
     * Update user account data within the application
     */
    private void updateData() {
        handlerMessages = new MainHandler(this);
        HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();

        if(HARDWARE_TOKEN == null) {
        	ToastMaster.makeText(YodoSplash.this, R.string.no_wifi, Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    /**
     * Gets object used to encrypt user's information
     * @return	Encrypter
     */
    public Encrypter getEncrypter(){
        if(oEncrypter == null)
            oEncrypter = new Encrypter();
        return oEncrypter;
    }
    
    /**
     * Connects to the switch and gets the user authorization
     */
    private void requestHardwareAuthorization() {
        String sEncryptedUsrData;

        // Encrypting user's  to create request
        getEncrypter().setsUnEncryptedString(HARDWARE_TOKEN);
        getEncrypter().rsaEncrypt(this);
        sEncryptedUsrData = this.getEncrypter().bytesToHex();

        new SwitchServer(YodoSplash.this).execute(SwitchServer.AUTH_HW_MERCH_REQUEST, sEncryptedUsrData);
    }

	@Override
	public void setData(ServerResponse data, int queryType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(YodoSplash.this);
		finish();

        if(data != null) {
            String code = data.getCode();
            if(code.equals(YodoGlobals.AUTHORIZED)) {
                Intent intent = new Intent(YodoSplash.this, YodoFare.class);
                startActivity(intent);
            } else if(code.equals(YodoGlobals.ERROR_FAILED)) { 
            	Intent intent = new Intent(YodoSplash.this, YodoRegistration.class);
                startActivity(intent);
            }
            else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
            } else {
            	builder.setTitle(Html.fromHtml("<font color='#FF0000'>" + data.getCode() + "</font>"));
            	builder.setMessage(Html.fromHtml("<font color='#FF0000'>" + data.getMessage() + "</font>"));
            	builder.setPositiveButton(getString(R.string.ok), null);
            	AlertDialog alertDialog = builder.create();
            	alertDialog.show();
            }
        } else {
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
	}
}

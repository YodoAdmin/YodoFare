package co.yodo.pos.fare.main;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import co.yodo.pos.fare.R;
import co.yodo.pos.fare.helper.Encrypter;
import co.yodo.pos.fare.helper.HardwareToken;
import co.yodo.pos.fare.helper.Language;
import co.yodo.pos.fare.helper.ToastMaster;
import co.yodo.pos.fare.helper.YodoBase;
import co.yodo.pos.fare.helper.YodoGlobals;
import co.yodo.pos.fare.serverconnection.ServerResponse;
import co.yodo.pos.fare.serverconnection.SwitchServer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class YodoRegistration extends Activity implements YodoBase {
	/*!< Time Stamp */
	private SimpleDateFormat dateFormat;
	private String timeStamp;
	
	/*!< Date pattern to be used for date within the reguests */
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'hh:mm:ssZZZZ";
	
	/*!< Variable used as an authentication number */
	private static String HARDWARE_TOKEN;
	private HardwareToken token;
	
	/*!< Object used to encrypt user's information */
	private Encrypter oEncrypter;
	
	/*!< GUI Controllers */
	private EditText passwordText;
	
	/*!< Separators */
	private static final String	REQ_SEP = ",";
	
	/**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoRegistration> wMain;

        public MainHandler(YodoRegistration main) {
            super();
            this.wMain = new WeakReference<YodoRegistration>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoRegistration main = wMain.get();

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yodo_registration);
    }
	
	private void setupGUI() {
		handlerMessages = new MainHandler(this);
    	passwordText = (EditText)this.findViewById(R.id.merchPipText); 
    }
	
	private void updateData() {
		token = new HardwareToken(getApplicationContext());
		HARDWARE_TOKEN = token.getToken();
		
		dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
		long time = (long)(System.currentTimeMillis());
		timeStamp = dateFormat.format(time);
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	
    	Language.changeLanguage(this);
    	setContentView(R.layout.activity_yodo_registration);
    	
    	setupGUI();
        updateData();
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

	/*!< Handle Button Actions */
	public void showPressed(View v) {
    	if(((CheckBox)v).isChecked()) 
    		passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    	else 
    		passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
	
	public void nextPressed(View v) {
		String merch_pip = passwordText.getText().toString();
		requestMerchantRegistration(merch_pip);
    }
	
	/**
	 * Connects to the switch and send a request merchant registration
	 * @return String 
	 */
	private void requestMerchantRegistration(String merch_pip) {
		String sEncryptedMerchData;
		StringBuilder merchData = new StringBuilder();
		
		merchData.append(HARDWARE_TOKEN).append(REQ_SEP);
		merchData.append(merch_pip).append(REQ_SEP);
		merchData.append(timeStamp);
			
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(merchData.toString());
		getEncrypter().rsaEncrypt(this);
		sEncryptedMerchData = getEncrypter().bytesToHex();
		
		SwitchServer request = new SwitchServer(YodoRegistration.this);
		request.setDialog(true, getString(R.string.registering_merch));
		request.execute(SwitchServer.REG_MERCH_REQUEST, sEncryptedMerchData);
	}

	@Override
	public void setData(ServerResponse data, int queryType) {
		if(data != null) {
			String code = data.getCode();
			if(code.equals(YodoGlobals.AUTHORIZED_REGISTRATION)) {
				Intent intent = new Intent(YodoRegistration.this, YodoFare.class);
				startActivity(intent);
				finish();
			} else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
                finish();
            } else {
            	AlertDialog.Builder builder = new AlertDialog.Builder(YodoRegistration.this);
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

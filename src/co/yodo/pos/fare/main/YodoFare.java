package co.yodo.pos.fare.main;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import co.yodo.pos.fare.R;
import co.yodo.pos.fare.helper.CreateAlertDialog;
import co.yodo.pos.fare.helper.Encrypter;
import co.yodo.pos.fare.helper.HardwareToken;
import co.yodo.pos.fare.helper.ToastMaster;
import co.yodo.pos.fare.helper.TransparentProgressDialog;
import co.yodo.pos.fare.helper.YodoBase;
import co.yodo.pos.fare.helper.YodoGlobals;
import co.yodo.pos.fare.serverconnection.ServerResponse;
import co.yodo.pos.fare.serverconnection.SwitchServer;

public class YodoFare extends Activity implements YodoBase {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
	/*!< Bluetooth Admin */
	private BluetoothAdapter mBluetoothAdapter;
	private final static int REQUEST_ENABLE_BLUETOOTH = 1;
	private final static int REQUEST_DISCOVERABLE_BLUETOOTH = 2;
	
	/*!< Messages ID */
	private static final int DEVICE_CONNECTED = 2;
	
	/*!< Variable used as an authentication number */
    private static String HARDWARE_TOKEN;
    
    /*!< Merchant Data */
	private static final String MOCK_GPS1 = "0";
	private static final String MOCK_GPS2 = "0";
	private double current_total = 0.00;
	
	/*!< Currency */
	private static String CURRENCY;
	private classCurrency[] currencyList;
    
    /*!< Object used to encrypt user's information */
    private Encrypter oEncrypter;
    
    /*!< ID for queries */
    private final static int BAL_REQ     = 0;
    private final static int BAL_TDY_REQ = 1;
    private final static int EXCH_REQ    = 2;
    
    /*!< Async Tasks for Balance */
    private SwitchServer totalBalance;
    private SwitchServer todayBalance;
    private String totalData;
    private String todayData;
    private boolean historyFlag = false;
	
	/*!< GUI controllers */
	private TextView totalText;
	private TextView currencyView;
	private SlidingPaneLayout mSlidingLayout;
	private EditText inputBox;
	
	private ImageView oldFee;
	private ImageView adultFee;
	private ImageView childFee;
	private ImageView childFee1;
	private ImageView studentFee;
	private ImageView currentFee;
	
	private ImageView onePersons;
	private ImageView twoPersons;
	private ImageView threePersons;
	private ImageView currentPersons;
	
	private int actual_fee;
	private static final int OLD_FARE     = 0;
	private static final int ADULT_FARE   = 1;
	private static final int CHILD_FARE   = 2;
	private static final int STUDENT_FARE = 3;
	
	/*private int actual_zone;
	private static final int ZONE_1 = 1;
	private static final int ZONE_2 = 2;
	private static final int ZONE_3 = 3;*/
	
	/*!< User's data separator */
	private static final String	REQ_SEP = ",";
	
	/*!< Preferences */
	private SharedPreferences settings;
	private int actualLanguage;
	private int actualCurrency = 0;
	
	/*!< Alert Messages */
	private AlertDialog alertDialog;
	
	/*!< Progress Dialog */
	private TransparentProgressDialog progressD;
	
	/**
     * Handles the message if there isn't internet connection
     */
    private static class MainHandler extends Handler {
        private final WeakReference<YodoFare> wMain;

        public MainHandler(YodoFare main) {
            super();
            this.wMain = new WeakReference<YodoFare>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            YodoFare main = wMain.get();

            // message arrived after activity death
            if(main == null)
                return;

            if(msg.what == YodoGlobals.NO_INTERNET) {
                ToastMaster.makeText(main, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == YodoGlobals.GENERAL_ERROR) {
                ToastMaster.makeText(main, R.string.error, Toast.LENGTH_LONG).show();
            }
            else if(msg.what == DEVICE_CONNECTED) {
				String response = msg.getData().getString("message");
				ToastMaster.makeText(main, main.getString(R.string.connected) + " " + response, Toast.LENGTH_SHORT).show();
			}
        }
    }

    private static MainHandler handlerMessages;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_yodo_pos);
        
        setupGUI();
        updateData();
    }
	
	@Override
    public void onResume() {
    	super.onResume();
    	
    	if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
    		setupBluetooth(); 
    	}
    }
	
	@Override 
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {			
			if(DEBUG)
				Log.e("keyboard", "connected");
	    } 
	    else if(newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
	    	if(DEBUG)
	    		Log.e("keyboard", "disconnected");
	    }
	}
	
	/**
	 *  Setting up the primary GUI 
	 */
    private void setupGUI() {
    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    	settings = getSharedPreferences(YodoGlobals.PREFERENCES, MODE_PRIVATE);
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	handlerMessages = new MainHandler(this);
    	
    	mSlidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
    	
    	boolean use = settings.getBoolean(YodoGlobals.FIRST_USE, YodoGlobals.DEFAULT_USE);
    	if(use) {
    		mSlidingLayout.openPane();
    		SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(YodoGlobals.FIRST_USE, false);
			editor.commit();
			
			Builder builder = new AlertDialog.Builder(YodoFare.this);
			builder.setTitle(getString(R.string.instructions_title));
        	builder.setMessage(getString(R.string.instructions_message));
        	builder.setPositiveButton(getString(R.string.ok), null);
        	alertDialog = builder.create();
        	alertDialog.show();
    	}
    	
		totalText    = (TextView)this.findViewById(R.id.totalText);
		currencyView = (TextView)this.findViewById(R.id.currency);
		
		oldFee     = (ImageView) findViewById(R.id.oldImageView);
		adultFee   = (ImageView) findViewById(R.id.adultImageView);
		childFee   = (ImageView) findViewById(R.id.childImageView);
		childFee1  = (ImageView) findViewById(R.id.voidImageView);
		studentFee = (ImageView) findViewById(R.id.studentImageView); 
		
		onePersons   = (ImageView) findViewById(R.id.oneImageView);
		twoPersons   = (ImageView) findViewById(R.id.twoImageView);
		threePersons = (ImageView) findViewById(R.id.threeImageView);
		
		adultFee.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		onePersons.setBackgroundResource(R.drawable.selected_rounded_corners);
		GradientDrawable drawable = (GradientDrawable) onePersons.getBackground();
		drawable.setColor(getResources().getColor(R.color.holo_blue_light));

		actual_fee  = ADULT_FARE;
		//actual_zone = ZONE_1;	
	}
    
    private void updateData() {
    	HardwareToken token = new HardwareToken(getApplicationContext());
        HARDWARE_TOKEN = token.getToken();
        
        loadCurrency();
        
        actualLanguage = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
        actualCurrency = settings.getInt(YodoGlobals.ID_CURRENCY, YodoGlobals.DEFAULT_CURRENCY);
		CURRENCY = currencyList[actualCurrency].getName();
		setCurrencyImage();
		
		currentFee     = adultFee;
		currentPersons = onePersons;
		
		String fee = settings.getString(YodoGlobals.ID_ADULT_FEE_1, YodoGlobals.DEFAULT_ADULTFEE);
		totalText.setText(fee);
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
     * Create all the choices for the list
     */
	private void loadCurrency() {
		currencyList = new classCurrency[3];

		// define the display string, the image, and the value to use when the choice is selected
		currencyList[0] = new classCurrency("MexicanPeso", getImg(R.drawable.mexico));
		currencyList[1] = new classCurrency("CAD", getImg(R.drawable.canada));
		currencyList[2] = new classCurrency("USD", getImg(R.drawable.us));
	}
	
	private void setCurrencyImage() {
		if(CURRENCY.equals("USD")) {
			currencyView.setText("USD");
			currencyView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.us, 0, 0, 0);
		}
		else if(CURRENCY.equals("CAD")) {
			currencyView.setText("CAD");
			currencyView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.canada, 0, 0, 0);
		}
		else if(CURRENCY.equals("MexicanPeso")) {
			currencyView.setText("MexicanPeso");
			currencyView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mexico, 0, 0, 0);
		}
	}
	
	private void setupBluetooth() {		
		// Not support Bluetooth 
		if(mBluetoothAdapter == null) {
			ToastMaster.makeText(this, R.string.not_support_bt, Toast.LENGTH_LONG).show();
		} else {
			mBluetoothAdapter.setName(YodoGlobals.YODO_POS + "-" + getString(R.string.merchant));
			
			if(mBluetoothAdapter.isEnabled()) {
				startAdvertisingService(); 
			} 
			else {
				Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBTIntent, REQUEST_ENABLE_BLUETOOTH);
			}
		}
	}
	
	/*!< Handle Buttons Actions */
	private void startAdvertisingService() {		
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BLUETOOTH);
	}
	
	public void oldFeeSelected(View v) {
		childFee1.setBackgroundColor(0);
		currentFee.setBackgroundColor(0);
		oldFee.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		onePersonsSelected(null);
		
		currentFee = oldFee;
		actual_fee = OLD_FARE;
		
		String fee = settings.getString(YodoGlobals.ID_OLD_FEE_1, YodoGlobals.DEFAULT_OLDFEE);
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
    
	public void adultFeeSelected(View v) {
		childFee1.setBackgroundColor(0);
		currentFee.setBackgroundColor(0);
		adultFee.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		onePersonsSelected(null);
		
		currentFee = adultFee;
		actual_fee = ADULT_FARE;
		
		String fee = settings.getString(YodoGlobals.ID_ADULT_FEE_1, YodoGlobals.DEFAULT_ADULTFEE);
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void childFeeSelected(View v) {
		currentFee.setBackgroundColor(0);
		childFee.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		childFee1.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		onePersonsSelected(null);
		
		currentFee = childFee;
		actual_fee = CHILD_FARE;;
		
		String fee = settings.getString(YodoGlobals.ID_CHILD_FEE_1, YodoGlobals.DEFAULT_CHILDFEE);
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void studentFeeSelected(View v) {
		childFee1.setBackgroundColor(0);
		currentFee.setBackgroundColor(0);
		studentFee.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
		onePersonsSelected(null);
		
		currentFee = studentFee;
		actual_fee = STUDENT_FARE;
		
		String fee = settings.getString(YodoGlobals.ID_STUDENT_FEE_1, YodoGlobals.DEFAULT_STUDENTFEE);
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void onePersonsSelected(View v) {
		currentPersons.setBackgroundColor(0);
		onePersons.setBackgroundResource(R.drawable.selected_rounded_corners);
		GradientDrawable drawable = (GradientDrawable) onePersons.getBackground();
		drawable.setColor(getResources().getColor(R.color.holo_blue_light));
		currentPersons = onePersons;
		
		//actual_zone = ZONE_1;
		String fee;
		
		switch(actual_fee) {
			case OLD_FARE:
				fee = settings.getString(YodoGlobals.ID_OLD_FEE_1, YodoGlobals.DEFAULT_OLDFEE);
				break;
				
			case ADULT_FARE:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_1, YodoGlobals.DEFAULT_ADULTFEE);
				break;
			
			case CHILD_FARE:
				fee = settings.getString(YodoGlobals.ID_CHILD_FEE_1, YodoGlobals.DEFAULT_CHILDFEE);
				break;
				
			case STUDENT_FARE:
				fee = settings.getString(YodoGlobals.ID_STUDENT_FEE_1, YodoGlobals.DEFAULT_STUDENTFEE);
				break;
				
			default:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_1, YodoGlobals.DEFAULT_ADULTFEE);
				break;
		}
		
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void twoPersonsSelected(View v) {
		currentPersons.setBackgroundColor(0);
		twoPersons.setBackgroundResource(R.drawable.selected_rounded_corners);
		GradientDrawable drawable = (GradientDrawable) twoPersons.getBackground();
		drawable.setColor(getResources().getColor(R.color.holo_blue_light));
		currentPersons = twoPersons;
		
		//actual_zone = ZONE_2;
		String fee;
		
		switch(actual_fee) {
			case OLD_FARE:
				fee = settings.getString(YodoGlobals.ID_OLD_FEE_2, YodoGlobals.DEFAULT_OLDFEE);
				break;
				
			case ADULT_FARE:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_2, YodoGlobals.DEFAULT_ADULTFEE);
				break;
			
			case CHILD_FARE:
				fee = settings.getString(YodoGlobals.ID_CHILD_FEE_2, YodoGlobals.DEFAULT_CHILDFEE);
				break;
				
			case STUDENT_FARE:
				fee = settings.getString(YodoGlobals.ID_STUDENT_FEE_2, YodoGlobals.DEFAULT_STUDENTFEE);
				break;
				
			default:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_2, YodoGlobals.DEFAULT_ADULTFEE);
				break;
		}
		
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void threePersonsSelected(View v) {
		currentPersons.setBackgroundColor(0);
		threePersons.setBackgroundResource(R.drawable.selected_rounded_corners);
		GradientDrawable drawable = (GradientDrawable) threePersons.getBackground();
		drawable.setColor(getResources().getColor(R.color.holo_blue_light));
		currentPersons = threePersons;
		
		//actual_zone = ZONE_3;
		String fee;
		
		switch(actual_fee) {
			case OLD_FARE:
				fee = settings.getString(YodoGlobals.ID_OLD_FEE_3, YodoGlobals.DEFAULT_OLDFEE);
				break;
				
			case ADULT_FARE:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_3, YodoGlobals.DEFAULT_ADULTFEE);
				break;
			
			case CHILD_FARE:
				fee = settings.getString(YodoGlobals.ID_CHILD_FEE_3, YodoGlobals.DEFAULT_CHILDFEE);
				break;
				
			case STUDENT_FARE:
				fee = settings.getString(YodoGlobals.ID_STUDENT_FEE_3, YodoGlobals.DEFAULT_STUDENTFEE);
				break;
				
			default:
				fee = settings.getString(YodoGlobals.ID_ADULT_FEE_3, YodoGlobals.DEFAULT_ADULTFEE);
				break;
		}
		
		Double total = current_total + Double.valueOf(fee);
		totalText.setText(String.format("%.2f", total));
	}
	
	public void resetPressed(View v) {
		current_total = 0.00;
		adultFeeSelected(null);
		onePersonsSelected(null);
	}
	
	public void addPressed(View v) {
		current_total = Double.valueOf(totalText.getText().toString());
		
		/*switch(actual_zone) {
			case ZONE_1:
				onePersonsSelected(null);
				break;
				
			case ZONE_2:
				twoPersonsSelected(null);
				break;
			
			case ZONE_3:
				threePersonsSelected(null);
				break;
		}*/
	}
	
	/**
     * Gets Merch history
     */
	public void payPressed(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		alertDialog = null;
		
		final EditText input = new EditText(this);
		input.setOnKeyListener(new View.OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				/// Prevent adding new line
	            if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) 
	            	return true;
	            
				if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
					String client = input.getText().toString();
					
					if(client == null || client.equals("")) {
						if(DEBUG)
							Log.e("YodoPOS ID", "null input");
					} else {
						if(alertDialog != null)
							alertDialog.dismiss();
						
						requestExchange(client); 
					}
					return true;
	            }
				return false;
			}
	    });
		builder.setTitle(getString(R.string.barcode_scanner));
		builder.setView(input);
		builder.setNegativeButton(getString(R.string.cancel), null);
		alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		alertDialog.show();
	}
	
	public void historyPressed(View v) {
		if(mSlidingLayout.isOpen()) 
            mSlidingLayout.closePane();
		
    	// Input PIP dialog 	
    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.dialog_with_password, null);
    	inputBox = (EditText) layout.findViewById(R.id.dialogInputBox);
    	
    	/// Listener to click event on the dialog in order to view the sks code
    	DialogInterface.OnClickListener okButtonClickListener = new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		String pip = inputBox.getText().toString();
	    		historyFlag = true;
	    		requestHistoryRequest(pip);
	    		requestDailyHistoryRequest(pip);
	    	}
	    };
    				
	    CreateAlertDialog.showAlertDialog(YodoFare.this, layout, inputBox,
	    		getString(R.string.input_pip),
	    		null, 
	    		okButtonClickListener, 
	    		null); 
    }
	
	/**
	 *  Creates the Alert Dialog to change the actual currency 
	 */
	public void currencyPressed(View v) {
		if(mSlidingLayout.isOpen()) 
            mSlidingLayout.closePane();
		
		ListAdapter adapter = new CurrencyAdapter(this, currencyList);
		AlertDialog.Builder alertCurrency = new AlertDialog.Builder(this);
		alertCurrency.setTitle(R.string.select_currency);

		alertCurrency.setSingleChoiceItems(adapter, actualCurrency, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getBaseContext(), currencyList[item].getName(), Toast.LENGTH_SHORT).show();
				CURRENCY = currencyList[item].getName();
				setCurrencyImage();
				actualCurrency = item;
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt(YodoGlobals.ID_CURRENCY, item);
				editor.commit(); 
				
				dialog.dismiss();
			}
		}); 
		
		AlertDialog alert = alertCurrency.create();
		alert.show(); 
	}
	
	public void changeLanguageClick(View v) {
		if(mSlidingLayout.isOpen()) 
            mSlidingLayout.closePane();
            
		AlertDialog.Builder alertLanguage = new AlertDialog.Builder(this);
		alertLanguage.setTitle(R.string.sel_language);
		
		alertLanguage.setSingleChoiceItems(YodoGlobals.languages, actualLanguage, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(getBaseContext(), YodoGlobals.languages[item], Toast.LENGTH_LONG).show();
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt(YodoGlobals.ID_LANGUAGE, item);
				editor.commit();
				
				Intent intent = new Intent(YodoFare.this, YodoSplash.class);
				startActivity(intent);
				finish();
				
				alertDialog.cancel();
			}
		}); 
		
		alertDialog = alertLanguage.create();
		alertDialog.show(); 
	}
	
	public void setFeeClick(View view) {
		if(mSlidingLayout.isOpen()) 
            mSlidingLayout.closePane();
		
		String old_fee_1     = settings.getString(YodoGlobals.ID_OLD_FEE_1, YodoGlobals.DEFAULT_OLDFEE);
		String old_fee_2     = settings.getString(YodoGlobals.ID_OLD_FEE_2, YodoGlobals.DEFAULT_OLDFEE);
		String old_fee_3     = settings.getString(YodoGlobals.ID_OLD_FEE_3, YodoGlobals.DEFAULT_OLDFEE);
		
		String adult_fee_1   = settings.getString(YodoGlobals.ID_ADULT_FEE_1, YodoGlobals.DEFAULT_ADULTFEE);
		String adult_fee_2   = settings.getString(YodoGlobals.ID_ADULT_FEE_2, YodoGlobals.DEFAULT_ADULTFEE);
		String adult_fee_3   = settings.getString(YodoGlobals.ID_ADULT_FEE_3, YodoGlobals.DEFAULT_ADULTFEE);
		
		String child_fee_1   = settings.getString(YodoGlobals.ID_CHILD_FEE_1, YodoGlobals.DEFAULT_CHILDFEE);
		String child_fee_2   = settings.getString(YodoGlobals.ID_CHILD_FEE_2, YodoGlobals.DEFAULT_CHILDFEE);
		String child_fee_3   = settings.getString(YodoGlobals.ID_CHILD_FEE_3, YodoGlobals.DEFAULT_CHILDFEE);
		
		String student_fee_1 = settings.getString(YodoGlobals.ID_STUDENT_FEE_1, YodoGlobals.DEFAULT_STUDENTFEE);
		String student_fee_2 = settings.getString(YodoGlobals.ID_STUDENT_FEE_2, YodoGlobals.DEFAULT_STUDENTFEE);
		String student_fee_3 = settings.getString(YodoGlobals.ID_STUDENT_FEE_3, YodoGlobals.DEFAULT_STUDENTFEE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(YodoFare.this);
		View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
		
		final EditText oldFee1     = (EditText) v.findViewById(R.id.oldFee1Text);
		final EditText oldFee2     = (EditText) v.findViewById(R.id.oldFee2Text);
		final EditText oldFee3     = (EditText) v.findViewById(R.id.oldFee3Text);
		
		final EditText adultFee1   = (EditText) v.findViewById(R.id.adultFee1Text);
		final EditText adultFee2   = (EditText) v.findViewById(R.id.adultFee2Text);
		final EditText adultFee3   = (EditText) v.findViewById(R.id.adultFee3Text);
		
		final EditText childFee1   = (EditText) v.findViewById(R.id.childFee1Text);
		final EditText childFee2   = (EditText) v.findViewById(R.id.childFee2Text);
		final EditText childFee3   = (EditText) v.findViewById(R.id.childFee3Text);
		
		final EditText studentFee1 = (EditText) v.findViewById(R.id.studentFee1Text);
		final EditText studentFee2 = (EditText) v.findViewById(R.id.studentFee2Text);
		final EditText studentFee3 = (EditText) v.findViewById(R.id.studentFee3Text);
		
		oldFee1.setText(old_fee_1);
		oldFee2.setText(old_fee_2);
		oldFee3.setText(old_fee_3);
		
		adultFee1.setText(adult_fee_1);
		adultFee2.setText(adult_fee_2);
		adultFee3.setText(adult_fee_3);
		
		childFee1.setText(child_fee_1);
		childFee2.setText(child_fee_2);
		childFee3.setText(child_fee_3);
		
		studentFee1.setText(student_fee_1);
		studentFee2.setText(student_fee_2);
		studentFee3.setText(student_fee_3);
		
		builder.setTitle(getString(R.string.action_settings));
		builder.setView(v);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {                
            	SharedPreferences.Editor editor = settings.edit();
				
            	editor.putString(YodoGlobals.ID_OLD_FEE_1, oldFee1.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_OLD_FEE_2, oldFee2.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_OLD_FEE_3, oldFee3.getText().toString().replace(',', '.'));
            	
            	editor.putString(YodoGlobals.ID_ADULT_FEE_1, adultFee1.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_ADULT_FEE_2, adultFee2.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_ADULT_FEE_3, adultFee3.getText().toString().replace(',', '.'));
            	
            	editor.putString(YodoGlobals.ID_CHILD_FEE_1, childFee1.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_CHILD_FEE_2, childFee2.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_CHILD_FEE_3, childFee3.getText().toString().replace(',', '.'));
            	
            	editor.putString(YodoGlobals.ID_STUDENT_FEE_1, studentFee1.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_STUDENT_FEE_2, studentFee2.getText().toString().replace(',', '.'));
            	editor.putString(YodoGlobals.ID_STUDENT_FEE_3, studentFee3.getText().toString().replace(',', '.'));
				
				editor.commit();
                dialog.dismiss();
                
                finish();
				startActivity(getIntent());
            }
        });
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		});
		
		final AlertDialog alertDialog = builder.create();
        alertDialog.show();
	}
	
	public void aboutClick(View v) {
		if(mSlidingLayout.isOpen()) 
            mSlidingLayout.closePane();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.about);
    	String message = "IMEI: " + HARDWARE_TOKEN + "\n" + getString(R.string.version); 
    	builder.setMessage(message);
    	builder.setCancelable(true);
    	
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	public void exitClick(View v) {
		finish();
		System.exit(0);
	}
	
	/**
     * Dialog Button Actions
     * */
    public void showPressed(View v) {
        if(((CheckBox)v).isChecked())
            inputBox.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else
            inputBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    
	/**
	 *  Create Balance Dialog 
	 */
	private void balanceDialog() {
		LayoutInflater inflater = this.getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_merch_balance, null);
		
		TextView todayCreditsText = ((TextView) layout.findViewById(R.id.yodoTodayCashTextView));
		TextView todayDebitsText = ((TextView) layout.findViewById(R.id.yodoTodayDebitsTextView));
		TextView todayBalanceText = ((TextView) layout.findViewById(R.id.yodoTodayBalanceTextView));
		
		TextView creditsText = ((TextView) layout.findViewById(R.id.yodoCashTextView));
		TextView debitsText = ((TextView) layout.findViewById(R.id.yodoDebitsTextView));
		TextView balanceText = ((TextView) layout.findViewById(R.id.yodoBalanceTextView));
		
		AlertDialog.Builder alert = new AlertDialog.Builder(YodoFare.this);
		alert.setPositiveButton("Ok", null);
		alert.setView(layout);
		
		String aParams[] = totalData.split(ServerResponse.ENTRY_SEPARATOR);
		double total = 0.0;
		
		for(String param : aParams) {
			String aParam[] = param.split(" ");
			
			if(aParam[0].equals(ServerResponse.DEBIT_ELEM + ":")) {
				Double result = Double.valueOf(aParam[1]);
				debitsText.setText(String.format("%.2f", result));
				total -= result;

			} else if(aParam[0].equals(ServerResponse.CREDIT_ELEM + ":")) {
				Double result = Double.valueOf(aParam[1]);
				creditsText.setText(String.format("%.2f", result));
				total += result;
			}
		}
		balanceText.setText(String.format("%.2f", total * -1));
		
		aParams = todayData.split(ServerResponse.ENTRY_SEPARATOR);
		total = 0.0;
		
		for(String param : aParams) {
			String aParam[] = param.split(" ");
			
			if(aParam[0].equals(ServerResponse.DEBIT_ELEM + ":")) {
				Double result = Double.valueOf(aParam[1]);
				todayDebitsText.setText(String.format("%.2f", result));
				total -= result;

			} else if(aParam[0].equals(ServerResponse.CREDIT_ELEM + ":")) {
				Double result = Double.valueOf(aParam[1]);
				todayCreditsText.setText(String.format("%.2f", result));
				total += result;
			}
		}
		todayBalanceText.setText(String.format("%.2f", total * -1));
		alert.setTitle("Merchant Balance Report");
		
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}
	
	/**
	 * Connects to the switch and send a exchange request
	 * @return String 
	 */
	private void requestExchange(String client) {
		String sEncryptedMerchData, sEncryptedExchangeUsrData;
		StringBuilder sEncryptedUsrData = new StringBuilder();
		StringBuilder sExchangeUsrData = new StringBuilder();
		
		/// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(HARDWARE_TOKEN);
		getEncrypter().rsaEncrypt(this);
		sEncryptedMerchData = getEncrypter().bytesToHex();
		
		sEncryptedUsrData.append(sEncryptedMerchData).append(REQ_SEP);
		sEncryptedUsrData.append(client).append(REQ_SEP);
		
		sExchangeUsrData.append(MOCK_GPS1).append(REQ_SEP);
		sExchangeUsrData.append(MOCK_GPS2).append(REQ_SEP);
		sExchangeUsrData.append(totalText.getText()).append(REQ_SEP);
		sExchangeUsrData.append("0.00").append(REQ_SEP); 
		sExchangeUsrData.append("0.00").append(REQ_SEP);
		sExchangeUsrData.append(CURRENCY);
		
		getEncrypter().setsUnEncryptedString(sExchangeUsrData.toString());
		getEncrypter().rsaEncrypt(this);
		sEncryptedExchangeUsrData = this.getEncrypter().bytesToHex();	
		sEncryptedUsrData.append(sEncryptedExchangeUsrData);
		
		progressD = new TransparentProgressDialog(this, R.drawable.spinner);
		progressD.show();
		
		SwitchServer request = new SwitchServer(YodoFare.this);
		request.setType(EXCH_REQ);
		request.execute(SwitchServer.EXCH_MERCH_REQUEST, sEncryptedUsrData.toString());
	}
    
    /**
	 * Connects to the switch and send a history request
	 * @return String 
	 */
	private void requestHistoryRequest(String pip) {
		String sEncryptedMerchData;
		StringBuilder sBalanceData = new StringBuilder();
		
		sBalanceData.append(HARDWARE_TOKEN).append(REQ_SEP);
		sBalanceData.append(pip).append(REQ_SEP);
		sBalanceData.append(YodoGlobals.QUERY_BALANCE);
		
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(sBalanceData.toString());
		getEncrypter().rsaEncrypt(this);
		sEncryptedMerchData = this.getEncrypter().bytesToHex();
			
		totalBalance = new SwitchServer(YodoFare.this);
		totalBalance.setType(BAL_REQ);
		totalBalance.setDialog(true, getString(R.string.balance_message));
		totalBalance.execute(SwitchServer.QRY_BAL_REQUEST, sEncryptedMerchData);
	}
	
	/**
	 * Connects to the switch and send a daily balance request
	 * @return String 
	 */
	private void requestDailyHistoryRequest(String pip) {
		String sEncryptedMerchData;
		StringBuilder sBalanceData = new StringBuilder();
		
		sBalanceData.append(HARDWARE_TOKEN).append(REQ_SEP);
		sBalanceData.append(pip).append(REQ_SEP);
		sBalanceData.append(YodoGlobals.QUERY_TODAY_BALANCE);
		
		// Encrypting user's data to create request
		getEncrypter().setsUnEncryptedString(sBalanceData.toString());
		getEncrypter().rsaEncrypt(this);
		sEncryptedMerchData = this.getEncrypter().bytesToHex();
			
		todayBalance = new SwitchServer(YodoFare.this);
		todayBalance.setType(BAL_TDY_REQ);
		todayBalance.setDialog(true, getString(R.string.balance_message));
		todayBalance.execute(SwitchServer.QRY_BAL_REQUEST, sEncryptedMerchData);
	}

	@Override
	public void setData(ServerResponse data, int queryType) {	
		AlertDialog.Builder builder = new AlertDialog.Builder(YodoFare.this);
		String message = null;
		
		if(progressD != null)
			progressD.dismiss();
		
		if(data != null) {
			String code = data.getCode();
			if(code.equals(YodoGlobals.AUTHORIZED)) {
	            switch(queryType) {
	                case BAL_REQ:
                    	totalData = data.getParams();
                    	
                    	if(todayBalance != null && todayBalance.getStatus() == AsyncTask.Status.FINISHED) {
                    		balanceDialog();
                    		historyFlag = false;
                    		todayBalance = totalBalance = null;
                    	}
	                    break;
	                    
	                case BAL_TDY_REQ:
                    	todayData = data.getParams();
                    	
                    	if(totalBalance != null && totalBalance.getStatus() == AsyncTask.Status.FINISHED) {
                    		balanceDialog();
                    		historyFlag = false;
                    		todayBalance = totalBalance = null;
                   	 	} 
	                    break;
	                    
	                case EXCH_REQ:
	                	message = getString(R.string.exchange_code)    + " " + data.getCode() + "\n" + 
	                			  getString(R.string.exchange_auth)    + " " + data.getAuthNumber() + "\n" +
	                			  getString(R.string.exchange_message) + " " + data.getMessage();
	                	
	                	builder.setMessage(message);
	                	builder.setPositiveButton(getString(R.string.ok), null);
	                	alertDialog = builder.create();
	                	alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	                	alertDialog.show();
	                    break;
	            }
			} else if(code.equals(YodoGlobals.ERROR_INTERNET)) {
                handlerMessages.sendEmptyMessage(YodoGlobals.NO_INTERNET);
            } else {
            	if(historyFlag) {
            		if(queryType == BAL_REQ) {
            			todayBalance.cancel(true);
            			todayBalance = null;
            		}
            		
            		if(queryType == BAL_TDY_REQ) {
            			totalBalance.cancel(true);
            			totalBalance = null;
            		}
            	}
            		
        		builder.setTitle(Html.fromHtml("<font color='#FF0000'>" + data.getCode() + "</font>"));
            	builder.setMessage(Html.fromHtml("<font color='#FF0000'>" + data.getMessage() + "</font>"));
            	builder.setPositiveButton(getString(R.string.ok), null);
            	alertDialog = builder.create();
            	alertDialog.show();
            	
            	MediaPlayer mp = MediaPlayer.create(YodoFare.this, R.raw.error);
                mp.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });   
                mp.start();
            }
		} else {
            handlerMessages.sendEmptyMessage(YodoGlobals.GENERAL_ERROR);
        }
	}
	
	/**
	 * POJO for holding each list choice
	 */
	class classCurrency {
		private String   name;
		private Drawable img;

		public classCurrency(String name, Drawable img) {
			this.name = name;
			this.img = img;
		}

		public String getName() {
			return this.name;
		}

		public Drawable getImg() {
			return this.img;
		}
	}
	
	private Drawable getImg(int res) {
		Drawable img = getResources().getDrawable(res);
		img.setBounds(0, 0, 48, 48);
		return img;
	}
	
	/**
     * Definition of the list adapter...uses the View Holder pattern to
     * optimize performance.
     */
	@SuppressWarnings("rawtypes")
	static class CurrencyAdapter extends ArrayAdapter {
		private static final int RESOURCE = R.layout.row;
		private LayoutInflater inflater;

	    static class ViewHolder {
	        TextView nameTxVw;
	    }

		@SuppressWarnings("unchecked")
		public CurrencyAdapter(Context context, classCurrency[] objects) {
			super(context, RESOURCE, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if(convertView == null) {
				// inflate a new view and setup the view holder for future use
				convertView = inflater.inflate(RESOURCE, null);

				holder = new ViewHolder();
				holder.nameTxVw = (TextView)convertView.findViewById(R.id.currencyName);
				convertView.setTag(holder);
			} else {
				// view already defined, retrieve view holder
				holder = (ViewHolder)convertView.getTag();
			}

			classCurrency cat = (classCurrency)getItem(position);
			if(cat == null) {
				if(DEBUG)
					Log.e("TAG", "Invalid category for position: " + position );
			}
			holder.nameTxVw.setText(cat.getName());
			holder.nameTxVw.setCompoundDrawables(cat.getImg(), null, null, null);

			return convertView;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
			// Bluetooth is turned on
			if(resultCode == RESULT_OK) {
				startAdvertisingService();
			} 
			// User denied to turn on 
			else if(resultCode == RESULT_CANCELED) {
				ToastMaster.makeText(YodoFare.this, R.string.not_bt, Toast.LENGTH_LONG).show();
			}
		} 
		else if(requestCode == REQUEST_DISCOVERABLE_BLUETOOTH) {
			if(resultCode == RESULT_CANCELED) {
				ToastMaster.makeText(YodoFare.this, R.string.not_bt, Toast.LENGTH_LONG).show();
			}
		}
	}
}

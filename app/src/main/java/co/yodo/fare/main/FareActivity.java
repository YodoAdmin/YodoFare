package co.yodo.fare.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import co.yodo.fare.R;
import co.yodo.fare.adapter.CurrencyAdapter;
import co.yodo.fare.broadcastreceiver.BroadcastMessage;
import co.yodo.fare.component.ClearEditText;
import co.yodo.fare.component.JsonParser;
import co.yodo.fare.component.ToastMaster;
import co.yodo.fare.component.YodoHandler;
import co.yodo.fare.data.Currency;
import co.yodo.fare.data.ServerResponse;
import co.yodo.fare.helper.AlertDialogHelper;
import co.yodo.fare.helper.AppConfig;
import co.yodo.fare.helper.AppUtils;
import co.yodo.fare.net.YodoRequest;
import co.yodo.fare.scanner.QRScanner;
import co.yodo.fare.scanner.QRScannerFactory;
import co.yodo.fare.scanner.QRScannerListener;
import co.yodo.fare.service.LocationService;
import co.yodo.fare.service.RESTService;

public class FareActivity extends AppCompatActivity implements YodoRequest.RESTListener, QRScannerListener {
    /** DEBUG */
    private static final String TAG = FareActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** The Local Broadcast Manager */
    private LocalBroadcastManager lbm;

    /** Orientation detector */
    private OrientationEventListener mOrientationListener;
    private int mLastRotation;

    /** Hardware Token */
    private String hardwareToken;

    /** Gui controllers */
    private SlidingPaneLayout mSlidingLayout;
    private Spinner mScannersSpinner;
    private TextView mTotalFareView;
    private ImageView mLocationView;
    private View mCurrentFee;
    private View mCurrentZone;

    /** Popup Window for Tips */
    private PopupWindow mPopupMessage;
    private BigDecimal equivalentTotal;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    /** Balance Temp */
    private HashMap<String, String> historyData = null;
    private HashMap<String, String> todayData = null;

    /** Location */
    private Location location;

    /** Current Scanners */
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** Total to pay */
    private BigDecimal mCurrentTotal = BigDecimal.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_fare );

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcasts();

        AppUtils.setupAdvertising( ac, AppUtils.isAdvertisingServiceRunning( ac ), false );

        if( currentScanner != null && isScanning ) {
            isScanning = false;
            currentScanner.startScan();
        }

        Intent iLoc = new Intent( ac, LocationService.class );
        if( AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) )
            stopService( iLoc );
        startService( iLoc );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcasts();

        if( currentScanner != null && currentScanner.isScanning() ) {
            isScanning = true;
            currentScanner.close();
        }

        if( AppUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mOrientationListener.disable();
        QRScannerFactory.destroy();
    }

    @Override
    public void onBackPressed() {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.destroy();
            return;
        }

        super.onBackPressed();
    }

    /**
     * Initialized all the GUI main components
     */
    private void setupGUI() {
        // get the context
        ac = FareActivity.this;
        // get local broadcast
        lbm = LocalBroadcastManager.getInstance( ac );

        // Globals
        mSlidingLayout   = (SlidingPaneLayout) findViewById( R.id.sliding_panel_layout );
        mScannersSpinner = (Spinner) findViewById( R.id.scannerSpinner );
        mLocationView    = (ImageView) findViewById( R.id.locationIconView );
        mTotalFareView   = (TextView) findViewById( R.id.totalFareText );

        // Popup
        mPopupMessage  = new PopupWindow( ac );

        // Sliding Panel Configurations
        mSlidingLayout.setParallaxDistance( 30 );

        mScannersSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if( mSlidingLayout.isOpen() )
                    mSlidingLayout.closePane();

                ( (TextView) parentView.getChildAt( 0 ) ).setTextColor( Color.WHITE );
                AppUtils.saveScanner(ac, position);
                AppUtils.Logger(TAG, ((TextView) selectedItemView).getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<QRScannerFactory.SupportedScanners> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanners.values()
        );

        int position = AppUtils.getScanner( ac );
        if( position >= QRScannerFactory.SupportedScanners.length ) {
            position = AppConfig.DEFAULT_SCANNER;
            AppUtils.saveScanner( ac, position );
        }

        mScannersSpinner.setAdapter( adapter );
        mScannersSpinner.setSelection( position );

        handlerMessages = new YodoHandler( FareActivity.this );
        YodoRequest.getInstance().setListener( this );

        if( AppUtils.isFirstLogin( ac ) ) {
            mSlidingLayout.openPane();

            /*new ShowcaseView.Builder( this )
                    .setTarget( new ViewTarget( R.id.optionsView, this ) )
                    .setContentTitle( R.string.tutorial_title )
                    .setContentText( R.string.tutorial_message )
                    .build();*/

            AppUtils.saveFirstLogin( ac, false );
        }

        if( !AppUtils.isLocationEnabled( ac ) ) {
            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                    startActivity( intent );
                }
            };

            AlertDialogHelper.showAlertDialog( ac, R.string.gps_enable, onClick );
        }

        mLastRotation = getWindowManager().getDefaultDisplay().getRotation();
        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = getWindowManager().getDefaultDisplay().getRotation();

                if( rotation != mLastRotation ) {

                    if( currentScanner != null && currentScanner.isScanning() ) {
                        currentScanner.close();
                        currentScanner.startScan();
                    }

                    mLastRotation = rotation;
                }
            }
        };

        if( mOrientationListener.canDetectOrientation() ) {
            mOrientationListener.enable();
        }

        resetClick( null );
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );
        AppUtils.setCurrencyIcon( ac, mTotalFareView, false );

        mTotalFareView.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setupPopup( v );
                return false;
            }
        });

        mTotalFareView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch( event.getAction() ) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if( mPopupMessage != null )
                            mPopupMessage.dismiss();
                        break;
                }
                return false;
            }
        });

        new getCurrentBalance().execute();

        // mock location
        location = new Location( "flp" );
        location.setLatitude( 0.00 );
        location.setLongitude( 0.00 );
    }

    /**
     * Setup a PopupWindow below a View
     * @param v The view for te popup
     */
    private void setupPopup(View v) {
        LinearLayout viewGroup = (LinearLayout) findViewById( R.id.popup_window );
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View layout = layoutInflater.inflate( R.layout.popup_window, viewGroup );

        TextView cashTotal      = (TextView) layout.findViewById( R.id.cashTotalText );
        ProgressBar progressBar = (ProgressBar) layout.findViewById( R.id.progressBarPopUp );
        cashTotal.setText( equivalentTotal.setScale( 2, RoundingMode.DOWN ).toString() );

        if( equivalentTotal == null ) {
            cashTotal.setVisibility( View.GONE );
            progressBar.setVisibility( View.VISIBLE );
        }

        mPopupMessage.setWidth( mTotalFareView.getWidth() );
        mPopupMessage.setHeight( LinearLayout.LayoutParams.WRAP_CONTENT );
        mPopupMessage.setContentView( layout );
        mPopupMessage.showAtLocation( v, Gravity.CENTER, 0, 0 );
    }

    /**
     * Creates a dialog to show the balance information
     */
    private void balanceDialog() {
        if( historyData.size() == 1 || todayData.size() == 1 ) {
            ToastMaster.makeText(ac, R.string.no_balance, Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout root = (LinearLayout) findViewById( R.id.layoutDialogRoot );
        View layout = inflater.inflate( R.layout.dialog_balance, root, false );

        TextView todayCreditsText = ( (TextView) layout.findViewById( R.id.yodoTodayCashTextView ) );
        TextView todayDebitsText  = ( (TextView) layout.findViewById( R.id.yodoTodayDebitsTextView ) );
        TextView todayBalanceText = ( (TextView) layout.findViewById( R.id.yodoTodayBalanceTextView ) );

        TextView creditsText = ( (TextView) layout.findViewById( R.id.yodoCashTextView ) );
        TextView debitsText  = ( (TextView) layout.findViewById( R.id.yodoDebitsTextView ) );
        TextView balanceText = ( (TextView) layout.findViewById( R.id.yodoBalanceTextView ) );

        BigDecimal total = BigDecimal.ZERO;

        BigDecimal result = new BigDecimal( historyData.get( ServerResponse.DEBIT ) );
        debitsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.subtract( result );

        result = new BigDecimal( historyData.get( ServerResponse.CREDIT ) );
        creditsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.add( result );

        balanceText.setText(
                total.negate().setScale( 2, RoundingMode.DOWN ).toString()
        );
        total = BigDecimal.ZERO;

        result = new BigDecimal( todayData.get( ServerResponse.DEBIT ) );
        todayDebitsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.subtract( result );

        result = new BigDecimal( todayData.get( ServerResponse.CREDIT ) );
        todayCreditsText.setText( result.setScale( 2, RoundingMode.DOWN ).toString() );
        total = total.add( result );

        todayBalanceText.setText(
                total.negate().setScale( 2, RoundingMode.DOWN ).toString()
        );

        AlertDialogHelper.showAlertDialog( ac, getString( R.string.yodo_title ), layout );
        todayData = historyData = null;
    }

    /**
     * Changes the current language
     * @param v View, used to get the title
     */
    public void setLanguageClick(View v) {
        mSlidingLayout.closePane();

        final String title       = ((Button) v).getText().toString();
        final String[] languages = getResources().getStringArray( R.array.languages_array );
        final int current        = AppUtils.getLanguage( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();

                ToastMaster.makeText( ac, languages[item], Toast.LENGTH_SHORT ).show();
                AppUtils.saveLanguage( ac, item );

                recreate();
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                languages,
                current,
                onClick
        );
    }

    /**
     * Changes the current currency
     * @param v View, used to get the title
     */
    public void setCurrencyClick(View v) {
        mSlidingLayout.closePane();

        final String[] currency = getResources().getStringArray( R.array.currency_array );
        final String[] icons    = getResources().getStringArray( R.array.currency_icon_array );

        Currency[] currencyList = new Currency[currency.length];
        for( int i = 0; i < currency.length; i++ )
            currencyList[i] = new Currency( currency[i], AppUtils.getDrawableByName( ac, icons[i] ) );

        final String title        = ((Button) v).getText().toString();
        final ListAdapter adapter = new CurrencyAdapter( ac, currencyList );
        final int current         = AppUtils.getCurrency(ac);

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                AppUtils.saveCurrency(ac, item);

                Drawable icon = AppUtils.getDrawableByName( ac, icons[ item ] );
                icon.setBounds(0, 0, mTotalFareView.getLineHeight(), (int) (mTotalFareView.getLineHeight() * 0.9));
                mTotalFareView.setCompoundDrawables(icon, null, null, null);
                new getCurrentBalance().execute();

                dialog.dismiss();
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                adapter,
                current,
                onClick
        );
    }

    /**
     * Settings for the fee values
     * @param v View, not used
     */
    public void settingsClick(View v) {
        mSlidingLayout.closePane();

        Intent intent = new Intent( ac, SettingsActivity.class );
        startActivity( intent );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick(View v) {
        mSlidingLayout.closePane();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );
        final CheckBox remember = new CheckBox( ac );
        remember.setText( R.string.remember_pass);

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( FareActivity.this );

                if( remember.isChecked() )
                    AppUtils.savePassword( ac, pip );
                else
                    AppUtils.savePassword( ac, null );

                YodoRequest.getInstance().createProgressDialog(
                        FareActivity.this ,
                        YodoRequest.ProgressDialogType.NORMAL
                );

                YodoRequest.getInstance().requestHistory(
                        FareActivity.this,
                        hardwareToken, pip
                );

                YodoRequest.getInstance().requestDailyHistory(
                        FareActivity.this,
                        hardwareToken, pip
                );
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                inputBox, true, true,
                remember,
                onClick
        );
    }

    /**
     * Shows some basic information about the POS
     * @param v View, not used
     */
    public void aboutClick(View v) {
        mSlidingLayout.closePane();

        final String title   = ((Button) v).getText().toString();
        final String message = getString( R.string.imei )       + " " +
                AppUtils.getHardwareToken( ac ) + "\n" +
                getString( R.string.version ) + "/" +
                RESTService.getSwitch() + "\n\n" +
                getString( R.string.about_message );

        LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this ), false );

        TextView emailView = (TextView) layout.findViewById( R.id.emailView );
        TextView messageView = (TextView) layout.findViewById( R.id.messageView );

        SpannableString email = new SpannableString( getString( R.string.about_email ) );
        email.setSpan( new UnderlineSpan(), 0, email.length(), 0 );

        emailView.setText( email );
        messageView.setText( message );

        emailView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_SEND );
                String[] recipients = { getString( R.string.about_email ) };
                intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                intent.putExtra( Intent.EXTRA_SUBJECT, hardwareToken );
                intent.setType( "text/html" );
                startActivity( Intent.createChooser( intent, "Send Email" ) );
            }
        });

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                layout
        );
    }

    public void feeSelectedClick(View fee) {
        if( mCurrentFee != null )
            mCurrentFee.setBackgroundColor( 0 );

        mCurrentFee = fee;
        mCurrentFee.setBackgroundColor( getResources().getColor( R.color.holo_blue_bright ) );
        View newZone = findViewById( R.id.oneZoneView );

        zoneSelectedClick( newZone );
    }

    public void zoneSelectedClick(View zone) {
        if( mCurrentZone != null )
            switchImage((ImageView) mCurrentZone, false);
        int tempZone = switchImage( (ImageView) zone, true );

        mCurrentZone = zone;

        mTotalFareView.setText(
                mCurrentTotal.add( new BigDecimal( getFare( tempZone ) ) )
                        .setScale( 2, RoundingMode.DOWN ).toString()
        );
    }

    /**
     * Resets the values to 0.00
     * @param v The View, not used
     */
    public void resetClick(View v) {
        mCurrentTotal = BigDecimal.ZERO;
        feeSelectedClick( findViewById( R.id.adultFeeView ) );
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    public void addClick(View v) {
        mCurrentTotal = new BigDecimal( mTotalFareView.getText().toString() );
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void yodoPayClick(View v) {
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.close();
            return;
        }

        if( v != null)
            AppUtils.rotateImage( v );

        currentScanner = QRScannerFactory.getInstance(
                this,
                (QRScannerFactory.SupportedScanners) mScannersSpinner.getSelectedItem()
        );

        if( currentScanner != null )
            currentScanner.startScan();
    }

    /**
     * Switch the image of the zone buttons
     * @param current The ImageView to change the state
     * @param selected The state
     */
    private Integer switchImage(ImageView current, boolean selected) {
        Integer result = null;

        switch( current.getId() ) {
            case R.id.oneZoneView:
                if( selected )
                    current.setImageResource( R.drawable.one_selected );
                else
                    current.setImageResource( R.drawable.one );

                result = AppConfig.ZONE_1;
                break;

            case R.id.twoZoneView:
                if( selected )
                    current.setImageResource( R.drawable.two_selected );
                else
                    current.setImageResource( R.drawable.two );

                result = AppConfig.ZONE_2;
                break;

            case R.id.threeZoneView:
                if( selected )
                    current.setImageResource( R.drawable.three_selected );
                else
                    current.setImageResource( R.drawable.three );

                result = AppConfig.ZONE_3;
                break;
        }
        return result;
    }

    private String getFare(int zone) {
        String result = null;

        switch( mCurrentFee.getId() ) {
            case R.id.oldFeeView:
                result = AppUtils.getOldFare( ac, zone );
                break;

            case R.id.adultFeeView:
                result = AppUtils.getAdultFare( ac, zone );
                break;

            case R.id.childFeeView:
                result = AppUtils.getChildFare( ac, zone );
                break;

            case R.id.studentFeeView:
                result = AppUtils.getStudentFare( ac, zone );
                break;
        }
        return result;
    }

    /**
     * Register/Unregister the broadcast receiver.
     */
    private void registerBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction( BroadcastMessage.ACTION_NEW_LOCATION );

        lbm.registerReceiver( mFareBroadcastReceiver, filter );
    }

    private void unregisterBroadcasts() {
        lbm.unregisterReceiver( mFareBroadcastReceiver );
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case QUERY_BAL_REQUEST:
                code = response.getCode();
                AppUtils.Logger( TAG, "History ");
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    historyData = response.getParams();
                    if( todayData != null ) {
                        AppUtils.Logger( TAG, "Entro en history ");
                        balanceDialog();
                    }
                } else if( code.equals( ServerResponse.ERROR_FAILED ) ) {
                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage();

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                    todayData = historyData = null;
                }
                break;

            case QUERY_DAY_REQUEST:
                code = response.getCode();
                AppUtils.Logger( TAG, "Today ");
                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    todayData = response.getParams();
                    if( historyData != null ) {
                        AppUtils.Logger( TAG, "Entro en today ");
                        balanceDialog();
                    }
                }
                break;

            case EXCH_MERCH_REQUEST:
            case ALT_MERCH_REQUEST:
                // Returns the selected fare to adult
                resetClick( null );
                // Handle the response message
                code = response.getCode();
                final String ex_authNumber = response.getAuthNumber();
                final String ex_message    = response.getMessage();
                final String ex_balance    = response.getParam( ServerResponse.BALANCE );

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.startSound( ac, AppConfig.SUCCESSFUL );
                    message = getString( R.string.exchange_auth ) + " " + ex_authNumber + "\n" +
                            getString( R.string.exchange_message ) + " " + ex_message;

                    if( ex_balance != null )
                        message += "\n" + getString( R.string.exchange_balance ) + " " +
                                new BigDecimal( ex_balance ).setScale( 2, RoundingMode.DOWN );

                    AlertDialogHelper.showAlertDialog( ac, response.getCode(), message, null );
                } else {
                    AppUtils.startSound( ac, AppConfig.ERROR );

                    Message msg = new Message();
                    msg.what = YodoHandler.SERVER_ERROR;
                    message  = response.getMessage() + "\n" + response.getParam( ServerResponse.PARAMS );

                    Bundle bundle = new Bundle();
                    bundle.putString( YodoHandler.CODE, code );
                    bundle.putString( YodoHandler.MESSAGE, message );
                    msg.setData( bundle );

                    handlerMessages.sendMessage( msg );
                }

                if( AppUtils.isLiveScan( ac ) )
                    yodoPayClick( null );

                break;
        }
    }

    @Override
    public void onNewData(String data) {
        String totalPurchase = mTotalFareView.getText().toString();

        final String[] currency = getResources().getStringArray( R.array.currency_array );

        switch( data.length() ) {
            case AppConfig.SKS_SIZE:
                YodoRequest.getInstance().createProgressDialog(
                        FareActivity.this,
                        YodoRequest.ProgressDialogType.TRANSPARENT
                );

                YodoRequest.getInstance().requestExchange(
                        FareActivity.this,
                        hardwareToken,
                        data,
                        totalPurchase,
                        "0.00",
                        "0.00",
                        location.getLatitude(),
                        location.getLongitude(),
                        currency[ AppUtils.getCurrency( ac ) ]
                );
                break;

            case AppConfig.ALT_SIZE:
                YodoRequest.getInstance().createProgressDialog(
                        FareActivity.this,
                        YodoRequest.ProgressDialogType.TRANSPARENT
                );

                YodoRequest.getInstance().requestAlternate(
                        FareActivity.this,
                        hardwareToken,
                        data,
                        totalPurchase,
                        "0.00",
                        "0.00",
                        location.getLatitude(),
                        location.getLongitude(),
                        currency[ AppUtils.getCurrency( ac ) ]
                );
                break;

            default:
                AppUtils.startSound( ac, AppConfig.ERROR );
                ToastMaster.makeText( FareActivity.this, R.string.exchange_error, Toast.LENGTH_SHORT ).show();

                if( AppUtils.isLiveScan( ac ) )
                    yodoPayClick( null );

                break;
        }
    }

    /**
     * The broadcast receiver for the location service, it will receive all the
     * updates from the location service and send it to the gateway.
     */
    private BroadcastReceiver mFareBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String action = i.getAction();
            AppUtils.Logger(TAG, ">> FareActivity >> Event");
			/* Broadcast: ACTION_NEW_LOCATION */
			/* ****************************** */
            if( action.equals( BroadcastMessage.ACTION_NEW_LOCATION ) ) {
                AppUtils.Logger(TAG, ">> FareActivity >> ACTION_NEW_LOCATION");

                Parcelable p = i.getParcelableExtra( BroadcastMessage.EXTRA_NEW_LOCATION );
                if( p != null && p instanceof Location ) {
                    location = (Location) p;
                    mLocationView.setImageResource( R.drawable.location );
                }
            }
        }
    };

    private class getCurrentBalance extends AsyncTask<String, String, JSONArray> {
        /** JSON Url */
        private String url = RESTService.getRoot() + "/yodo/currency/index.json";

        /** JSON Tags */
        private String TAG = "YodoCurrency";
        private String CURRENCY_TAG = "currency";
        private String RATE_TAG     = "rate";

        /** Currencies */
        private String[] currencies = ac.getResources().getStringArray( R.array.currency_array );
        private String URL_CURRENCY  = "EUR";
        //private String BASE_CURRENCY = "CAD";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            equivalentTotal = null;
        }

        @Override
        protected JSONArray doInBackground(String... arg0) {
            // instantiate our json parser
            JsonParser jParser = new JsonParser( ac );
            // get json string from url
            return jParser.getJSONFromUrl( url );
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            if( json != null ) {
                BigDecimal cad_currency = null, current_currency = null;
                for( int i = 0; i < json.length(); i++ ) {
                    try {
                        JSONObject temp = json.getJSONObject( i );
                        JSONObject c    = (JSONObject) temp.get( TAG );
                        String currency = (String) c.get( CURRENCY_TAG );
                        String rate     = (String) c.get( RATE_TAG );

                        if( currency.equals( currencies[ AppConfig.DEFAULT_CURRENCY ] ) )
                            cad_currency = new BigDecimal( rate );

                        if( currency.equals( currencies[ AppUtils.getCurrency( ac ) ]) )
                            current_currency = new BigDecimal( rate );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Get raw value, in order to transform
                BigDecimal temp_tender = new BigDecimal( mTotalFareView.getText().toString() );

                if( cad_currency != null ) {
                    if( URL_CURRENCY.equals( currencies[ AppUtils.getCurrency( ac ) ] ) ) {
                        equivalentTotal = temp_tender.multiply( cad_currency );
                    } else if( current_currency != null ) {
                        BigDecimal currency_rate = cad_currency.divide( current_currency, 2 );
                        equivalentTotal = temp_tender.multiply( currency_rate );
                    }

                    if( mPopupMessage.isShowing() ) {
                        mPopupMessage.getContentView().findViewById( R.id.progressBarPopUp ).setVisibility( View.GONE );
                        mPopupMessage.getContentView().findViewById( R.id.cashTotalText ).setVisibility( View.VISIBLE );
                    }
                }
            }
        }
    }
}

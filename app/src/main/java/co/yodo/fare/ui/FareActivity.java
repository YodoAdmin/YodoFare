package co.yodo.fare.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.component.SKS;
import co.yodo.fare.helper.AppConfig;
import co.yodo.fare.helper.GUIUtils;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.helper.SystemUtils;
import co.yodo.fare.manager.PromotionManager;
import co.yodo.fare.service.LocationService;
import co.yodo.fare.ui.adapter.ScannerAdapter;
import co.yodo.fare.ui.notification.AlertDialogHelper;
import co.yodo.fare.ui.notification.MessageHandler;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.fare.ui.notification.ToastMaster;
import co.yodo.fare.ui.option.AboutOption;
import co.yodo.fare.ui.option.BalanceOption;
import co.yodo.fare.ui.scanner.QRScannerFactory;
import co.yodo.fare.ui.scanner.contract.QRScanner;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.AlternateRequest;
import co.yodo.restapi.network.request.ExchangeRequest;

public class FareActivity extends AppCompatActivity implements
        ApiClient.RequestsListener,
        QRScanner.QRScannerListener,
        PromotionManager.IPromotionListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = FareActivity.class.getSimpleName();

    /** The context object */
    private Context ac;

    /** Hardware Token */
    private String mHardwareToken;

    /** Messages Handler */
    private MessageHandler mHandlerMessages;

    /** Manager for the server requests */
    @Inject
    ApiClient mRequestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper mProgressManager;

    /** GUI controllers */
    @BindView( R.id.splActivityFare )
    SlidingPaneLayout splActivityFare;

    @BindView( R.id.sScannerSelector )
    Spinner sScannerSelector;

    @BindView( R.id.ivLocationIcon )
    ImageView ivLocationIcon;

    @BindView( R.id.tvTotal )
    TextView tvTotal;

    @BindView( R.id.ivYodoGear )
    ImageView ivYodoGear;

    private View mCurrentFee;
    private View mCurrentZone;

    /** Options from the navigation window */
    private BalanceOption mBalanceOption;
    private AboutOption mAboutOption;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager mPromotionManager;
    private boolean isPublishing = false;

    /** Current Scanner */
    private QRScannerFactory mScannerFactory;
    private QRScanner mCurrentScanner;
    private boolean isScanning = false;

    /** Total to pay */
    private BigDecimal mCurrentTotal = BigDecimal.ZERO;

    /** Location */
    private Location mLocation = new Location( "flp" );

    /** Code for the error dialog */
    private static final int REQUEST_CODE_LOCATION_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA   = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    /** Response codes for the queries */
    private static final int EXCH_REQ = 0x00;
    private static final int ALT_REQ  = 0x01;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        PrefUtils.setLanguage( this );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_fare );

        setupGUI();
        updateData();
    }

    @Override
    public void onStart() {
        super.onStart();
        // register to event bus
        EventBus.getDefault().register( this );

        // Setup the required permissions
        LocationService.setup(
                this,
                PERMISSIONS_REQUEST_LOCATION,
                REQUEST_CODE_LOCATION_SERVICES
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the listener for the request (this activity)
        mRequestManager.setListener( this );

        // Start the scanner if necessary
        if( mCurrentScanner != null && isScanning ) {
            isScanning = false;
            mCurrentScanner.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the scanner while app is not focus (only if scanning)
        if( mCurrentScanner != null && mCurrentScanner.isScanning() ) {
            isScanning = true;
            mCurrentScanner.stopScan();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister from event bus
        EventBus.getDefault().unregister( this );
        // Stop location service while app is in background
        if( SystemUtils.isMyServiceRunning( ac, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( ac, LocationService.class );
            stopService( iLoc );
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerFactory.destroy();
    }

    @Override
    public void onBackPressed() {
        if( mCurrentScanner != null && mCurrentScanner.isScanning() ) {
            mCurrentScanner.stopScan();
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
        mHandlerMessages = new MessageHandler( FareActivity.this );

        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Setup promotion manager start it
        mPromotionManager = new PromotionManager( this );
        mPromotionManager.startService();

        // creates the factory for the scanners
        mScannerFactory = new QRScannerFactory( this );

        // Global options (navigation window)
        mBalanceOption = new BalanceOption( this, mHandlerMessages, mPromotionManager );
        mAboutOption   = new AboutOption( this );

        // Sliding Panel Configurations
        splActivityFare.setParallaxDistance( 30 );

        // Sets up the spinner, listeners, popup, and set currency
        initializeScannerSpinner();

        // Reset all the values
        resetClick( null );

        // Set the currency icon
        GUIUtils.setMerchantCurrencyIcon( ac, tvTotal );

        // If it is the first login, show the navigation panel
        if( PrefUtils.isFirstLogin( ac ) ) {
            splActivityFare.openPane();
            PrefUtils.saveFirstLogin( ac, false );
        }
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        // Get the saved hardware token
        mHardwareToken = PrefUtils.getHardwareToken( ac );
        if( mHardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.message_no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }
    }

    /**
     * Initializes the spinner for the scanners
     */
    private void initializeScannerSpinner() {
        // Add item listener to the spinner
        sScannerSelector.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parentView, View selectedItemView, int position, long id ) {
                TextView scanner = (TextView) selectedItemView;
                if( scanner != null )
                    SystemUtils.Logger( TAG, scanner.getText().toString() );
                PrefUtils.saveScanner( ac, position );
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
            }
        });

        // Create the adapter for the supported qr scanners
        ArrayAdapter<QRScannerFactory.SupportedScanner> adapter = new ScannerAdapter(
                this,
                android.R.layout.simple_list_item_1,
                QRScannerFactory.SupportedScanner.values()
        );

        // Set the current scanner
        sScannerSelector.setAdapter( adapter );
        sScannerSelector.setSelection( PrefUtils.getScanner( ac ) );
    }

    /**
     * Settings for the fee values
     * @param v View, not used
     */
    public void settingsClick( View v ) {
        splActivityFare.closePane();
        Intent intent = new Intent( ac, SettingsActivity.class );
        startActivity( intent );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void getBalanceClick( View v ) {
        splActivityFare.closePane();
        mBalanceOption.execute();
    }

    /**
     * Shows some basic information about the POS
     * @param v View, not used
     */
    public void aboutClick( View v ) {
        splActivityFare.closePane();
        mAboutOption.execute();
    }

    public void feeSelectedClick( View fee ) {
        if( mCurrentFee != null )
            mCurrentFee.setBackgroundColor( 0 );

        mCurrentFee = fee;
        mCurrentFee.setBackgroundColor( ContextCompat.getColor( ac, R.color.holo_blue_bright ) );
        View newZone = findViewById( R.id.oneZoneView );

        zoneSelectedClick( newZone );
    }

    public void zoneSelectedClick( View zone ) {
        if( mCurrentZone != null )
            switchImage( (ImageView) mCurrentZone, false );
        int tempZone = switchImage( (ImageView) zone, true );

        mCurrentZone = zone;
        final String currentFare = mCurrentTotal.add( new BigDecimal( getFare( tempZone ) ) )
                .setScale( 2, RoundingMode.DOWN ).toString();

        tvTotal.setText(
                currentFare
        );
    }

    /**
     * Resets the values to 0.00
     * @param v The View, not used
     */
    public void resetClick( View v ) {
        mCurrentTotal = BigDecimal.ZERO;
        feeSelectedClick( findViewById( R.id.adultFeeView ) );
    }

    /** Handle numeric add clicked
     *  @param v The View, used to get the amount
     */
    public void addClick( View v ) {
        mCurrentTotal = new BigDecimal( tvTotal.getText().toString() );
    }

    /**
     * Request the permission for the camera
     */
    private void showCamera() {
        if( mCurrentScanner != null && mCurrentScanner.isScanning() ) {
            mCurrentScanner.stopScan();
            return;
        }

        GUIUtils.rotateImage( ivYodoGear );
        mCurrentScanner = mScannerFactory.getScanner(
                (QRScannerFactory.SupportedScanner) sScannerSelector.getSelectedItem()
        );

        if( mCurrentScanner != null )
            mCurrentScanner.startScan();
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void yodoPayClick( View v ) {
        boolean cameraPermission = SystemUtils.requestPermission(
                FareActivity.this,
                R.string.message_permission_camera,
                Manifest.permission.CAMERA,
                PERMISSIONS_REQUEST_CAMERA
        );

        if( cameraPermission )
            showCamera();
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
                result = PrefUtils.getOldFare( ac, zone );
                break;

            case R.id.adultFeeView:
                result = PrefUtils.getAdultFare( ac, zone );
                break;

            case R.id.childFeeView:
                result = PrefUtils.getChildFare( ac, zone );
                break;

            case R.id.studentFeeView:
                result = PrefUtils.getStudentFare( ac, zone );
                break;
        }
        return result;
    }

    @Override
    public void onPrepare() {
        if( PrefUtils.isAdvertising( ac ) ) {
            isPublishing = true;
            mPromotionManager.unpublish();
        }
    }

    @Override
    public void onResponse( int responseCode, ServerResponse response ) {
        mProgressManager.destroyProgressDialog();
        String code, message;

        // If it was publishing before the request
        if( isPublishing ) {
            isPublishing = false;
            mPromotionManager.publish();
        }

        switch( responseCode ) {

            case EXCH_REQ:
            case ALT_REQ:
                // Returns the selected fare to adult
                resetClick( null );

                // Handle the response message
                code = response.getCode();
                final String ex_authNumber = response.getAuthNumber();
                final String ex_message    = response.getMessage();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    SystemUtils.startSound( ac, AppConfig.SUCCESSFUL );
                    message = getString( R.string.exchange_auth )    + " " + ex_authNumber + "\n" +
                              getString( R.string.exchange_message ) + " " + ex_message;

                    AlertDialogHelper.showAlertDialog( ac, response.getCode(), message, null );
                } else {
                    SystemUtils.startSound( ac, AppConfig.ERROR );
                    message = response.getMessage();
                    MessageHandler.sendMessage( mHandlerMessages, code, message );
                }

                if( PrefUtils.isLiveScan( ac ) )
                    showCamera();

                break;
        }
    }

    @Override
    public void onScanResult( String data ) {
        String total = tvTotal.getText().toString();
        SystemUtils.Logger( TAG, data );

        SKS code = SKS.build( data );
        if( code == null ) {
            SystemUtils.startSound( ac, AppConfig.ERROR );
            ToastMaster.makeText( this, R.string.exchange_error, Toast.LENGTH_LONG ).show();

            if( PrefUtils.isLiveScan( ac ) )
                showCamera();
        } else {
            final String client = code.getClient();
            final SKS.PAYMENT method = code.getPaymentMethod();

            mProgressManager.createProgressDialog(
                    FareActivity.this,
                    ProgressDialogHelper.ProgressDialogType.TRANSPARENT
            );

            switch( method ) {
                case YODO:
                    mRequestManager.invoke(
                            new ExchangeRequest(
                                    EXCH_REQ,
                                    mHardwareToken,
                                    client,
                                    total,
                                    "0.00",
                                    "0.00",
                                    mLocation.getLatitude(),
                                    mLocation.getLongitude(),
                                    PrefUtils.getMerchantCurrency( ac )
                            )
                    );
                    break;

                case HEART:
                    mRequestManager.invoke(
                            new AlternateRequest(
                                    ALT_REQ,
                                    String.valueOf( method.ordinal() ),
                                    mHardwareToken,
                                    client,
                                    total,
                                    "0.00",
                                    "0.00",
                                    mLocation.getLatitude(),
                                    mLocation.getLongitude(),
                                    PrefUtils.getMerchantCurrency( ac )
                            )
                    );
                    break;
            }
        }
    }

    @SuppressWarnings("unused") // it receives events from the Location Service
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onLocationEvent( Location location ) {
        // Remove Sticky Event
        EventBus.getDefault().removeStickyEvent( Location.class );
        // Process the Event
        mLocation = location;
        ivLocationIcon.setImageResource( R.drawable.location );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if( !SystemUtils.isLocationEnabled( ac ) )
                    finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch( requestCode ) {
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    showCamera();
                }
                break;

            case PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    LocationService.enable( this, REQUEST_CODE_LOCATION_SERVICES );
                } else {
                    // Permission Denied
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        }
    }

    @Override
    public void onConnected( @Nullable Bundle bundle ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connected" );
        if( PrefUtils.isAdvertising( ac ) )
            mPromotionManager.publish();
    }

    @Override
    public void onConnectionSuspended( int i ) {
        SystemUtils.Logger( TAG, "GoogleApiClient connection suspended" );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult result ) {
        SystemUtils.Logger( TAG, "connection to GoogleApiClient failed" );
    }
}

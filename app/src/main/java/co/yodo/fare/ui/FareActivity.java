package co.yodo.fare.ui;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
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
import android.widget.LinearLayout;
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
import co.yodo.fare.utils.ErrorUtils;
import co.yodo.fare.utils.ImageUtils;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.component.SKS;
import co.yodo.fare.helper.BluetoothPrinterUtil;
import co.yodo.fare.helper.ESCUtil;
import co.yodo.fare.helper.FormatUtils;
import co.yodo.fare.helper.GUIUtils;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.helper.SystemUtils;
import co.yodo.fare.manager.PromotionManager;
import co.yodo.fare.service.LocationService;
import co.yodo.fare.ui.adapter.ScannerAdapter;
import co.yodo.fare.ui.notification.AlertDialogHelper;
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
import timber.log.Timber;

public class FareActivity extends AppCompatActivity implements
        QRScanner.QRScannerListener,
        PromotionManager.IPromotionListener {
    /** DEBUG */
    @SuppressWarnings( "unused" )
    private static final String TAG = FareActivity.class.getSimpleName();

    /** The application context object */
    @Inject
    Context context;

    /** Manager for the server requests */
    @Inject
    ApiClient requestManager;

    /** Progress dialog for the requests */
    @Inject
    ProgressDialogHelper progressManager;

    /** GUI controllers */
    @BindView( R.id.text_total )
    TextView tvTotal;

    @BindView( R.id.image_yodo_gear )
    ImageView ivYodoGear;

    @BindView( R.id.layout_adult_fee )
    LinearLayout llAdultFee;

    @BindView( R.id.image_zone_one )
    ImageView ivZoneOne;

    @BindView( R.id.layout_fare )
    SlidingPaneLayout splFare;

    @BindView( R.id.spinner_scanner_selector )
    Spinner sScannerSelector;

    @BindView( R.id.image_location )
    ImageView ivLocationIcon;

    @BindView( R.id.text_route_number )
    TextView tvRouteNumber;

    /** Hardware Token */
    private String hardwareToken;

    /** Current selections */
    private View currentFee;
    private View currentZone;

    /** Options from the navigation window */
    private BalanceOption balanceOption;
    private AboutOption aboutOption;

    /** Current Scanner */
    private QRScannerFactory scannerFactory;
    private QRScanner currentScanner;
    private boolean isScanning = false;

    /** Handles the start/stop subscribe/unsubscribe functions of Nearby */
    private PromotionManager promotionManager;
    private boolean isPublishing = false;

    /** Total to pay */
    private BigDecimal currentTotal = BigDecimal.ZERO;

    /** Location */
    private Location location = new Location( "flp" );

    /** Code for the error dialog */
    private static final int REQUEST_CODE_LOCATION_SERVICES = 0;

    /** Request codes for the permissions */
    private static final int PERMISSIONS_REQUEST_CAMERA   = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        PrefUtils.setLanguage( this );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
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
        // Start the scanner if necessary
        if( currentScanner != null && isScanning ) {
            isScanning = false;
            currentScanner.startScan();
        }

        // Set route
        tvRouteNumber.setText( getString( R.string.text_route_number, PrefUtils.getBusRoute( context ) ) );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the scanner while app is not focus (only if scanning)
        if( currentScanner != null && currentScanner.isScanning() ) {
            isScanning = true;
            currentScanner.stopScan();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister from event bus
        EventBus.getDefault().unregister( this );

        // Stop location service while app is in background
        if( SystemUtils.isMyServiceRunning( context, LocationService.class.getName() ) ) {
            Intent iLoc = new Intent( context, LocationService.class );
            stopService( iLoc );
        }
    }

    @Override
    public void onBackPressed() {
        // If we are scanning, first close the camera
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.stopScan();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Initialized all the GUI main components
     */
    private void setupGUI() {
        // Injection
        ButterKnife.bind( this );
        YodoApplication.getComponent().inject( this );

        // Sliding Panel Configurations
        splFare.setParallaxDistance( 30 );

        // Set the currency icon
        GUIUtils.setMerchantCurrencyIcon( context, tvTotal );

        // Reset all the values
        reset( null );

        // Global options (navigation window)
        aboutOption = new AboutOption( this );
        balanceOption = new BalanceOption( this, promotionManager );

        // Sets up the spinner for the cameras
        initializeScannerSpinner();

        // creates the factory for the scanners
        scannerFactory = new QRScannerFactory( this );

        // Setup promotion manager start it
        promotionManager = new PromotionManager( this );
        promotionManager.startService();

        // If it is the first login, show the navigation panel
        if( PrefUtils.isFirstLogin( context ) ) {
            splFare.openPane();
            PrefUtils.saveFirstLogin( context, false );
        }
    }

    /**
     * Set-up the basic information
     */
    private void updateData() {
        // Get the saved hardware token
        hardwareToken = PrefUtils.getHardwareToken();
        if( hardwareToken == null ) {
            ToastMaster.makeText( context, R.string.error_hardware, Toast.LENGTH_LONG ).show();
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
                PrefUtils.saveScanner( context, position );
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
        sScannerSelector.setSelection( PrefUtils.getScanner( context ) );
    }

    /**
     * Resets the values to 0.00 and default options
     * @param v The View, not used
     */
    public void reset( View v ) {
        currentTotal = BigDecimal.ZERO;
        feeSelection( llAdultFee );
    }

    /**
     * Settings for the fee values
     * @param v View, not used
     */
    public void settings( View v ) {
        splFare.closePane();
        Intent intent = new Intent( context, SettingsActivity.class );
        startActivity( intent );
    }

    /**
     * Gets the balance of the POS
     * @param v View, not used
     */
    public void balance( View v ) {
        splFare.closePane();
        balanceOption.execute();
    }

    /**
     * Shows some basic information aboutOption the POS
     * @param v View, not used
     */
    public void about( View v ) {
        splFare.closePane();
        aboutOption.execute();
    }

    /**
     * Action to select a current fee type
     * @param fee, The view of the new fee
     */
    public void feeSelection( View fee ) {
        if( currentFee != null ) {
            currentFee.setBackgroundColor( ContextCompat.getColor( context, R.color.colorInnerLayout ) );
        }

        currentFee = fee;
        currentFee.setBackgroundColor( ContextCompat.getColor( context, R.color.colorSelectedFee ) );
        zoneSelection( ivZoneOne );
    }

    /**
     * Action to select a current fee zone
     * @param zone, The view of the new zone
     */
    public void zoneSelection( View zone ) {
        if( currentZone != null ) {
            ImageUtils.handleFeeZone( (ImageView) currentZone, false );
        }

        currentZone = zone;
        final String fare = PrefUtils.getFare(
                context,
                currentFee,
                ImageUtils.handleFeeZone( (ImageView) zone, true )
        );

        // Add values to the fare view
        final String currentFare = currentTotal.add(
                new BigDecimal( fare )
        ).setScale( 2, RoundingMode.DOWN ).toString();

        tvTotal.setText( currentFare );
    }

    /**
     * Handle numeric add clicked
     * @param v The View, used to get the amount
     */
    public void addValue( View v ) {
        currentTotal = new BigDecimal( tvTotal.getText().toString() );
    }

    /**
     * Opens the scanner to realize a payment
     * @param v The View, not used
     */
    public void makePayment( View v ) {
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
     * Request the permission for the camera
     */
    private void showCamera() {
        // Rotate the yodo year icon
        GUIUtils.rotateImage( ivYodoGear );

        // Process the scanner
        if( currentScanner != null && currentScanner.isScanning() ) {
            currentScanner.stopScan();
        } else {
            currentScanner = scannerFactory.getScanner(
                    ( QRScannerFactory.SupportedScanner ) sScannerSelector.getSelectedItem()
            );
            currentScanner.startScan();
        }
    }

    @Override
    public void onScanResult( String data ) {
        // Log the data
        Timber.i( data );
        String total = tvTotal.getText().toString();

        SKS code = SKS.build( data );
        if( code == null ) {
            SystemUtils.startSound( context, SystemUtils.ERROR );
            ToastMaster.makeText( this, R.string.exchange_error, Toast.LENGTH_LONG ).show();
        } else {
            final String client = code.getClient();
            final SKS.PAYMENT method = code.getPaymentMethod();

            progressManager.create(
                    FareActivity.this,
                    ProgressDialogHelper.ProgressDialogType.TRANSPARENT
            );

            switch( method ) {
                case YODO:
                    requestManager.invoke(
                        new ExchangeRequest(
                                hardwareToken,
                                client,
                                total,
                                "0.00",
                                "0.00",
                                location.getLatitude(),
                                location.getLongitude(),
                                PrefUtils.getMerchantCurrency()
                        ), callback
                    );
                    break;

                case STATIC:
                case HEART:
                    requestManager.invoke(
                        new AlternateRequest(
                                String.valueOf( method.ordinal() ),
                                hardwareToken,
                                client,
                                total,
                                "0.00",
                                "0.00",
                                location.getLatitude(),
                                location.getLongitude(),
                                PrefUtils.getMerchantCurrency()
                        ), callback
                    );
                    break;

                default:
                    progressManager.destroy();
                    ErrorUtils.handleError(
                            FareActivity.this,
                            R.string.error_sks,
                            false
                    );
                    break;
            }
        }
    }

    @Override
    public void onConnected( @Nullable Bundle bundle ) {
        Timber.i( "GoogleApiClient connected" );
        if( PrefUtils.isAdvertising( context ) )
            promotionManager.publish();
    }

    @Override
    public void onConnectionSuspended( int i ) {
        Timber.i( "GoogleApiClient connection suspended" );
    }

    @Override
    public void onConnectionFailed( @NonNull ConnectionResult result ) {
        Timber.i( "connection to GoogleApiClient failed" );
    }

    @SuppressWarnings("unused") // it receives events from the Location Service
    @Subscribe( sticky = true, threadMode = ThreadMode.MAIN )
    public void onLocationEvent( Location location ) {
        // Remove Sticky Event
        EventBus.getDefault().removeStickyEvent( Location.class );
        // Process the Event
        this.location = location;
        ivLocationIcon.setImageResource( R.mipmap.location );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch( requestCode ) {
            case REQUEST_CODE_LOCATION_SERVICES:
                // The user didn't enable the GPS
                if( !SystemUtils.isLocationEnabled( context ) )
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

    private final ApiClient.RequestCallback callback = new ApiClient.RequestCallback() {
        @Override
        public void onPrepare() {
            if( PrefUtils.isAdvertising( context ) ) {
                isPublishing = true;
                promotionManager.unpublish();
            }
        }

        @Override
        public void onResponse( ServerResponse response ) {
            progressManager.destroy();
            reset( null );

            final String code = response.getCode();
            String info;
            if( code.equals( ServerResponse.AUTHORIZED ) ) {
                SystemUtils.startSound( context, SystemUtils.SUCCESSFUL );

                // Get the response values
                final String authNumber = response.getAuthNumber();
                final String message = response.getMessage();
                String balance = response.getParams().getAccountBalance();

                info = getString( R.string.exchange_auth ) + " " + authNumber + "\n" +
                        getString( R.string.exchange_message ) + " " + message;

                if( balance != null ) {
                    info += "\n" + getString( R.string.exchange_balance ) + " " +
                            FormatUtils.truncateDecimal( balance );
                } else {
                    balance = getString( R.string.error_no_item );
                }

                final BluetoothDevice printer = BluetoothPrinterUtil.getDevice();
                if( printer != null ) {
                    // 2: Get the cash values
                    final String total = tvTotal.getText().toString();

                    // 3: Generate a order data
                    byte[] data = ESCUtil.parseData(
                            response,
                            total,
                            balance
                    );

                    // 4: Using InnerPrinter print data
                    BluetoothPrinterUtil.printData( printer, data );
                }
            } else {
                SystemUtils.startSound( context, SystemUtils.ERROR );
                switch( code ) {
                    case ServerResponse.ERROR_DUP_AUTH:
                        info = getString( R.string.error_20 );
                        break;

                    case ServerResponse.ERROR_NO_BALANCE:
                        info = getString( R.string.error_21 );
                        break;

                    default:
                        info = getString( R.string.error_unknown );
                        break;
                }
            }

            AlertDialogHelper.create( FareActivity.this, info );

            // If it was publishing before the request
            if( isPublishing ) {
                isPublishing = false;
                promotionManager.publish();
            }

            if( PrefUtils.isLiveScan( context ) ) {
                showCamera();
            }
        }

        @Override
        public void onError( Throwable error ) {
            progressManager.destroy();
            ErrorUtils.handleApiError(
                    FareActivity.this,
                    error,
                    false
            );
        }
    };
}

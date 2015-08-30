package co.yodo.fare.scanner;

import android.app.Activity;
import android.hardware.Camera;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import co.yodo.fare.R;

/**
 * Created by luis on 10/07/15.
 * class that implements a handler for the zxing
 * scanner
 */
public class ZxingScanner extends QRScanner {
    /** DEBUG */
    public static final String TAG = "ZxingScanner";

    private IntentIntegrator integrator;

    /** GUI Controllers */
    private CompoundBarcodeView preview;
    private TableLayout opPanel;
    private RelativeLayout pvPanel;

    /** Camera Flags */
    private boolean previewing = false;

    /** Instance */
    private static volatile ZxingScanner instance = null;

    private ZxingScanner(Activity activity) {
        super( activity );

        preview = (CompoundBarcodeView) act.findViewById( R.id.barcode_scanner );
        opPanel = (TableLayout) act.findViewById( R.id.operationsPanel );
        pvPanel = (RelativeLayout) act.findViewById( R.id.previewPanel );

        integrator = new IntentIntegrator( act );
        integrator.setDesiredBarcodeFormats( IntentIntegrator.QR_CODE_TYPES  );
        integrator.setPrompt( "" );
        integrator.setBeepEnabled( true );

        preview.decodeContinuous( new BarcodeCallback() {
            @Override
            public void barcodeResult( BarcodeResult result ) {
                if( result.getText() != null ) {
                    String trimmed = result.getText().replaceAll( "\\s+", "" );
                    //AppUtils.Logger( "QRCODE", result.getText() + " - " + result.getText().length() );
                    //AppUtils.Logger( "QRCODE", trimmed + " - " + trimmed.length() );
                    releaseCamera();
                    listener.onNewData( trimmed );
                }
            }

            @Override
            public void possibleResultPoints( List<ResultPoint> resultPoints ) {
            }
        } );
    }

    public static ZxingScanner getInstance(Activity activity) {
        synchronized( ZxingScanner.class ) {
            if( instance == null )
                instance = new ZxingScanner( activity );
        }
        return instance;
    }

    public static ZxingScanner getInstance() {
        return instance;
    }

    @Override
    public void startScan() {
        if( !previewing ) {
            preview.initializeFromIntent( integrator.createScanIntent() );
            preview.resume();

            opPanel.setVisibility( View.GONE );
            pvPanel.setVisibility( View.VISIBLE );

            previewing = true;
        }
    }

    @Override
    public void close() {
        releaseCamera();
    }

    @Override
    public boolean isScanning() {
        return previewing;
    }

    @Override
    public void setFrontFaceCamera( boolean frontFacing ) {
        if( frontFacing )
            integrator.setCameraId( Camera.CameraInfo.CAMERA_FACING_FRONT );
        else
            integrator.setCameraId( Camera.CameraInfo.CAMERA_FACING_BACK );
    }

    @Override
    public void destroy() {
        releaseCamera();
        instance = null;
    }

    private void releaseCamera() {
        preview.pause();

        opPanel.setVisibility( View.VISIBLE );
        pvPanel.setVisibility( View.GONE );

        previewing = false;
    }

}
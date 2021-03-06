package co.yodo.fare.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import co.yodo.fare.R;
import co.yodo.fare.helper.PrefUtils;

public class HardwareScanner extends QRScanner {
	/** DEBUG */
	@SuppressWarnings( "unused" )
	private static final String TAG = HardwareScanner.class.getSimpleName();
	
	/** GUI Controllers */
	private AlertDialog inputDialog;

	public HardwareScanner( Activity activity ) {
		super( activity );
		
		AlertDialog.Builder builder = new AlertDialog.Builder( activity );
		final EditText input        = new EditText( activity );
		
		input.setOnKeyListener( new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Prevent adding new line
	            if( event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER )
	            	return true;
	            
				if( event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER ) {
					String scanData = input.getText().toString();

					inputDialog.dismiss();

					PrefUtils.Logger(TAG, scanData);
					PrefUtils.hideSoftKeyboard( act );

					if( listener != null )
                        listener.onNewData( scanData );

					input.setText( "" );
					return true;
	            }
				return false;
			}
        	    });
		
		builder.setTitle( activity.getString( R.string.barcode_scanner ) );
        builder.setIcon( R.drawable.ic_launcher );
		builder.setView( input );
		builder.setNegativeButton( activity.getString(R.string.cancel), null );

        inputDialog = builder.create();
        inputDialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
	}

	@Override
	public void setFrontFaceCamera( boolean frontFacing ) {}
	
	@Override
	public void startScan() {
        inputDialog.show();
	}

    @Override
    public void stopScan() {
        inputDialog.dismiss();
    }
	
	@Override
	public boolean isScanning() {
		return inputDialog.isShowing();
	}

    @Override
	public void destroy() {
		super.destroy();
		// Set to null all the variables
        inputDialog.dismiss();
        inputDialog = null;
	}
}

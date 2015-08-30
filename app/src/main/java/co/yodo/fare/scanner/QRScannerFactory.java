package co.yodo.fare.scanner;

import android.app.Activity;

public class QRScannerFactory {
	public enum SupportedScanners {
		Hardware   ( "Barcode Scanner" ),
		SoftScanner1( "Camera Front" ),
        SoftScanner2( "Camera Back" );
		
		private String value;
        public static final long length = values().length;
		
		SupportedScanners(String value) {
			this.value = value;
		}
		
		@Override 
		public String toString() {
		    return value;
		}
	}
	
	public static QRScanner getInstance(Activity activity, SupportedScanners scanner) {
		QRScanner qrscanner = null;

		switch( scanner ) {
			case Hardware:
				qrscanner = HardwareScanner.getInstance( activity );
			break;
			
			case SoftScanner1:
                /*if( AppUtils.getScannerEngine( activity ).equals( ZBarScanner.TAG  ) )
                    qrscanner = ZBarScanner.getInstance( activity );
                else*/
                    qrscanner = ZxingScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( true );
			break;

            case SoftScanner2:
                /*if( AppUtils.getScannerEngine( activity ).equals( ZBarScanner.TAG  ) )
                    qrscanner = ZBarScanner.getInstance( activity );
                else*/
                    qrscanner = ZxingScanner.getInstance( activity );
                qrscanner.setFrontFaceCamera( false );
            break;
		}

        if( qrscanner != null && activity instanceof QRScannerListener )
            qrscanner.setListener( (QRScannerListener) activity );

		return qrscanner;
	}
	
	public static void destroy() {
        QRScanner qrscanner = HardwareScanner.getInstance();
		
		if( qrscanner != null )
			qrscanner.destroy();
		
		qrscanner = ZBarScanner.getInstance();
		
		if( qrscanner != null )
			qrscanner.destroy();
	}
}

package co.yodo.fare.ui.scanner;

import android.app.Activity;

import co.yodo.fare.R;
import co.yodo.fare.ui.scanner.contract.QRScanner;

public class QRScannerFactory {
	/** SKS Sizes - key 1024 */
	//public static final int SKS_SIZE = 256;
	//public static final int ALT_SIZE = 257;

	/** SKS Sizes - key 512 */
	public static final int SKS_SIZE = 128;
	public static final int ALT_SIZE = 129;

	public enum SupportedScanner {
		Hardware    ( R.string.text_scanner_hardware ),
		CameraFront ( R.string.text_scanner_software_front ),
		CameraBack  ( R.string.text_scanner_software_back );

		private int value;
		public static final long length = values().length;

		SupportedScanner( int value ) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/** QR Scanners */
	private final HardwareScanner hardwareScanner;
	private final ScanditScanner softwareScanner;

	public QRScannerFactory( Activity activity ) throws ClassCastException {
		if( activity instanceof QRScanner.QRScannerListener ) {
			hardwareScanner = new HardwareScanner( activity );
			softwareScanner = new ScanditScanner( activity );
			hardwareScanner.setListener( (QRScanner.QRScannerListener) activity );
			softwareScanner.setListener( (QRScanner.QRScannerListener) activity );
		} else {
			throw new ClassCastException( activity.getLocalClassName() + " does not implement the QRScannerListener" );
		}
	}

	/**
	 * Gets the requested scanner
	 * @param scanner Could be hardware, front and back software
	 * @return The QR scanner
	 */
	public QRScanner getScanner( SupportedScanner scanner ) {
		switch( scanner ) {
			case CameraFront:
				softwareScanner.setFrontFaceCamera( true );
				return softwareScanner;

			case CameraBack:
				softwareScanner.setFrontFaceCamera( false );
				return softwareScanner;

			default:
				return hardwareScanner;
		}
	}
}
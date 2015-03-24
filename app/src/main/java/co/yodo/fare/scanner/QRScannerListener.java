package co.yodo.fare.scanner;

public interface QRScannerListener {
    /**
     * Listener for the data of the scanner
     * @param data String data received
     */
	public void onNewData(String data);
}

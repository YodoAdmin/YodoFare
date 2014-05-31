package co.yodo.pos.fare.helper;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class HardwareToken {
	private Context _context;
	
	public HardwareToken(Context _context) {
		this._context = _context;
	}
	
	/**
	 * Gets the imei or the mac of the mobile
	 */
	public String getToken() {
		TelephonyManager telephonyManager = (TelephonyManager)_context.getSystemService(Context.TELEPHONY_SERVICE);
		String HARDWARE_TOKEN = telephonyManager.getDeviceId();
		
		if(HARDWARE_TOKEN == null) {
			WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
			if(!wifiManager.isWifiEnabled()) {
				return null;
			}
			
			WifiInfo wifiInf = wifiManager.getConnectionInfo();
			String tempMAC = wifiInf.getMacAddress();
			HARDWARE_TOKEN = tempMAC.replaceAll(":", "");
		}
		return HARDWARE_TOKEN;
	}
}

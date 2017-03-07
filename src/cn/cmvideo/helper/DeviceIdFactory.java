package cn.cmvideo.helper;

import java.util.UUID;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceIdFactory 
{
	
	public static final String TAG 	= "DeviceIdFactory";

	public static String getDeviceId (Context context) 
	{

		final TelephonyManager tm 	= (TelephonyManager) context.getSystemService (Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice 	= "" + tm.getDeviceId();
		tmSerial 	= "" + tm.getSimSerialNumber();
		androidId 	= "" + android.provider.Settings.Secure.getString( context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		
		String formatStr 	= String.format ("deviceId=%s, serialNumber=%s, androidId=%s", tmDevice, tmSerial, androidId);
		
		Log.d (TAG, formatStr); 

		UUID deviceUuid 	= new UUID (androidId.hashCode (), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode ());
		String uniqueId 	= deviceUuid.toString ();
		
		Log.d (TAG, "deviceUuid=" + deviceUuid); 

		return uniqueId;
	}
}

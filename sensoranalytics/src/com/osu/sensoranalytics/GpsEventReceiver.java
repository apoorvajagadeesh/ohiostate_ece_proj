package com.osu.sensoranalytics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GpsEventReceiver extends BroadcastReceiver {

	public static final String GPS_ENABLED_CHANGE = "android.location.GPS_ENABLED_CHANGE";
	public static final String GPS_FIX_CHANGE = "android.location.GPS_FIX_CHANGE";
	public static final String AGPS_DATA_EXPIRED = "com.osu.sensoranalytics.AGPS_DATA_EXPIRED";

	public static final String LOCATION_UPDATE_RECEIVED = "com.osu.sensoranalytics.LOCATION_UPDATE_RECEIVED";
	public static final long MILLIS_PER_DAY = 86400000;
	
	private static Intent mAgpsIntent = new Intent(AGPS_DATA_EXPIRED);
	private static Intent mLocationIntent = new Intent(LOCATION_UPDATE_RECEIVED);
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		

		Set<String> fallbackUpdateNetworks = new HashSet<String>();
		if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_UPDATE_WIFI, false)) {
			fallbackUpdateNetworks.add(SettingsActivity.KEY_PREF_UPDATE_NETWORKS_WIFI);
		}
		Set<String> updateNetworks = sharedPref.getStringSet(SettingsActivity.KEY_PREF_UPDATE_NETWORKS, fallbackUpdateNetworks);
		
		if (intent.getAction().equals(GPS_ENABLED_CHANGE) || intent.getAction().equals(GPS_ENABLED_CHANGE)) {
			boolean notifyFix = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_FIX, false);
			boolean notifySearch = sharedPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFY_SEARCH, false);
			if (notifyFix || notifySearch) {
				boolean isRunning = false;
				ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if (PasvLocListenerService.class.getName().equals(service.service.getClassName())) {
						isRunning = true;
					}
				}
				if (!isRunning) {
					Intent startServiceIntent = new Intent(context, PasvLocListenerService.class);
					startServiceIntent.setAction(intent.getAction());
					startServiceIntent.putExtras(intent.getExtras());
					context.startService(startServiceIntent);
				}
			}
		} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) 
				&& updateNetworks.contains(SettingsActivity.KEY_PREF_UPDATE_NETWORKS_WIFI)) {
			NetworkInfo netinfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (netinfo == null) return;
			if (!netinfo.isConnected()) return;

			Log.i(this.getClass().getSimpleName(), "WiFi is connected");
			refreshAgps(context, true, false);
		} else if ((intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION )) ||
				(intent.getAction().equals(AGPS_DATA_EXPIRED))) {
			boolean isAgpsExpired = false;
			if (intent.getAction().equals(AGPS_DATA_EXPIRED)) {
				Log.i(this.getClass().getSimpleName(), "AGPS data expired, checking available networks");
				isAgpsExpired = true;
			}
			NetworkInfo netinfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			if (netinfo == null) return;
			if (!netinfo.isConnected()) return;
			String type;
			if ((netinfo.getType() < ConnectivityManager.TYPE_MOBILE_MMS) || (netinfo.getType() > ConnectivityManager.TYPE_MOBILE_HIPRI)) {
				type = Integer.toString(netinfo.getType());
			} else {

				type = SettingsActivity.KEY_PREF_UPDATE_NETWORKS_MOBILE;
			}
			if (!updateNetworks.contains(type)) return;
			if (!isAgpsExpired)
				Log.i(this.getClass().getSimpleName(), "Network of type " + netinfo.getTypeName() + " is connected");

			refreshAgps(context, !isAgpsExpired, false);
		}
	}
	

	static void refreshAgps(Context context, boolean enforceInterval, boolean wantFeedback) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		long last = sharedPref.getLong(SettingsActivity.KEY_PREF_UPDATE_LAST, 0);
		long freqDays = Long.parseLong(sharedPref.getString(SettingsActivity.KEY_PREF_UPDATE_FREQ, "0"));
		long now = System.currentTimeMillis();
		if (enforceInterval && (last + freqDays * MILLIS_PER_DAY > now)) return;
		//Log.d(GpsEventReceiver.class.getSimpleName(), String.format("refreshAgps, enforceInterval: %b, wantFeedback: %b", enforceInterval, wantFeedback));
		
		new AgpsUpdateTask(wantFeedback).execute(context, mAgpsIntent, sharedPref, freqDays * MILLIS_PER_DAY);
	}
	
	private static class AgpsUpdateTask extends AsyncTask<Object, Void, Integer> {
		Context mContext;
		boolean mWantFeedback = false;
		
		public AgpsUpdateTask(boolean wantFeedback) {
			super();
			mWantFeedback = wantFeedback;
		}


		@Override
		protected Integer doInBackground(Object... args) {
			mContext = (Context) args[0];
			Intent agpsIntent = (Intent) args[1];
			SharedPreferences sharedPref = (SharedPreferences) args[2];
			long freqMillis = (Long) args[3];
			
			int nc = WifiCapabilities.getNetworkConnectivity();
			if (nc == WifiCapabilities.NETWORK_CAPTIVE_PORTAL) {
				Log.i(GpsEventReceiver.class.getSimpleName(), "Captive portal detected, cannot update AGPS data");
				return nc;
			} else if (nc == WifiCapabilities.NETWORK_ERROR) {
				Log.i(GpsEventReceiver.class.getSimpleName(), "No network available, cannot update AGPS data");
				return nc;
			}
			
			AlarmManager alm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, agpsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alm.cancel(pi);

			SharedPreferences.Editor spEditor = sharedPref.edit();
			spEditor.putLong(SettingsActivity.KEY_PREF_UPDATE_LAST, System.currentTimeMillis());
			LocationManager locman = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			List<String> allProviders = locman.getAllProviders();
			PendingIntent tempIntent = PendingIntent.getBroadcast(mContext, 0, mLocationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			Log.i(GpsEventReceiver.class.getSimpleName(), "Requesting AGPS data update");
			if (allProviders.contains(LocationManager.GPS_PROVIDER))
				locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, tempIntent);
			locman.sendExtraCommand("gps", "force_xtra_injection", null);
			locman.sendExtraCommand("gps", "force_time_injection", null);
			locman.removeUpdates(tempIntent);
			spEditor.commit();
			
			if (freqMillis > 0) {
				long next = System.currentTimeMillis() + freqMillis;
				alm.set(AlarmManager.RTC, next, pi);
				Log.i(GpsEventReceiver.class.getSimpleName(), String.format("Next update due %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS (after %2$d ms)", next, freqMillis));
			}

			return nc;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if ((mContext == null) || !mWantFeedback) return;
			String message = "";
			switch (result) {
			case WifiCapabilities.NETWORK_AVAILABLE:
				message = mContext.getString(R.string.status_agps);
				break;
			case WifiCapabilities.NETWORK_CAPTIVE_PORTAL:
				message = mContext.getString(R.string.status_agps_captive);
				break;
			case WifiCapabilities.NETWORK_ERROR:
				message = mContext.getString(R.string.status_agps_error);
				break;
			}
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}

	}


}

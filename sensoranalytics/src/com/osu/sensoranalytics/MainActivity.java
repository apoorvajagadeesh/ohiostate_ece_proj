package com.osu.sensoranalytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CheckBox;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.LayerManager;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.util.MapViewProjection;


import com.osu.sensoranalytics.R;
import com.osu.sensoranalytics.mapsforge.PersistentTileCache;
import com.osu.sensoranalytics.widgets.GpsSnrView;
import com.osu.sensoranalytics.widgets.GpsStatusView;




public class MainActivity extends FragmentActivity implements ActionBar.TabListener, GpsStatus.Listener, LocationListener, OnSharedPreferenceChangeListener, SensorEventListener, ViewPager.OnPageChangeListener {

	public static boolean isHeartTabSelected = false;
	public static Camera camera = null;
	public static double EARTH_CIRCUMFERENCE = 40000000;
	private static final int STYLE_MARKER = 0;
	private static final int STYLE_STROKE = 1;
	private static final int STYLE_FILL = 2;
	private static final String[] LOCATION_PROVIDER_STYLES = {
			"location_provider_blue",
			"location_provider_green",
			"location_provider_orange",
			"location_provider_purple",
			"location_provider_red"
	};

	private static final String LOCATION_PROVIDER_BLUE = "location_provider_blue";

	private static final String LOCATION_PROVIDER_RED = "location_provider_red";

	private static final String LOCATION_PROVIDER_GRAY = "location_provider_gray";

	private static final String KEY_LOCATION_STALE = "isStale";

	private static List<String> mAvailableProviderStyles;


	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	boolean isStopped;

	boolean isWideScreen;

	private static final int iSensorRate = 3000000;

	private static LocationManager mLocationManager;
	private SensorManager mSensorManager;
	private Sensor mOrSensor;
	private Sensor mAccSensor;
	private Sensor mGyroSensor;
	private Sensor mMagSensor;
	private Sensor mLightSensor;
	private Sensor mProximitySensor;
	private Sensor mPressureSensor;
	private Sensor mHumiditySensor;
	private Sensor mTempSensor;

	private byte mAccSensorRes = 3;
	private byte mGyroSensorRes = 4;
	private byte mMagSensorRes = 2;
	private byte mLightSensorRes = 1;
	private byte mProximitySensorRes = 1;
	private byte mPressureSensorRes = 0;
	private byte mHumiditySensorRes = 0;
	private byte mTempSensorRes = 1;

	private long mOrLast = 0;
	private long mAccLast = 0;
	private long mGyroLast = 0;
	private long mMagLast = 0;
	private long mLightLast = 0;
	private long mProximityLast = 0;
	private long mPressureLast = 0;
	private long mHumidityLast = 0;
	private long mTempLast = 0;

	private static ConnectivityManager mConnectivityManager;
	private static WifiManager mWifiManager;


	protected static boolean isGpsViewReady = false;
	protected static LinearLayout gpsRootLayout;
	protected static GpsStatusView gpsStatusView;
	protected static GpsSnrView gpsSnrView;
	protected static TextView gpsLat;
	protected static TextView gpsLon;
	protected static TextView orDeclination;
	protected static TextView gpsSpeed;
	protected static TextView gpsAlt;
	protected static TextView gpsTime;
	protected static TextView gpsBearing;
	protected static TextView gpsAccuracy;
	protected static TextView gpsOrientation;
	protected static TextView gpsSats;
	protected static TextView gpsTtff;

	protected static boolean isSensorViewReady = false;
	protected static TextView accStatus;
	protected static TextView accHeader;
	protected static TextView accTotal;
	protected static TextView accX;
	protected static TextView accY;
	protected static TextView accZ;
	protected static TextView rotStatus;
	protected static TextView rotHeader;
	protected static TextView rotTotal;
	protected static TextView rotX;
	protected static TextView rotY;
	protected static TextView rotZ;
	protected static TextView magStatus;
	protected static TextView magHeader;
	protected static TextView magTotal;
	protected static TextView magX;
	protected static TextView magY;
	protected static TextView magZ;
	protected static TextView orStatus;
	protected static TextView orHeader;
	protected static TextView orAzimuth;
	protected static TextView orAziText;
	protected static TextView orPitch;
	protected static TextView orRoll;
	protected static TextView miscHeader;
	protected static TextView tempStatus;
	protected static TextView tempHeader;
	protected static TextView metTemp;
	protected static TextView pressureStatus;
	protected static TextView pressureHeader;
	protected static TextView metPressure;
	protected static TextView humidStatus;
	protected static TextView humidHeader;
	protected static TextView metHumid;
	protected static TextView lightStatus;
	protected static TextView lightHeader;
	protected static TextView light;
	protected static TextView proximityStatus;
	protected static TextView proximityHeader;
	protected static TextView proximity;


	public static final String TAG = "HeartRateMonitor";
	public static final CircularFifoQueue bpmQueue = new CircularFifoQueue(40);
	public static int bpm;


	protected static boolean isRadioViewReady = false;
	protected static LinearLayout wifiAps;

	protected static boolean isMapViewReady = false;
	protected static boolean isMapViewAttached = true;
	protected static MapView mapMap;
	protected static TileDownloadLayer mapDownloadLayer = null;
	protected static TileCache mapTileCache = null;
	protected static ImageButton mapReattach;
	protected static HashMap<String, Circle> mapCircles;
	protected static HashMap<String, Marker> mapMarkers;

	protected static HashMap<String, Location> providerLocations;

	protected static HashMap<String, String> providerStyles;
	protected static HashMap<String, String> providerAppliedStyles;
	protected static Handler providerInvalidationHandler = null;
	protected static HashMap<String, Runnable> providerInvalidators;
	private static final int PROVIDER_EXPIRATION_DELAY = 2000;

	private static List<ScanResult> scanResults = null;
	private static String selectedBSSID = "";
	protected static Handler networkTimehandler = null;
	protected static Runnable networkTimeRunnable = null;
	protected static Handler wifiTimehandler = null;
	protected static Runnable wifiTimeRunnable = null;
	private static final int WIFI_REFRESH_DELAY = 1000;

	private final static Integer OR_FROM_ROT_TALL[] = {
			ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
			ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
			ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
			ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE};

	private final static Integer OR_FROM_ROT_WIDE[] = {
			ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
			ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
			ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
			ActivityInfo.SCREEN_ORIENTATION_PORTRAIT};

	private static SharedPreferences mSharedPreferences;


	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
				scanResults = mWifiManager.getScanResults();
				if (isRadioViewReady) {
					refreshWifiResults();
				}
			} else {
				mWifiManager.startScan();
			}
		}
	};

	private Thread.UncaughtExceptionHandler defaultUEH;

	private final void onWifiEntryClick(String BSSID) {
		selectedBSSID = BSSID;
		refreshWifiResults();
	}

	private final void addWifiResult(ScanResult result) {
		final ScanResult r = result;
		android.view.View.OnClickListener clis = new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onWifiEntryClick(r.BSSID);
			}
		};

		View divider = new View(wifiAps.getContext());
		divider.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
		divider.setBackgroundColor(getResources().getColor(android.R.color.tertiary_text_dark));
		divider.setOnClickListener(clis);
		wifiAps.addView(divider);

		LinearLayout wifiLayout = new LinearLayout(wifiAps.getContext());
		wifiLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		wifiLayout.setOrientation(LinearLayout.HORIZONTAL);
		wifiLayout.setWeightSum(22);
		wifiLayout.setMeasureWithLargestChildEnabled(false);

		ImageView wifiType = new ImageView(wifiAps.getContext());
		wifiType.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.MATCH_PARENT, 3));
		if (WifiCapabilities.isAdhoc(result)) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_adhoc);
		} else if ((WifiCapabilities.isEnterprise(result)) || (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.EAP)) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_eap);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.PSK) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_psk);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.WEP) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_wep);
		} else if (WifiCapabilities.getScanResultSecurity(result) == WifiCapabilities.OPEN) {
			wifiType.setImageResource(R.drawable.ic_content_wifi_open);
		} else {
			wifiType.setImageResource(R.drawable.ic_content_wifi_unknown);
		}

		wifiType.setScaleType(ScaleType.CENTER);
		wifiLayout.addView(wifiType);

		TableLayout wifiDetails = new TableLayout(wifiAps.getContext());
		wifiDetails.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19));
		TableRow innerRow1 = new TableRow(wifiAps.getContext());
		TextView newMac = new TextView(wifiAps.getContext());
		newMac.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 14));
		newMac.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		newMac.setText(result.BSSID);
		innerRow1.addView(newMac);
		TextView newCh = new TextView(wifiAps.getContext());
		newCh.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
		newCh.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		innerRow1.addView(newCh);
		TextView newLevel = new TextView(wifiAps.getContext());
		newLevel.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3));
		newLevel.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Medium);
		newLevel.setText(String.valueOf(result.level));
		innerRow1.addView(newLevel);
		innerRow1.setOnClickListener(clis);
		wifiDetails.addView(innerRow1, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		TableRow innerRow2 = new TableRow(wifiAps.getContext());
		TextView newSSID = new TextView(wifiAps.getContext());
		newSSID.setLayoutParams(new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 19));
		newSSID.setTextAppearance(wifiAps.getContext(), android.R.style.TextAppearance_Small);
		newSSID.setText(result.SSID);
		innerRow2.addView(newSSID);
		innerRow2.setOnClickListener(clis);
		wifiDetails.addView(innerRow2, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		wifiLayout.addView(wifiDetails);
		wifiLayout.setOnClickListener(clis);
		wifiAps.addView(wifiLayout);
	}

	private final void refreshWifiResults() {
		if (scanResults != null) {
			wifiAps.removeAllViews();
			for (ScanResult result : scanResults) {
				if (result.BSSID.equals(selectedBSSID)) {
					addWifiResult(result);
				}
			}
			for (ScanResult result : scanResults) {
				if (!result.BSSID.equals(selectedBSSID)) {
					addWifiResult(result);
				}
			}
		}
	}

	public static int accuracyToColor(int accuracy) {
		switch (accuracy) {
			case SENSOR_STATUS_ACCURACY_HIGH:
				return (R.color.accHigh);
			case SENSOR_STATUS_ACCURACY_MEDIUM:
				return (R.color.accMedium);
			case SENSOR_STATUS_ACCURACY_LOW:
				return (R.color.accLow);
			case SENSOR_STATUS_UNRELIABLE:
				return (R.color.accUnreliable);
			default:
				return (android.R.color.background_dark);
		}
	}


	protected static void applyLocationProviderStyle(Context context, String provider, String styleName) {
		String sn = (styleName != null) ? styleName : assignLocationProviderStyle(provider);

		Boolean isStyleChanged = !sn.equals(providerAppliedStyles.get(provider));
		Boolean needsRedraw = false;

		Resources res = context.getResources();
		TypedArray style = res.obtainTypedArray(res.getIdentifier(sn, "array", context.getPackageName()));

		Circle circle = mapCircles.get(provider);
		if (circle != null) {
			circle.getPaintFill().setColor(style.getColor(STYLE_FILL, R.color.circle_gray_fill));
			circle.getPaintStroke().setColor(style.getColor(STYLE_STROKE, R.color.circle_gray_stroke));
			needsRedraw = isStyleChanged && circle.isVisible();
		}

		Marker marker = mapMarkers.get(provider);
		if (marker != null) {
			Drawable drawable = style.getDrawable(STYLE_MARKER);
			Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
			marker.setBitmap(bitmap);
			needsRedraw = needsRedraw || (isStyleChanged && marker.isVisible());
		}

		if (needsRedraw)
			mapMap.getLayerManager().redrawLayers();
		providerAppliedStyles.put(provider, sn);
		style.recycle();
	}


	protected static String assignLocationProviderStyle(String provider) {
		String styleName = providerStyles.get(provider);
		if (styleName == null) {
			if (mAvailableProviderStyles.isEmpty())
				mAvailableProviderStyles.addAll(Arrays.asList(LOCATION_PROVIDER_STYLES));
			styleName = mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + provider, mAvailableProviderStyles.get(0));
			providerStyles.put(provider, styleName);
			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + provider, styleName);
			spEditor.commit();
		}
		return styleName;
	}


	public String formatOrientation(float bearing) {
		return
				(bearing < 11.25) ? getString(R.string.value_N) :
						(bearing < 33.75) ? getString(R.string.value_NNE) :
								(bearing < 56.25) ? getString(R.string.value_NE) :
										(bearing < 78.75) ? getString(R.string.value_ENE) :
												(bearing < 101.25) ? getString(R.string.value_E) :
														(bearing < 123.75) ? getString(R.string.value_ESE) :
																(bearing < 146.25) ? getString(R.string.value_SE) :
																		(bearing < 168.75) ? getString(R.string.value_SSE) :
																				(bearing < 191.25) ? getString(R.string.value_S) :
																						(bearing < 213.75) ? getString(R.string.value_SSW) :
																								(bearing < 236.25) ? getString(R.string.value_SW) :
																										(bearing < 258.75) ? getString(R.string.value_WSW) :
																												(bearing < 280.25) ? getString(R.string.value_W) :
																														(bearing < 302.75) ? getString(R.string.value_WNW) :
																																(bearing < 325.25) ? getString(R.string.value_NW) :
																																		(bearing < 347.75) ? getString(R.string.value_NNW) :
																																				getString(R.string.value_N);
	}


	public static int getColorFromGeneration(int generation) {
		switch (generation) {
			case 2:
				return (R.color.gen2);
			case 3:
				return (R.color.gen3);
			case 4:
				return (R.color.gen4);
			default:
				return (android.R.color.transparent);
		}
	}


	public static byte getSensorDecimals(Sensor sensor, byte maxDecimals) {
		if (sensor == null) return 0;
		float res = sensor.getResolution();
		if (res == 0) return maxDecimals;
		return (byte) Math.min(maxDecimals,
				(sensor != null) ? (byte) Math.max(Math.ceil(
						(float) -Math.log10(sensor.getResolution())), 0) : 0);
	}


	public static boolean isLocationStale(Location location) {
		Bundle extras = location.getExtras();
		if (extras == null)
			return false;
		return extras.getBoolean(KEY_LOCATION_STALE);
	}


	public static void markLocationAsStale(Location location) {
		if (location.getExtras() == null)
			location.setExtras(new Bundle());
		location.getExtras().putBoolean(KEY_LOCATION_STALE, true);
	}


	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				Context c = getApplicationContext();
				File dumpDir = c.getExternalFilesDir(null);
				File dumpFile = new File(dumpDir, "senanl-" + System.currentTimeMillis() + ".log");
				PrintStream s;
				try {
					InputStream buildInStream = getResources().openRawResource(R.raw.build);
					s = new PrintStream(dumpFile);
					s.append("Senanal build: ");

					int i;
					try {
						i = buildInStream.read();
						while (i != -1) {
							s.write(i);
							i = buildInStream.read();
						}
						buildInStream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					s.append("\n\n");
					e.printStackTrace(s);
					s.flush();
					s.close();
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				defaultUEH.uncaughtException(t, e);
			}
		});

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		final ActionBar actionBar = getActionBar();

		setContentView(R.layout.activity_main);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Configuration config = getResources().getConfiguration();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int rot = wm.getDefaultDisplay().getRotation();
		isWideScreen = (config.orientation == Configuration.ORIENTATION_LANDSCAPE &&
				(rot == Surface.ROTATION_0 || rot == Surface.ROTATION_180) ||
				config.orientation == Configuration.ORIENTATION_PORTRAIT &&
						(rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270));
		Log.d("MainActivity", "isWideScreen=" + Boolean.toString(isWideScreen));

		int dpX = (int) (this.getResources().getDisplayMetrics().widthPixels / this.getResources().getDisplayMetrics().density);

		if (dpX < 192) {
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
		} else if (dpX < 320) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		} else if (dpX < 384) {
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
		} else if ((dpX < 448) || ((config.orientation == Configuration.ORIENTATION_PORTRAIT) && (dpX < 544))) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		} else {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		setEmbeddedTabs(actionBar, true);

		providerLocations = new HashMap<String, Location>();

		mAvailableProviderStyles = new ArrayList<String>(Arrays.asList(LOCATION_PROVIDER_STYLES));

		providerStyles = new HashMap<String, String>();
		providerAppliedStyles = new HashMap<String, String>();

		providerInvalidationHandler = new Handler();
		providerInvalidators = new HashMap<String, Runnable>();

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(
					actionBar.newTab()
							.setIcon(mSectionsPagerAdapter.getPageIcon(i))
							.setTabListener(this));
		}


		AndroidGraphicFactory.createInstance(this.getApplication());


		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		mHumiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		mAccSensorRes = getSensorDecimals(mAccSensor, mAccSensorRes);
		mGyroSensorRes = getSensorDecimals(mGyroSensor, mGyroSensorRes);
		mMagSensorRes = getSensorDecimals(mMagSensor, mMagSensorRes);
		mLightSensorRes = getSensorDecimals(mLightSensor, mLightSensorRes);
		mProximitySensorRes = getSensorDecimals(mProximitySensor, mProximitySensorRes);
		mPressureSensorRes = getSensorDecimals(mPressureSensor, mPressureSensorRes);
		mHumiditySensorRes = getSensorDecimals(mHumiditySensor, mHumiditySensorRes);
		mTempSensorRes = getSensorDecimals(mTempSensor, mTempSensorRes);

		networkTimehandler = new Handler();
		networkTimeRunnable = new Runnable() {
			@Override
			public void run() {
			}
		};

		wifiTimehandler = new Handler();
		wifiTimeRunnable = new Runnable() {

			@Override
			public void run() {
				mWifiManager.startScan();
				wifiTimehandler.postDelayed(this, WIFI_REFRESH_DELAY);
			}
		};

		updateLocationProviderStyles();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	protected void onDestroy() {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}


	public void onGpsStatusChanged(int event) {
		GpsStatus status = mLocationManager.getGpsStatus(null);
		int satsInView = 0;
		int satsUsed = 0;
		Iterable<GpsSatellite> sats = status.getSatellites();
		for (GpsSatellite sat : sats) {
			satsInView++;
			if (sat.usedInFix()) {
				satsUsed++;
			}
		}

		if (isGpsViewReady) {
			gpsSats.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
			gpsTtff.setText(String.valueOf(status.getTimeToFirstFix() / 1000));
			gpsStatusView.showSats(sats);
			gpsSnrView.showSats(sats);
		}

		if ((isMapViewReady) && (satsUsed == 0)) {
			Location location = providerLocations.get(LocationManager.GPS_PROVIDER);
			if (location != null)
				markLocationAsStale(location);
			applyLocationProviderStyle(this, LocationManager.GPS_PROVIDER, LOCATION_PROVIDER_GRAY);
		}
	}


	public void onLocationChanged(Location location) {
		if (Double.isNaN(location.getLatitude()) || Double.isNaN(location.getLongitude())) {
			markLocationAsStale(providerLocations.get(location.getProvider()));
			if (isMapViewReady)
				applyLocationProviderStyle(this, location.getProvider(), LOCATION_PROVIDER_GRAY);
			return;
		}

		if (providerLocations.containsKey(location.getProvider()))
			providerLocations.put(location.getProvider(), new Location(location));


		if (isMapViewReady) {
			LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());

			Circle circle = mapCircles.get(location.getProvider());
			Marker marker = mapMarkers.get(location.getProvider());

			if (circle != null) {
				circle.setLatLong(latLong);
				if (location.hasAccuracy()) {
					circle.setVisible(true);
					circle.setRadius(location.getAccuracy());
				} else {
					Log.d("MainActivity", "Location from " + location.getProvider() + " has no accuracy");
					circle.setVisible(false);
				}
			}

			if (marker != null) {
				marker.setLatLong(latLong);
				marker.setVisible(true);
			}

			applyLocationProviderStyle(this, location.getProvider(), null);

			Runnable invalidator = providerInvalidators.get(location.getProvider());
			if (invalidator != null) {
				providerInvalidationHandler.removeCallbacks(invalidator);
				providerInvalidationHandler.postDelayed(invalidator, PROVIDER_EXPIRATION_DELAY);
			}

			if ((circle != null) || (marker != null) || (invalidator != null))
				updateMap();
		}

		if ((location.getProvider().equals(LocationManager.GPS_PROVIDER)) && (isGpsViewReady)) {
			if (location.hasAccuracy()) {
				gpsAccuracy.setText(String.format("%.0f", location.getAccuracy()));
			} else {
				gpsAccuracy.setText(getString(R.string.value_none));
			}

			gpsLat.setText(String.format("%.5f%s", location.getLatitude(), getString(R.string.unit_degree)));
			gpsLon.setText(String.format("%.5f%s", location.getLongitude(), getString(R.string.unit_degree)));
			gpsTime.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", location.getTime()));

			if (location.hasAltitude()) {
				gpsAlt.setText(String.format("%.0f", location.getAltitude()));
				orDeclination.setText(String.format("%.0f%s", new GeomagneticField(
						(float) location.getLatitude(),
						(float) location.getLongitude(),
						(float) location.getAltitude(),
						location.getTime()
				).getDeclination(), getString(R.string.unit_degree)));
			} else {
				gpsAlt.setText(getString(R.string.value_none));
				orDeclination.setText(getString(R.string.value_none));
			}

			if (location.hasBearing()) {
				gpsBearing.setText(String.format("%.0f%s", location.getBearing(), getString(R.string.unit_degree)));
				gpsOrientation.setText(formatOrientation(location.getBearing()));
			} else {
				gpsBearing.setText(getString(R.string.value_none));
				gpsOrientation.setText(getString(R.string.value_none));
			}

			if (location.hasSpeed()) {
				gpsSpeed.setText(String.format("%.0f", (location.getSpeed()) * 3.6));
			} else {
				gpsSpeed.setText(getString(R.string.value_none));
			}

		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_agps:
				Log.i(this.getLocalClassName(), "User requested AGPS data update");
				GpsEventReceiver.refreshAgps(this, false, true);
				return true;
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {

		getActionBar().setSelectedNavigationItem(position);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if ((isMapViewReady) && (mapDownloadLayer != null))
			mapDownloadLayer.onPause();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		isStopped = false;
		registerLocationProviders(this);
		mLocationManager.addGpsStatusListener(this);
		mSensorManager.registerListener(this, mOrSensor, iSensorRate);
		mSensorManager.registerListener(this, mAccSensor, iSensorRate);
		mSensorManager.registerListener(this, mGyroSensor, iSensorRate);
		mSensorManager.registerListener(this, mMagSensor, iSensorRate);
		mSensorManager.registerListener(this, mLightSensor, iSensorRate);
		mSensorManager.registerListener(this, mProximitySensor, iSensorRate);
		mSensorManager.registerListener(this, mPressureSensor, iSensorRate);
		mSensorManager.registerListener(this, mHumiditySensor, iSensorRate);
		mSensorManager.registerListener(this, mTempSensor, iSensorRate);


		registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));

		wifiTimehandler.postDelayed(wifiTimeRunnable, WIFI_REFRESH_DELAY);


		if ((isMapViewReady) && (mapDownloadLayer != null))
			mapDownloadLayer.onResume();
	}


	public void onSensorChanged(SensorEvent event) {

		boolean isRateElapsed = false;

		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				isRateElapsed = (event.timestamp / 1000) - mAccLast >= iSensorRate;

				/*if (Math.pow(event.values[2], 2) > Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2)) {

					if (isWideScreen)
						setRequestedOrientation(OR_FROM_ROT_WIDE[this.getWindowManager().getDefaultDisplay().getRotation()]);
					else
						setRequestedOrientation(OR_FROM_ROT_TALL[this.getWindowManager().getDefaultDisplay().getRotation()]);
				} else {*/
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

				break;
			case Sensor.TYPE_ORIENTATION:
				isRateElapsed = (event.timestamp / 1000) - mOrLast >= iSensorRate;
				break;
			case Sensor.TYPE_GYROSCOPE:
				isRateElapsed = (event.timestamp / 1000) - mGyroLast >= iSensorRate;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				isRateElapsed = (event.timestamp / 1000) - mMagLast >= iSensorRate;
				break;
			case Sensor.TYPE_LIGHT:
				isRateElapsed = (event.timestamp / 1000) - mLightLast >= iSensorRate;
				break;
			case Sensor.TYPE_PROXIMITY:
				isRateElapsed = (event.timestamp / 1000) - mProximityLast >= iSensorRate;
				break;
			case Sensor.TYPE_PRESSURE:
				isRateElapsed = (event.timestamp / 1000) - mPressureLast >= iSensorRate;
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				isRateElapsed = (event.timestamp / 1000) - mHumidityLast >= iSensorRate;
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				isRateElapsed = (event.timestamp / 1000) - mTempLast >= iSensorRate;
				break;
		}
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		//Log.i("APOJCHECk", Boolean.toString(prefs.getBoolean("upload_check", false)));
		final boolean upload_check = prefs.getBoolean("upload_check", false);

		if (isSensorViewReady && isRateElapsed) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					mAccLast = event.timestamp / 1000;
					accX.setText(String.format("%." + mAccSensorRes + "f", event.values[0]));
					accY.setText(String.format("%." + mAccSensorRes + "f", event.values[1]));
					accZ.setText(String.format("%." + mAccSensorRes + "f", event.values[2]));
					accTotal.setText(String.format("%." + mAccSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					accStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postaccData(event.values[0], event.values[1], event.values[2]);
					}
					break;
				case Sensor.TYPE_ORIENTATION:
					mOrLast = event.timestamp / 1000;
					orAzimuth.setText(String.format("%.0f%s", event.values[0], getString(R.string.unit_degree)));
					orAziText.setText(formatOrientation(event.values[0]));
					orPitch.setText(String.format("%.0f%s", event.values[1], getString(R.string.unit_degree)));
					orRoll.setText(String.format("%.0f%s", event.values[2], getString(R.string.unit_degree)));
					orStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postorientData(event.values[0], event.values[1], event.values[2]);
					}
					break;
				case Sensor.TYPE_GYROSCOPE:
					mGyroLast = event.timestamp / 1000;
					rotX.setText(String.format("%." + mGyroSensorRes + "f", event.values[0]));
					rotY.setText(String.format("%." + mGyroSensorRes + "f", event.values[1]));
					rotZ.setText(String.format("%." + mGyroSensorRes + "f", event.values[2]));
					rotTotal.setText(String.format("%." + mGyroSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					rotStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postgyrData(event.values[0], event.values[1], event.values[2]);
					}
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					mMagLast = event.timestamp / 1000;
					magX.setText(String.format("%." + mMagSensorRes + "f", event.values[0]));
					magY.setText(String.format("%." + mMagSensorRes + "f", event.values[1]));
					magZ.setText(String.format("%." + mMagSensorRes + "f", event.values[2]));
					magTotal.setText(String.format("%." + mMagSensorRes + "f", Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));
					magStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postmagData(event.values[0], event.values[1], event.values[2]);
					}
					break;
				case Sensor.TYPE_LIGHT:
					mLightLast = event.timestamp / 1000;
					light.setText(String.format("%." + mLightSensorRes + "f", event.values[0]));
					lightStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postlightData(event.values[0]);
					}
					break;
				case Sensor.TYPE_PROXIMITY:
					mProximityLast = event.timestamp / 1000;
					proximity.setText(String.format("%." + mProximitySensorRes + "f", event.values[0]));
					proximityStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
				case Sensor.TYPE_PRESSURE:
					mPressureLast = event.timestamp / 1000;
					metPressure.setText(String.format("%." + mPressureSensorRes + "f", event.values[0]));
					pressureStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					if (upload_check) {
						postpressureData(event.values[0]);
					}
					break;
				case Sensor.TYPE_RELATIVE_HUMIDITY:
					mHumidityLast = event.timestamp / 1000;
					metHumid.setText(String.format("%." + mHumiditySensorRes + "f", event.values[0]));
					humidStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
				case Sensor.TYPE_AMBIENT_TEMPERATURE:
					mTempLast = event.timestamp / 1000;
					metTemp.setText(String.format("%." + mTempSensorRes + "f", event.values[0]));
					tempStatus.setTextColor(getResources().getColor(accuracyToColor(event.accuracy)));
					break;
			}
		}
		if (isGpsViewReady && isRateElapsed) {
			switch (event.sensor.getType()) {
				case Sensor.TYPE_ORIENTATION:
					gpsStatusView.setYaw(event.values[0]);
					break;
			}
		}
	}

	public void postaccData(final float accx, final float accy, final float accz) {
		// Create a new HttpClient and Post Header
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);
		Log.i("APOJSERV", prefs.getString("server_address", null));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/accdata");
					final String test = "http://" + server_address + "/accdata";
					Log.i("APOJSERV", "POSTING TO" + test);
					HttpPost httppost = new HttpPost("http://" + server_address + "/accdata");
					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("accx", String.valueOf(accx)));
						nameValuePairs.add(new BasicNameValuePair("accy", String.valueOf(accy)));
						nameValuePairs.add(new BasicNameValuePair("accz", String.valueOf(accz)));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {
						Log.i("APOJ", "exce" + e);
					} catch (IOException e) {
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void postorientData(final float azimuth, final float pitch, final float roll) {

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/orientdata");
					HttpPost httppost = new HttpPost("http://" + server_address + "/orientdata");
					try {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("azimuth", String.valueOf(azimuth)));
						nameValuePairs.add(new BasicNameValuePair("pitch", String.valueOf(pitch)));
						nameValuePairs.add(new BasicNameValuePair("roll", String.valueOf(roll)));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {

						Log.i("APOJ", "exce" + e);
					} catch (IOException e) {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void postgyrData(final float rotx, final float roty, final float rotz) {
		// Create a new HttpClient and Post Header
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/rotdata");
					HttpPost httppost = new HttpPost("http://" + server_address + "/rotdata");
					try {
						// Add your data
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("rotx", String.valueOf(rotx)));
						nameValuePairs.add(new BasicNameValuePair("roty", String.valueOf(roty)));
						nameValuePairs.add(new BasicNameValuePair("rotz", String.valueOf(rotz)));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

						// Execute HTTP Post Request
						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void postmagData(final float magx, final float magy, final float magz) {
		// Create a new HttpClient and Post Header
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/magdata");
					HttpPost httppost = new HttpPost("http://" + server_address + "/magdata");
					try {
						// Add your data
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("magx", String.valueOf(magx)));
						nameValuePairs.add(new BasicNameValuePair("magy", String.valueOf(magy)));
						nameValuePairs.add(new BasicNameValuePair("magz", String.valueOf(magz)));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

						// Execute HTTP Post Request
						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						Log.i("APOJ", "exce" + e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void postlightData(final float data) {
		// Create a new HttpClient and Post Header
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/lightdata");
					HttpPost httppost = new HttpPost("http://" + server_address + "/lightdata");
					try {
						// Add your data
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("light", String.valueOf(data)));

						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

						// Execute HTTP Post Request
						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						Log.i("APOJ", "exce" + e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void postpressureData(final float data) {
		// Create a new HttpClient and Post Header
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String server_address = prefs.getString("server_address", null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpclient = new DefaultHttpClient();
					//HttpPost httppost = new HttpPost("http://192.168.0.10:8080/pressuredata");
					HttpPost httppost = new HttpPost("http://" + server_address + "/pressuredata");
					try {
						// Add your data
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("pressure", String.valueOf(data)));

						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

						// Execute HTTP Post Request
						HttpResponse response = httpclient.execute(httppost);

					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						Log.i("APOJ", "exce" + e);
					} catch (IOException e) {
						// TODO Auto-generated catch block
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
										  String key) {
		if (key.equals(SettingsActivity.KEY_PREF_LOC_PROV)) {
			registerLocationProviders(this);
			updateLocationProviders(this);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	protected void onStop() {
		isStopped = true;
		mLocationManager.removeUpdates(this);
		mLocationManager.removeGpsStatusListener(this);
		mSensorManager.unregisterListener(this);
		try {
			unregisterReceiver(mWifiScanReceiver);
		} catch (IllegalArgumentException e) {
			Log.d(this.getLocalClassName(), "WifiScanReceiver was never registered, caught exception");
		}
		networkTimehandler.removeCallbacks(networkTimeRunnable);
		wifiTimehandler.removeCallbacks(wifiTimeRunnable);
		super.onStop();
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		Log.i("TABB", Integer.toString(tab.getPosition()));

		if(tab.getPosition() == 3)
			isHeartTabSelected = true;

		else
			isHeartTabSelected = false;

		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
	}

	protected void registerLocationProviders(Context context) {
		Set<String> providers = new HashSet<String>(mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>(Arrays.asList(new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}))));
		List<String> allProviders = mLocationManager.getAllProviders();

		mLocationManager.removeUpdates(this);

		ArrayList<String> removedProviders = new ArrayList<String>();
		for (String pr : providerLocations.keySet())
			if (!providers.contains(pr))
				removedProviders.add(pr);
		for (String pr : removedProviders)
			providerLocations.remove(pr);

		for (String pr : providers) {
			if (allProviders.indexOf(pr) >= 0) {
				if (!providerLocations.containsKey(pr)) {
					Location location = new Location("");
					providerLocations.put(pr, location);
				}
				if (!isStopped) {
					mLocationManager.requestLocationUpdates(pr, 0, 0, this);
					Log.d("MainActivity", "Registered with provider: " + pr);
				}
			} else {
				Log.w("MainActivity", "No " + pr + " location provider found. Data display will not be available for this provider.");
			}
		}

		if ((!providers.contains(LocationManager.GPS_PROVIDER)) && (!isStopped) && (allProviders.indexOf(LocationManager.GPS_PROVIDER) >= 0))
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	private void setEmbeddedTabs(Object actionBar, Boolean embed_tabs) {
		try {
			Method setHasEmbeddedTabsMethod = actionBar.getClass()
					.getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
			setHasEmbeddedTabsMethod.setAccessible(true);
			setHasEmbeddedTabsMethod.invoke(actionBar, embed_tabs);
		} catch (Exception e) {
			Log.e("", "Error marking actionbar embedded", e);
		}
	}


	protected static void updateLocationProviders(Context context) {
		if (isMapViewReady) {
			Set<String> providers = mSharedPreferences.getStringSet(SettingsActivity.KEY_PREF_LOC_PROV, new HashSet<String>(Arrays.asList(new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER})));

			updateLocationProviderStyles();

			mapCircles = new HashMap<String, Circle>();
			mapMarkers = new HashMap<String, Marker>();

			ArrayList<String> removedProviders = new ArrayList<String>();
			for (String pr : providerInvalidators.keySet())
				if (!providers.contains(pr))
					removedProviders.add(pr);
			for (String pr : removedProviders)
				providerInvalidators.remove(pr);

			Log.d("MainActivity", "Provider location cache: " + providerLocations.keySet().toString());

			Layers layers = mapMap.getLayerManager().getLayers();

			for (int i = 0; i < layers.size(); )
				if ((layers.get(i) instanceof TileRendererLayer) || (layers.get(i) instanceof TileDownloadLayer)) {
					i++;
				} else {
					layers.remove(i);
				}

			for (String pr : providers) {
				if ((!pr.equals(LocationManager.GPS_PROVIDER)) && (providerInvalidators.get(pr)) == null) {
					final String provider = pr;
					final Context ctx = context;
					providerInvalidators.put(pr, new Runnable() {
						private String mProvider = provider;

						@Override
						public void run() {
							if (isMapViewReady) {
								Location location = providerLocations.get(mProvider);
								if (location != null)
									markLocationAsStale(location);
								applyLocationProviderStyle(ctx, mProvider, LOCATION_PROVIDER_GRAY);
							}
						}
					});
				}

				String styleName = assignLocationProviderStyle(pr);
				LatLong latLong;
				float acc;
				boolean visible;
				if ((providerLocations.get(pr) != null) && (providerLocations.get(pr).getProvider() != "")) {
					latLong = new LatLong(providerLocations.get(pr).getLatitude(),
							providerLocations.get(pr).getLongitude());
					if (providerLocations.get(pr).hasAccuracy())
						acc = providerLocations.get(pr).getAccuracy();
					else
						acc = 0;
					visible = true;
					if (isLocationStale(providerLocations.get(pr)))
						styleName = LOCATION_PROVIDER_GRAY;
					Log.d("MainActivity", pr + " has " + latLong.toString());
				} else {
					latLong = new LatLong(0, 0);
					acc = 0;
					visible = false;
					Log.d("MainActivity", pr + " has no location, hiding");
				}

				Resources res = context.getResources();
				TypedArray style = res.obtainTypedArray(res.getIdentifier(styleName, "array", context.getPackageName()));
				Paint fill = AndroidGraphicFactory.INSTANCE.createPaint();
				fill.setColor(style.getColor(STYLE_FILL, R.color.circle_gray_fill));
				fill.setStyle(Style.FILL);
				Paint stroke = AndroidGraphicFactory.INSTANCE.createPaint();
				stroke.setColor(style.getColor(STYLE_STROKE, R.color.circle_gray_stroke));
				stroke.setStrokeWidth(4);
				stroke.setStyle(Style.STROKE);
				Circle circle = new Circle(latLong, acc, fill, stroke);
				mapCircles.put(pr, circle);
				layers.add(circle);
				circle.setVisible(visible);

				Drawable drawable = style.getDrawable(STYLE_MARKER);
				Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
				Marker marker = new Marker(latLong, bitmap, 0, -bitmap.getHeight() * 9 / 20);
				mapMarkers.put(pr, marker);
				layers.add(marker);
				marker.setVisible(visible);
				style.recycle();
			}

			updateMap();
		}
	}


	public static void updateLocationProviderStyles() {
		List<String> allProviders = mLocationManager.getAllProviders();
		allProviders.remove(LocationManager.PASSIVE_PROVIDER);
		if (allProviders.contains(LocationManager.GPS_PROVIDER)) {
			providerStyles.put(LocationManager.GPS_PROVIDER,
					mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + LocationManager.GPS_PROVIDER, LOCATION_PROVIDER_RED));
			mAvailableProviderStyles.remove(LOCATION_PROVIDER_RED);
			allProviders.remove(LocationManager.GPS_PROVIDER);
		}
		if (allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
			providerStyles.put(LocationManager.NETWORK_PROVIDER,
					mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + LocationManager.NETWORK_PROVIDER, LOCATION_PROVIDER_BLUE));
			mAvailableProviderStyles.remove(LOCATION_PROVIDER_BLUE);
			allProviders.remove(LocationManager.NETWORK_PROVIDER);
		}
		for (String prov : allProviders) {
			if (mAvailableProviderStyles.isEmpty())
				mAvailableProviderStyles.addAll(Arrays.asList(LOCATION_PROVIDER_STYLES));
			providerStyles.put(prov,
					mSharedPreferences.getString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + prov, mAvailableProviderStyles.get(0)));
			mAvailableProviderStyles.remove(providerStyles.get(prov));
		}
		;
		SharedPreferences.Editor spEditor = mSharedPreferences.edit();
		for (String prov : providerStyles.keySet())
			spEditor.putString(SettingsActivity.KEY_PREF_LOC_PROV_STYLE + prov, providerStyles.get(prov));
		spEditor.commit();
	}


	//Update Map tab with current location

	public static void updateMap() {
		boolean needsRedraw = false;
		Dimension dimension = mapMap.getModel().mapViewDimension.getDimension();

		if ((dimension == null) || (!isMapViewAttached)) {
			mapMap.getLayerManager().redrawLayers();
			return;
		}

		int tileSize = mapMap.getModel().displayModel.getTileSize();
		BoundingBox bb = null;
		BoundingBox bb2 = null;
		for (Location l : providerLocations.values())
			if ((l != null) && (l.getProvider() != "")) {
				double lat = l.getLatitude();
				double lon = l.getLongitude();
				double yRadius = l.hasAccuracy() ? ((l.getAccuracy() * 360.0f) / EARTH_CIRCUMFERENCE) : 0;
				double xRadius = l.hasAccuracy() ? (yRadius * Math.abs(Math.cos(lat))) : 0;

				double minLon = Math.max(lon - xRadius, -180);
				double maxLon = Math.min(lon + xRadius, 180);
				double minLat = Math.max(lat - yRadius, -90);
				double maxLat = Math.min(lat + yRadius, 90);

				if (!isLocationStale(l)) {
					if (bb != null) {
						minLat = Math.min(bb.minLatitude, minLat);
						maxLat = Math.max(bb.maxLatitude, maxLat);
						minLon = Math.min(bb.minLongitude, minLon);
						maxLon = Math.max(bb.maxLongitude, maxLon);
					}
					bb = new BoundingBox(minLat, minLon, maxLat, maxLon);
				} else {
					if (bb2 != null) {
						minLat = Math.min(bb2.minLatitude, minLat);
						maxLat = Math.max(bb2.maxLatitude, maxLat);
						minLon = Math.min(bb2.minLongitude, minLon);
						maxLon = Math.max(bb2.maxLongitude, maxLon);
					}
					bb2 = new BoundingBox(minLat, minLon, maxLat, maxLon);
				}
			}
		if (bb == null) bb = bb2;
		if (bb == null) {
			needsRedraw = true;
		} else {
			byte newZoom = LatLongUtils.zoomForBounds(dimension, bb, tileSize);
			if (newZoom < mapMap.getModel().mapViewPosition.getZoomLevel()) {
				mapMap.getModel().mapViewPosition.setZoomLevel(newZoom);
			} else {
				needsRedraw = true;
			}

			MapViewProjection proj = new MapViewProjection(mapMap);
			Point nw = proj.toPixels(new LatLong(bb.maxLatitude, bb.minLongitude));
			Point se = proj.toPixels(new LatLong(bb.minLatitude, bb.maxLongitude));

			if ((nw.x < 0) || (nw.y < 0) || (se.x > dimension.width) || (se.y > dimension.height)) {
				mapMap.getModel().mapViewPosition.setCenter(bb.getCenterPoint());
			} else {
				needsRedraw = true;
			}
		}
		if (needsRedraw)
			mapMap.getLayerManager().redrawLayers();
	}


	// Each tab is implemented as a separate fragment, in total there are 4 tabs

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			switch (position) {
				case 0:
					fragment = new SensorSectionFragment();
					return fragment;
				case 1:
					fragment = new GpsSectionFragment();
					return fragment;
				case 2:
					fragment = new MapSectionFragment();
					return fragment;
				 case 3:
				   	fragment = new HeartRateFragment();
				 	return fragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		public Drawable getPageIcon(int position) {
			switch (position) {
				case 0:
					return getResources().getDrawable(R.drawable.ic_action_sensor);
				case 1:
					return getResources().getDrawable(R.drawable.ic_action_gps);
				case 2:
					return getResources().getDrawable(R.drawable.ic_action_map);
				  case 3:
				    return getResources().getDrawable(R.drawable.ic_action_radio);
			}
			return null;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 1:
					return getString(R.string.title_section1).toUpperCase(l);
				case 0:
					return getString(R.string.title_section2).toUpperCase(l);
				case 3:
				  return getString(R.string.title_section3).toUpperCase(l);
				case 2:
					return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}

	/*
	public static class BlankFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		public BlankFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
			Log.i("SENSOR_ANALYTICS", "onCreateView Main Activity Blank fragment");
			return rootView;
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();

		}
	}

	*/


	// Tab #2, GPS Fragment

	public static class GpsSectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		public GpsSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			//Camera camera = null;
			Log.i("SENSOR_ANALYTICS", "onCreateView Main Activity GPS fragment");
			View rootView = inflater.inflate(R.layout.fragment_main_gps, container, false);
			gpsRootLayout = (LinearLayout) rootView.findViewById(R.id.gpsRootLayout);
			gpsSnrView = (GpsSnrView) rootView.findViewById(R.id.gpsSnrView);
			gpsStatusView = new GpsStatusView(rootView.getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
			params.weight = 1;
			gpsRootLayout.addView(gpsStatusView, 0, params);
			gpsLat = (TextView) rootView.findViewById(R.id.gpsLat);
			gpsLon = (TextView) rootView.findViewById(R.id.gpsLon);
			orDeclination = (TextView) rootView.findViewById(R.id.orDeclination);
			gpsSpeed = (TextView) rootView.findViewById(R.id.gpsSpeed);
			gpsAlt = (TextView) rootView.findViewById(R.id.gpsAlt);
			gpsTime = (TextView) rootView.findViewById(R.id.gpsTime);
			gpsBearing = (TextView) rootView.findViewById(R.id.gpsBearing);
			gpsAccuracy = (TextView) rootView.findViewById(R.id.gpsAccuracy);
			gpsOrientation = (TextView) rootView.findViewById(R.id.gpsOrientation);
			gpsSats = (TextView) rootView.findViewById(R.id.gpsSats);
			gpsTtff = (TextView) rootView.findViewById(R.id.gpsTtff);

			isGpsViewReady = true;


			if(isHeartTabSelected == false) {

				try {
				camera.setPreviewCallback(null);
				camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					camera.stopPreview();
					camera.release();
					camera = null;
				} catch (RuntimeException e) {
				}

			}

			return rootView;
		}

		@Override
		public void onDestroyView() {

			//Camera camera = null;


			Log.i("SENSOR_ANALYTICS", "onDestroyView Main Activity GPS fragment");
			if(isHeartTabSelected == true){

				try {
					camera = Camera.open();
				} catch (RuntimeException e) {
				} finally {
					//if (c != null) c.release();
				}

				camera.startPreview();
				Camera.Parameters parameters = camera.getParameters();

				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

				camera.setParameters(parameters);
				camera.startPreview();
			}
			super.onDestroyView();
			isGpsViewReady = false;
		}
	}


	// Tab # 1 - Sensor Data Fragment

	public static class SensorSectionFragment extends Fragment {
		public static final String ARG_SECTION_NUMBER = "section_number";

		public SensorSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_sensors, container, false);
			Log.i("SENSOR_ANALYTICS", "onCreateView Sensor Activity Sensor Section fragment");

			// Initialize controls
			accStatus = (TextView) rootView.findViewById(R.id.accStatus);
			accHeader = (TextView) rootView.findViewById(R.id.accHeader);
			accX = (TextView) rootView.findViewById(R.id.accX);
			accY = (TextView) rootView.findViewById(R.id.accY);
			accZ = (TextView) rootView.findViewById(R.id.accZ);
			accTotal = (TextView) rootView.findViewById(R.id.accTotal);
			rotStatus = (TextView) rootView.findViewById(R.id.rotStatus);
			rotHeader = (TextView) rootView.findViewById(R.id.rotHeader);
			rotX = (TextView) rootView.findViewById(R.id.rotX);
			rotY = (TextView) rootView.findViewById(R.id.rotY);
			rotZ = (TextView) rootView.findViewById(R.id.rotZ);
			rotTotal = (TextView) rootView.findViewById(R.id.rotTotal);
			magStatus = (TextView) rootView.findViewById(R.id.magStatus);
			magHeader = (TextView) rootView.findViewById(R.id.magHeader);
			magX = (TextView) rootView.findViewById(R.id.magX);
			magY = (TextView) rootView.findViewById(R.id.magY);
			magZ = (TextView) rootView.findViewById(R.id.magZ);
			magTotal = (TextView) rootView.findViewById(R.id.magTotal);
			orStatus = (TextView) rootView.findViewById(R.id.orStatus);
			orHeader = (TextView) rootView.findViewById(R.id.orHeader);
			orAzimuth = (TextView) rootView.findViewById(R.id.orAzimuth);
			orAziText = (TextView) rootView.findViewById(R.id.orAziText);
			orPitch = (TextView) rootView.findViewById(R.id.orPitch);
			orRoll = (TextView) rootView.findViewById(R.id.orRoll);
			miscHeader = (TextView) rootView.findViewById(R.id.miscHeader);
			tempStatus = (TextView) rootView.findViewById(R.id.tempStatus);
			tempHeader = (TextView) rootView.findViewById(R.id.tempHeader);
			metTemp = (TextView) rootView.findViewById(R.id.metTemp);
			pressureStatus = (TextView) rootView.findViewById(R.id.pressureStatus);
			pressureHeader = (TextView) rootView.findViewById(R.id.pressureHeader);
			metPressure = (TextView) rootView.findViewById(R.id.metPressure);
			humidStatus = (TextView) rootView.findViewById(R.id.humidStatus);
			humidHeader = (TextView) rootView.findViewById(R.id.humidHeader);
			metHumid = (TextView) rootView.findViewById(R.id.metHumid);
			lightStatus = (TextView) rootView.findViewById(R.id.lightStatus);
			lightHeader = (TextView) rootView.findViewById(R.id.lightHeader);
			light = (TextView) rootView.findViewById(R.id.light);
			proximityStatus = (TextView) rootView.findViewById(R.id.proximityStatus);
			proximityHeader = (TextView) rootView.findViewById(R.id.proximityHeader);
			proximity = (TextView) rootView.findViewById(R.id.proximity);

			isSensorViewReady = true;

			return rootView;
		}

		@Override
		public void onDestroyView() {
			Log.i("SENSOR_ANALYTICS", "onDestroyView Main Activity Sensor fragment");
			super.onDestroyView();
			isSensorViewReady = false;
		}
	}


	// Tab # 3 - Map Fragment

	public static class MapSectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		public MapSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);

			mapReattach = (ImageButton) rootView.findViewById(R.id.mapReattach);

			mapReattach.setVisibility(View.GONE);
			isMapViewAttached = true;

			OnClickListener clis = new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v == mapReattach) {
						isMapViewAttached = true;
						if (isMapViewReady) {
							mapReattach.setVisibility(View.GONE);
							updateMap();
						}
					}
				}
			};
			mapReattach.setOnClickListener(clis);
			mapMap = new MapView(rootView.getContext());
			((FrameLayout) rootView).addView(mapMap, 0);

			mapMap.setClickable(true);
			mapMap.getMapScaleBar().setVisible(true);
			mapMap.setBuiltInZoomControls(true);
			mapMap.getMapZoomControls().setZoomLevelMin((byte) 10);
			mapMap.getMapZoomControls().setZoomLevelMax((byte) 20);

			if (mapTileCache == null)
				mapTileCache = PersistentTileCache.createTileCache(rootView.getContext(), "MapQuest",
						mapMap.getModel().displayModel.getTileSize(), 1f,
						mapMap.getModel().frameBufferModel.getOverdrawFactor());

			LayerManager layerManager = mapMap.getLayerManager();
			Layers layers = layerManager.getLayers();
			layers.clear();

			float lat = mSharedPreferences.getFloat(SettingsActivity.KEY_PREF_MAP_LAT, 360.0f);
			float lon = mSharedPreferences.getFloat(SettingsActivity.KEY_PREF_MAP_LON, 360.0f);

			if ((lat < 360.0f) && (lon < 360.0f)) {
				mapMap.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
			}

			int zoom = mSharedPreferences.getInt(SettingsActivity.KEY_PREF_MAP_ZOOM, 16);
			mapMap.getModel().mapViewPosition.setZoomLevel((byte) zoom);

			OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{
					"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"
			}, 80);
			onlineTileSource.setName("MapQuest")
					.setAlpha(false)
					.setBaseUrl("/tiles/1.0.0/map/")
					.setExtension("png")
					.setParallelRequestsLimit(8)
					.setProtocol("http")
					.setTileSize(256)
					.setZoomLevelMax((byte) 18)
					.setZoomLevelMin((byte) 0);

			mapDownloadLayer = new TileDownloadLayer(mapTileCache,
					mapMap.getModel().mapViewPosition, onlineTileSource,
					AndroidGraphicFactory.INSTANCE);
			layers.add(mapDownloadLayer);
			mapDownloadLayer.onResume();

			GestureDetector gd = new GestureDetector(rootView.getContext(),
					new GestureDetector.SimpleOnGestureListener() {
						public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
							mapReattach.setVisibility(View.VISIBLE);
							isMapViewAttached = false;
							return false;
						}
					}
			);

			mapMap.setGestureDetector(gd);

			isMapViewReady = true;
			updateLocationProviders(rootView.getContext());


			return rootView;
		}

		@Override
		public void onDestroyView() {

			Log.i("SENSOR_ANALYTICS", "onDestroyView Main Activity MAP fragment");
			LatLong center = mapMap.getModel().mapViewPosition.getCenter();
			byte zoom = mapMap.getModel().mapViewPosition.getZoomLevel();

			SharedPreferences.Editor spEditor = mSharedPreferences.edit();
			spEditor.putFloat(SettingsActivity.KEY_PREF_MAP_LAT, (float) center.latitude);
			spEditor.putFloat(SettingsActivity.KEY_PREF_MAP_LON, (float) center.longitude);
			spEditor.putInt(SettingsActivity.KEY_PREF_MAP_ZOOM, zoom);
			spEditor.commit();

			super.onDestroyView();
			isMapViewReady = false;
		}
	}



	// Tab # 4 - HeartRateMonitor Fragment


	public static class HeartRateFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";
		private Metronome metronome;
		private static final AtomicBoolean processing = new AtomicBoolean(false);

		private static SurfaceView preview = null;
		private static SurfaceHolder previewHolder = null;
		//private static Camera camera = null;

		private static TextView text = null;

		private static WakeLock wakeLock = null;

		private static int averageIndex = 0;
		private static final int averageArraySize = 100;
		private static final int[] averageArray = new int[averageArraySize];

		public static enum TYPE {
			GREEN, RED
		};

		private static TYPE currentType = TYPE.GREEN;

		public static TYPE getCurrent() {
			return currentType;
		}

		private static int beatsIndex = 0;
		private static final int beatsArraySize = 14;
		private static final int[] beatsArray = new int[beatsArraySize];
		private static final long[] timesArray = new long[beatsArraySize];
		private static double beats = 0;
		private static long startTime = 0;

		private static GraphView graphView;
		private static GraphViewSeries exampleSeries;

		static int counter = 0;

		private static final int sampleSize = 256;
		private static final CircularFifoQueue sampleQueue = new CircularFifoQueue(
				sampleSize);
		private static final CircularFifoQueue timeQueue = new CircularFifoQueue(
				sampleSize);


		private static final FFT


				fft = new FFT(sampleSize);

		public HeartRateFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_heart, container, false);
			Log.i("SENSOR_ANALYTICS", "onCreateView Main Activity Heart rate fragment");
			preview = (SurfaceView) rootView.findViewById(R.id.preview);
			previewHolder = preview.getHolder();
			previewHolder.addCallback(surfaceCallback);
			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


			text = (TextView) rootView.findViewById(R.id.text);

			PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
			wakeLock = pm
					.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

			graphView = new LineGraphView(getActivity() // context
					, "Heart rate" // heading
			);
			graphView.setScrollable(true);

			exampleSeries = new GraphViewSeries("Heart rate",
					new GraphViewSeriesStyle(Color.RED, 8),
					new GraphView.GraphViewData[]{});
			graphView.addSeries(exampleSeries);

			graphView.setViewPort(0, 60);
			graphView.setVerticalLabels(new String[]{""});
			graphView.setHorizontalLabels(new String[]{""});
			graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
			graphView.setBackgroundColor(Color.WHITE);

			LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.graph1);
			layout.addView(graphView);

			wakeLock.acquire();

			try {
				camera = Camera.open();
			} catch (RuntimeException e) {
			}


			startTime = System.currentTimeMillis();


			metronome = new Metronome(getActivity());
			metronome.start();
			return rootView;
		}

		public void onPause() {
			super.onPause();

			Log.i("SENSOR_ANALYTICS", "onDestroyView Main Activity HEART fragment");
			wakeLock.release();

			try {
				camera.setPreviewCallback(null);
				camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				camera.stopPreview();
				camera.release();
				camera = null;
			} catch (RuntimeException e) {
			}

			bpm = -1;
		}

		private static PreviewCallback previewCallback = new PreviewCallback() {


			@Override
			public void onPreviewFrame(byte[] data, Camera cam) {
				if (data == null)
					throw new NullPointerException();
				Camera.Size size = cam.getParameters().getPreviewSize();
				if (size == null)
					throw new NullPointerException();

				if (!processing.compareAndSet(false, true))
					return;

				int width = size.width;
				int height = size.height;

				int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(),
						height, width);


				sampleQueue.add((double) imgAvg);
				timeQueue.add(System.currentTimeMillis());

				double[] y = new double[sampleSize];
				double[] x = ArrayUtils.toPrimitive((Double[]) sampleQueue
						.toArray(new Double[0]));
				long[] time = ArrayUtils.toPrimitive((Long[]) timeQueue
						.toArray(new Long[0]));

				if (timeQueue.size() < sampleSize) {
					processing.set(false);

					return;
				}

				double Fs = ((double) timeQueue.size())
						/ (double) (time[timeQueue.size() - 1] - time[0]) * 1000;

				fft.fft(x, y);

				int low = Math.round((float) (sampleSize * 40 / 60 / Fs));
				int high = Math.round((float) (sampleSize * 160 / 60 / Fs));

				int bestI = 0;
				double bestV = 0;
				for (int i = low; i < high; i++) {
					double value = Math.sqrt(x[i] * x[i] + y[i] * y[i]);

					if (value > bestV) {
						bestV = value;
						bestI = i;
					}
				}

				bpm = Math.round((float) (bestI * Fs * 60 / sampleSize));
				bpmQueue.add(bpm);

				text.setText(String.valueOf(bpm));// + "," +
				// String.valueOf(Math.round((float)
				// Fs)));


				counter++;
				exampleSeries.appendData(new GraphView.GraphViewData(counter,
						imgAvg), true, 1000);
				processing.set(false);


			}
		};

		private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {


			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Log.i("SENSOR_ANALYTICS", "surfaceCreated Main Activity Heart rate fragment");
				try {
					camera.setPreviewDisplay(previewHolder);
					camera.setPreviewCallback(previewCallback);
				} catch (Throwable t) {
					Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
				}
			}


			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
									   int height) {
				Log.i("SENSOR_ANALYTICS", "surfaceChanged Main Activity Heart rate fragment");

				camera.startPreview();
				Camera.Parameters parameters = camera.getParameters();

				if(isHeartTabSelected == true)
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				Camera.Size size = getSmallestPreviewSize(width, height, parameters);
				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					Log.d(TAG, "Using width=" + size.width + " height="
							+ size.height);
				}
				camera.setParameters(parameters);
				camera.startPreview();
			}


			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// Ignore
			}
		};

		private static Camera.Size getSmallestPreviewSize(int width, int height,
														  Camera.Parameters parameters) {
			Camera.Size result = null;

			for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
				if (size.width <= width && size.height <= height) {
					if (result == null) {
						result = size;
					} else {
						int resultArea = result.width * result.height;
						int newArea = size.width * size.height;

						if (newArea < resultArea)
							result = size;
					}
				}
			}

			return result;
		}
	}
}

package com.osu.sensoranalytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.View;
import android.text.method.KeyListener;
import 	android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.widget.Button;
import android.util.Log;
import android.widget.CheckBox;
import android.preference.PreferenceManager;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.LinearLayout;
import 	android.view.MotionEvent;
import android.widget.Switch;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity  {


	private EditText serveraddress;

	public static final String KEY_PREF_NOTIFY_FIX = "pref_notify_fix";
	public static final String KEY_PREF_NOTIFY_SEARCH = "pref_notify_search";
	public static final String KEY_PREF_UPDATE_WIFI = "pref_update_wifi";
	public static final String KEY_PREF_UPDATE_NETWORKS = "pref_update_networks";
	public static final String KEY_PREF_UPDATE_NETWORKS_WIFI = Integer.toString(ConnectivityManager.TYPE_WIFI);
	public static final String KEY_PREF_UPDATE_NETWORKS_MOBILE = Integer.toString(ConnectivityManager.TYPE_MOBILE);
	public static final String KEY_PREF_UPDATE_FREQ = "pref_update_freq";
	public static final String KEY_PREF_UPDATE_LAST = "pref_update_last";
	public static final String KEY_PREF_LOC_PROV = "pref_loc_prov";
	public static final String KEY_PREF_LOC_PROV_STYLE = "pref_loc_prov_style.";
	public static final String KEY_PREF_MAP_LAT = "pref_map_lat";
	public static final String KEY_PREF_MAP_LON = "pref_map_lon";
	public static final String KEY_PREF_MAP_ZOOM = "pref_map_zoom";

	private SharedPreferences mSharedPreferences;


	private static EditText editText2;
	private CheckBox chkIos;
	private Switch upload_switch;
	private LinearLayout MainLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);


		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


		editText2 = (EditText) findViewById(R.id.server_address);
		upload_switch = (Switch)findViewById(R.id.uploadswitch);

		boolean defchecked =false;

		if(!prefs.contains("server_address")) {
			editText2.setText("192.168.1.1:8080");

		}
		else {
			editText2.setText(prefs.getString("server_address", null));

		}
		if(!prefs.contains("upload_check")){
			upload_switch.setChecked(false);
		}
		else {
			upload_switch.setChecked(prefs.getBoolean("upload_check", defchecked));
		}




		editText2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				// If it loses focus...
				if (!hasFocus) {
					// Hide soft keyboard.
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText2.getWindowToken(), 0);
				}
			}
		});


		MainLayout = (LinearLayout) findViewById(R.id.MainLayout);

		MainLayout.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				return true;
			}
		});


		editText2.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				prefs.edit().putString("server_address", s.toString()).commit();
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				prefs.edit().putString("server_address", s.toString()).commit();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after)
			{
			}

		});

		upload_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				prefs.edit().putBoolean("upload_check", isChecked).commit();
			}
		});
		}
	}

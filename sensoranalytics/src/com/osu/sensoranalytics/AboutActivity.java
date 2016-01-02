

package com.osu.sensoranalytics;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager;

import com.osu.sensoranalytics.R;

@SuppressWarnings("unused")
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		TextView aboutBuild = (TextView) findViewById(R.id.aboutBuild);
		
		InputStream buildInStream = getResources().openRawResource(R.raw.build);
		ByteArrayOutputStream buildOutStream = new ByteArrayOutputStream();
		
		int i;
		try {
			i = buildInStream.read();
			while (i != -1) {
				if (i >= 32) buildOutStream.write(i);
				i = buildInStream.read();
			}
			buildInStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			aboutBuild.setText(String.format("%s %s (%s)", this.getString(R.string.about_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName, buildOutStream.toString()));
		} catch(PackageManager.NameNotFoundException e) {
			aboutBuild.setText(buildOutStream.toString());
		}
		
		TextView aboutText = (TextView) findViewById(R.id.aboutText);
		//aboutText.setText(Html.fromHtml(this.getString(R.string.about_text)));
	}

}

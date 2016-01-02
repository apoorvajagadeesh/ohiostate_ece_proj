package org.inaturalist.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by apoorvajagadeesh on 15-11-29.
 */
public class CloudActivity extends BaseFragmentActivity {
    private static TextView data;

    @Override
    protected void onStart()
    {
        super.onStart();
        FlurryAgent.onStartSession(this, "J4KDVDHXFJSYX56YQDX9");
        FlurryAgent.logEvent(this.getClass().getSimpleName());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud);
        onDrawerCreate(savedInstanceState);
        data = (TextView)findViewById(R.id.cloudtext);
        //data.setText("Testing");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Apoj","inside run");
                try {
                    Log.d("Apoj","trying http connection");
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpGet httppost = new HttpGet("http://api.carriots.com/streams/?device=defaultDevice@apojosu.apojosu");
                    httppost.setHeader("carriots.apikey", "b8b2f11c98bf1ec63e697a8054a344c5ed9ba3e63d42a06b94061d1d89641e29");
                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    String responseString = new BasicResponseHandler().handleResponse(response);
                    JSONObject jsobj = new JSONObject(responseString);
                    JSONArray array = jsobj.getJSONArray("result");
                    Log.d("Apoj", "response "+array);
                    //setText(data,responseString);
                    String StringBuffer="";
                    for(int i = 0 ; i < array.length() ; i++){
                        Log.d("Apoj", array.getJSONObject(i).getString("data"));
                        StringBuffer = StringBuffer + array.getJSONObject(i).getString("data") +"    ";
                        Log.d("Apoj", array.getJSONObject(i).getString("created_at"));
                        StringBuffer = StringBuffer + array.getJSONObject(i).getString("created_at") + "\n";
                    }
                    setText(data, StringBuffer);


                } catch (Exception e) {
                    // Exception handling
                    Log.d("Apoj", e.toString());
                }
            }
        }).start();


    }

    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}

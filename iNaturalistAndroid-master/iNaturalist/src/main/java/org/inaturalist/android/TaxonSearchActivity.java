package org.inaturalist.android;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TaxonSearchActivity extends SherlockListActivity {
    private static final String LOG_TAG = "TaxonSearchActivity";
    
    public static final String TAXON_ID = "taxon_id";
	public static final String ID_NAME = "id_name";
	public static final String TAXON_NAME = "taxon_name";
	public static final String ICONIC_TAXON_NAME = "iconic_taxon_name";
    public static final String ID_PIC_URL = "id_url";
    public static final String FIELD_ID = "field_id";
    public static final String IS_CUSTOM = "is_custom";

    public static final String SPECIES_GUESS = "species_guess";
    public static final String SHOW_UNKNOWN = "show_unknown";
    public static final int UNKNOWN_TAXON_ID = -1;


    private TaxonAutoCompleteAdapter mAdapter;

    private int mFieldId;

    private ProgressBar mProgress;
    
    private INaturalistApp mApp;
    private boolean mShowUnknown;

    @Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "J4KDVDHXFJSYX56YQDX9");
		FlurryAgent.logEvent(this.getClass().getSimpleName());
	}
    @Override
    public void onResume() {
        super.onResume();
        if (mApp == null) { mApp = (INaturalistApp) getApplicationContext(); }
    }
 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}	


    private ArrayList<JSONObject> autocomplete(String input) {
        ArrayList<JSONObject> resultList = null;

        if (!isNetworkAvailable()) {
            return new ArrayList<JSONObject>();
        }

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(INaturalistService.HOST + "/taxa/search.json");
            sb.append("?q=");
            sb.append(URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONArray predsJsonArray = new JSONArray(jsonResults.toString());

            // Extract the Place descriptions from the results
            resultList = new ArrayList<JSONObject>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    } 
    
    
    private class TaxonAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<JSONObject> mResultList;
        private Context mContext;
        private String mCurrentSearchString;

        public TaxonAutoCompleteAdapter(Context context, int resourceId) {
            super(context, resourceId, new ArrayList<String>());
            
            mContext = context;
            
            mResultList = new ArrayList<JSONObject>();
        }

        @Override
        public int getCount() {
            return (mResultList != null ? mResultList.size() : 0);
        }

        @Override
        public String getItem(int index) {
            try {
                return mResultList.get(index).getString("name");
            } catch (JSONException e) {
                return "";
            }
        }
        
        private void toggleLoading(final boolean isLoading) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isLoading) {
                        getListView().setVisibility(View.GONE);
                        mProgress.setVisibility(View.VISIBLE);
                    } else {
                        mProgress.setVisibility(View.GONE);
                        getListView().setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    
                    if (constraint != null) {
                        if (constraint.length() == 0) {
                            filterResults.values = new ArrayList<JSONObject>();
                            filterResults.count = 0;
                            
                        } else {
                            toggleLoading(true);

                            // Retrieve the autocomplete results.
                            ArrayList<JSONObject> results;
                            mCurrentSearchString = (String) constraint;
                            results = autocomplete(constraint.toString());

                            if (!constraint.equals(mCurrentSearchString)) {
                                // In the meanwhile, new searches were initiated by the user - ignore this result
                                return null;
                            }

                            // Assign the data to the FilterResults
                            filterResults.values = results;
                            filterResults.count = results.size();
                        }
                    }
                    
                    toggleLoading(false);
                    
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0 && results.values != null) {
                        mResultList = (ArrayList<JSONObject>) results.values;
                        if (mShowUnknown) mResultList.add(0, null);
                        notifyDataSetChanged();
                    }
                    else {
                        if ((results != null) && (results.values != null)) {
                            mResultList = (ArrayList<JSONObject>) results.values;
                            if ((mResultList.size() == 0) && (mCurrentSearchString != null) && (mCurrentSearchString.length() > 0)) {
                                // No results - add in the current search string as a custom observation
                                JSONObject customObs = new JSONObject();
                                try {
                                    customObs.put("is_custom", true);
                                    customObs.put("name", mCurrentSearchString);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mResultList.add(customObs);
                            }

                            if (mShowUnknown) mResultList.add(0, null);

                        }
                        
                        notifyDataSetInvalidated();
                    }
                }};
                
                return filter;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.taxon_result_item, parent, false); 
            JSONObject item = mResultList.get(position);
            JSONObject defaultName;
            String displayName = null;

            if (item == null) {
                // It's the unknown taxon row (the first row)
                ((ViewGroup)view.findViewById(R.id.taxon_result)).setVisibility(View.GONE);
                ((ViewGroup)view.findViewById(R.id.unknown_taxon_result)).setVisibility(View.VISIBLE);
                view.setTag(null);
                return view;
            } else {
                ((ViewGroup)view.findViewById(R.id.taxon_result)).setVisibility(View.VISIBLE);
                ((ViewGroup)view.findViewById(R.id.unknown_taxon_result)).setVisibility(View.GONE);
            }

            // Get the taxon display name according to device locale
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Locale deviceLocale = getResources().getConfiguration().locale;
            String deviceLexicon =   deviceLocale.getDisplayLanguage(Locale.ENGLISH);

            try {
				JSONArray taxonNames = item.getJSONArray("taxon_names");
                int minPosition = Integer.MAX_VALUE;
				for (int i = 0; i < taxonNames.length(); i++) {
					JSONObject taxonName = taxonNames.getJSONObject(i);
					String lexicon = taxonName.getString("lexicon");
                    Integer currentPosition = taxonName.getInt("position");
					if ((lexicon.equals(deviceLexicon)) && (currentPosition < minPosition)) {
						// Found the appropriate lexicon for the taxon
						displayName = taxonName.getString("name");
                        minPosition = currentPosition;
					}
				}
			} catch (JSONException e3) {
				e3.printStackTrace();
			}

            if (displayName == null) {
            	// Couldn't extract the display name from the taxon names list - use the default one
            	try {
            		displayName = item.getString("unique_name");
            	} catch (JSONException e2) {
            		displayName = "unknown";
            	}
            	try {
            		defaultName = item.getJSONObject("default_name");
            		displayName = defaultName.getString("name");
            	} catch (JSONException e1) {
            		// alas
            	}
            }
            
            try {
                ImageView idPic = (ImageView) view.findViewById(R.id.id_pic);
                TextView idName = (TextView) view.findViewById(R.id.id_name);
                TextView idTaxonName = (TextView) view.findViewById(R.id.id_taxon_name);

                if (item.optBoolean("is_custom", false)) {
                    // Custom-named taxon
                    idName.setText(item.getString("name"));
                    idTaxonName.setVisibility(View.GONE);
                    idPic.setImageResource(R.drawable.iconic_taxon_unknown);
                } else {
                    idName.setText(displayName);
                    idTaxonName.setText(item.getString("name"));
                    idTaxonName.setTypeface(null, Typeface.ITALIC);
                    UrlImageViewHelper.setUrlDrawable(idPic, item.getString("image_url"));
                }
                view.setTag(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }

    } 
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   @Override
   public void onBackPressed() {
       setResult(RESULT_CANCELED);
       finish();
   }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (mApp == null) { mApp = (INaturalistApp) getApplicationContext(); }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setIcon(android.R.color.transparent);
        
        LayoutInflater li = LayoutInflater.from(this);
        View customView = li.inflate(R.layout.taxon_search_action_bar, null);
        actionBar.setCustomView(customView);
        actionBar.setLogo(R.drawable.up_icon);
       
        setContentView(R.layout.taxon_search);
        
        Intent intent = getIntent();
        mFieldId = intent.getIntExtra(FIELD_ID, 0);
        
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mProgress.setVisibility(View.GONE);
        
        mAdapter = new TaxonAutoCompleteAdapter(getApplicationContext(), R.layout.taxon_result_item);
        final EditText autoCompView = (EditText) customView.findViewById(R.id.search_text);
        
        autoCompView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) mAdapter.getFilter().filter(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        String initialSearch = intent.getStringExtra(SPECIES_GUESS);
        mShowUnknown = intent.getBooleanExtra(SHOW_UNKNOWN, false);

        if ((initialSearch != null) && (initialSearch.trim().length() > 0)) {
        	autoCompView.setText(initialSearch);
        	autoCompView.setSelection(initialSearch.length());
            autoCompView.requestFocus();

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(autoCompView, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        } else {
        	autoCompView.setText("");
        }

        setListAdapter(mAdapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        JSONObject item = (JSONObject) v.getTag();
        try {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();

            if (item != null) {
                String displayName = null;
                // Get the taxon display name according to configuration of the current iNat network
                String inatNetwork = mApp.getInaturalistNetworkMember();
                String networkLexicon = mApp.getStringResourceByName("inat_lexicon_" + inatNetwork);
                try {
                    JSONArray taxonNames = item.getJSONArray("taxon_names");
                    for (int i = 0; i < taxonNames.length(); i++) {
                        JSONObject taxonName = taxonNames.getJSONObject(i);
                        String lexicon = taxonName.getString("lexicon");
                        if (lexicon.equals(networkLexicon)) {
                            // Found the appropriate lexicon for the taxon
                            displayName = taxonName.getString("name");
                            break;
                        }
                    }
                } catch (JSONException e3) {
                    e3.printStackTrace();
                }

                if (displayName == null) {
                    // Couldn't extract the display name from the taxon names list - use the default one
                    try {
                        displayName = item.getString("unique_name");
                    } catch (JSONException e2) {
                        displayName = "unknown";
                    }
                    try {
                        JSONObject defaultName = item.getJSONObject("default_name");
                        displayName = defaultName.getString("name");
                    } catch (JSONException e1) {
                        // alas
                    }
                }

                if (item.optBoolean("is_custom", false)) {
                    // Custom named taxon
                    bundle.putString(TaxonSearchActivity.ID_NAME, item.getString("name"));
                    bundle.putBoolean(TaxonSearchActivity.IS_CUSTOM, true);
                } else {
                    bundle.putString(TaxonSearchActivity.ID_NAME, displayName);
                    bundle.putString(TaxonSearchActivity.TAXON_NAME, item.getString("name"));
                    bundle.putString(TaxonSearchActivity.ICONIC_TAXON_NAME, item.getString("iconic_taxon_name"));
                    bundle.putString(TaxonSearchActivity.ID_PIC_URL, item.getString("image_url"));
                    bundle.putBoolean(TaxonSearchActivity.IS_CUSTOM, false);
                    bundle.putInt(TaxonSearchActivity.TAXON_ID, item.getInt("id"));

                }
                bundle.putInt(TaxonSearchActivity.FIELD_ID, mFieldId);

            } else {
                // Unknown taxon
                bundle.putInt(TaxonSearchActivity.TAXON_ID, UNKNOWN_TAXON_ID);
            }

            intent.putExtras(bundle);

            setResult(RESULT_OK, intent);
            finish();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
         ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         return activeNetworkInfo != null && activeNetworkInfo.isConnected();
     }

}

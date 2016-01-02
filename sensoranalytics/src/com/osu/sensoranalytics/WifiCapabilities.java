

package com.osu.sensoranalytics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.util.Log;

public abstract class WifiCapabilities {


    public static final String PSK = "PSK";
    public static final String WEP = "WEP";
    public static final String EAP = "EAP";
    public static final String OPEN = "Open";

    public static final String[] EAP_METHOD = { "PEAP", "TLS", "TTLS" };

    private static final String ADHOC_CAPABILITY = "[IBSS]";
    private static final String ENTERPRISE_CAPABILITY = "-EAP-";

    public static final String BSSID_ANY = "any";
    public static final int NETWORK_ID_NOT_SET = -1;

    static final int NETWORK_ID_ANY = -2;
   
    public static final int MATCH_NONE = 0;
    public static final int MATCH_WEAK = 1;
    public static final int MATCH_STRONG = 2;
    public static final int MATCH_EXACT = 3;

    public static final int IDENTITY = 0;
    public static final int ANONYMOUS_IDENTITY = 1;
    public static final int CLIENT_CERT = 2;
    public static final int CA_CERT = 3;
    public static final int PRIVATE_KEY = 4;
    public static final int MAX_ENTRPRISE_FIELD = 5;
    
    public static final String CAPTIVE_PORTAL_SERVER = "clients3.google.com";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    
    public static final int NETWORK_AVAILABLE = 0;
    public static final int NETWORK_CAPTIVE_PORTAL = 1;
    public static final int NETWORK_ERROR = 2;

    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { WEP, PSK, EAP };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
       
        return OPEN;
    }

    public static boolean isAdhoc(ScanResult scanResult) {
        return scanResult.capabilities.contains(ADHOC_CAPABILITY);
    }
    public static boolean isEnterprise(ScanResult scanResult) {
        return scanResult.capabilities.contains(ENTERPRISE_CAPABILITY);
    }

    public static int getNetworkConnectivity() {
        HttpURLConnection urlConnection = null;

        String mUrl = "http://" + CAPTIVE_PORTAL_SERVER + "/generate_204";
        Log.d(WifiCapabilities.class.getSimpleName(), "Checking " + mUrl + " to see if we're behind a captive portal");
        try {
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // we got a valid response, but not from the real google
            return (urlConnection.getResponseCode() != 204)?NETWORK_CAPTIVE_PORTAL:NETWORK_AVAILABLE;
        } catch (IOException e) {
            Log.d(WifiCapabilities.class.getSimpleName(), "Probably not a portal: exception " + e);
            return NETWORK_ERROR;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}

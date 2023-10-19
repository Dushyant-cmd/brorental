package com.brorental.brorental.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utility {
    private static String TAG = "Utility.java";
    //To check connectivity with network
    public static boolean isNetworkAvailable(Context ctx) {
        try {
            ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            return manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                    manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
        } catch (Exception e) {
            Log.d(TAG, "isNetworkAvailable: " + e);
        }
        return false;
    }
}

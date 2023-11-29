package com.brorental.brorental.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;

import com.brorental.brorental.broadcasts.ConnectionBroadcast;
import com.brorental.brorental.interfaces.UtilsInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {
    public static String TAG = "Utility.java", rupeeIcon = "\u20B9";
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

    public static void hideKeyboardFrom(Activity activity, Context context, View view1, boolean isFragment) {
        try {
            if(isFragment) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
            } else {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = activity.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.d(TAG, "hideKeyboardFrom: " + e);
        }
    }

    public static void registerConnectivityBR(Context ctx, AppClass appClass) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ctx.registerReceiver(new ConnectionBroadcast(), intentFilter);
    }

    public static void noNetworkDialog(Context ctx, UtilsInterface.RefreshInterface refreshInterface) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage("No connection");
            builder.setCancelable(false);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (isNetworkAvailable(ctx)) {
                        dialogInterface.dismiss();
                        refreshInterface.refresh(0);
                    } else
                        noNetworkDialog(ctx, refreshInterface);
                }
            });
            builder.create().show();
        } catch (Exception e) {
            Log.d(TAG, "noNetworkDialog: " + e);
        }
    }

    public static String getCurrDateAndTime(Context ctx) {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
        String dateAndTime = spf.format(date);
        return dateAndTime;
    }
}

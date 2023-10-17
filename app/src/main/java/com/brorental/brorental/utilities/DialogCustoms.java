package com.brorental.brorental.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class DialogCustoms {
    private static String TAG = "DialogCustoms.java";
    /**Below method will display a snackbar */
    public static void showSnackBar(Context ctx, String msg, View root) {
        try {
            Snackbar bar = Snackbar.make(root, msg, Snackbar.LENGTH_SHORT);
            bar.show();
        } catch (Exception e) {
            Log.d(TAG, "showSnackBar: " + e);
        }
    }
}

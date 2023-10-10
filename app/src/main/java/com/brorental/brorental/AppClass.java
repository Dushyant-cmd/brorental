package com.brorental.brorental;

import android.app.Application;

import com.brorental.brorental.localdb.SharedPref;

public class AppClass extends Application {
    public SharedPref sharedPref;
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();

    }

    private void initialize() {
        sharedPref = new SharedPref(getApplicationContext());
    }
}

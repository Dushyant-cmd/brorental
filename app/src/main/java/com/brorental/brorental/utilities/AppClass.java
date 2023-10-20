package com.brorental.brorental.utilities;

import android.app.Application;

import com.brorental.brorental.localdb.SharedPref;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppClass extends Application {
    public SharedPref sharedPref;
    public FirebaseFirestore firestore;
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        sharedPref = new SharedPref(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
    }
}

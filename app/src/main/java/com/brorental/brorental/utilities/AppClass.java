package com.brorental.brorental.utilities;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.brorental.brorental.broadcasts.ConnectionBroadcast;
import com.brorental.brorental.localdb.SharedPref;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class AppClass extends Application {
    public SharedPref sharedPref;
    public FirebaseFirestore firestore;
    public FirebaseStorage storage;
    public ConnectionBroadcast broadcast;
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        sharedPref = new SharedPref(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        broadcast = new ConnectionBroadcast();
    }
}

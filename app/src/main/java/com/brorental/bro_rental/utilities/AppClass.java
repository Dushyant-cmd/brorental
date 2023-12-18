package com.brorental.bro_rental.utilities;

import android.app.Application;

import com.brorental.bro_rental.broadcasts.ConnectionBroadcast;
import com.brorental.bro_rental.localdb.SharedPref;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
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
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        sharedPref = new SharedPref(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        broadcast = new ConnectionBroadcast();
    }
}

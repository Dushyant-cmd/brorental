package com.brorental.brorental.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.brorental.brorental.R;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;

public class RentActivity extends AppCompatActivity {
    private AppClass appClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RentActivity.this, appClass);
    }
}
package com.brorental.brorental.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityRideBinding;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;

public class RideActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRideBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ride);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RideActivity.this, appClass);
        setListeners();
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
package com.brorental.brorental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.MainActivity;
import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private String TAG = "SplashActivity.java";
    private ActivitySplashBinding binding;
    private AppClass appClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(SplashActivity.this, R.layout.activity_splash);
        appClass = (AppClass) getApplication();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + appClass.sharedPref.isFirstTime());
                if(!appClass.sharedPref.isFirstTime()) {
                    if(!appClass.sharedPref.isLogin()) {
                        Intent i = new Intent(SplashActivity.this, SignUpAndLogin.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }
                } else {
                    appClass.sharedPref.setFirstTime(false);
                    Intent i = new Intent(SplashActivity.this, ScreenSliderActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            }
        }, 2000);
    }
}
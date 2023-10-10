package com.brorental.brorental.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityScreenSliderBinding;

public class ScreenSliderActivity extends AppCompatActivity {

    private String TAG = "SliderActivity.java";

    private ActivityScreenSliderBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ScreenSliderActivity.this, R.layout.activity_screen_slider);
//        ScreenSliderAdapter adapter = new ScreenSliderAdapter(getSupportFragmentManager());
//        binding.viewPager.setAdapter();
    }
}
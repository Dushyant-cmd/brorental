package com.brorental.brorental.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.adapters.SliderAdapter;
import com.brorental.brorental.databinding.ActivityRentDetailsBinding;
import com.brorental.brorental.utilities.AppClass;

public class RentDetailsActivity extends AppCompatActivity {
    private ActivityRentDetailsBinding binding;
    private String TAG = "RentDetailsActivity.java";
    private AppClass application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent_details);
        SliderAdapter adapter = new SliderAdapter(getSupportFragmentManager());
        application = (AppClass) getApplication();
        adapter.addTitle("");
        adapter.addTitle("");
        adapter.addTitle("");
        binding.viewPager.setAdapter(adapter);
        binding.dotsIndicator.attachTo(binding.viewPager);
    }
}
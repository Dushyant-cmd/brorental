package com.brorental.brorental.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityRentDetailsBinding;

public class RentDetailsActivity extends AppCompatActivity {
    private ActivityRentDetailsBinding binding;
    private String TAG = "RentDetailsActivity.java";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent_details);

    }
}
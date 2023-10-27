package com.brorental.brorental.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityProfileBinding;
import com.brorental.brorental.fragments.ProfileEditDetails;
import com.brorental.brorental.utilities.AppClass;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private String TAG = "ProfileActivity.java";
    private AppClass appClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        appClass = (AppClass) getApplication();
        binding.nameTV.setText(appClass.sharedPref.getUser().getName());
        binding.pinTV.setText("Pin: " + appClass.sharedPref.getUser().getPin());
        binding.mobTV.setText("Mobile: " + appClass.sharedPref.getUser().getMobile());
        binding.mobTV.setText("Alternate Mobile: " + appClass.sharedPref.getAlternateMob());

        Glide.with(this).load("https://i.stack.imgur.com/l60Hf.png").into(binding.profileCirIV);
        setListeners();
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.editTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileEditDetails())

                        .addToBackStack(null).commit();
            }
        });
    }
}
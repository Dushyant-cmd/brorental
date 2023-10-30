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
        binding.altMobTV.setText("Alternate Mobile: " + appClass.sharedPref.getAlternateMob());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.profile_24).into(binding.profileCirIV);
        Glide.with(this).load(appClass.sharedPref.getAadhaarImg()).placeholder(R.drawable.profile_24).into(binding.aadharIV);
        Glide.with(this).load(appClass.sharedPref.getDLImg()).placeholder(R.drawable.profile_24).into(binding.dLImgView);
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
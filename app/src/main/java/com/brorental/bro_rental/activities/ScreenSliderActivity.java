package com.brorental.bro_rental.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.R;
import com.brorental.bro_rental.databinding.ActivityScreenSliderBinding;
import com.brorental.bro_rental.utilities.Utility;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenSliderActivity extends AppCompatActivity {

    private String TAG = "SliderActivity.java";

    private ActivityScreenSliderBinding binding;
    private AppClass application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(ScreenSliderActivity.this, R.layout.activity_screen_slider);
        application = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(ScreenSliderActivity.this, application);
        List<SlideModel> list = new ArrayList<>();
        list.add(new SlideModel(R.drawable.slider_1, ScaleTypes.FIT));
        list.add(new SlideModel(R.drawable.slider_2, ScaleTypes.FIT));
        list.add(new SlideModel(R.drawable.slider_3, ScaleTypes.FIT));
        binding.imageSlider.setImageList(list, ScaleTypes.FIT);
        setListeners();
    }

    private void setListeners() {
        binding.skipBtn.setOnClickListener(view -> {
            application.sharedPref.setFirstTime(false);
            Intent i = new Intent(ScreenSliderActivity.this, SignUpAndLogin.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }
}
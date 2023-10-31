package com.brorental.brorental.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityLoginBinding;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;

public class SignUpAndLogin extends AppCompatActivity {
    private String TAG = "LoginActivity.java";
    private ActivityLoginBinding binding;
    private AppClass appClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(SignUpAndLogin.this, appClass);
        binding.nextBtn.setOnClickListener(view -> {
            Intent i = new Intent(this, OtpActivity.class);
            i.putExtra("phone", binding.eTMobileNumber.getText().toString());
            i.putExtra("referCode", "234123");
            startActivity(i);
        });
    }
}
package com.brorental.brorental;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.activities.OtpActivity;
import com.brorental.brorental.databinding.ActivityLoginBinding;

public class SignUpAndLogin extends AppCompatActivity {
    private String TAG = "LoginActivity.java";
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.nextBtn.setOnClickListener(view -> {
            Intent i = new Intent(this, OtpActivity.class);
            i.putExtra("phone", binding.eTMobileNumber.getText().toString());
            i.putExtra("referCode", "234123");
            startActivity(i);
        });
    }
}
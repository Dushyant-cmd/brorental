package com.brorental.brorental;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.brorental.brorental.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity.java";
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

    }
}
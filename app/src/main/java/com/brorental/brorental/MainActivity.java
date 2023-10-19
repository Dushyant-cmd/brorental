package com.brorental.brorental;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.brorental.brorental.databinding.ActivityMainBinding;
import com.brorental.brorental.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String TAG = "MainBinding.java";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, 0, 0);
        toggle.syncState();
        binding.drawerLayout.addDrawerListener(toggle);
        binding.searchLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(new SearchFragment());
            }
        });
    }

    private void openFragment(SearchFragment searchFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fT = fm.beginTransaction();
        fT.replace(R.id.fragmentContainer, searchFragment);
        fT.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fT.addToBackStack(null);
        fT.commit();
    }
}
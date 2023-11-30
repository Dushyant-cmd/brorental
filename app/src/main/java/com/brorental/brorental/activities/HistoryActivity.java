package com.brorental.brorental.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.MainActivity;
import com.brorental.brorental.R;
import com.brorental.brorental.adapters.HistoryPagerAdapter;
import com.brorental.brorental.databinding.ActivityHistoryBinding;
import com.brorental.brorental.fragments.RentHistoryFragment;
import com.brorental.brorental.fragments.RideHistoryFragment;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.brorental.brorental.utilities.Utility;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityHistoryBinding binding;
    private HistoryPagerAdapter viewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_history);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(HistoryActivity.this, appClass);
        viewPagerAdapter = new HistoryPagerAdapter(this);
        viewPagerAdapter.addFragmentAndTitle(new RentHistoryFragment());
        viewPagerAdapter.addFragmentAndTitle(new RideHistoryFragment());
        binding.viewPager.setAdapter(viewPagerAdapter);
        //ViewPager2 need a TabLayoutMediator instance where pass tabLayout, viewPager2, anonymous
        //class
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                switch(position) {
                    case 0:
                        tab.setText("Rent History");
                        break;
                    case 1:
                        tab.setText("Ride History");
                        break;
                    default:
                        break;
                }
            }
        }).attach();

        if (appClass.sharedPref.getStatus().matches("pending")) {
            DialogCustoms.noKycDialog(HistoryActivity.this, HistoryActivity.this, appClass);
            Toast.makeText(HistoryActivity.this, "Upload Profile.", Toast.LENGTH_SHORT).show();
        }

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
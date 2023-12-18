package com.brorental.bro_rental.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bro_rental.R;
import com.brorental.bro_rental.adapters.HistoryPagerAdapter;
import com.brorental.bro_rental.databinding.ActivityHistoryBinding;
import com.brorental.bro_rental.fragments.RentHistoryFragment;
import com.brorental.bro_rental.fragments.RideHistoryFragment;
import com.brorental.bro_rental.interfaces.UtilsInterface;
import com.brorental.bro_rental.models.User;
import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.utilities.DialogCustoms;
import com.brorental.bro_rental.utilities.ProgressDialog;
import com.brorental.bro_rental.utilities.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;

public class HistoryActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityHistoryBinding binding;
    private HistoryPagerAdapter viewPagerAdapter;
    private String TAG = "HistoryActivity.java";

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
            DialogCustoms.noKycDialog(HistoryActivity.this, HistoryActivity.this, appClass, new UtilsInterface.NoKycInterface() {
                @Override
                public void refresh(AlertDialog alertDialog) {
                    getProfile(alertDialog);
                }
            });
            Toast.makeText(HistoryActivity.this, "Upload Profile.", Toast.LENGTH_SHORT).show();
        }

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getProfile(AlertDialog alertDialog) {
        android.app.AlertDialog pDialog = ProgressDialog.createAlertDialog(HistoryActivity.this);
        pDialog.show();
        appClass.firestore.collection("users").document(appClass.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
                        pDialog.dismiss();
                        appClass.sharedPref.saveUser(new User(d.getString("name"), d.getString("mobile"), d.getString("pin"),
                                d.getString("totalRentItem"), d.getString("totalRides"), true,
                                d.getString("profileUrl"), d.getString("wallet")));
                        appClass.sharedPref.setAadhaarImg(d.getString("aadhaarImgUrl"));
                        appClass.sharedPref.setAadhaarPath(d.getString("aadhaarImgPath"));
                        appClass.sharedPref.setDLImg(d.getString("drivingLicenseImg"));
                        appClass.sharedPref.setDLPath(d.getString("drivingLicImgPath"));
                        appClass.sharedPref.setProfilePath(d.getString("profileImgPath"));
                        appClass.sharedPref.setStatus(d.getString("status"));
                        onActivityResult(101, RESULT_OK, null);

                        if(alertDialog != null)
                            if(!appClass.sharedPref.getStatus().equalsIgnoreCase("pending")) {
                                alertDialog.dismiss();
                            }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }
}
package com.brorental.brorental.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityRideBinding;
import com.brorental.brorental.interfaces.UtilsInterface;
import com.brorental.brorental.models.User;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.brorental.brorental.utilities.ProgressDialog;
import com.brorental.brorental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RideActivity extends AppCompatActivity {
    private AppClass appClass;
    private ActivityRideBinding binding;
    private String[] fromArr;
    private String[] toArr;
    private boolean isReadyRide = false;
    private String from = "", to = "";
    private String TAG = "RideActivity.java";
    private long amt, dis;
    private UtilsInterface.RideInterface refreshListener;
    private AlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ride);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RideActivity.this, appClass);
        String status = appClass.sharedPref.getStatus();
        if (status.equalsIgnoreCase("pending")) {
            DialogCustoms.noKycDialog(RideActivity.this, this, appClass, new UtilsInterface.NoKycInterface() {
                @Override
                public void refresh(androidx.appcompat.app.AlertDialog alertDialog) {
                    getProfile(alertDialog);
                }
            });
            Toast.makeText(this, "Upload Profile.", Toast.LENGTH_SHORT).show();
            return;
        }

        getData();
        setListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(RideActivity.this)) {
            queries();
        } else {
            Utility.noNetworkDialog(RideActivity.this, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
    }

    private void queries() {
        appClass.firestore.collection("appData")
                .document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        fromArr = task.getResult().getString("from").split(",");
                        toArr = task.getResult().getString("to").split(",");
                        List<String> fromList = new ArrayList<>();
                        List<String> toList = new ArrayList<>();
                        fromList.add("Select From");
                        toList.add("Select To");
                        Collections.addAll(fromList, fromArr);
                        Collections.addAll(toList, toArr);
                        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(RideActivity.this, android.R.layout.simple_spinner_dropdown_item, fromList);
                        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(RideActivity.this, android.R.layout.simple_spinner_dropdown_item, toList);
                        binding.spinnerFrom.setAdapter(fromAdapter);
                        binding.spinnerTo.setAdapter(toAdapter);
                        pDialog = ProgressDialog.createAlertDialog(RideActivity.this);
                    }
                });
    }

    private void setListeners() {
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.tvRideViewAll.setOnClickListener(v -> {
            Intent i = new Intent(RideActivity.this, HistoryActivity.class);
            startActivity(i);
        });

        binding.spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                from = binding.spinnerFrom.getSelectedItem().toString();
                if (!to.toLowerCase().contains("select") && !from.toLowerCase().contains("select")) {
                    getRideDetails(from, to);
                } else
                    binding.textLL.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //no selected
            }
        });

        binding.spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                to = binding.spinnerTo.getSelectedItem().toString();
                if (!to.toLowerCase().contains("select") && !from.toLowerCase().contains("select")) {
                    getRideDetails(from, to);
                } else
                    binding.textLL.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //no selected
            }
        });

        binding.btnAdd.setOnClickListener(view -> {
            if (!from.toLowerCase().contains("select") && !to.toLowerCase().contains("select") && isReadyRide) {
                checkAndAddRide();
                binding.rideLL.setVisibility(View.GONE);
                binding.progressLottie.setVisibility(View.VISIBLE);
            } else {
                DialogCustoms.showSnackBar(RideActivity.this, "Please change pickup and destination.", binding.getRoot());
            }
        });
    }


    private void getProfile(androidx.appcompat.app.AlertDialog alertDialog) {
        android.app.AlertDialog pDialog = ProgressDialog.createAlertDialog(RideActivity.this);
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

                        if (alertDialog != null)
                            if (!appClass.sharedPref.getStatus().equalsIgnoreCase("pending")) {
                                alertDialog.dismiss();
                                getData();
                            }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                    }
                });
    }

    private void checkAndAddRide() {
        appClass.firestore.collection("partners").document(appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long totalRides = Long.parseLong(task.getResult().getString("totalRides"));
                            if (totalRides <= 3) {
                                appClass.firestore.collection("ids").document("appid").get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot d = task.getResult();
                                                    long rideId = d.getLong("rideId") + 1;
                                                    updateRideId(rideId, totalRides);
                                                    addRide(rideId);
                                                } else {
                                                    Log.d(TAG, "onComplete: " + task.getException());
                                                }
                                            }
                                        });
                            } else {
                                DialogCustoms.showSnackBar(RideActivity.this, "Complete previous 3 rides", binding.getRoot());
                            }
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void addRide(long rideId) {
        String docId = UUID.randomUUID().toString();
        HashMap<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("to", to);
        map.put("amount", amt);
        map.put("distance", dis);
        map.put("broRentalId", appClass.sharedPref.getUser().getPin());
        map.put("broRentalNumber", appClass.sharedPref.getUser().getMobile());
        map.put("broPartnerId", "");
        map.put("profileUrl", appClass.sharedPref.getUser().getProfileUrl());
        map.put("endTimestamp", 0);
        map.put("startTimestamp", 0);
        map.put("paymentMode", "");
        map.put("name", appClass.sharedPref.getUser().getName());
        map.put("rideId", rideId);
        map.put("pin", docId);
        map.put("status", "pending");
        map.put("timestamp", System.currentTimeMillis());
        appClass.firestore.collection("rideHistory")
                .document(String.valueOf(rideId))
                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.rideLL.setVisibility(View.VISIBLE);
                        binding.progressLottie.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            pDialog.dismiss();
                            Intent i = new Intent(RideActivity.this, HistoryActivity.class);
                            startActivity(i);
                            Toast.makeText(RideActivity.this, "Ride Added Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            pDialog.dismiss();
                            DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                        }
                    }
                });
    }

    private void updateRideId(long rideId, long totalRides) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("rideId", rideId);
        appClass.firestore.collection("ids").document("appid")
                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: " + "success");
                    }
                });

        totalRides += 1;
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("totalRides", String.valueOf(totalRides));
        long finalTotalRides = totalRides;
        appClass.firestore.collection("partners").document(appClass.sharedPref.getUser().getPin())
                .update(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            appClass.sharedPref.setTotalRides(String.valueOf(finalTotalRides));
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: err:" + task.getException());
                        }
                    }
                });
    }

    private void getRideDetails(String from, String to) {
        pDialog.show();
        appClass.firestore.collection("points")
                .whereEqualTo("from", from)
                .whereEqualTo("to", to)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            pDialog.dismiss();
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if (!documents.isEmpty()) {
                                binding.textLL.setVisibility(View.VISIBLE);
                                DocumentSnapshot d = documents.get(0);
                                amt = d.getLong("amount");
                                dis = d.getLong("distance");

                                binding.tvRideAmt.setText("\u20b9 " + amt);
                                binding.tvDis.setText(dis + " /km");
                                isReadyRide = true;
                            } else {
                                binding.textLL.setVisibility(View.GONE);
                                isReadyRide = false;
                            }
                        } else {
                            pDialog.dismiss();
                            isReadyRide = false;
                            DialogCustoms.showSnackBar(RideActivity.this, task.getException().getMessage(), binding.getRoot());
                        }
                    }
                });
    }
}
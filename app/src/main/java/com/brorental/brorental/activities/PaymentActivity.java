package com.brorental.brorental.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.brorental.brorental.R;
import com.brorental.brorental.localdb.SharedPref;
import com.brorental.brorental.models.RentItemModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;


public class PaymentActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    SharedPref sharedPreferences;
    String rechargeAmt, advertId, broPartnerId, rentStart, rentEnd, hours;
    String tag = "PaymentActivity.java";
    final int UPI_PAYMENT = 0;
    private String TAG = "PaymentActivity.java";
    private JSONObject jsonData;
    private RentItemModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SharedPref(PaymentActivity.this);
        try {
            if(getIntent().hasExtra("rentBundle")) {
                Bundle bundle = getIntent().getBundleExtra("rentBundle");
                rechargeAmt = bundle.getString("amt");
                advertId = bundle.getString("id");
                jsonData = new JSONObject(Objects.requireNonNull(bundle.getString("data")));
                model = new Gson().fromJson(String.valueOf(jsonData), RentItemModel.class);
                broPartnerId = jsonData.getString("broPartnerId");
                hours = String.valueOf(bundle.getLong("hours"));
                rentStart = bundle.getString("rentStartDate");
                rentEnd = bundle.getString("rentEndDate");
            }
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e);
        }

        Log.d(TAG, "onCreate: " + sharedPreferences.getUser().getWallet());
        if (isNetworkConnected()) {
            queries();
        } else {
            openAlertDialog();
        }
    }

    public void queries() {
        firestore.collection("appData").document("lowerSettings").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                //Upi transaction with Intent
                                payUsingUpi(rechargeAmt, task.getResult().getString("upi"), "BroRental", "BroRental Cooperation");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "No Upi App is Found", Toast.LENGTH_LONG).show();
                                Log.v(tag, e + "");
                                onBackPressed();
                            }
                        } else {
                            Log.v(tag, task.getException() + "");
                        }
                    }
                });
    }

    public void openAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Check Internet Connection");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    queries();
                } else {
                    openAlertDialog();
                }
            }
        });
        builder.create().show();
    }

    public void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                Log.v(tag, resultCode + "");
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        //Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        //Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isNetworkConnected()) {
            //result of returned launched Activity
            String str = data.get(0);
            Log.d(tag, "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                // on below line we are getting details about transaction when completed.
                String transactionDet = "done";
                String transactionAmt = rechargeAmt;
                //get current date and time
                Calendar cal = Calendar.getInstance();
                Date date = cal.getTime();
                SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                String dateAndTime = spf.format(date);
                Log.v(tag, dateAndTime);
                if (transactionAmt != null) {
                    long currentUserBalance = Long.parseLong(sharedPreferences.getUser().getWallet());
                    long updatedBalance = currentUserBalance - Long.parseLong(transactionAmt);
                    //Map interface subclass instance contains value object in key-value pair where key must be a String.
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("wallet", String.valueOf(updatedBalance));
                    firestore.collection("users").document(sharedPreferences.getUser().getPin() + "").update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    sharedPreferences.setWallet(String.valueOf(updatedBalance));
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("amount", transactionAmt);
                                    map.put("date", dateAndTime);
                                    map.put("info", null);
                                    map.put("name", sharedPreferences.getUser().getName());
                                    map.put("status", "completed");
                                    map.put("type", "rent");
                                    map.put("advertisementId", advertId);
                                    map.put("timestamp", System.currentTimeMillis());
                                    map.put("isBroRental", true);
                                    map.put("broRentalId", sharedPreferences.getUser().getPin());
                                    map.put("broPartnerId", broPartnerId);
                                    firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            HashMap<String, Object> map2 = new HashMap<>();
                                            map2.put("rentImages", model.getAdsImageUrl());
                                            map2.put("name", model.getName());
                                            map2.put("advertisementId", model.getId());
                                            map2.put("totalRentCost", rechargeAmt);
                                            map2.put("extraCharge", model.getExtraCharge());
                                            map2.put("id", UUID.randomUUID().toString());
                                            map2.put("rentStartTime", rentStart);
                                            map2.put("rentEndTime", rentEnd);
                                            map2.put("broRentalId", sharedPreferences.getUser().getPin());
                                            map2.put("broPartnerId", broPartnerId);
                                            map2.put("status", "pending");
                                            map2.put("totalHours", hours);
                                            map2.put("perHourCharge", model.getPerHourCharge());
                                            map2.put("paymentMode", "online");
                                            firestore.collection("rentHistory").add(map2)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if(task.isSuccessful()) {
                                                                        Log.v(tag, "success");
                                                                        Toast.makeText(PaymentActivity.this, "Transaction done successfully", Toast.LENGTH_LONG).show();
                                                                    } else {
                                                                        Log.d(TAG, "onComplete: " + task.getException());
                                                                    }
                                                                }
                                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.v(tag, e + "");
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.v(tag, e + "");
                                }
                            });
                }
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }

        onBackPressed();
    }

    //check mobile device is connected to network or not.
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
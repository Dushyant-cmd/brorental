package com.brorental.brorental.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.brorental.R;
import com.brorental.brorental.adapters.PaymentAdapter;
import com.brorental.brorental.databinding.ActivityPaymentHistoryBinding;
import com.brorental.brorental.models.PaymentHistoryModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.brorental.brorental.utilities.ProgressDialog;
import com.brorental.brorental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentHistory extends AppCompatActivity {
    private String TAG = "PaymentHistory.java";
    private ActivityPaymentHistoryBinding binding;
    private AppClass appClass;
    private ArrayList<PaymentHistoryModel> list = new ArrayList<>();
    private PaymentAdapter adapter;
    private long wal;
    private AlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_history);
        appClass = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(PaymentHistory.this, appClass);
        adapter = new PaymentAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        long wal = (Long.parseLong(appClass.sharedPref.getUser().getTotalRent()) * 2500) + Long.parseLong(appClass.sharedPref.getUser().getWallet());
        binding.walletTV.setText(Utility.rupeeIcon + wal);
        pDialog = ProgressDialog.createAlertDialog(PaymentHistory.this);
        setListeners();
        getData();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(this)) {
            getTransactions();
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("connect", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.isNetworkAvailable(PaymentHistory.this)) {
                        bar.dismiss();
                        Toast.makeText(PaymentHistory.this, "Connected", Toast.LENGTH_SHORT).show();
                    } else {
                        bar.dismiss();
                        bar.show();
                    }
                }
            });
        }
    }

    private void setListeners() {
        binding.wdLy.setOnClickListener(view1 -> {
            BottomSheetDialog sheet = new BottomSheetDialog(PaymentHistory.this);
            View view = LayoutInflater.from(PaymentHistory.this).inflate(R.layout.add_cash_sheet, null);
            sheet.setContentView(view);
            Button submitBtn = view.findViewById(R.id.confirmRec);
            Button cancelBtn = view.findViewById(R.id.cancelRec);
            EditText rechargeET = view.findViewById(R.id.rechargeAmt);
            submitBtn.setOnClickListener(v -> {
                wal = Long.parseLong(appClass.sharedPref.getUser().getWallet());
                pDialog.show();
                appClass.firestore.collection("users").document(appClass.sharedPref.getUser().getPin())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    appClass.sharedPref.setTotalRentItem(task.getResult().getString("totalRentItem"));
                                    long ttlRentItem = Long.parseLong(appClass.sharedPref.getUser().getTotalRent());
                                    long holdSecDep = (2500 * ttlRentItem);
                                    long amt = Long.parseLong(rechargeET.getText().toString());
                                    if (!String.valueOf(amt).isEmpty() && amt > 0) {
                                        if (ttlRentItem > 0) {
                                            if (wal > holdSecDep)
                                                wal = wal - holdSecDep;
                                            if (amt <= wal) {
                                                long acWal = Long.parseLong(appClass.sharedPref.getUser().getWallet());
                                                String newWalAmt = "";
                                                if (amt > acWal)
                                                    newWalAmt = String.valueOf(acWal - amt);
                                                else
                                                    newWalAmt = String.valueOf(amt - acWal);
                                                HashMap<String, Object> map1 = new HashMap<>();
                                                map1.put("wallet", newWalAmt);
                                                String finalNewWalAmt = newWalAmt;
                                                appClass.firestore.collection("users")
                                                        .document(appClass.sharedPref.getUser().getPin())
                                                        .update(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    appClass.sharedPref.setWallet(finalNewWalAmt);
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("amount", String.valueOf(amt));
                                                                    map.put("date", Utility.getCurrDateAndTime(PaymentHistory.this));
                                                                    map.put("info", null);
                                                                    map.put("name", appClass.sharedPref.getUser().getName());
                                                                    map.put("status", "pending");
                                                                    map.put("type", "withdraw");
                                                                    map.put("advertisementId", "");
                                                                    map.put("timestamp", System.currentTimeMillis());
                                                                    map.put("isBroRental", false);
                                                                    map.put("broRentalId", appClass.sharedPref.getUser().getPin());
                                                                    map.put("broPartnerId", "");
                                                                    appClass.firestore.collection("transactions")
                                                                            .add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        sheet.dismiss();
                                                                                        pDialog.dismiss();
                                                                                        long wal = (Long.parseLong(appClass.sharedPref.getUser().getTotalRent()) * 2500) + Long.parseLong(appClass.sharedPref.getUser().getWallet());
                                                                                        binding.walletTV.setText(Utility.rupeeIcon + wal);
                                                                                        getData();
                                                                                        DialogCustoms.showSnackBar(PaymentHistory.this, "Withdrawal Successfully", binding.getRoot());
                                                                                    } else {
                                                                                        pDialog.dismiss();
                                                                                        sheet.dismiss();
                                                                                        DialogCustoms.showSnackBar(PaymentHistory.this, "Please check internet and try again", binding.getRoot());
                                                                                        Log.d(TAG, "onComplete: " + task.getException());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    pDialog.dismiss();
                                                                    Toast.makeText(appClass, "Please try again later.", Toast.LENGTH_SHORT).show();
                                                                    Log.d(TAG, "onComplete: " + task.getException());
                                                                }
                                                            }
                                                        });
                                            } else {
                                                pDialog.dismiss();
                                                DialogCustoms.showSnackBar(PaymentHistory.this, "Can't withdraw deposit", binding.getRoot());
                                            }
                                        } else if (ttlRentItem == 0 && amt < wal) {
                                            String newWalAmt = (wal - amt) + "";
                                            HashMap<String, Object> map1 = new HashMap<>();
                                            map1.put("wallet", newWalAmt);
                                            appClass.firestore.collection("users")
                                                    .document(appClass.sharedPref.getUser().getPin())
                                                    .update(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                appClass.sharedPref.setWallet(newWalAmt);
                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("amount", String.valueOf(amt));
                                                                map.put("date", Utility.getCurrDateAndTime(PaymentHistory.this));
                                                                map.put("info", null);
                                                                map.put("name", appClass.sharedPref.getUser().getName());
                                                                map.put("status", "pending");
                                                                map.put("type", "withdraw");
                                                                map.put("advertisementId", "");
                                                                map.put("timestamp", System.currentTimeMillis());
                                                                map.put("isBroRental", false);
                                                                map.put("broRentalId", appClass.sharedPref.getUser().getPin());
                                                                map.put("broPartnerId", "");
                                                                appClass.firestore.collection("transactions")
                                                                        .add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    sheet.dismiss();
                                                                                    pDialog.dismiss();
                                                                                    long wal = (Long.parseLong(appClass.sharedPref.getUser().getTotalRent()) * 2500) + Long.parseLong(appClass.sharedPref.getUser().getWallet());
                                                                                    binding.walletTV.setText(Utility.rupeeIcon + wal);
                                                                                    getData();
                                                                                    DialogCustoms.showSnackBar(PaymentHistory.this, "Withdrawal Successfully", binding.getRoot());
                                                                                } else {
                                                                                    pDialog.dismiss();
                                                                                    sheet.dismiss();
                                                                                    DialogCustoms.showSnackBar(PaymentHistory.this, "Please check internet and try again", binding.getRoot());
                                                                                    Log.d(TAG, "onComplete: " + task.getException());
                                                                                }
                                                                            }
                                                                        });
                                                            } else {
                                                                pDialog.dismiss();
                                                                Toast.makeText(appClass, "Please try again later.", Toast.LENGTH_SHORT).show();
                                                                Log.d(TAG, "onComplete: " + task.getException());
                                                            }
                                                        }
                                                    });
                                        } else {
                                            DialogCustoms.showSnackBar(PaymentHistory.this, "Balance is too low.", binding.getRoot());
                                            pDialog.dismiss();
                                        }
                                    } else {
                                        pDialog.dismiss();
                                        DialogCustoms.showSnackBar(PaymentHistory.this, "Enter valid amount.", binding.getRoot());
                                    }
                                } else {
                                    pDialog.dismiss();
                                }
                            }
                        });
            });

            cancelBtn.setOnClickListener(v -> sheet.dismiss());
            sheet.show();
        });
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions();
            }
        });
    }

    private void getTransactions() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("transactions").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        try {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> dList = task.getResult().getDocuments();
                                list.clear();
                                for (int i = 0; i < dList.size(); i++) {
                                    DocumentSnapshot d = dList.get(i);
                                    list.add(new PaymentHistoryModel(d.getString("advertisementId"), d.getString("amount"),
                                            d.getString("broPartnerId"), d.getString("broRentalId"), d.getString("date"),
                                            d.getString("info"), d.getString("name"), d.getString("type"), d.getString("status"),
                                            d.getBoolean("isBroRental"), d.getLong("timestamp"), d.getString("id")));
                                }

                                adapter.submitList(list);
                            } else {
                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: " + e);
                        }
                    }
                });
    }
}
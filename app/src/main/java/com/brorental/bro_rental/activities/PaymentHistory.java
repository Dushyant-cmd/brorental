package com.brorental.bro_rental.activities;

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

import com.brorental.bro_rental.R;
import com.brorental.bro_rental.adapters.PaymentAdapter;
import com.brorental.bro_rental.databinding.ActivityPaymentHistoryBinding;
import com.brorental.bro_rental.models.PaymentHistoryModel;
import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.utilities.DialogCustoms;
import com.brorental.bro_rental.utilities.ProgressDialog;
import com.brorental.bro_rental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
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
        binding.walletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
        pDialog = ProgressDialog.createAlertDialog(PaymentHistory.this);
        setListeners();
        getData();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(this)) {
            queries();
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
                if (!rechargeET.getText().toString().isEmpty()) {
                    long amt = Long.parseLong(rechargeET.getText().toString());
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
                                        if (amt > 0 && Utility.getTotalWallet(appClass) > 0) {
                                            if (holdSecDep > 0) {
                                                if (amt <= wal) {
                                                    String newWalAmt = "";
                                                    newWalAmt = String.valueOf(wal - amt);
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
                                                                                            binding.walletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
                                                                                            getData();
                                                                                            Toast.makeText(PaymentHistory.this, "Withdrawal Successfully", Toast.LENGTH_SHORT).show();
//                                                                                            DialogCustoms.showSnackBar(PaymentHistory.this, "Withdrawal Successfully", view.getRootView());
                                                                                        } else {
                                                                                            pDialog.dismiss();
                                                                                            sheet.dismiss();
                                                                                            DialogCustoms.showSnackBar(PaymentHistory.this, "Please check internet and try again", view.getRootView());
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
                                                    Toast.makeText(appClass, "Can't withdraw security deposit", Toast.LENGTH_SHORT).show();
//                                                    DialogCustoms.showSnackBar(PaymentHistory.this, "Can't withdraw security deposit", view.getRootView());
                                                }
                                            } else {
                                                String newWalAmt = "";
                                                if (amt > wal)
                                                    newWalAmt = String.valueOf(amt - wal);
                                                else
                                                    newWalAmt = String.valueOf(wal - amt);
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
                                                                                        binding.walletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
                                                                                        getData();
                                                                                        Toast.makeText(PaymentHistory.this, "Withdrawal Successfully", Toast.LENGTH_SHORT).show();
//                                                                                        DialogCustoms.showSnackBar(PaymentHistory.this, "Withdrawal Successfully", view.getRootView());
                                                                                    } else {
                                                                                        pDialog.dismiss();
                                                                                        sheet.dismiss();
                                                                                        DialogCustoms.showSnackBar(PaymentHistory.this, "Please check internet and try again", view.getRootView());
                                                                                        Log.d(TAG, "onComplete: " + task.getException());
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    pDialog.dismiss();
                                                                    sheet.dismiss();
                                                                    Toast.makeText(appClass, "Please try again later.", Toast.LENGTH_SHORT).show();
                                                                    Log.d(TAG, "onComplete: " + task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            Toast.makeText(appClass, "Invalid withdraw", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    } else {
                                        sheet.dismiss();
                                        pDialog.dismiss();
                                    }
                                }
                            });
                }
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
                queries();
            }
        });
    }

    private void queries() {
        //tranactions
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("transactions").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
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
                                adapter.notifyDataSetChanged();
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
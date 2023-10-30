package com.brorental.brorental.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brorental.brorental.MainActivity;
import com.brorental.brorental.R;
import com.brorental.brorental.adapters.PaymentAdapter;
import com.brorental.brorental.databinding.ActivityPaymentHistoryBinding;
import com.brorental.brorental.models.PaymentHistoryModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistory extends AppCompatActivity {
    private String TAG = "PaymentHistory.java";
    private ActivityPaymentHistoryBinding binding;
    private AppClass appClass;
    private ArrayList<PaymentHistoryModel> list = new ArrayList<>();
    private PaymentAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_history);
        appClass = (AppClass) getApplication();
        adapter = new PaymentAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if(Utility.isNetworkAvailable(this)) {
            getTransactions();
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("connect", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utility.isNetworkAvailable(PaymentHistory.this)) {
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

    private void getTransactions() {
        appClass.firestore.collection("transactions").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            if(task.isSuccessful()) {
                                List<DocumentSnapshot> dList = task.getResult().getDocuments();
                                for(int i=0; i<dList.size(); i++) {
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
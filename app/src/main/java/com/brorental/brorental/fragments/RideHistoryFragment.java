package com.brorental.brorental.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brorental.brorental.R;
import com.brorental.brorental.adapters.RideHistoryAdapter;
import com.brorental.brorental.databinding.FragmentRideHistoryBinding;
import com.brorental.brorental.interfaces.UtilsInterface;
import com.brorental.brorental.models.HistoryModel;
import com.brorental.brorental.models.RideHistoryModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RideHistoryFragment extends Fragment {
    private FragmentRideHistoryBinding binding;
    private String TAG = "RideHistoryFragment.java";
    private AppClass appClass;
    private List<RideHistoryModel> list = new ArrayList<>();
    private RideHistoryAdapter adapter;
    private Context context;
    private Activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_history, container, false);
        activity = requireActivity();
        context = requireContext();
        appClass = (AppClass) activity.getApplication();
        adapter = new RideHistoryAdapter(context, appClass);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerView.setAdapter(adapter);
        getData();
        setListeners();
        return binding.getRoot();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(context)) {
            queries();
        } else {
            Utility.noNetworkDialog(context, new UtilsInterface.RefreshInterface() {
                @Override
                public void refresh(int catePosition) {
                    queries();
                }
            });
        }
    }

    private void setListeners() {
        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    private void queries() {
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        appClass.firestore.collection("rideHistory").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if(task.isSuccessful()) {
                            list.clear();
                            for(DocumentSnapshot d: task.getResult().getDocuments()) {
                                RideHistoryModel model = d.toObject(RideHistoryModel.class);
                                list.add(model);
                            }
                            Log.d(TAG, "onComplete: " + list);
                            adapter.submitList(list);
                            adapter.addRefreshListeners(new UtilsInterface.RideHistoryListener() {
                                @Override
                                public void updateStatus(String status, String docId, int pos, RideHistoryModel data) {
                                    //empty.
                                }

                                @Override
                                public void contactListener(String type) {
                                    if(type.equalsIgnoreCase("phone")) {
                                        Intent i = new Intent(Intent.ACTION_DIAL);
                                        i.setData(Uri.parse("tel:" + appClass.sharedPref.getCustomerCareNum()));
                                        activity.startActivity(i);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}
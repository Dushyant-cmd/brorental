package com.brorental.brorental.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.brorental.R;
import com.brorental.brorental.adapters.RideHistoryAdapter;
import com.brorental.brorental.databinding.FragmentRideHistoryBinding;
import com.brorental.brorental.interfaces.UtilsInterface;
import com.brorental.brorental.models.RideHistoryModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.brorental.brorental.utilities.ProgressDialog;
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
    private DocumentSnapshot lastDoc;
    private long page = 0;
    private AlertDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ride_history, container, false);
        activity = requireActivity();
        context = requireContext();
        pDialog = ProgressDialog.createAlertDialog(context);
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
                        if (task.isSuccessful()) {
                            list.clear();
                            List<DocumentSnapshot> dList = task.getResult().getDocuments();
                            for (DocumentSnapshot d : dList) {
                                RideHistoryModel model = d.toObject(RideHistoryModel.class);
                                list.add(model);
                            }
                            Log.d(TAG, "onComplete: " + list);
                            adapter.submitList(list);
                            adapter.notifyDataSetChanged();

                            adapter.addRefreshListeners(new UtilsInterface.RideHistoryListener() {
                                @Override
                                public void updateStatus(String status, String docId, int pos, RideHistoryModel data) {
                                    //empty.
                                }

                                @Override
                                public void contactListener(String type) {
                                    Intent i = new Intent(Intent.ACTION_DIAL);
                                    i.setData(Uri.parse("tel:" + type));
                                    activity.startActivity(i);
                                }
                            });

                            if (!dList.isEmpty())
                                lastDoc = dList.get(dList.size() - 1);

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                                binding.nestedSv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                        //Check if user scrolled till bottom
                                        if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                            Log.v(TAG, "list scroll till bottom");
                                            if (Utility.isNetworkAvailable(requireContext()) && page == 0) {
                                                page++;
                                                loadMoreRideResult();
                                            } else if (!Utility.isNetworkAvailable(requireContext())) {
                                                Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void loadMoreRideResult() {
        pDialog.show();
        appClass.firestore.collection("rideHistory").whereEqualTo("broRentalId", appClass.sharedPref.getUser().getPin())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        page = 0;
                        pDialog.dismiss();
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> dList = task.getResult().getDocuments();
                            if (!dList.isEmpty()) {
                                for (DocumentSnapshot d : dList) {
                                    RideHistoryModel model = d.toObject(RideHistoryModel.class);
                                    list.add(model);
                                }

                                if (!dList.isEmpty())
                                    lastDoc = dList.get(dList.size() - 1);

                                adapter.submitList(list);
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show();
                                page++;
                            }
                        } else {
                            DialogCustoms.showSnackBar(context, "Please try again", binding.getRoot());
                        }
                    }
                });
    }
}
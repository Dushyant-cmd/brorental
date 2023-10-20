package com.brorental.brorental.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brorental.brorental.R;
import com.brorental.brorental.adapters.HintAdapter;
import com.brorental.brorental.databinding.FragmentSearchBinding;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private String TAG = "SearchFragment.java";
    private ArrayList<String> hintList = new ArrayList<>();
    private HintAdapter adapter;
    private FirebaseFirestore mFirestore;
    private String[] cateArr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        adapter = new HintAdapter(getActivity(), hintList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        mFirestore = ((AppClass) getActivity().getApplication()).firestore;
        if (Utility.isNetworkAvailable(getActivity())) {
            getCategories();
        } else {
            noNetworkDialog();
        }
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (cateArr != null && !newText.isEmpty()) {
                    hintList.clear();
                    for (int i = 0; i < cateArr.length; i++) {
                        if (cateArr[i].contains(newText)) {
                            hintList.add(cateArr[i]);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    hintList.clear();
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        return binding.getRoot();
    }

    private void noNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("No connection");
        builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utility.isNetworkAvailable(getActivity())) {
                    getCategories();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    noNetworkDialog();
                }
            }
        });
        builder.create().show();
    }

    private void getCategories() {
        mFirestore.collection("appData").document("constants")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot d = task.getResult();
                            cateArr = d.getString("categories").split(",");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}
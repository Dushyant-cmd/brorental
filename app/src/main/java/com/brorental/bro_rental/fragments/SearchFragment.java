package com.brorental.bro_rental.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brorental.bro_rental.MainActivity;
import com.brorental.bro_rental.R;
import com.brorental.bro_rental.adapters.HintAdapter;
import com.brorental.bro_rental.databinding.FragmentSearchBinding;
import com.brorental.bro_rental.interfaces.UtilsInterface;
import com.brorental.bro_rental.localdb.RoomDb;
import com.brorental.bro_rental.localdb.StateEntity;
import com.brorental.bro_rental.retrofit.ApiService;
import com.brorental.bro_rental.retrofit.RetrofitClient;
import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.utilities.DialogCustoms;
import com.brorental.bro_rental.utilities.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    public FragmentSearchBinding binding;
    private String TAG = "SearchFragment.java", selectedState = "";
    private ArrayList<String> hintList = new ArrayList<>();
    private AppClass appClass;
    private HintAdapter adapter;
    private ArrayAdapter<String> arrayAdapter;
    private FirebaseFirestore mFirestore;
    private String[] cateArr;
    private ArrayList<String> stateList = new ArrayList<>();
    private List<StateEntity> roomList = new ArrayList<>();
    private ApiService apiService;
    private RoomDb roomDb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        adapter = new HintAdapter(getActivity(), hintList);
        appClass = (AppClass) getActivity().getApplication();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);
        mFirestore = ((AppClass) getActivity().getApplication()).firestore;
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        roomDb = RoomDb.getInstance(requireContext());
        roomList = roomDb.getStateDao().getStates();
        stateList.add("Select your state");

        if (Utility.isNetworkAvailable(getActivity())) {
            getCategories();
            if (roomList.isEmpty())
                getState();
            else {
                for (int i = 0; i < roomList.size(); i++) {
                    stateList.add(roomList.get(i).getState());
                }
                arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, stateList);
                binding.spinner.setAdapter(arrayAdapter);
            }
        } else {
            noNetworkDialog();
        }

        binding.searchView.requestFocus();
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int catePosition = Arrays.asList(cateArr).indexOf(query.trim());
                if (catePosition < 0) {
                    DialogCustoms.showSnackBar(getActivity(), "Select Valid Category", binding.getRoot());
                } else
                    adapter.refreshInterface.refresh(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (cateArr != null && !newText.isEmpty()) {
                    hintList.clear();
                    for (int i = 0; i < cateArr.length; i++) {
                        if (cateArr[i].toLowerCase().contains(newText.trim())) {
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
        adapter.addRefreshListener(new UtilsInterface.SearchInterface() {
            /**Below method will refresh list on MainActivity */
            @Override
            public void refresh(String query) {
                if (!stateList.isEmpty()) {
                    try {
                        MainActivity hostAct = (MainActivity) requireActivity();
                        if (!hostAct.isFinishing())
                            hostAct.queries(binding.spinner.getSelectedItem().toString(), query.toLowerCase());
                        appClass.sharedPref.setState((String) binding.spinner.getSelectedItem());
                        Utility.hideKeyboardFrom(getActivity(), getContext(), binding.getRoot(), true);
                        hostAct.getSupportFragmentManager().popBackStackImmediate();
                    } catch (IndexOutOfBoundsException e) {
                        DialogCustoms.showSnackBar(getActivity(), "Select Valid Category", binding.getRoot());
                    } catch (Exception e) {
                        Log.d(TAG, "refresh: " + e);
                    }
                } else {
                    DialogCustoms.showSnackBar(getActivity(), "Select state", binding.getRoot());
                }


            }
        });
    }

//    private void getState() {
//        mFirestore.collection("appData").document("constants")
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: " + task.getResult().getString("state"));
//                            String[] states = task.getResult().getString("state").split(",");
//                            Collections.addAll(stateList, states);
//                            if(!appClass.sharedPref.getState().isEmpty()) {
//                                int selectedStatePos = stateList.indexOf(appClass.sharedPref.getState());
//                                stateList.remove(selectedStatePos);
//                                stateList.remove(0);
//                                stateList.add(0, appClass.sharedPref.getState());
//                            }
//                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, stateList);
//                            binding.spinner.setAdapter(adapter);
//                            binding.spinner.setVisibility(View.VISIBLE);
//                        } else {
//                            Log.d(TAG, "onComplete: " + task.getException());
//                        }
//                    }
//                });
//    }

    private void getState() {
        try {
            JSONObject json = new JSONObject();
            json.put("country", "india");
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (json).toString());
            String url = "https://countriesnow.space/api/v0.1/countries/states";
            apiService.getCountryState(url, body)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                try {
                                    roomDb.getStateDao().deleteStates();
                                    JSONObject json1 = new JSONObject(response.body().toString());
                                    JSONObject dataJs = json1.getJSONObject("data");
                                    JSONArray jsonArray1 = dataJs.getJSONArray("states");
                                    for (int i = 0; i < jsonArray1.length(); i++) {
                                        JSONObject js = jsonArray1.getJSONObject(i);
                                        roomDb.getStateDao().insertState(new StateEntity(js.getString("name")));
                                        stateList.add(js.getString("name"));
                                    }

                                    arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, stateList);
                                    binding.spinner.setAdapter(arrayAdapter);
                                } catch (Exception e) {
//                                    createErrorDialog(requireActivity(), e.getMessage());
                                    Log.d(TAG, "onResponse: " + e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "getStates: " + e);
        }
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
                    if (roomList.isEmpty())
                        getState();
                    else {
                        for (int i = 0; i < roomList.size(); i++) {
                            stateList.add(roomList.get(i).getState());
                        }
                        arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, stateList);
                        binding.spinner.setAdapter(arrayAdapter);
                    }
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
                            String str = Objects.requireNonNull(d.getString("categories")).toLowerCase();
                            cateArr = str.split(",");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
}
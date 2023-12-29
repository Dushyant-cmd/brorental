package com.brorental.bro_rental;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.bro_rental.activities.HistoryActivity;
import com.brorental.bro_rental.activities.PaymentActivity;
import com.brorental.bro_rental.activities.PaymentHistory;
import com.brorental.bro_rental.activities.ProfileActivity;
import com.brorental.bro_rental.activities.RideActivity;
import com.brorental.bro_rental.adapters.RentListAdapter;
import com.brorental.bro_rental.databinding.ActivityMainBinding;
import com.brorental.bro_rental.fragments.SearchFragment;
import com.brorental.bro_rental.models.RentItemModel;
import com.brorental.bro_rental.models.User;
import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.utilities.DialogCustoms;
import com.brorental.bro_rental.utilities.ProgressDialog;
import com.brorental.bro_rental.utilities.Utility;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String TAG = "MainBinding.java";
    private RentListAdapter adapter;
    private ArrayList<RentItemModel> list = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private AppClass appClass;
    private TextView headerWalletTV, viewProfileTV, headerNameTV;
    private ImageView headerImageView;
    private LinearLayout headerWalletLL;
    private int page = 0;
    private DocumentSnapshot lastDoc;
    private AlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        appClass = (AppClass) getApplication();
        mFirestore = appClass.firestore;
        pDialog = ProgressDialog.createAlertDialog(MainActivity.this);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, 0, 0);
        mDrawerToggle.syncState();
        //After instantiating your ActionBarDrawerToggle
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.navigation_menu_ic, getTheme());
        mDrawerToggle.setHomeAsUpIndicator(drawable);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        binding.drawerLayout.addDrawerListener(mDrawerToggle);

        //header listeners and dynamic text.
        View headerView = binding.navigationView.getHeaderView(0);
        headerWalletTV = headerView.findViewById(R.id.walletTV);
        headerImageView = headerView.findViewById(R.id.profileIV);
        viewProfileTV = headerView.findViewById(R.id.viewProfileTV);
        headerNameTV = headerView.findViewById(R.id.nameTV);
        headerWalletLL = headerView.findViewById(R.id.walletLL);
        Log.d(TAG, "onCreate: " + appClass.sharedPref.getUser().getTotalRent() + "\n" + appClass.sharedPref.getUser().getWallet());
        headerWalletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
        headerNameTV.setText(appClass.sharedPref.getUser().getName());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.profile_24).into(headerImageView);

        setListeners();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(MainActivity.this, appClass);
        getData();

        adapter = new RentListAdapter(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }

    private void getData() {
        if (Utility.isNetworkAvailable(this)) {
            getProfile();
            queries("", "");
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("Refresh", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utility.isNetworkAvailable(MainActivity.this)) {
                        queries("", "");
                        getProfile();
                        bar.dismiss();
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    } else {
                        bar.dismiss();
                        bar.show();
                    }
                }
            });

            bar.show();
        }
    }

    private void setListeners() {
        //header listeners and dynamic text.
        View headerView = binding.navigationView.getHeaderView(0);
        TextView walletTV = headerView.findViewById(R.id.walletTV);
        ImageView imageView = headerView.findViewById(R.id.profileIV);
        TextView viewProfileTV = headerView.findViewById(R.id.viewProfileTV);
        TextView nameTV = headerView.findViewById(R.id.nameTV);
        LinearLayout walletLL = headerView.findViewById(R.id.walletLL);
        long wal = (Long.parseLong(appClass.sharedPref.getUser().getTotalRent()) * 2500) + Long.parseLong(appClass.sharedPref.getUser().getWallet());
        walletTV.setText("\u20B9 " + wal);
        nameTV.setText(appClass.sharedPref.getUser().getName());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForRes(i);
            }
        });

        viewProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForRes(i);
            }
        });

        binding.addCashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog sheet = new BottomSheetDialog(MainActivity.this);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_cash_sheet, null);
                sheet.setContentView(view);
                Button submitBtn = view.findViewById(R.id.confirmRec);
                Button cancelBtn = view.findViewById(R.id.cancelRec);
                EditText rechargeET = view.findViewById(R.id.rechargeAmt);
                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, PaymentActivity.class);
                        i.putExtra("addCash", true);
                        i.putExtra("amt", rechargeET.getText().toString());
                        startActivityForRes(i);
                        sheet.dismiss();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sheet.dismiss();
                    }
                });
                sheet.show();
            }
        });
        binding.searchLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(new SearchFragment());
            }
        });

        walletLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PaymentHistory.class);
                startActivityForRes(i);
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(i);
                } else if (id == R.id.rent) {
                    binding.drawerLayout.close();
                } else if (id == R.id.driving) {
                    Intent i = new Intent(MainActivity.this, RideActivity.class);
                    startActivity(i);
                } else if (id == R.id.history) {
                    Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(i);
                } else if (id == R.id.termsCon) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://brorental.com/terms"));
                    startActivity(i);
                }

                return true;
            }
        });

        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    public void queries(String selectedState, String category) {
//        Log.d(TAG, "getData: " + selectedState + "," + category);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.noData.setVisibility(View.GONE);
        Query query = mFirestore.collection("rent")
                .whereEqualTo("status", "approved")
                .whereEqualTo("liveStatus", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10);

        if (!selectedState.isEmpty()) {
            query = mFirestore.collection("rent").whereEqualTo("state", selectedState)
                    .whereEqualTo("category", category)
                    .whereEqualTo("status", "approved")
                    .whereEqualTo("liveStatus", true)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10);
        }
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            list.clear();
                            List<DocumentSnapshot> docList = task.getResult().getDocuments();
                            for (int i = 0; i < docList.size(); i++) {
                                DocumentSnapshot d = docList.get(i);
                                RentItemModel model = d.toObject(RentItemModel.class);
                                list.add(model);
                            }

                            adapter.notifyDataSetChanged();

                            if (!docList.isEmpty()) {
                                lastDoc = docList.get(docList.size() - 1);
                            } else {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.noData.setVisibility(View.VISIBLE);
                            }

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                                binding.nestedSv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                        //Check if user scrolled till bottom
                                        if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                            Log.v(TAG, "list scroll till bottom");
                                            if (Utility.isNetworkAvailable(MainActivity.this) && page == 0) {
                                                page++;
                                                loadMoreGameResult();
                                            } else if (!Utility.isNetworkAvailable(MainActivity.this)) {
                                                Toast.makeText(MainActivity.this, "Check internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            Log.d(TAG, "onComplete: " + docList.size());
                        } else {
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.noData.setVisibility(View.VISIBLE);
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }

    private void loadMoreGameResult() {
        pDialog.show();
        Query query = mFirestore.collection("rent")
                .startAfter(lastDoc)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                page = 0;
                pDialog.dismiss();
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> dList = task.getResult().getDocuments();
                    if (!dList.isEmpty()) {
                        for (DocumentSnapshot d : dList) {
                            RentItemModel model = d.toObject(RentItemModel.class);
                            list.add(model);
                        }

                        if (!dList.isEmpty())
                            lastDoc = dList.get(dList.size() - 1);

                        adapter.submitList(list);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                        page++;
                    }
                } else {
                    DialogCustoms.showSnackBar(MainActivity.this, "Please try again", binding.getRoot());
                }
            }
        });
    }

    private void openFragment(SearchFragment searchFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fT = fm.beginTransaction();
        fT.replace(R.id.fragmentContainer, searchFragment);
        fT.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fT.addToBackStack(null);
        fT.commit();
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        super.onActivityResult(reqCode, resCode, intent);
        switch (reqCode) {
            case 101:
                Log.d(TAG, "onActivityResult: 44");
                headerWalletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
                headerNameTV.setText(appClass.sharedPref.getUser().getName());
                Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(headerImageView);
                getProfile();
                break;
            default:
                break;
        }
    }

    public void startActivityForRes(Intent i) {
        if (!isFinishing())
            startActivityForResult(i, 101);
    }

    private void getProfile() {
        appClass.firestore.collection("users").document(appClass.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
                        appClass.sharedPref.saveUser(new User(d.getString("name"), d.getString("mobile"), d.getString("pin"),
                                d.getString("totalRentItem"), d.getString("totalRides"), true,
                                d.getString("profileUrl"), d.getString("wallet")));
                        appClass.sharedPref.setAadhaarImg(d.getString("aadhaarImgUrl"));
                        appClass.sharedPref.setAadhaarPath(d.getString("aadhaarImgPath"));
                        appClass.sharedPref.setDLImg(d.getString("drivingLicenseImg"));
                        appClass.sharedPref.setDLPath(d.getString("drivingLicImgPath"));
                        appClass.sharedPref.setProfilePath(d.getString("profileImgPath"));
                        appClass.sharedPref.setStatus(d.getString("status"));

                        headerWalletTV.setText(Utility.rupeeIcon + Utility.getTotalWallet(appClass));
                        headerNameTV.setText(appClass.sharedPref.getUser().getName());
                        Glide.with(MainActivity.this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.default_profile).into(headerImageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                        if (Utility.isNetworkAvailable(MainActivity.this)) {
                            getProfile();
                        }
                    }
                });
    }
}
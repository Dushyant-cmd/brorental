package com.brorental.brorental;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brorental.brorental.activities.HistoryActivity;
import com.brorental.brorental.activities.PaymentActivity;
import com.brorental.brorental.activities.PaymentHistory;
import com.brorental.brorental.activities.ProfileActivity;
import com.brorental.brorental.activities.RideActivity;
import com.brorental.brorental.activities.SignUpAndLogin;
import com.brorental.brorental.activities.SplashActivity;
import com.brorental.brorental.adapters.RentListAdapter;
import com.brorental.brorental.broadcasts.ConnectionBroadcast;
import com.brorental.brorental.databinding.ActivityMainBinding;
import com.brorental.brorental.fragments.SearchFragment;
import com.brorental.brorental.models.RentItemModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.brorental.brorental.utilities.Utility;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        appClass = (AppClass) getApplication();
        mFirestore = appClass.firestore;
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

        setListners();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(MainActivity.this, appClass);
        if(Utility.isNetworkAvailable(this)) {
            getData("", "");
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "No Connection", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("Refresh", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Utility.isNetworkAvailable(MainActivity.this)) {
                        getData("", "");
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

        adapter = new RentListAdapter(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
        adapter.submitList(list);
    }

    private void setListners() {
        //header listeners and dynamic text.
        View headerView = binding.navigationView.getHeaderView(0);
        TextView walletTV = headerView.findViewById(R.id.walletTV);
        ImageView imageView = headerView.findViewById(R.id.profileIV);
        TextView viewProfileTV = headerView.findViewById(R.id.viewProfileTV);
        TextView nameTV = headerView.findViewById(R.id.nameTV);
        LinearLayout walletLL = headerView.findViewById(R.id.walletLL);
        walletTV.setText("\u20B9 " + appClass.sharedPref.getUser().getWallet());
        nameTV.setText(appClass.sharedPref.getUser().getName());

        Glide.with(this).load(appClass.sharedPref.getUser().getProfileUrl()).placeholder(R.drawable.profile_24).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(i);
            }
        });

        viewProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(i);
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
                        startActivity(i);
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
                startActivity(i);
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.profile) {
                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(i);
                } else if(id == R.id.rent) {
                    binding.drawerLayout.close();
                } else if(id == R.id.driving) {
                    Intent i = new Intent(MainActivity.this, RideActivity.class);
                    startActivity(i);
                } else if(id == R.id.history) {
                    Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(i);
                } else if(id == R.id.termsCon) {
                    DialogCustoms.showSnackBar(MainActivity.this, "Terms & Conditions", binding.getRoot());
                } else if(id == R.id.logout) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Are you sure to log-out");
                    builder.setPositiveButton("Log-out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(MainActivity.this, SignUpAndLogin.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }
                return true;
            }
        });

        binding.swipeRef.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData("", "");
            }
        });
    }

    public void getData(String selectedState, String category) {
        Log.d(TAG, "getData: " + selectedState + "," + category);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        Query query = mFirestore.collection("rent").limit(10);
        if(!selectedState.isEmpty()) {
            query = mFirestore.collection("rent").whereEqualTo("state", selectedState)
                    .whereEqualTo("category", category).limit(10);
        }
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        binding.swipeRef.setRefreshing(false);
                        binding.shimmer.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if(task.isSuccessful()) {
                            list.clear();
                            List<DocumentSnapshot> docList = task.getResult().getDocuments();
                            for(int i=0; i<docList.size(); i++) {
                                DocumentSnapshot d = docList.get(i);
                                RentItemModel model = d.toObject(RentItemModel.class);
                                list.add(model);
//                                list.add(new RentItemModel(d.getString("advertisementId"), d.getString("address"),
//                                        d.getString("adsImageUrl"), d.getString("broPartnerId"),
//                                        d.getString("category"), d.getString("docId"), d.getString("extraCharge"),
//                                        d.getString("ownerDescription"), d.getString("perHourCharge"), d.getString("state"),
//                                        d.getString("status"), d.getString("vehicleNumber"), d.getString("timings"),
//                                        d.getString("year"), d.getString("productHealth"), d.getString("productColor"),
//                                        d.getString("name")));
                            }

                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "onComplete: " + docList.size());
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
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
}
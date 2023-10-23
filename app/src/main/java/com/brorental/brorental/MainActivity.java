package com.brorental.brorental;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.brorental.brorental.adapters.RentListAdapter;
import com.brorental.brorental.databinding.ActivityMainBinding;
import com.brorental.brorental.fragments.SearchFragment;
import com.brorental.brorental.models.RentItemModel;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private String TAG = "MainBinding.java";
    private RentListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
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
        binding.searchLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(new SearchFragment());
            }
        });

        ArrayList<RentItemModel> list = new ArrayList<>();
        list.add(new RentItemModel("223adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Audio", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("225adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Electronics", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("224adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Television", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("223adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Bike", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("225adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Car", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("224adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "House", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("223adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Mobile", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("225adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Fridge", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        list.add(new RentItemModel("224adf", "Delhi,India",
                "https://images.unsplash.com/photo-1532974297617-c0f05fe48bff?auto=format&fit=crop&q=80&w=1964&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "1000", "Bike", "Akljdlfjadlj", "20", "This is for rent",
                "50", "Delhi", "live", "DL339900", "10:00PM - 11:00PM"));
        adapter = new RentListAdapter(this);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
        adapter.submitList(list);
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
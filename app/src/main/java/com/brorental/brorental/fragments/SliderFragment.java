package com.brorental.brorental.fragments;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.FragmentSliderBinding;

public class SliderFragment extends Fragment {
    private String TAG = "SliderFragment.java";
    private FragmentSliderBinding binding;
    public SliderFragment() {
        //required empty constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.fragment_slider, container, false);
        if(getArguments() != null) {
            binding.img.setImageResource((int) getArguments().getLong("image"));
            binding.title.setText(getArguments().getString("title"));
        }
        return binding.getRoot();
    }
}
package com.brorental.brorental.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.LangSpinnerItemBinding;

import java.util.ArrayList;

public class LanguageSpinnerAdapter extends ArrayAdapter<String> {
    ArrayList<String> list = new ArrayList<>();
    private Context ctx;
    public LanguageSpinnerAdapter(@NonNull Context context, int res, ArrayList<String> list) {
        super(context, res);
        this.list = list;
        this.ctx = context;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LangSpinnerItemBinding binding = LangSpinnerItemBinding.inflate(LayoutInflater.from(parent.getContext())
                , parent, false);
        if(list.get(position).equalsIgnoreCase("hindi")) {
            binding.tvLang.setCompoundDrawablesWithIntrinsicBounds(ctx.getResources().getDrawable(R.drawable.indian_flag), null, null, null);
        } else if(list.get(position).equalsIgnoreCase("english")) {
            binding.tvLang.setCompoundDrawablesWithIntrinsicBounds(ctx.getResources().getDrawable(R.drawable.indian_flag), null, null, null);
        }
        return binding.getRoot();
    }
}

package com.brorental.brorental.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.brorental.R;

import java.util.ArrayList;

public class HintAdapter extends RecyclerView.Adapter<HintAdapter.ViewHolder> {
    private Context ctx;
    private String TAG = "HintAdapter.java";
    private ArrayList<String> list;

    public HintAdapter(Context ctx, ArrayList<String> list) {
        this.ctx = ctx;
        this.list = list;
    }
    @NonNull
    @Override
    public HintAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hint_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HintAdapter.ViewHolder holder, int position) {
        String str = list.get(position);
        holder.hintTV.setText(str);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView hintTV;
        public ViewHolder(View view) {
            super(view);
            hintTV = view.findViewById(R.id.hintTV);
        }
    }
}

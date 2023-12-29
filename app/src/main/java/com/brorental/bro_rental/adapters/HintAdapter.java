package com.brorental.bro_rental.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bro_rental.MainActivity;
import com.brorental.bro_rental.R;
import com.brorental.bro_rental.interfaces.UtilsInterface;

import java.util.ArrayList;

public class HintAdapter extends RecyclerView.Adapter<HintAdapter.ViewHolder> {
    private Context ctx;
    private String TAG = "HintAdapter.java";
    private ArrayList<String> list;
    private MainActivity hostAct;
    public UtilsInterface.SearchInterface refreshInterface;

    public HintAdapter(Context ctx, ArrayList<String> list) {
        hostAct = (MainActivity) ctx;
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
        holder.hintTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshInterface.refresh(str);
            }
        });
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

    public void addRefreshListener(UtilsInterface.SearchInterface refreshInterface) {
        this.refreshInterface = refreshInterface;
    }
}

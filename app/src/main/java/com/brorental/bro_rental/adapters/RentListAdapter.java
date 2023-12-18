package com.brorental.bro_rental.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.bro_rental.MainActivity;
import com.brorental.bro_rental.R;
import com.brorental.bro_rental.activities.RentDetailsActivity;
import com.brorental.bro_rental.databinding.RentListItemBinding;
import com.brorental.bro_rental.models.RentItemModel;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

public class RentListAdapter extends ListAdapter<RentItemModel, RentListAdapter.ViewHolder> {
    private Context ctx;
    private MainActivity activity;
    private String TAG = "RentListAdapter.java";
    public RentListAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<RentItemModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull RentItemModel oldItem, @NonNull RentItemModel newItem) {
                Log.d("RentListAdapter.java", "areItemsTheSame: " + oldItem.advertisementId.equals(newItem.advertisementId));
                return oldItem.advertisementId.equals(newItem.advertisementId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull RentItemModel oldItem, @NonNull RentItemModel newItem) {
                Log.d("RentListAdapter.java", "areContentsTheSame: " + oldItem.equals(newItem));
                return oldItem.equals(newItem);
            }
        });
        this.ctx = ctx;
        this.activity = (MainActivity) ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int whichType) {
        RentListItemBinding binding = RentListItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        RentItemModel data = getItem(pos);
        holder.binding.cateTV.setText(data.getCategory());
        holder.binding.addTV.setText(data.getAddress());
        String spanned = data.getPerHourCharge();
        holder.binding.perHourTV.setText("\u20B9 " + Html.fromHtml(spanned));
        holder.binding.timingsTV.setText(data.getTimings());
        Log.d(TAG, "onBindViewHolder: " + data.getAdsImageUrl());
        String thumbnail = data.getAdsImageUrl().split(",")[0];
        Glide.with(ctx).load(thumbnail).placeholder(com.denzcoskun.imageslider.R.drawable.placeholder)
                .into(holder.binding.pdImgView);

        holder.binding.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ctx, RentDetailsActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(data);
                i.putExtra("data", json);
                activity.startActivityForRes(i);
            }
        });
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        public RentListItemBinding binding;
        public ViewHolder(RentListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

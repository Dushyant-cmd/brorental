package com.brorental.brorental.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.PaymentHistoryListItemBinding;
import com.brorental.brorental.databinding.RentHistoryListItemBinding;
import com.brorental.brorental.models.HistoryModel;
import com.brorental.brorental.models.PaymentHistoryModel;
import com.bumptech.glide.Glide;

public class RentHistoryAdapter extends ListAdapter<HistoryModel, RentHistoryAdapter.ViewHolder> {
    private String TAG = "PaymentAdapter.java";
    private Context context;

    public RentHistoryAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<HistoryModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull HistoryModel oldItem, @NonNull HistoryModel newItem) {
                return oldItem.id.matches(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull HistoryModel oldItem, @NonNull HistoryModel newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rent_history_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryModel data = getItem(position);
        String[] imageArr = data.rentImages.split(",");
        Glide.with(context).load(imageArr[0]).placeholder(com.denzcoskun.imageslider.R.drawable.placeholder).into(holder.binding.pdImgView);
        holder.binding.perHourTV.setText("Total Cost: \u20B9 " + data.totalRentCost);
        holder.binding.pin.setText("Pin: " + data.id.substring(0, 4) + " share it with BroPartner");
        holder.binding.timingsTV.setText("From: " + data.rentStartTime + " To: " + data.rentEndTime);
        holder.binding.extraHourCh.setText("\u20B9 Extra charge per hour");
        holder.binding.payStatus.setText("Payment Completed " + data.paymentMode);
        holder.binding.advertId.setText("Advertisement Id: " + data.advertisementId);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private RentHistoryListItemBinding binding;
        public ViewHolder(RentHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

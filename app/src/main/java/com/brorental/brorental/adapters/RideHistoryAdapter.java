package com.brorental.brorental.adapters;

import static android.os.Build.VERSION_CODES.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.brorental.brorental.R;
import com.brorental.brorental.databinding.RideHistoryListItemBinding;
import com.brorental.brorental.interfaces.UtilsInterface;
import com.brorental.brorental.models.RideHistoryModel;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.Utility;
import com.bumptech.glide.Glide;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RideHistoryAdapter extends ListAdapter<RideHistoryModel, RideHistoryAdapter.ViewHolder> {
    private Context ctx;
    private UtilsInterface.RideHistoryListener rideStatusListener;
    private AppClass appClass;
    public RideHistoryAdapter(Context ctx, AppClass appClass) {
        super(new DiffUtil.ItemCallback<RideHistoryModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull RideHistoryModel oldItem, @NonNull RideHistoryModel newItem) {
                return oldItem.getTimestamp() == newItem.getTimestamp();
            }

            @Override
            public boolean areContentsTheSame(@NonNull RideHistoryModel oldItem, @NonNull RideHistoryModel newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.appClass = appClass;
        this.ctx = ctx;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RideHistoryListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), com.brorental.brorental.R.layout.ride_history_list_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideHistoryModel data = getItem(position);
        SimpleDateFormat spf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date d = new Date(data.getTimestamp());
        String orderTime = spf.format(d);
        String status = data.getStatus().toLowerCase();

        holder.binding.tvStatus.setText("Status: " + status);
        holder.binding.tvName.setText(data.getName());
        holder.binding.tvAmt.setText("Ride cost: " + Utility.rupeeIcon + data.getAmount());
        holder.binding.tvOrdered.setText(orderTime);
        holder.binding.tvDis.setText(data.getDistance() + " KM");
        holder.binding.tvFrom.setText(data.getFrom());
        holder.binding.tvTo.setText(data.getTo());
        holder.binding.pin.setText(data.getDocId().substring(0, 4));
        Glide.with(ctx).load(data.getProfileUrl()).placeholder(com.brorental.brorental.R.drawable.default_profile).into(holder.binding.civProfile);

        holder.binding.tvDial.setOnClickListener(view -> {
            rideStatusListener.contactListener("phone");
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RideHistoryListItemBinding binding;
        public ViewHolder(RideHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void addRefreshListeners(UtilsInterface.RideHistoryListener rideStatusListener) {
        this.rideStatusListener = rideStatusListener;
    }
}

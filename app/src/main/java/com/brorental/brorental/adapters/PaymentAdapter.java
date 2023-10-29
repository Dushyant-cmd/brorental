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
import com.brorental.brorental.models.PaymentHistoryModel;

public class PaymentAdapter extends ListAdapter<PaymentHistoryModel, PaymentAdapter.ViewHolder> {
    private String TAG = "PaymentAdapter.java";

    public PaymentAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<PaymentHistoryModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull PaymentHistoryModel oldItem, @NonNull PaymentHistoryModel newItem) {
                return oldItem.id.matches(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull PaymentHistoryModel oldItem, @NonNull PaymentHistoryModel newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.payment_history_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private PaymentHistoryListItemBinding binding;
        public ViewHolder(PaymentHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

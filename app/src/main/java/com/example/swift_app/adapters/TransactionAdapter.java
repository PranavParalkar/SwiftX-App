package com.example.swift_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private final String currentUserId;

    public TransactionAdapter(List<Transaction> transactions, String currentUserId) {
        this.transactions = transactions;
        this.currentUserId = currentUserId;
    }

    public void updateList(List<Transaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        
        holder.tvTitle.setText(tx.getDisplayName(currentUserId));
        holder.tvSubtitle.setText(tx.getType() + " • " + (tx.getCreatedAt() != null ? tx.getCreatedAt().split("T")[0] : "Today"));
        holder.tvAmount.setText(tx.getFormattedAmount(currentUserId));
        holder.tvInitial.setText(String.valueOf(holder.tvTitle.getText().charAt(0)).toUpperCase());

        if (tx.isPending()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Pending");
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.swift_app.activities.TransactionDetailActivity.class);
            intent.putExtra("transaction_json", new com.google.gson.Gson().toJson(tx));
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvAmount, tvStatus, tvInitial;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTransactionTitle);
            tvSubtitle = view.findViewById(R.id.tvTransactionSubtitle);
            tvAmount = view.findViewById(R.id.tvTransactionAmount);
            tvStatus = view.findViewById(R.id.tvTransactionStatus);
            tvInitial = view.findViewById(R.id.tvTransactionInitial);
        }
    }
}

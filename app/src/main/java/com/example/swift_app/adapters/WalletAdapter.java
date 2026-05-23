package com.example.swift_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.utils.CurrencyFormatter;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {

    private List<Wallet> wallets;

    public WalletAdapter(List<Wallet> wallets) {
        this.wallets = wallets;
    }

    public void updateList(List<Wallet> newList) {
        this.wallets = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency_wallet, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet wallet = wallets.get(position);
        holder.tvCode.setText(wallet.getCurrency());
        holder.tvName.setText(getCurrencyName(wallet.getCurrency()));
        holder.tvBalance.setText(CurrencyFormatter.format(wallet.getBalance(), wallet.getCurrency()));
    }

    private String getCurrencyName(String code) {
        switch (code) {
            case "USD": return "US Dollar";
            case "EUR": return "Euro";
            case "GBP": return "British Pound";
            case "INR": return "Indian Rupee";
            case "KES": return "Kenyan Shilling";
            case "PHP": return "Philippine Peso";
            default: return "Digital Currency";
        }
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvBalance;

        ViewHolder(View view) {
            super(view);
            tvCode = view.findViewById(R.id.tvCurrencyCode);
            tvName = view.findViewById(R.id.tvCurrencyName);
            tvBalance = view.findViewById(R.id.tvCurrencyBalance);
        }
    }
}

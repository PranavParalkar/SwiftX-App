package com.example.swift_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.models.User;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<User> contacts;
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(User contact);
    }

    public ContactAdapter(List<User> contacts, OnContactClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User contact = contacts.get(position);
        holder.tvName.setText(contact.getFullName());
        holder.tvEmail.setText(contact.getEmail());
        holder.tvInitial.setText(String.valueOf(contact.getFullName().charAt(0)).toUpperCase());

        holder.itemView.setOnClickListener(v -> listener.onContactClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvInitial;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvContactName);
            tvEmail = view.findViewById(R.id.tvContactEmail);
            tvInitial = view.findViewById(R.id.tvContactInitial);
        }
    }
}

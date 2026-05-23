package com.example.swift_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.models.AuditLog;

import java.util.List;

public class AuditLogAdapter extends RecyclerView.Adapter<AuditLogAdapter.ViewHolder> {

    private List<AuditLog> logList;

    public AuditLogAdapter(List<AuditLog> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audit_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLog log = logList.get(position);
        holder.tvAction.setText(log.getAction().toUpperCase());
        holder.tvEntity.setText(log.getEntity() + ": " + log.getEntityId());
        holder.tvTimestamp.setText(log.getCreatedAt());

        // Simple color coding
        if (log.getAction().contains("reject") || log.getAction().contains("fail")) {
            holder.tvAction.setTextColor(holder.itemView.getContext().getColor(R.color.swift_error));
        } else if (log.getAction().contains("verify") || log.getAction().contains("complete")) {
            holder.tvAction.setTextColor(holder.itemView.getContext().getColor(R.color.swift_success));
        } else {
            holder.tvAction.setTextColor(holder.itemView.getContext().getColor(R.color.swift_primary));
        }
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void updateList(List<AuditLog> newList) {
        this.logList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction, tvEntity, tvTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.tvAction);
            tvEntity = itemView.findViewById(R.id.tvEntity);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

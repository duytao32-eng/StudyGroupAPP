package com.example.studygroupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.model.AttendanceSummary;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private List<AttendanceSummary> summaryList;

    public DashboardAdapter(List<AttendanceSummary> summaryList) {
        this.summaryList = summaryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceSummary summary = summaryList.get(position);
        holder.tvDashName.setText(summary.getName());
        holder.tvDashId.setText(summary.getStudentId() != null ? "MSSV: " + summary.getStudentId() : "MSSV: N/A");
        holder.tvDashCount.setText(summary.getCount() + " buổi");
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDashName, tvDashId, tvDashCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDashName = itemView.findViewById(R.id.tvDashName);
            tvDashId = itemView.findViewById(R.id.tvDashId);
            tvDashCount = itemView.findViewById(R.id.tvDashCount);
        }
    }
}
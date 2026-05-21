package com.example.studygroupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.model.Assignment;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    private List<Assignment> assignmentList;
    private OnItemClickListener listener; // Thêm bộ lắng nghe sự kiện

    // Tạo Interface (khung chuẩn) để truyền sự kiện bấm ra ngoài Fragment
    public interface OnItemClickListener {
        void onItemClick(Assignment assignment);
    }

    // Nâng cấp Hàm tạo (Constructor) yêu cầu phải có Listener
    public AssignmentAdapter(List<Assignment> assignmentList, OnItemClickListener listener) {
        this.assignmentList = assignmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignmentList.get(position);
        holder.tvAssignmentTitle.setText(assignment.getTitle());
        holder.tvAssignmentDesc.setText(assignment.getDescription());

        if (assignment.getDeadline() != null) {
            String timeStr = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(assignment.getDeadline());
            holder.tvAssignmentDeadline.setText("Hạn chót: " + timeStr);
        } else {
            holder.tvAssignmentDeadline.setText("Không có hạn chót");
        }

        // BẮT SỰ KIỆN: Khi người dùng bấm vào toàn bộ Thẻ (ItemView)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(assignment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAssignmentTitle, tvAssignmentDesc, tvAssignmentDeadline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAssignmentTitle = itemView.findViewById(R.id.tvAssignmentTitle);
            tvAssignmentDesc = itemView.findViewById(R.id.tvAssignmentDesc);
            tvAssignmentDeadline = itemView.findViewById(R.id.tvAssignmentDeadline);
        }
    }
}
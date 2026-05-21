package com.example.studygroupapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.activity.GroupDetailActivity;
import com.example.studygroupapp.model.Group;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;

    public GroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.tvGroupName.setText(group.getName());
        holder.tvGroupCode.setText("Mã nhóm: " + group.getCode());

        // Lấy ID của người đang dùng app hiện tại
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // LOGIC PHÂN LOẠI NHÃN (BADGE)
        if (group.getCreatorId() != null && group.getCreatorId().equals(currentUserId)) {
            // Nếu bạn là người tạo nhóm
            holder.tvRoleBadge.setText("Trưởng nhóm");
            holder.tvRoleBadge.setBackgroundColor(Color.parseColor("#E64A19")); // Màu Đỏ Cam
        } else {
            // Nếu bạn chỉ là thành viên
            holder.tvRoleBadge.setText("Thành viên");
            holder.tvRoleBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Màu Xanh lá
        }

        // Sự kiện khi bấm vào Thẻ nhóm
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GroupDetailActivity.class);
            intent.putExtra("GROUP_ID", group.getId());
            intent.putExtra("GROUP_NAME", group.getName());
            intent.putExtra("CREATOR_ID", group.getCreatorId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvGroupCode, tvRoleBadge; // Ánh xạ thêm tvRoleBadge

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupCode = itemView.findViewById(R.id.tvGroupCode);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
        }
    }
}
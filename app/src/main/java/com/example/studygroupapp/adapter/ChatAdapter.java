package com.example.studygroupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.model.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public ChatAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);

        // THUẬT TOÁN ĐIỀU HƯỚNG BONG BÓNG CHAT
        if (message.getSenderId().equals(currentUserId)) {
            // Nếu là mình gửi -> Bật layout Phải, Tắt layout Trái
            holder.layoutRight.setVisibility(View.VISIBLE);
            holder.layoutLeft.setVisibility(View.GONE);
            holder.tvMessageRight.setText(message.getText());
        } else {
            // Nếu là người khác gửi -> Bật layout Trái, Tắt layout Phải
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.layoutRight.setVisibility(View.GONE);
            holder.tvSenderNameLeft.setText(message.getSenderName());
            holder.tvMessageLeft.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutLeft, layoutRight;
        TextView tvMessageLeft, tvSenderNameLeft, tvMessageRight;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            layoutRight = itemView.findViewById(R.id.layoutRight);
            tvMessageLeft = itemView.findViewById(R.id.tvMessageLeft);
            tvSenderNameLeft = itemView.findViewById(R.id.tvSenderNameLeft);
            tvMessageRight = itemView.findViewById(R.id.tvMessageRight);
        }
    }
}
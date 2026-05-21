package com.example.studygroupapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.model.Post;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostTitle.setText(post.getTitle());
        holder.tvPostContent.setText(post.getContent());

        // Định dạng thời gian
        String timeStr = "Không rõ thời gian";
        if (post.getTimestamp() != null) {
            Date date = post.getTimestamp().toDate();
            timeStr = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(date);
        }

        holder.tvPostAuthorTime.setText("Bởi " + post.getAuthorName() + " • " + timeStr);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostTitle, tvPostContent, tvPostAuthorTime;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            tvPostAuthorTime = itemView.findViewById(R.id.tvPostAuthorTime);
        }
    }
}
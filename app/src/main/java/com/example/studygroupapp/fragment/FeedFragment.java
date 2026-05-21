package com.example.studygroupapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.adapter.PostAdapter;
import com.example.studygroupapp.model.Post;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedFragment extends Fragment {

    private String groupId, creatorId;
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private ExtendedFloatingActionButton fabAddPost;

    public FeedFragment(String groupId, String creatorId) {
        this.groupId = groupId;
        this.creatorId = creatorId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        rvPosts = view.findViewById(R.id.rvPosts);
        fabAddPost = view.findViewById(R.id.fabAddPost);

        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        rvPosts.setAdapter(postAdapter);

        // Kiểm tra quyền: Chỉ Trưởng nhóm mới được thấy nút Đăng bài
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId.equals(creatorId)) {
            fabAddPost.setVisibility(View.VISIBLE);
        }

        fabAddPost.setOnClickListener(v -> showAddPostDialog(currentUserId));

        loadPostsFromFirebase();

        return view;
    }

    private void loadPostsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Announcements")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Post post = doc.toObject(Post.class);
                            postList.add(post);
                        }
                        // Sắp xếp bài mới nhất lên đầu
                        Collections.sort(postList, (p1, p2) -> {
                            if (p1.getTimestamp() == null || p2.getTimestamp() == null) return 0;
                            return p2.getTimestamp().compareTo(p1.getTimestamp());
                        });
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ==========================================
    // NÂNG CẤP SIÊU CẤP: NÃO BỘ CHO FORM MỚI
    // ==========================================
    private void showAddPostDialog(String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());

        // 1. CHỈ ĐỊNH SỬ DỤNG LAYOUT MỚI TOANH (dialog_create_post.xml)
        View view = getLayoutInflater().inflate(R.layout.dialog_create_post, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // 2. ÁNH XẠ (FINDVIEWBYID) CÁC Ô NHẬP LIỆU MỚI
        TextInputEditText edtPostTitle = view.findViewById(R.id.edtPostTitle); // Ô tiêu đề (ngắn)
        TextInputEditText edtPostContent = view.findViewById(R.id.edtPostContent); // Ô nội dung chi tiết (rộng)
        MaterialButton btnSubmitPost = view.findViewById(R.id.btnSubmitPost); // Nút đăng bài

        btnSubmitPost.setOnClickListener(v -> {
            // 3. LẤY DỮ LIỆU TỪ CẢ 2 Ô NHẬP LIỆU THẬT
            String title = edtPostTitle.getText().toString().trim();
            String content = edtPostContent.getText().toString().trim(); // Lấy nội dung chi tiết thật!

            // Kiểm tra rỗng cho cả 2
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập nội dung chi tiết!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy tên Trưởng nhóm trước khi đăng
            db.collection("Users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
                String authorName = userDoc.exists() && userDoc.getString("fullName") != null ? userDoc.getString("fullName") : "Trưởng nhóm";

                // 4. TẠO ĐỐI TƯỢNG POST VỚI TIÊU ĐỀ VÀ NỘI DUNG THẬT (Thay vì mượn tạm như trước)
                String postId = db.collection("Announcements").document().getId();
                Post newPost = new Post(postId, groupId, title, content, authorName, Timestamp.now());

                db.collection("Announcements").document(postId).set(newPost)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "✅ Đã đăng thông báo mới!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadPostsFromFirebase(); // Tải lại danh sách
                        });
            });
        });
        dialog.show();
    }
}
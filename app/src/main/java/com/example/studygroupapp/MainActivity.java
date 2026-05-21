package com.example.studygroupapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.activity.LoginActivity;
import com.example.studygroupapp.activity.ProfileActivity;
import com.example.studygroupapp.adapter.GroupAdapter;
import com.example.studygroupapp.model.Group;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private RecyclerView rvGroups;
    private TabLayout tabLayoutGroups; // Khai báo bộ lọc

    private GroupAdapter groupAdapter;
    private List<Group> allGroupList; // Kho dữ liệu gốc (chứa tất cả)
    private List<Group> displayedGroupList; // Kho dữ liệu đang hiển thị trên màn hình

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvGroups = findViewById(R.id.rvGroups);
        tabLayoutGroups = findViewById(R.id.tabLayoutGroups);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        allGroupList = new ArrayList<>();
        displayedGroupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(displayedGroupList);
        rvGroups.setAdapter(groupAdapter);

        loadMyGroups();
        checkUserProfileStatus();

        // ==========================================
        // SỰ KIỆN KHI BẤM CHỌN TAB LỌC NHÓM
        // ==========================================
        tabLayoutGroups.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterGroups(tab.getPosition()); // Gọi hàm lọc danh sách dựa vào vị trí Tab
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // ==========================================
        // TÍNH NĂNG VUỐT ĐỂ XÓA (CẬP NHẬT LẠI)
        // ==========================================
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Group selectedGroup = displayedGroupList.get(position);

                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xác nhận rời nhóm")
                        .setMessage("Bạn có chắc chắn muốn rời khỏi nhóm '" + selectedGroup.getName() + "' không?")
                        .setPositiveButton("RỜI NHÓM", (dialog, which) -> {
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            selectedGroup.getMembers().remove(currentUserId);
                            db.collection("Groups").document(selectedGroup.getId())
                                    .update("members", selectedGroup.getMembers())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Đã rời nhóm thành công!", Toast.LENGTH_SHORT).show();
                                        allGroupList.remove(selectedGroup); // Xóa khỏi kho gốc
                                        displayedGroupList.remove(position); // Xóa khỏi kho hiển thị
                                        groupAdapter.notifyItemRemoved(position);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Lỗi: Không thể rời nhóm!", Toast.LENGTH_SHORT).show();
                                        groupAdapter.notifyItemChanged(position);
                                    });
                        })
                        .setNegativeButton("HỦY", (dialog, which) -> groupAdapter.notifyItemChanged(position))
                        .setCancelable(false)
                        .show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvGroups);

        ImageView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        ImageView btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        ExtendedFloatingActionButton fabAddGroup = findViewById(R.id.fabAddGroup);
        fabAddGroup.setOnClickListener(v -> {
            CharSequence[] options = new CharSequence[]{"Tạo nhóm mới", "Tham gia nhóm bằng Mã"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Bạn muốn làm gì?");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) showCreateGroupDialog();
                else showJoinGroupDialog();
            });
            builder.show();
        });
    }

    // ==========================================
    // THUẬT TOÁN LỌC NHÓM THÔNG MINH
    // ==========================================
    private void filterGroups(int tabIndex) {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();

        displayedGroupList.clear();

        for (Group group : allGroupList) {
            if (tabIndex == 0) {
                // Tab 0: TẤT CẢ (Đổ toàn bộ vào)
                displayedGroupList.add(group);
            } else if (tabIndex == 1) {
                // Tab 1: QUẢN LÝ (Chỉ lấy nhóm do mình tạo)
                if (group.getCreatorId() != null && group.getCreatorId().equals(currentUserId)) {
                    displayedGroupList.add(group);
                }
            } else if (tabIndex == 2) {
                // Tab 2: THAM GIA (Chỉ lấy nhóm do người khác tạo)
                if (group.getCreatorId() != null && !group.getCreatorId().equals(currentUserId)) {
                    displayedGroupList.add(group);
                }
            }
        }
        groupAdapter.notifyDataSetChanged(); // Yêu cầu giao diện vẽ lại danh sách
    }

    private void loadMyGroups() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("Groups")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allGroupList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Group group = document.toObject(Group.class);
                            allGroupList.add(group);
                        }
                        // Lấy dữ liệu về xong thì tự động kích hoạt bộ lọc dựa vào Tab đang được chọn
                        filterGroups(tabLayoutGroups.getSelectedTabPosition());
                    }
                });
    }

    private void checkUserProfileStatus() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || documentSnapshot.getString("fullName") == null || documentSnapshot.getString("studentId") == null) {
                        Toast.makeText(MainActivity.this, "Vui lòng cập nhật Họ tên và MSSV để tiếp tục sử dụng!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    }
                });
    }

    private void showCreateGroupDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        com.google.android.material.textfield.TextInputEditText edtGroupName = view.findViewById(R.id.edtGroupName);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnSubmitCreateGroup);

        btnSubmit.setOnClickListener(v -> {
            String groupName = edtGroupName.getText().toString().trim();
            if (groupName.isEmpty()) return;

            String groupCode = generateGroupCode();
            String currentUserId = mAuth.getCurrentUser().getUid();
            String documentId = db.collection("Groups").document().getId();

            List<String> members = new ArrayList<>();
            members.add(currentUserId);

            Group newGroup = new Group(documentId, groupName, groupCode, currentUserId, members);

            db.collection("Groups").document(documentId).set(newGroup)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Đã tạo nhóm! Mã: " + groupCode, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        loadMyGroups();
                    });
        });
        dialog.show();
    }

    private void showJoinGroupDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_join_group, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        com.google.android.material.textfield.TextInputEditText edtGroupCodeInput = view.findViewById(R.id.edtGroupCodeInput);
        com.google.android.material.button.MaterialButton btnSubmitJoinGroup = view.findViewById(R.id.btnSubmitJoinGroup);

        btnSubmitJoinGroup.setOnClickListener(v -> {
            String inputCode = edtGroupCodeInput.getText().toString().trim().toUpperCase();
            if (inputCode.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập mã!", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("Groups").whereEqualTo("code", inputCode).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Group group = document.toObject(Group.class);
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            if (group.getMembers() != null && group.getMembers().contains(currentUserId)) {
                                Toast.makeText(MainActivity.this, "Bạn đã ở trong nhóm này rồi!", Toast.LENGTH_SHORT).show();
                            } else {
                                group.getMembers().add(currentUserId);
                                db.collection("Groups").document(group.getId()).set(group)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, "Tham gia thành công!", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            loadMyGroups();
                                        });
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Mã nhóm không tồn tại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        dialog.show();
    }

    private String generateGroupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            code.append(chars.charAt((int) (rnd.nextFloat() * chars.length())));
        }
        return code.toString();
    }
}
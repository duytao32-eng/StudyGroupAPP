package com.example.studygroupapp.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.studygroupapp.R;
import com.example.studygroupapp.fragment.AssignmentFragment;
import com.example.studygroupapp.fragment.AttendanceFragment;
import com.example.studygroupapp.fragment.ChatFragment; // THÊM IMPORT CHO TAB THẢO LUẬN
import com.example.studygroupapp.fragment.FeedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GroupDetailActivity extends AppCompatActivity {

    private String groupId, groupName, creatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getStringExtra("GROUP_ID");
        groupName = getIntent().getStringExtra("GROUP_NAME");
        creatorId = getIntent().getStringExtra("CREATOR_ID");

        TextView tvDetailGroupName = findViewById(R.id.tvDetailGroupName);
        tvDetailGroupName.setText(groupName);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Mở Tab Điểm Danh làm mặc định ngay khi vừa vào nhóm
        loadFragment(new AttendanceFragment(groupId, groupName, creatorId));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_attendance) {
                loadFragment(new AttendanceFragment(groupId, groupName, creatorId));
                return true;
            } else if (itemId == R.id.nav_feed) {
                loadFragment(new FeedFragment(groupId, creatorId));
                return true;
            } else if (itemId == R.id.nav_assignment) {
                // TODO: Chờ tạo AssignmentFragment ở Bước tiếp theo
                 loadFragment(new AssignmentFragment(groupId, creatorId));
                return true;
            } else if (itemId == R.id.nav_chat) {
                loadFragment(new ChatFragment(groupId));
                return true;
            }
            return false;
        });
    }

    // Hàm phụ trợ để nhét Fragment vào cái FrameLayout
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutContainer, fragment)
                .commit();
    }
}
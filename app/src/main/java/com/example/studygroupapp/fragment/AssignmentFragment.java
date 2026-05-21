package com.example.studygroupapp.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.adapter.AssignmentAdapter;
import com.example.studygroupapp.model.Assignment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentFragment extends Fragment {

    private String groupId, creatorId;
    private RecyclerView rvAssignments;
    private AssignmentAdapter assignmentAdapter;
    private List<Assignment> assignmentList;
    private ExtendedFloatingActionButton fabAddAssignment;
    private Date selectedDeadline = null; // Biến lưu Deadline

    public AssignmentFragment(String groupId, String creatorId) {
        this.groupId = groupId;
        this.creatorId = creatorId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        rvAssignments = view.findViewById(R.id.rvAssignments);
        fabAddAssignment = view.findViewById(R.id.fabAddAssignment);

        rvAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        assignmentList = new ArrayList<>();
        assignmentAdapter = new AssignmentAdapter(assignmentList);
        rvAssignments.setAdapter(assignmentAdapter);

        // Phân quyền: Trưởng nhóm mới thấy nút Giao bài
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId.equals(creatorId)) {
            fabAddAssignment.setVisibility(View.VISIBLE);
        }

        fabAddAssignment.setOnClickListener(v -> showAddAssignmentDialog());
        loadAssignments();

        return view;
    }

    private void loadAssignments() {
        FirebaseFirestore.getInstance().collection("Assignments")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        assignmentList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Assignment assignment = doc.toObject(Assignment.class);
                            assignmentList.add(assignment);
                        }
                        // Xếp bài mới/Hạn chót gần nhất lên đầu
                        Collections.sort(assignmentList, (a1, a2) -> {
                            if (a1.getDeadline() == null || a2.getDeadline() == null) return 0;
                            return a1.getDeadline().compareTo(a2.getDeadline());
                        });
                        assignmentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddAssignmentDialog() {
        selectedDeadline = null; // Reset biến
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_create_assignment, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText edtAssignTitle = view.findViewById(R.id.edtAssignTitle);
        TextInputEditText edtAssignDesc = view.findViewById(R.id.edtAssignDesc);
        MaterialButton btnSelectDeadline = view.findViewById(R.id.btnSelectDeadline);
        MaterialButton btnSubmitAssignment = view.findViewById(R.id.btnSubmitAssignment);

        // GỌI BỘ LỊCH VÀ ĐỒNG HỒ CỦA ANDROID
        btnSelectDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            // 1. Hiện bảng chọn Ngày
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);

                // 2. Chọn xong Ngày thì hiện bảng chọn Giờ
                new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    selectedDeadline = calendar.getTime(); // Lưu vào biến
                    String timeStr = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(selectedDeadline);
                    btnSelectDeadline.setText("Hạn chót: " + timeStr); // Đổi text trên nút
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSubmitAssignment.setOnClickListener(v -> {
            String title = edtAssignTitle.getText().toString().trim();
            String desc = edtAssignDesc.getText().toString().trim();

            if (title.isEmpty() || selectedDeadline == null) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên và chọn Deadline!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String assignId = db.collection("Assignments").document().getId();
            Assignment newAssign = new Assignment(assignId, groupId, title, desc, selectedDeadline, creatorId);

            db.collection("Assignments").document(assignId).set(newAssign)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Đã đăng bài tập!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadAssignments();
                    });
        });
        dialog.show();
    }
}
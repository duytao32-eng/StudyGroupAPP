package com.example.studygroupapp.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.adapter.AssignmentAdapter;
import com.example.studygroupapp.model.Assignment;
import com.example.studygroupapp.model.Submission;
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
    private Date selectedDeadline = null;

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

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. GẮN SỰ KIỆN CLICK VÀO ADAPTER (Phân luồng User/Creator)
        assignmentAdapter = new AssignmentAdapter(assignmentList, assignment -> {
            if (currentUserId.equals(creatorId)) {
                // Trưởng nhóm bấm vào -> Xem ai đã nộp
                showSubmissionsList(assignment);
            } else {
                // Sinh viên bấm vào -> Form nộp bài
                showSubmitDialog(assignment, currentUserId);
            }
        });
        rvAssignments.setAdapter(assignmentAdapter);

        // 2. Phân quyền nút Tạo bài tập
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
                        Collections.sort(assignmentList, (a1, a2) -> {
                            if (a1.getDeadline() == null || a2.getDeadline() == null) return 0;
                            return a1.getDeadline().compareTo(a2.getDeadline());
                        });
                        assignmentAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ==========================================
    // TÍNH NĂNG 1: SINH VIÊN NỘP BÀI (CÓ CHẤM DEADLINE TRỄ/ĐÚNG)
    // ==========================================
    private void showSubmitDialog(Assignment assignment, String currentUserId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_submit_assignment, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvSubmitAssignTitle = view.findViewById(R.id.tvSubmitAssignTitle);
        TextInputEditText edtSubmitLink = view.findViewById(R.id.edtSubmitLink);
        MaterialButton btnSubmitWork = view.findViewById(R.id.btnSubmitWork);

        tvSubmitAssignTitle.setText(assignment.getTitle());

        btnSubmitWork.setOnClickListener(v -> {
            String link = edtSubmitLink.getText().toString().trim();
            if (link.isEmpty()) {
                Toast.makeText(requireContext(), "Bạn chưa dán link bài làm!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Thuật toán kiểm tra Deadline
            Date now = new Date();
            boolean isLate = assignment.getDeadline() != null && now.after(assignment.getDeadline());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Lấy tên thật của sinh viên trước khi nộp
            db.collection("Users").document(currentUserId).get().addOnSuccessListener(userDoc -> {
                String studentName = userDoc.exists() && userDoc.getString("fullName") != null ? userDoc.getString("fullName") : "Sinh viên";

                String subId = db.collection("Submissions").document().getId();
                Submission submission = new Submission(subId, assignment.getId(), currentUserId, studentName, link, now, isLate);

                db.collection("Submissions").document(subId).set(submission)
                        .addOnSuccessListener(aVoid -> {
                            if (isLate) {
                                Toast.makeText(requireContext(), "⚠️ Đã nộp thành công nhưng BỊ TRỄ HẠN!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "✅ Nộp bài ĐÚNG HẠN thành công!", Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        });
            });
        });
        dialog.show();
    }

    // ==========================================
    // TÍNH NĂNG 2: TRƯỞNG NHÓM XEM DANH SÁCH BÀI NỘP
    // ==========================================
    private void showSubmissionsList(Assignment assignment) {
        FirebaseFirestore.getInstance().collection("Submissions")
                .whereEqualTo("assignmentId", assignment.getId())
                .orderBy("submitTime", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> records = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(requireContext(), "Chưa có sinh viên nào nộp bài!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Submission sub = doc.toObject(Submission.class);
                        String status = sub.isLate() ? "[TRỄ HẠN] ❌" : "[ĐÚNG HẠN] ✅";
                        records.add(sub.getStudentName() + "\n" + status + " Link: " + sub.getLinkUrl());
                    }

                    // Dùng hộp thoại mặc định hiển thị danh sách cho nhanh và trực quan
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Danh sách nộp bài (" + records.size() + ")")
                            .setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, records), null)
                            .setPositiveButton("ĐÓNG", null)
                            .show();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }

    // Hàm tạo bài tập (giữ nguyên)
    private void showAddAssignmentDialog() {
        selectedDeadline = null;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_create_assignment, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText edtAssignTitle = view.findViewById(R.id.edtAssignTitle);
        TextInputEditText edtAssignDesc = view.findViewById(R.id.edtAssignDesc);
        MaterialButton btnSelectDeadline = view.findViewById(R.id.btnSelectDeadline);
        MaterialButton btnSubmitAssignment = view.findViewById(R.id.btnSubmitAssignment);

        btnSelectDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    selectedDeadline = calendar.getTime();
                    String timeStr = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(selectedDeadline);
                    btnSelectDeadline.setText("Hạn chót: " + timeStr);
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
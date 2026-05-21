package com.example.studygroupapp.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.adapter.AttendanceHistoryAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AttendanceFragment extends Fragment {

    private String groupId, groupName, creatorId;
    private MaterialButton btnGenerateQR, btnScanQR, btnViewAttendance, btnEnterPin;
    private RecyclerView rvAttendanceHistory;
    private List<String> historyList;

    // Đã thay đổi thành Adapter xịn xò
    private AttendanceHistoryAdapter historyAdapter;

    public AttendanceFragment(String groupId, String groupName, String creatorId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.creatorId = creatorId;
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedData = result.getContents();
                    if (scannedData.startsWith("ATTENDANCE_" + groupId + "_")) {
                        verifyPinAndSaveAttendance(scannedData.split("_")[2]);
                    } else {
                        Toast.makeText(requireContext(), "Mã QR không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        btnGenerateQR = view.findViewById(R.id.btnGenerateQR);
        btnScanQR = view.findViewById(R.id.btnScanQR);
        btnViewAttendance = view.findViewById(R.id.btnViewAttendance);
        btnEnterPin = view.findViewById(R.id.btnEnterPin);
        rvAttendanceHistory = view.findViewById(R.id.rvAttendanceHistory);

        // Setup lịch sử cá nhân cho sinh viên
        rvAttendanceHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyList = new ArrayList<>();

        // Đã thay đổi cách gọi Adapter
        historyAdapter = new AttendanceHistoryAdapter(historyList);
        rvAttendanceHistory.setAdapter(historyAdapter);

        checkUserRole(view); // Truyền view vào để ánh xạ
        loadMyAttendanceHistory(); // Tự động tải lịch sử cá nhân

        btnGenerateQR.setOnClickListener(v -> showQRCodeDialog());
        btnScanQR.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Quét mã của Trưởng nhóm");
            barcodeLauncher.launch(options);
        });
        btnEnterPin.setOnClickListener(v -> showEnterPinDialog());

        btnViewAttendance.setOnClickListener(v -> showAttendanceDashboard());

        return view;
    }

    private void checkUserRole(View view) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        boolean isCreator = currentUserId.equals(creatorId);
        btnGenerateQR.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        btnViewAttendance.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        btnScanQR.setVisibility(isCreator ? View.GONE : View.VISIBLE);
        btnEnterPin.setVisibility(isCreator ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.tvHistoryTitle).setVisibility(isCreator ? View.GONE : View.VISIBLE);
        rvAttendanceHistory.setVisibility(isCreator ? View.GONE : View.VISIBLE);
    }

    // ==========================================
    // 1. TẢI LỊCH SỬ ĐI HỌC CỦA BẢN THÂN
    // ==========================================
    private void loadMyAttendanceHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("Attendance")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Date date = doc.getTimestamp("timestamp").toDate();
                        String time = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(date);
                        historyList.add("Có mặt lúc: " + time);
                    }
                    historyAdapter.notifyDataSetChanged();
                });
    }

    // ==========================================
    // 2. DASHBOARD THỐNG KÊ CHO TRƯỞNG NHÓM (Sẽ nâng cấp ở Phần 2)
    // ==========================================
    // NÂNG CẤP: DASHBOARD THỐNG KÊ XỊN XÒ CHO TRƯỞNG NHÓM
    private void showAttendanceDashboard() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Mở bảng Form Dialog mới lên trước
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_dashboard, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvDashboard = dialogView.findViewById(R.id.rvDashboard);
        MaterialButton btnCloseDashboard = dialogView.findViewById(R.id.btnCloseDashboard);

        rvDashboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<com.example.studygroupapp.model.AttendanceSummary> summaryList = new ArrayList<>();
        com.example.studygroupapp.adapter.DashboardAdapter dashboardAdapter = new com.example.studygroupapp.adapter.DashboardAdapter(summaryList);
        rvDashboard.setAdapter(dashboardAdapter);

        btnCloseDashboard.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 2. Tải dữ liệu từ mây và bơm vào Adapter
        db.collection("Attendance").whereEqualTo("groupId", groupId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> counts = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String uid = doc.getString("userId");
                        counts.put(uid, counts.getOrDefault(uid, 0) + 1);
                    }

                    for (String uid : counts.keySet()) {
                        db.collection("Users").document(uid).get().addOnSuccessListener(userDoc -> {
                            String name = userDoc.exists() && userDoc.getString("fullName") != null ? userDoc.getString("fullName") : "Thành viên";
                            String studentId = userDoc.exists() ? userDoc.getString("studentId") : "N/A";
                            int total = counts.get(uid);

                            // Tạo đối tượng Summary và đẩy vào danh sách
                            summaryList.add(new com.example.studygroupapp.model.AttendanceSummary(name, studentId, total));
                            dashboardAdapter.notifyDataSetChanged();
                        });
                    }
                });
    }

    private void verifyPinAndSaveAttendance(String pin) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupId).get().addOnSuccessListener(doc -> {
            String activePin = doc.getString("attendancePin");
            Date pinExpiry = doc.getDate("pinExpiry");
            Date now = new Date();

            if (activePin != null && activePin.equals(pin)) {
                if (pinExpiry != null && now.before(pinExpiry)) {
                    saveAttendance();
                } else {
                    Toast.makeText(requireContext(), "⏳ Mã điểm danh đã hết hạn 5 phút.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), "❌ Mã không hợp lệ hoặc nhóm chưa mở điểm danh!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi mạng!", Toast.LENGTH_SHORT).show());
    }

    private void saveAttendance() {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance().collection("Attendance").add(data).addOnSuccessListener(ref -> {
            Toast.makeText(requireContext(), "Điểm danh thành công!", Toast.LENGTH_SHORT).show();
            loadMyAttendanceHistory();
        });
    }

    private void showQRCodeDialog() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentPin = String.format("%04d", new Random().nextInt(10000));
        long expiryTimeMillis = System.currentTimeMillis() + (5 * 60 * 1000);
        Date expiryDate = new Date(expiryTimeMillis);

        Map<String, Object> updates = new HashMap<>();
        updates.put("attendancePin", currentPin);
        updates.put("pinExpiry", expiryDate);

        db.collection("Groups").document(groupId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                    View view = getLayoutInflater().inflate(R.layout.dialog_show_qr, null);
                    builder.setView(view);
                    android.app.AlertDialog dialog = builder.create();
                    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    ImageView imgQRCode = view.findViewById(R.id.imgQRCode);
                    TextView tvPinCode = view.findViewById(R.id.tvPinCode);
                    tvPinCode.setText(currentPin);

                    try {
                        String qrData = "ATTENDANCE_" + groupId + "_" + currentPin;
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 600, 600);
                        imgQRCode.setImageBitmap(bitmap);
                    } catch (Exception e) { e.printStackTrace(); }

                    dialog.setOnDismissListener(d -> Toast.makeText(requireContext(), "Mã vẫn còn hiệu lực đến lúc hết 5 phút!", Toast.LENGTH_LONG).show());
                    dialog.show();
                    Toast.makeText(requireContext(), "⏳ Cổng mở! Mã điểm danh tự hủy sau 5 phút.", Toast.LENGTH_LONG).show();
                });
    }

    private void showEnterPinDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_enter_pin, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText edtPinInput = view.findViewById(R.id.edtPinInput);
        MaterialButton btnSubmitPin = view.findViewById(R.id.btnSubmitPin);

        btnSubmitPin.setOnClickListener(v -> {
            String inputPin = edtPinInput.getText().toString().trim();
            if (inputPin.isEmpty() || inputPin.length() < 4) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ 4 số!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            verifyPinAndSaveAttendance(inputPin);
        });
        dialog.show();
    }
}
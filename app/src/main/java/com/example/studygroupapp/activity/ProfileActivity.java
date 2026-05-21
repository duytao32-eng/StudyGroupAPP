package com.example.studygroupapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studygroupapp.MainActivity;
import com.example.studygroupapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtStudentId;
    private AutoCompleteTextView actvFaculty, actvCohort, actvClass;
    private MaterialButton btnSaveProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Khai báo dữ liệu tĩnh
    private final String[] FACULTIES = {"CNTT", "Kinh Tế", "Y"};
    private final String[] COHORTS = {"23", "24", "25"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Đảm bảo đúng tên file layout của bạn

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        edtFullName = findViewById(R.id.edtFullName); // Nhớ check lại ID trong file XML của bạn
        edtStudentId = findViewById(R.id.edtStudentId);
        actvFaculty = findViewById(R.id.actvFaculty);
        actvCohort = findViewById(R.id.actvCohort);
        actvClass = findViewById(R.id.actvClass);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

      //  ImageView btnBack = findViewById(R.id.btnBack);
      //  if(btnBack != null) btnBack.setOnClickListener(v -> finish());

        setupDropdownMenus();
        loadUserProfile();

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    // ==========================================
    // THUẬT TOÁN ĐỔ DỮ LIỆU ĐỘNG CHO DROPDOWN
    // ==========================================
    private void setupDropdownMenus() {
        // Đổ dữ liệu cho Khoa
        ArrayAdapter<String> facultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, FACULTIES);
        actvFaculty.setAdapter(facultyAdapter);

        // Đổ dữ liệu cho Khóa
        ArrayAdapter<String> cohortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, COHORTS);
        actvCohort.setAdapter(cohortAdapter);

        // Lắng nghe sự kiện: Nếu chọn Khoa hoặc Khóa thì cập nhật lại Lớp
        actvFaculty.setOnItemClickListener((parent, view, position, id) -> generateClasses());
        actvCohort.setOnItemClickListener((parent, view, position, id) -> generateClasses());
    }

    private void generateClasses() {
        String selectedFaculty = actvFaculty.getText().toString();
        String selectedCohort = actvCohort.getText().toString();

        // Chỉ sinh ra lớp khi người dùng đã chọn CẢ Khoa VÀ Khóa
        if (selectedFaculty.isEmpty() || selectedCohort.isEmpty()) {
            actvClass.setAdapter(null);
            actvClass.setText("");
            return;
        }

        // Quy đổi Khoa sang Mã (CNTT -> TIN, Kinh Tế -> KT, Y -> Y)
        String facultyCode = "";
        if (selectedFaculty.equals("CNTT")) facultyCode = "TIN";
        else if (selectedFaculty.equals("Kinh Tế")) facultyCode = "KT";
        else if (selectedFaculty.equals("Y")) facultyCode = "Y";

        // Sinh danh sách từ 01 đến 05 (VD: DH23TIN01)
        List<String> classes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String classNumber = String.format("%02d", i); // Định dạng 01, 02...
            classes.add("DH" + selectedCohort + facultyCode + classNumber);
        }

        // Đổ danh sách Lớp vừa sinh ra vào ô chọn Lớp
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes);
        actvClass.setAdapter(classAdapter);
        actvClass.setText(""); // Xóa lựa chọn lớp cũ nếu người dùng đổi Khoa/Khóa
    }

    // ==========================================
    // LƯU & TẢI DỮ LIỆU FIREBASE
    // ==========================================
    private void saveUserProfile() {
        String name = edtFullName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String faculty = actvFaculty.getText().toString().trim();
        String cohort = actvCohort.getText().toString().trim();
        String className = actvClass.getText().toString().trim();

        if (name.isEmpty() || studentId.isEmpty() || faculty.isEmpty() || cohort.isEmpty() || className.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền và chọn đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", name);
        userMap.put("studentId", studentId);
        userMap.put("faculty", faculty);
        userMap.put("cohort", cohort);
        userMap.put("className", className);

        db.collection("Users").document(uid).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        if (doc.getString("fullName") != null) edtFullName.setText(doc.getString("fullName"));
                        if (doc.getString("studentId") != null) edtStudentId.setText(doc.getString("studentId"));

                        // Cập nhật các menu chọn
                        if (doc.getString("faculty") != null) actvFaculty.setText(doc.getString("faculty"), false);
                        if (doc.getString("cohort") != null) actvCohort.setText(doc.getString("cohort"), false);
                        if (doc.getString("className") != null) {
                            actvClass.setText(doc.getString("className"), false);
                            generateClasses(); // Kích hoạt lại hàm sinh lớp để nạp dữ liệu
                        }
                    }
                });
    }
}
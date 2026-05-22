package com.example.studygroupapp.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studygroupapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtRegName, edtRegEmail, edtRegPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth; // Biến đại diện cho Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Khởi tạo kết nối với Firebase
        mAuth = FirebaseAuth.getInstance();

        // 2. Tìm các ô nhập liệu và nút bấm trên giao diện
        edtRegName = findViewById(R.id.edtRegisterFullName);
        edtRegEmail = findViewById(R.id.edtRegisterEmail);
        edtRegPassword = findViewById(R.id.edtRegisterPassword);
        btnRegister = findViewById(R.id.btnRegisterSubmit);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Nút chữ "Quay về đăng nhập"
        tvGoToLogin.setOnClickListener(v -> finish());

        // 3. Xử lý khi bấm nút "ĐĂNG KÝ"
        btnRegister.setOnClickListener(v -> {
            String name = edtRegName.getText().toString().trim();
            String email = edtRegEmail.getText().toString().trim();
            String password = edtRegPassword.getText().toString().trim();

            // Kiểm tra xem có nhập thiếu không
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase yêu cầu mật khẩu phải từ 6 ký tự trở lên
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bắt đầu đẩy lên Firebase
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Thành công
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            finish(); // Đóng màn hình này, quay về màn hình Đăng nhập
                        } else {
                            // Thất bại (VD: email đã tồn tại, sai định dạng email)
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
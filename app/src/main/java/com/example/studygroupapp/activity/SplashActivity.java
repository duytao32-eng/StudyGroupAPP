package com.example.studygroupapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studygroupapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Đếm ngược 2000 milliseconds (2 giây)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Đã đăng nhập -> Vào Trang chủ
                startActivity(new Intent(SplashActivity.this, com.example.studygroupapp.MainActivity.class));
            } else {
                // Chưa đăng nhập -> Vào trang Đăng nhập
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish(); // Đóng màn hình Splash lại
        }, 2000);
    }
}
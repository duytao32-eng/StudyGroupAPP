package com.example.studygroupapp.model;

public class User {
    private String uid;
    private String fullName;
    private String studentId;
    private String email;

    // Firebase bắt buộc phải có hàm tạo rỗng
    public User() {
    }

    public User(String uid, String fullName, String studentId, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.studentId = studentId;
        this.email = email;
    }

    // Các hàm Getter và Setter
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
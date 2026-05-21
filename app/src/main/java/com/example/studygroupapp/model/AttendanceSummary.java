package com.example.studygroupapp.model;

public class AttendanceSummary {
    private String name, studentId;
    private int count;

    public AttendanceSummary(String name, String studentId, int count) {
        this.name = name;
        this.studentId = studentId;
        this.count = count;
    }

    public String getName() { return name; }
    public String getStudentId() { return studentId; }
    public int getCount() { return count; }
}
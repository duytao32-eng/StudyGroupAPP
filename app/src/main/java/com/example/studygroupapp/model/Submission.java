package com.example.studygroupapp.model;

import java.util.Date;

public class Submission {
    private String id, assignmentId, studentId, studentName, linkUrl;
    private Date submitTime;
    private boolean isLate;

    public Submission() {}

    public Submission(String id, String assignmentId, String studentId, String studentName, String linkUrl, Date submitTime, boolean isLate) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.linkUrl = linkUrl;
        this.submitTime = submitTime;
        this.isLate = isLate;
    }

    public String getId() { return id; }
    public String getAssignmentId() { return assignmentId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getLinkUrl() { return linkUrl; }
    public Date getSubmitTime() { return submitTime; }
    public boolean isLate() { return isLate; }
}
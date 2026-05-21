package com.example.studygroupapp.model;

import com.google.firebase.Timestamp;

public class Post {
    private String id;
    private String groupId;
    private String title;
    private String content;
    private String authorName;
    private Timestamp timestamp;

    public Post() {
        // Firebase yêu cầu hàm tạo rỗng
    }

    public Post(String id, String groupId, String title, String content, String authorName, Timestamp timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthorName() { return authorName; }
    public Timestamp getTimestamp() { return timestamp; }
}
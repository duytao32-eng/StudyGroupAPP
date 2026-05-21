package com.example.studygroupapp.model;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId;
    private String senderName;
    private String text;
    private Timestamp timestamp;

    public Message() {} // Firebase yêu cầu hàm tạo rỗng

    public Message(String senderId, String senderName, String text, Timestamp timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
}
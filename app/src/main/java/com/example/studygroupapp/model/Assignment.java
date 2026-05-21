package com.example.studygroupapp.model;

import java.util.Date;

public class Assignment {
    private String id, groupId, title, description, creatorId;
    private Date deadline;

    public Assignment() {}

    public Assignment(String id, String groupId, String title, String description, Date deadline, String creatorId) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.creatorId = creatorId;
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDeadline() { return deadline; }
    public String getCreatorId() { return creatorId; }
}
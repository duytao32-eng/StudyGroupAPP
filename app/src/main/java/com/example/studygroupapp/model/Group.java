package com.example.studygroupapp.model;

import java.util.List;

public class Group {
    private String id;
    private String name;
    private String code;
    private String creatorId;
    private List<String> members; // ĐÃ THÊM: Danh sách chứa ID của các thành viên

    public Group() {
    }

    public Group(String id, String name, String code, String creatorId, List<String> members) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.creatorId = creatorId;
        this.members = members;
    }

    // Các hàm Getter / Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }
}
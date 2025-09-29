package com.example.securingweb.dto;

import java.time.Instant;

public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String owner;
    private Instant createdAt;
    private Instant updatedAt;

    public BlogResponse() {}
    public BlogResponse(Long id, String title, String content, String owner, Instant createdAt, Instant updatedAt) {
        this.id = id; this.title = title; this.content = content; this.owner = owner; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}


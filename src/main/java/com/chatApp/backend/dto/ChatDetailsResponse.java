package com.chatApp.backend.dto;


import java.time.Instant;
import java.util.List;

public class ChatDetailsResponse {

    private Long id;
    private String type;
    private String title;
    private String displayTitle;
    private Instant createdAt;
    private List<UserSummaryResponse> members;

    public ChatDetailsResponse(Long id,
                               String type,
                               String title,
                               String displayTitle,
                               Instant createdAt,
                               List<UserSummaryResponse> members) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.displayTitle = displayTitle;
        this.createdAt = createdAt;
        this.members = members;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDisplayTitle() { return displayTitle; }
    public Instant getCreatedAt() { return createdAt; }
    public List<UserSummaryResponse> getMembers() { return members; }
}
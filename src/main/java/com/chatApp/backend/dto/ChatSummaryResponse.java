package com.chatApp.backend.dto;

import com.chatApp.backend.model.ChatType;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaryResponse {
    private Long id;
    private ChatType type;
    private String title;
    private Instant createdAt;
    private String displayTitle;

    public ChatSummaryResponse(Long id,
                                ChatType type,
                                String title,
                                String displayTitle,
                                Instant createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.displayTitle = displayTitle;
        this.createdAt = createdAt;
    }
}

package com.chatApp.backend.dto;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String chatId;
    private String sender;
    private String content;
    private String timestamp;
    private Long id;
    private String status;
}

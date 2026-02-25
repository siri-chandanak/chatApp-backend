package com.chatApp.backend.dto;

import java.time.Instant;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String content;
    private String senderName;
    private Instant createdAt;
    private String status;
}